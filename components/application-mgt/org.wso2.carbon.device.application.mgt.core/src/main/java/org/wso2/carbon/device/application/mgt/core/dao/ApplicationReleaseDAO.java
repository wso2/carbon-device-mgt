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

import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * This is responsible for Application Release related DAO operations.
 */
public interface ApplicationReleaseDAO {

    /**
     * To create an Application release.
     *
     * @param applicationRelease Application Release that need to be created.
     * @return Unique ID of the relevant release.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationRelease createRelease(ApplicationRelease applicationRelease) throws
            ApplicationManagementDAOException;

    /**
     * To get a release details with the particular version.
     * @param applicationUuid UUID of the application to get the release.
     * @param versionName Name of the version
     * @return ApplicationRelease for the particular version of the given application
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    ApplicationRelease getRelease(String applicationUuid, String versionName) throws ApplicationManagementDAOException;

    /**
     * To get all the releases of a particular application.
     *
     * @param applicationUUID Application UUID
     * @return list of the application releases
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    List<ApplicationRelease> getApplicationReleases(String applicationUUID) throws ApplicationManagementDAOException;

    /**
     * To update an Application release.
     * @param applicationRelease ApplicationRelease that need to be updated.
     * @return the updated Application Release
     * @throws ApplicationManagementDAOException Application Management DAO Exception
     */
    ApplicationRelease updateRelease(ApplicationRelease applicationRelease) throws ApplicationManagementDAOException;

    /**
     * To delete a particular release.
     *
     * @param id      ID of the Application which the release need to be deleted.
     * @param version Version of the Application Release
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteRelease(int id, String version) throws ApplicationManagementDAOException;

    /**
     * To delete the propertied of a particular Application Release.
     *
     * @param id ID of the ApplicationRelease in which properties need to be deleted.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteReleaseProperties(int id) throws ApplicationManagementDAOException;
}
