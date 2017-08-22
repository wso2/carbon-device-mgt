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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;
import java.util.Map;

/**
 * ApplicationDAO is responsible for handling all the Database related operations related with Application Management.
 */
public interface ApplicationDAO {

    Application createApplication(Application application) throws ApplicationManagementDAOException;

    ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException;

    Application getApplication(String uuid, int tenantId) throws ApplicationManagementDAOException;

    int getApplicationId(String uuid, int tenantId) throws ApplicationManagementDAOException;

    Application editApplication(Application application, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplication(String uuid, int tenantId) throws ApplicationManagementDAOException;

    int getApplicationCount(Filter filter) throws ApplicationManagementDAOException;

    void addProperties(Map<String, String> properties) throws ApplicationManagementDAOException;

    void editProperties(Map<String, String> properties) throws ApplicationManagementDAOException;

    void deleteProperties(int applicationId) throws ApplicationManagementDAOException;

    void deleteTags(int applicationId) throws ApplicationManagementDAOException;

    void addRelease(ApplicationRelease release) throws ApplicationManagementDAOException;

    void changeLifecycle(String applicationUUID, String lifecycleIdentifier, String username, int tenantId) throws
            ApplicationManagementDAOException;

    List<LifecycleStateTransition> getNextLifeCycleStates(String applicationUUID, int tenantId) throws
            ApplicationManagementDAOException;

    void updateScreenShotCount(String applicationUUID, int tenantId, int count) throws
            ApplicationManagementDAOException;

}
