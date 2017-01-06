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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the device deployer that will read and deploy the device type ui files from
 * "deployment/server/devicetypes-ui"
 * directory.
 */
public class DeviceTypeUIDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(DeviceTypeUIDeployer.class);
    protected Map<String, String> deviceTypeDeployedUIMap = new ConcurrentHashMap<String, String>();
    private static final String DEVICEMGT_JAGGERY_APP_PATH = CarbonUtils.getCarbonRepository() + File.separator
            + "jaggeryapps" + File.separator + "devicemgt" + File.separator + "app" + File.separator + "units"
            + File.separator;

    private static final String UNIT_PREFIX = "cdmf.unit.device.type";

    @Override
    public void init(ConfigurationContext configurationContext) {
    }

    @Override
    public void setDirectory(String s) {

    }

    @Override
    public void setExtension(String s) {

    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        if (!deploymentFileData.getFile().isDirectory()) {
            return;
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        if (tenantDomain != null && !tenantDomain.isEmpty()) {
            File jaggeryAppPath = new File(
                    DEVICEMGT_JAGGERY_APP_PATH + tenantDomain + "." + deploymentFileData.getName());
            try {
                if (!jaggeryAppPath.exists()) {
                    FileUtils.forceMkdir(jaggeryAppPath);
                    FileUtils.copyDirectory(deploymentFileData.getFile(), jaggeryAppPath);
                    File[] listOfFiles = jaggeryAppPath.listFiles();

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isFile()) {
                            String content = FileUtils.readFileToString(listOfFiles[i]);
                            FileUtils.writeStringToFile(listOfFiles[i], content.replaceAll(UNIT_PREFIX
                                    , tenantDomain + "." + UNIT_PREFIX));
                        }
                    }
                } else {
                    log.debug("units already exists " + deploymentFileData.getName());
                }
                this.deviceTypeDeployedUIMap.put(deploymentFileData.getAbsolutePath(),
                                                 jaggeryAppPath.getAbsolutePath());
            } catch (IOException e) {
                if (jaggeryAppPath.exists()) {
                    try {
                        FileUtils.deleteDirectory(jaggeryAppPath);
                    } catch (IOException e1) {
                        log.error("Failed to delete directory " + jaggeryAppPath.getAbsolutePath());
                    }
                }
                log.error("Cannot deploy deviceType ui : " + deploymentFileData.getName(), e);
                throw new DeploymentException(
                        "Device type ui file " + deploymentFileData.getName() + " is not deployed ", e);
            }

        } else {
            log.error("Cannot deploy deviceType ui: " + deploymentFileData.getName());
        }


    }

    @Override
    public void undeploy(String filePath) throws DeploymentException {
        try {
            String jaggeryUnitPath = this.deviceTypeDeployedUIMap.remove(filePath);
            FileUtils.deleteDirectory(new File(jaggeryUnitPath));
            log.info("Device Type units un deployed successfully.");
        } catch (IOException e) {
            throw new DeploymentException("Failed to remove the units: " + filePath);
        }
    }

}
