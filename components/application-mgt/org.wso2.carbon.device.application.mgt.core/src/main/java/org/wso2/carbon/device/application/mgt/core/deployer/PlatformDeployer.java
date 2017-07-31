/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class PlatformDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(PlatformDeployer.class);

    @Override
    public void init(ConfigurationContext configurationContext) {
        File deployementDir = new File(
                MultitenantUtils.getAxis2RepositoryPath(CarbonContext.getThreadLocalCarbonContext().
                        getTenantId()) + Constants.PLATFORMS_DEPLOYMENT_DIR_NAME);
        if (!deployementDir.exists()) {
            if (!deployementDir.mkdir()) {
                log.warn("Unable to create the deployment dir at: " + deployementDir.getPath());
            }
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        File deploymentFile = new File(deploymentFileData.getAbsolutePath());
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Platform.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Platform platformConf = (Platform) unmarshaller.unmarshal(deploymentFile);
            if (platformConf.getId().contentEquals(getPlatformID(deploymentFile.getName()))) {
                org.wso2.carbon.device.application.mgt.common.Platform platform = convert(platformConf);
                DataHolder.getInstance().getPlatformManager()
                        .register(CarbonContext.getThreadLocalCarbonContext().getTenantId(), platform);
            } else {
                log.error("Unable to deploy the platform - " + deploymentFile.getAbsolutePath()
                        + "!. Platform config file name - " + deploymentFile.getName()
                        + " should match with the 'id' provided within the platform configuration!");
            }
        } catch (JAXBException e) {
            log.error("Platform configuration file - " + deploymentFile.getAbsolutePath() + " is invalid!", e);
        } catch (PlatformManagementException e) {
            log.error("Unable to deploy the platform - " + deploymentFile.getAbsolutePath(), e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        String platformId = getPlatformID(fileName);
        try {
            DataHolder.getInstance().getPlatformManager()
                    .unregister(CarbonContext.getThreadLocalCarbonContext().getTenantId(), platformId, true);
        } catch (PlatformManagementException e) {
            log.error("Error occurred while undeploying the platform - " + fileName);
        }
    }

    private static String getPlatformID(String deploymentFileName) {
        if (deploymentFileName.contains(Constants.PLATFORM_DEPLOYMENT_EXT)) {
            return deploymentFileName.substring(0, deploymentFileName.length() -
                    Constants.PLATFORM_DEPLOYMENT_EXT.length());
        }
        return deploymentFileName;
    }

    private org.wso2.carbon.device.application.mgt.common.Platform convert(Platform platformConfig) {
        org.wso2.carbon.device.application.mgt.common.Platform platform =
                new org.wso2.carbon.device.application.mgt.common.Platform();
        platform.setIdentifier(platformConfig.getId());
        platform.setName(platformConfig.getName());
        platform.setDescription(platformConfig.getDescription());
        platform.setIconName(platformConfig.getIcon());
        platform.setFileBased(true);
        platform.setShared(platformConfig.isShared());
        platform.setDefaultTenantMapping(platformConfig.isTenantMapping());
        platform.setEnabled(false);
        List<org.wso2.carbon.device.application.mgt.common.Platform.Property> properties = new ArrayList<>();

        if (platformConfig.getProperties() != null) {
            for (Property propertyConfig : platformConfig.getProperties()) {
                org.wso2.carbon.device.application.mgt.common.Platform.Property property =
                        new org.wso2.carbon.device.application.mgt.common.Platform.Property();
                property.setName(propertyConfig.getName());
                property.setDefaultValue(propertyConfig.getDefaultValue());
                property.setOptional(propertyConfig.isOptional());
                properties.add(property);
            }
        }
        platform.setProperties(properties);
        return platform;
    }

    @Override
    public void setDirectory(String s) {
    }

    @Override
    public void setExtension(String s) {
    }
}
