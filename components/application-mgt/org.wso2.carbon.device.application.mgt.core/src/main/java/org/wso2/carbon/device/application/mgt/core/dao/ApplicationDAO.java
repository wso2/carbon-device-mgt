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

import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;
import java.util.Map;

public interface ApplicationDAO {

    Application createApplication(Application application) throws ApplicationManagementDAOException;

    ApplicationList getApplications(Filter filter) throws ApplicationManagementDAOException;

    Application getApplication(String uuid) throws ApplicationManagementDAOException;

    int getApplicationId(String uuid) throws ApplicationManagementDAOException;

    Application editApplication(Application application) throws ApplicationManagementDAOException;

    void deleteApplication(String uuid) throws ApplicationManagementDAOException;

    int getApplicationCount(Filter filter) throws ApplicationManagementDAOException;

    void addProperties(Map<String, String> properties) throws ApplicationManagementDAOException;

    void editProperties(Map<String, String> properties) throws ApplicationManagementDAOException;

    void deleteProperties(int applicationId) throws ApplicationManagementDAOException;

    void deleteTags(int applicationId) throws ApplicationManagementDAOException;

    void changeLifeCycle(LifecycleState lifecycleState) throws ApplicationManagementDAOException;

    void addRelease(ApplicationRelease release) throws ApplicationManagementDAOException;

    void changeLifecycle(String applicationUUID, String lifecycleIdentifier) throws ApplicationManagementDAOException;

    List<LifecycleStateTransition> getNextLifeCycleStates(String applicationUUID, int tenantId) throws
            ApplicationManagementDAOException;

}
