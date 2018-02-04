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
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Concrete implementation for Application Release Management related tasks.
 */
public class ApplicationReleaseManagerImpl implements ApplicationReleaseManager {
    private static Log log = LogFactory.getLog(ApplicationReleaseManagerImpl.class);

    @Override
    public ApplicationRelease createRelease(int applicationId, ApplicationRelease applicationRelease) throws
            ApplicationManagementException {
        Application application = validateApplication(applicationId);
        validateReleaseCreateRequest(applicationRelease.getUuid(), applicationRelease);
        if (log.isDebugEnabled()) {
            log.debug("Application release request is received for the application " + application.toString());
        }
        applicationRelease.setCreatedAt((Timestamp) new Date());
        try {
            ConnectionManagerUtil.beginDBTransaction();
            applicationRelease = ApplicationManagementDAOFactory.getApplicationReleaseDAO().
                    createRelease(applicationRelease, application.getId());
            ConnectionManagerUtil.commitDBTransaction();
            return applicationRelease;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease getRelease(String applicationUuid, String version, String releaseType) throws
            ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = validateApplicationRelease(applicationUuid);
        if (log.isDebugEnabled()) {
            log.debug("Application release retrieval request is received for the application " +
                    application.toString() + " and version " + version);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return ApplicationManagementDAOFactory.getApplicationReleaseDAO()
                    .getRelease(application.getName(), application.getType(), version, releaseType, tenantId );
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override public ApplicationRelease getReleaseByUuid(String applicationUuid) throws ApplicationManagementException {
        return null;
    }

    @Override
    public List<ApplicationRelease> getReleases(int applicationId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        Application application = validateApplication(applicationId);
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " +
                    application.toString());
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return ApplicationManagementDAOFactory.getApplicationReleaseDAO()
                    .getApplicationReleases(application.getName(), application.getType(), tenantId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

//  ToDo
    @Override
    public void changeDefaultRelease(String uuid, String version, boolean isDefault, String releaseChannel) throws
            ApplicationManagementException {
//        Application application = validateApplicationRelease(uuid);
//        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
//        if (log.isDebugEnabled()) {
//            log.debug("Request received to change the default release for the release channel " + releaseChannel
//                    + "for the application " + application.toString());
//        }
//
//        try {
//            ConnectionManagerUtil.beginDBTransaction();
//            ApplicationManagementDAOFactory.getApplicationReleaseDAO()
//                    .changeReleaseDefault(uuid, version, isDefault, releaseChannel, tenantId);
//            ConnectionManagerUtil.commitDBTransaction();
//        } catch (ApplicationManagementDAOException e) {
//            ConnectionManagerUtil.rollbackDBTransaction();
//            throw e;
//        } finally {
//            ConnectionManagerUtil.closeDBConnection();
//        }
    }

//  ToDo
    @Override
    public ApplicationRelease updateRelease(String applicationUuid, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
//        Application application = validateApplicationRelease(applicationUuid);
//        ApplicationRelease oldApplicationRelease = null;
//        if (applicationRelease == null || applicationRelease.getVersion() != null) {
//            throw new ApplicationManagementException(
//                    "Version is important to update the release of the application " + "with application UUID "
//                            + applicationUuid);
//        }
//        oldApplicationRelease = getRelease(applicationUuid, applicationRelease.getVersion());
//        if (oldApplicationRelease == null) {
//            throw new ApplicationManagementException(
//                    "Application release for the application " + applicationUuid + " with version " + applicationRelease
//                            .getVersion() + " does not exist. Cannot update the "
//                            + "release that is not existing.");
//        }
//        applicationRelease.setApplication(application);
//        try {
//            ConnectionManagerUtil.beginDBTransaction();
//            ApplicationRelease newApplicationRelease = ApplicationManagementDAOFactory.getApplicationReleaseDAO()
//                    .updateRelease(applicationRelease);
//            ConnectionManagerUtil.commitDBTransaction();
//            return newApplicationRelease;
//        } catch (ApplicationManagementDAOException e) {
//            ConnectionManagerUtil.rollbackDBTransaction();
//            throw e;
//        } finally {
//            ConnectionManagerUtil.closeDBConnection();
//        }
        return null;
    }

    @Override
    public void deleteApplicationRelease(String applicationUuid, String version, String releaseType)
            throws ApplicationManagementException {
        Application application = validateApplicationRelease(applicationUuid);
        ApplicationRelease applicationRelease = getRelease(applicationUuid, version, releaseType);
        if (applicationRelease == null) {
            throw new ApplicationManagementException(
                    "Cannot delete a non-existing application release for the " + "application with UUID "
                            + applicationUuid);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationManagementDAOFactory.getApplicationReleaseDAO().deleteRelease(application.getId(), version);
//            ToDO remove storage details as well
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteApplicationReleases(String applicationUuid) throws ApplicationManagementException {
        List<ApplicationRelease> applicationReleases = getReleases(applicationUuid);

        for (ApplicationRelease applicationRelease : applicationReleases) {
            deleteApplicationRelease(applicationUuid, applicationRelease.getVersion());
        }
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationID ID of the Application.
     * @return Application related with the UUID
     */
    private Application validateApplication(int applicationID) throws ApplicationManagementException {
        if (applicationID <= 0) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplicationById(applicationID);
        if (application == null) {
            throw new NotFoundException(
                    "Application of the " + applicationID + " does not exist.");
        }
        return application;
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    private ApplicationRelease validateApplicationRelease(String applicationUuid) throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        ApplicationRelease applicationRelease = DataHolder.getInstance().getApplicationReleaseManager().getRelease();
        if (applicationRelease == null) {
            throw new NotFoundException(
                    "Application with UUID " + applicationUuid + " does not exist.");
        }
        return applicationRelease;
    }

    /**
     * To validate a create release request to make sure all the pre-conditions satisfied.
     *
     * @param applicationUuid    UUID of the Application.
     * @param applicationRelease ApplicationRelease that need to be created.
     * @throws ApplicationManagementException Application Management Exception.
     */
    private void validateReleaseCreateRequest(String applicationUuid, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        if (applicationRelease == null || applicationRelease.getVersion() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                    + "creating release. It cannot be found.");
        }
        if (getRelease(applicationUuid, applicationRelease.getVersion()) != null) {
            throw new ApplicationManagementException(
                    "Application Release for the Application UUID " + applicationUuid + " " + "with the version "
                            + applicationRelease.getVersion() + " already exists. Cannot create an "
                            + "application release with the same version.");
        }
    }

}
