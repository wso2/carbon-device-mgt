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
package org.wso2.carbon.device.application.mgt.core.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.core.config.ApplicationConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.config.extensions.Extension;
import org.wso2.carbon.device.application.mgt.core.config.extensions.ExtensionsConfig;

public class ApplicationManagementServiceFactory {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceFactory.class);

    public enum ManagerService {
        APPLICATION_MANAGER,
        APPLICATION_RELEASE_MANAGER,
        CATEGORY_MANAGER,
        COMMENTS_MANAGER,
        LIFECYCLE_STATE_MANAGER,
        PLATFORM_MANAGER,
        RESOURCE_TYPE_MANAGER,
        SUBSCRIPTION_MANAGER,
        VISIBILITY_MANAGER
    }

    public ApplicationManagementService getApplicationManagementService(ManagerService managerService) {
        switch (managerService) {
            case APPLICATION_MANAGER:
                return new ApplicationManagerImpl();
            case APPLICATION_RELEASE_MANAGER:
                return new ApplicationReleaseManagerImpl();
            default:
                return null;
        }
    }

    public ApplicationManagementExtension applicationManagementExtensionsService(String extensionName) {
        ApplicationConfigurationManager applicationConfigurationManager = ApplicationConfigurationManager.getInstance();

        ExtensionsConfig extensionConfig = applicationConfigurationManager
                .getApplicationManagerConfiguration().getExtensionsConfig();

        Extension extension = extensionConfig.getExtensions().getExtensionByName(extensionName);

        try {
            Class<?> theClass = Class.forName(extension.getClassName());
            ApplicationManagementExtension appManagementExtension = (ApplicationManagementExtension) theClass.newInstance();
            appManagementExtension.setParameters(extension.getParameters());
            return appManagementExtension;
        } catch (ClassNotFoundException e) {
           log.error("Class not Found", e);
        } catch (IllegalAccessException e) {
            log.error("Illegal Access of Class", e);
        } catch (InstantiationException e) {
            log.error("Class instantiation exception", e);
        }


        return null;

    }
}
