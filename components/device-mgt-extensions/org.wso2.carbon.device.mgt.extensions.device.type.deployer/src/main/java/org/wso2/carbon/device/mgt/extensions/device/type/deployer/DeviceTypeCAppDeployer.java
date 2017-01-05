/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.device.type.deployer;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.util.DeviceTypePluginConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the device deployer that will read and deploy the device type files from
 * "deployment/server/carbonapps"
 * directory.
 */
public class DeviceTypeCAppDeployer implements AppDeploymentHandler {


    private static Log log = LogFactory.getLog(DeviceTypeCAppDeployer.class);
    private List<Artifact> deviceTypePlugins = new ArrayList<Artifact>();
    private List<Artifact> deviceTypeUIs = new ArrayList<Artifact>();

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfig)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts =
                carbonApplication.getAppConfig().getApplicationArtifact().getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (!validateArtifact(artifact)) {
                continue;
            }
            addArtifact(artifact);
        }

        try {
            deployTypeSpecifiedArtifacts(deviceTypeUIs, axisConfig, null,
                                         DeviceTypePluginConstants.CDMF_UI_TYPE_DIR);
            deployTypeSpecifiedArtifacts(deviceTypePlugins, axisConfig,
                                         DeviceTypePluginConstants.CDMF_PLUGIN_TYPE_EXTENSION,
                                         DeviceTypePluginConstants.CDMF_PLUGIN_TYPE_DIR);

        } catch (Exception e) {
            throw new DeploymentException(e.getMessage(), e);
        } finally {
            deviceTypePlugins.clear();
            deviceTypeUIs.clear();
        }

    }

    private void deployTypeSpecifiedArtifacts(List<Artifact> artifacts, AxisConfiguration axisConfig,
                                              String fileType, String directory) throws DeploymentException {
        for (Artifact artifact : artifacts) {
            Deployer deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, directory, fileType);
            if (deployer != null) {
                deploy(deployer, artifact);
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfig)
            throws DeploymentException {
        List<Artifact.Dependency> artifacts =
                carbonApplication.getAppConfig().getApplicationArtifact().getDependencies();

        deviceTypePlugins.clear();
        deviceTypeUIs.clear();

        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (!validateArtifact(artifact)) {
                continue;
            }
            addArtifact(artifact);
        }

        try {
            undeployTypeSpecifiedArtifacts(deviceTypeUIs, axisConfig, null,
                                           DeviceTypePluginConstants.CDMF_UI_TYPE_DIR);
            undeployTypeSpecifiedArtifacts(deviceTypePlugins, axisConfig,
                                           DeviceTypePluginConstants.CDMF_PLUGIN_TYPE_EXTENSION,
                                           DeviceTypePluginConstants.CDMF_PLUGIN_TYPE_DIR);
        } finally {
            deviceTypePlugins.clear();
            deviceTypeUIs.clear();
        }

    }

    private void undeployTypeSpecifiedArtifacts(List<Artifact> artifacts, AxisConfiguration axisConfig, String fileType
            , String directory) throws DeploymentException {
        for (Artifact artifact : artifacts) {
                Deployer deployer = AppDeployerUtils.getArtifactDeployer(axisConfig, directory, fileType);
            if (deployer != null &&
                    AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.equals(artifact.getDeploymentStatus())) {
                undeploy(deployer, artifact);
            }
        }
    }

    private boolean validateArtifact(Artifact artifact) {
        if (artifact == null) {
            return false;
        }
        List<CappFile> files = artifact.getFiles();
        if (files.size() != 1) {
            log.error("Synapse artifact types must have a single file to " +
                              "be deployed. But " + files.size() + " files found.");
            return false;
        }
        return true;
    }

    private void addArtifact(Artifact artifact) {
        if (DeviceTypePluginConstants.CDMF_PLUGIN_TYPE.equals(artifact.getType())) {
            deviceTypePlugins.add(artifact);
        } else if (DeviceTypePluginConstants.CDMF_UI_TYPE.equals(artifact.getType())) {
            deviceTypeUIs.add(artifact);
        }
    }

    void deploy(Deployer deployer, Artifact artifact) throws DeploymentException {
        String fileName = artifact.getFiles().get(0).getName();
        String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
        try {
            deployer.deploy(new DeploymentFileData(new File(artifactPath)));
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
        } catch (Exception e) {
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
            log.error("Deployment is failed due to " + e.getMessage(), e);
            throw new DeploymentException(e.getMessage(), e);
        }
    }

    private void undeploy(Deployer deployer, Artifact artifact) throws DeploymentException {
        String fileName = artifact.getFiles().get(0).getName();
        String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
        try {
            deployer.undeploy(new DeploymentFileData(new File(artifactPath), deployer).getAbsolutePath());
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
        } catch (Exception e) {
            artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
            log.error("Undeployment is failed due to " + e.getMessage(), e);
            throw new DeploymentException(e.getMessage(), e);
        }
    }
}
