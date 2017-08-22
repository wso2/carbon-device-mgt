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
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.util.Date;
import java.util.List;

/**
 * Concrete implementation for Application Release Management related tasks.
 */
public class ApplicationReleaseManagerImpl implements ApplicationReleaseManager {
    private static Log log = LogFactory.getLog(ApplicationReleaseManagerImpl.class);

    @Override
    public ApplicationRelease createRelease(String appicationUuid, ApplicationRelease applicationRelease) throws
            ApplicationManagementException {
        Application application = validateApplication(appicationUuid);
        validateReleaseCreateRequest(appicationUuid, applicationRelease);
        if (log.isDebugEnabled()) {
            log.debug("Application release request is received for the application " + application.toString());
        }
        applicationRelease.setCreatedAt(new Date());
        try {
            ConnectionManagerUtil.beginDBTransaction();
            applicationRelease.setApplication(application);
            applicationRelease = DAOFactory.getApplicationReleaseDAO().createRelease(applicationRelease);
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
    public ApplicationRelease getRelease(String applicationUuid, String version) throws
            ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = validateApplication(applicationUuid);
        if (log.isDebugEnabled()) {
            log.debug("Application release retrieval request is received for the application " +
                    application.toString() + " and version " + version);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getApplicationReleaseDAO().getRelease(applicationUuid, version, tenantId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<ApplicationRelease> getReleases(String applicationUuid) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        Application application = validateApplication(applicationUuid);
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " +
                    application.toString());
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getApplicationReleaseDAO().getApplicationReleases(applicationUuid, tenantId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void changeDefaultRelease(String uuid, String version, boolean isDefault, String releaseChannel) throws
            ApplicationManagementException {
        Application application = validateApplication(uuid);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Request received to change the default release for the release channel " + releaseChannel
                    + "for the application " + application.toString());
        }

        try {
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getApplicationReleaseDAO()
                    .changeReleaseDefault(uuid, version, isDefault, releaseChannel, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public ApplicationRelease updateRelease(String applicationUuid, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        Application application = validateApplication(applicationUuid);
        ApplicationRelease oldApplicationRelease = null;
        if (applicationRelease == null || applicationRelease.getVersionName() != null) {
            throw new ApplicationManagementException(
                    "Version is important to update the release of the application " + "with application UUID "
                            + applicationUuid);
        }
        oldApplicationRelease = getRelease(applicationUuid, applicationRelease.getVersionName());
        if (oldApplicationRelease == null) {
            throw new ApplicationManagementException(
                    "Application release for the application " + applicationUuid + " with version " + applicationRelease
                            .getVersionName() + " does not exist. Cannot update the "
                            + "release that is not existing.");
        }
        applicationRelease.setApplication(application);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            ApplicationRelease newApplicationRelease = DAOFactory.getApplicationReleaseDAO()
                    .updateRelease(applicationRelease);
            ConnectionManagerUtil.commitDBTransaction();
            return newApplicationRelease;
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteApplicationRelease(String applicationUuid, String version)
            throws ApplicationManagementException {
        Application application = validateApplication(applicationUuid);
        ApplicationRelease applicationRelease = getRelease(applicationUuid, version);
        if (applicationRelease == null) {
            throw new ApplicationManagementException(
                    "Cannot delete a non-existing application release for the " + "application with UUID "
                            + applicationUuid);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getApplicationReleaseDAO().deleteRelease(application.getId(), version);
            DAOFactory.getApplicationReleaseDAO().deleteReleaseProperties(applicationRelease.getId());
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
            deleteApplicationRelease(applicationUuid, applicationRelease.getVersionName());
        }
    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    private Application validateApplication(String applicationUuid) throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the relevant application.");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUuid);
        if (application == null) {
            throw new ApplicationManagementException(
                    "Application with UUID " + applicationUuid + " does not exist.");
        }
        return application;
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
        if (applicationRelease == null || applicationRelease.getVersionName() == null) {
            throw new ApplicationManagementException("ApplicationRelease version name is a mandatory parameter for "
                    + "creating release. It cannot be found.");
        }
        if (getRelease(applicationUuid, applicationRelease.getVersionName()) != null) {
            throw new ApplicationManagementException(
                    "Application Release for the Application UUID " + applicationUuid + " " + "with the version "
                            + applicationRelease.getVersionName() + " already exists. Cannot create an "
                            + "application release with the same version.");
        }
    }

}
