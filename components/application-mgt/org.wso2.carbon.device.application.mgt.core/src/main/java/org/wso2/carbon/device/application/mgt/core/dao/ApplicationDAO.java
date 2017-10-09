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
import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.LifecycleStateTransition;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * ApplicationDAO is responsible for handling all the Database related operations related with Application Management.
 */
public interface ApplicationDAO {

    /**
     * To create an application.
     *
     * @param application Application that need to be created.
     * @return Created Application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Application createApplication(Application application) throws ApplicationManagementDAOException;

    /**
     * To get the applications that satisfy the given criteria.
     *
     * @param filter   Filter criteria.
     * @param tenantId Id of the tenant.
     * @return Application list
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException;

    Application getApplication(String uuid, int tenantId, String userName) throws ApplicationManagementDAOException;

    int getApplicationId(String uuid, int tenantId) throws ApplicationManagementDAOException;

    Application editApplication(Application application, int tenantId) throws ApplicationManagementDAOException;

    void deleteApplication(String uuid, int tenantId) throws ApplicationManagementDAOException;

    int getApplicationCount(Filter filter) throws ApplicationManagementDAOException;

    void deleteProperties(int applicationId) throws ApplicationManagementDAOException;

    void deleteTags(int applicationId) throws ApplicationManagementDAOException;

    void changeLifecycle(String applicationUUID, String lifecycleIdentifier, String username, int tenantId) throws
            ApplicationManagementDAOException;

    List<LifecycleStateTransition> getNextLifeCycleStates(String applicationUUID, int tenantId) throws
            ApplicationManagementDAOException;

    void updateScreenShotCount(String applicationUUID, int tenantId, int count) throws
            ApplicationManagementDAOException;

    Category addCategory(Category category) throws ApplicationManagementDAOException;

    List<Category> getCategories() throws ApplicationManagementDAOException;

    Category getCategory(String name) throws ApplicationManagementDAOException;

    boolean isApplicationExistForCategory(String name) throws ApplicationManagementDAOException;

    void deleteCategory(String name) throws ApplicationManagementDAOException;
}
