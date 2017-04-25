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
import org.w3c.dom.Document;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagerException;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagerConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ApplicationConfigurationManager {

    private final String applicationMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            ApplicationManagerConstants.APPLICATION_CONFIG_XML_FILE;

    private static final Log log = LogFactory.getLog(ApplicationConfigurationManager.class);

    private ApplicationManagementConfigurations applicationManagerConfiguration;


    private static ApplicationConfigurationManager applicationConfigurationManager;

    private ApplicationConfigurationManager() {

    }

    public static ApplicationConfigurationManager getInstance() {
        if (applicationConfigurationManager == null) {
            applicationConfigurationManager = new ApplicationConfigurationManager();
            try {
                applicationConfigurationManager.initConfig();
            } catch (ApplicationManagerException e) {
                log.error(e);
            }
        }

        return applicationConfigurationManager;
    }


    public synchronized void initConfig() throws ApplicationManagerException {
        try {
            File appMgtConfig = new File(applicationMgtConfigXMLPath);
            Document doc = ApplicationManagementUtil.convertToDocument(appMgtConfig);

            /* Un-marshaling Certificate Management configuration */
            JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationManagementConfigurations.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            this.applicationManagerConfiguration = (ApplicationManagementConfigurations) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new ApplicationManagerException("Error occurred while initializing application config");
        }
    }

    public ApplicationManagementConfigurations getApplicationManagerConfiguration() {
        return applicationManagerConfiguration;
    }
}
