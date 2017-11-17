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

import java.sql.Timestamp;
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
     * To add unrestricted roles for a particular application.
     *
     * @param unrestrictedRoles unrestrictedRoles that could available the application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
     void addUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws ApplicationManagementDAOException;

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
    Application editApplication(Application application, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To delete the application identified by the UUID
     *
     * @param uuid     UUID of the application.
     * @param tenantId ID of tenant which the Application belongs to.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteApplication(String uuid, int tenantId) throws ApplicationManagementDAOException;

    /**
     * To get the application count that satisfies gives search query.
     *
     * @param filter Application Filter.
     * @return count of the applications
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    int getApplicationCount(Filter filter) throws ApplicationManagementDAOException;

    /**
     * To delete the properties of a application.
     *
     * @param applicationId ID of the application to delete the properties.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteProperties(int applicationId) throws ApplicationManagementDAOException;

    /**
     * To delete the tags of a application.
     *
     * @param applicationId ID of the application to delete the tags.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteTags(int applicationId) throws ApplicationManagementDAOException;

    /**
     * To change the lifecycle state of the application.
     *
     * @param applicationUUID     UUID of the application.
     * @param lifecycleIdentifier New lifecycle state.
     * @param username            Name of the user.
     * @param tenantId            ID of the tenant.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void changeLifecycle(String applicationUUID, String lifecycleIdentifier, String username, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To get the next possible lifecycle states for the application.
     *
     * @param applicationUUID UUID of the application.
     * @param tenantId        ID of the tenant.
     * @return Next possible lifecycle states.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    List<LifecycleStateTransition> getNextLifeCycleStates(String applicationUUID, int tenantId)
            throws ApplicationManagementDAOException;

    /**
     * To get the next possible lifecycle states for the application.
     *
     * @param lifecycle lifecycle of the application.
     * @param tenantId tenantId of the application useer.
     * @param appReleaseId relesse id of the application.
     * @param appId application id of the application.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    public void addLifecycle(Lifecycle lifecycle, int tenantId, int appReleaseId, int appId)
            throws ApplicationManagementDAOException;

    /**
     * To update the screen-shot count of a application.
     *
     * @param applicationUUID UUID of the application.
     * @param tenantId        ID of the tenant.
     * @param count           New count of the screen-shots.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void updateScreenShotCount(String applicationUUID, int tenantId, int count)
            throws ApplicationManagementDAOException;

    /**
     * To check whether atleast one application exist under category.
     *
     * @param categoryName Name of the category.
     * @return true if atleast one application exist under the given category, otherwise false.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    boolean isApplicationExist(String categoryName) throws ApplicationManagementDAOException;
}
