/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.core.config.extensions.Extension;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ConfigurationManager {

    private final String applicationMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.APPLICATION_CONFIG_XML_FILE;

    private static final Log log = LogFactory.getLog(ConfigurationManager.class);

    private Configurations configuration;


    private static ConfigurationManager configurationManager;

    private ConfigurationManager() {

    }

    public static ConfigurationManager getInstance() {
        if (configurationManager == null) {
            synchronized (ConfigurationManager.class) {
                if (configurationManager == null) {
                    configurationManager = new ConfigurationManager();
                    try {
                        configurationManager.initConfig();
                    } catch (ApplicationManagementException e) {
                        log.error(e);
                    }
                }
            }
        }
        return configurationManager;
    }


    private void initConfig() throws ApplicationManagementException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            this.configuration = (Configurations) unmarshaller.unmarshal(new File(applicationMgtConfigXMLPath));
        } catch (Exception e) {
            log.error(e);
            throw new InvalidConfigurationException("Error occurred while initializing application config: "
                    + applicationMgtConfigXMLPath, e);
        }
    }

    public Configurations getConfiguration() {
        return configuration;
    }

    public Extension getExtension(Extension.Name extName) throws InvalidConfigurationException {
        for (Extension extension : configuration.getExtensions()) {
            if (extension.getName().contentEquals(extName.toString())) {
                return extension;
            }
        }
        throw new InvalidConfigurationException("Expecting an extension with name - " + extName + " , but not found!");
    }
}
