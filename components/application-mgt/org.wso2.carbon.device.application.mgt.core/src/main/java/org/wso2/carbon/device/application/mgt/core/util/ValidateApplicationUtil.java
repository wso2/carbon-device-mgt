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
package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.config.Extension;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;

import java.lang.reflect.Constructor;

/**
 * This Util class is responsible for making sure single instance of each Extension Manager is used throughout for
 * all the tasks.
 */
public class ValidateApplicationUtil {

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationID ID of the Application.
     * @return Application related with the UUID
     */
    public static Application validateApplication(int applicationID) throws ApplicationManagementException {
        if (applicationID <= 0) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplicationById(applicationID);
        if (application == null) {
            throw new NotFoundException("Application of the " + applicationID + " does not exist.");
        }
        return application;
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    public static ApplicationRelease validateApplicationRelease(String applicationUuid) throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        ApplicationRelease applicationRelease = DataHolder.getInstance().getApplicationReleaseManager()
                .getReleaseByUuid(applicationUuid);
        if (applicationRelease == null) {
            throw new NotFoundException(
                    "Application with UUID " + applicationUuid + " does not exist.");
        }
        return applicationRelease;
    }
}
