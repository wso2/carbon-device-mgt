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
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
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
    int createApplication(Application application, int deviceId) throws ApplicationManagementDAOException;

    /**
     * To add tags for a particular application.
     *
     * @param tags tags that need to be added for a application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void addTags(List<Tag> tags, int applicationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To check application existence.
     *
     * @param appName appName that need to identify application.
     * @param type type that need to identify application.
     * @param tenantId tenantId that need to identify application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
     int isExistApplication(String appName, String type, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the applications that satisfy the given criteria.
     *
     * @param filter   Filter criteria.
     * @param tenantId Id of the tenant.
     * @return Application list
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationList getApplications(Filter filter, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the UUID of latest app release that satisfy the given criteria.
     *
     * @param appId   application id
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    String getUuidOfLatestRelease(int appId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param appName     name of the application to be retrieved.
     * @param tenantId ID of the tenant.
     * @param appType Type of the application.
     * @return the application
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Application getApplication(String appName, String appType, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param applicationId Id of the application to be retrieved.
     * @param tenantId ID of the tenant.
     * @return the application
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Application getApplicationById(int applicationId, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application with the given uuid
     *
     * @param appId ID of the application
     * @return the boolean value
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Boolean verifyApplicationExistenceById(int appId) throws ApplicationManagementDAOException;

    /**
     * To get the application id of the application specified by the UUID
     *
     * @param appName     name of the application.
     * @param appType     type of the application.
     * @param tenantId ID of the tenant.
     * @return ID of the Application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    int getApplicationId(String appName, String appType, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To edit the given application.
     *
     * @param application Application that need to be edited.
     * @param tenantId    Tenant ID of the Application.
     * @return Updated Application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Application editApplication(Application application, int tenantId) throws ApplicationManagementDAOException,
            ApplicationManagementException;

    /**
     * To delete the application
     *
     * @param appId     ID of the application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteApplication(int appId) throws ApplicationManagementDAOException;

    /**
     * To get the application count that satisfies gives search query.
     *
     * @param filter Application Filter.
     * @return count of the applications
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    int getApplicationCount(Filter filter) throws ApplicationManagementDAOException;


    /**
     * To delete the tags of a application.
     *
     * @param applicationId ID of the application to delete the tags.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteTags(int applicationId) throws ApplicationManagementDAOException;

    /**
     * To get an {@link Application} associated with the given release
     *
     * @param appReleaseUUID UUID of the {@link ApplicationRelease}
     * @param tenantId ID of the tenant
     * @return {@link Application} associated with the given release UUID
     * @throws ApplicationManagementDAOException if unable to fetch the Application from the data store.
     */
    Application getApplicationByRelease(String appReleaseUUID, int tenantId) throws ApplicationManagementDAOException;
}

