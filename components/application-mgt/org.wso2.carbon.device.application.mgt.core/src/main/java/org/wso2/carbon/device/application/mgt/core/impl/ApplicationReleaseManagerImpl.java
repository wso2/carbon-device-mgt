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
        Application application = validateReleaseCreateRequest(appicationUuid, applicationRelease);
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
        Application application = validationGetReleaseRequest(applicationUuid);
        if (log.isDebugEnabled()) {
            log.debug("Application release retrieval request is received for the application " +
                    application.toString() + " and version " + version);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getApplicationReleaseDAO().getRelease(applicationUuid, version);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<ApplicationRelease> getReleases(String applicationUuid) throws ApplicationManagementException {
        Application application = validationGetReleaseRequest(applicationUuid);
        if (log.isDebugEnabled()) {
            log.debug("Request is received to retrieve all the releases related with the application " +
                    application.toString());
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return DAOFactory.getApplicationReleaseDAO().getApplicationReleases(applicationUuid);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void makeDefaultRelease(int id) throws ApplicationManagementException {

    }

    @Override
    public void updateRelease(ApplicationRelease applicationRelease) throws ApplicationManagementException {

    }

    /**
     * To validate the pre-request of the ApplicationRelease.
     *
     * @param applicationUuid UUID of the Application.
     * @return Application related with the UUID
     */
    private Application validationGetReleaseRequest(String applicationUuid) throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to get the releases related to a particular application.");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUuid);
        if (application == null) {
            throw new ApplicationManagementException(
                    "Application with UUID " + applicationUuid + " does not exist. Cannot "
                            + "retrieve the releases for a non-existing application.");
        }
        return application;
    }

    /**
     * To validate a create release request to make sure all the pre-conditions satisfied.
     *
     * @param applicationUuid    UUID of the Application.
     * @param applicationRelease ApplicationRelease that need to be created.
     * @return the Application related with the particular Application Release
     * @throws ApplicationManagementException Application Management Exception.
     */
    private Application validateReleaseCreateRequest(String applicationUuid, ApplicationRelease applicationRelease)
            throws ApplicationManagementException {
        if (applicationUuid == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to do the application release");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplication(applicationUuid);
        if (application == null) {
            throw new ApplicationManagementException(
                    "Application with UUID " + applicationUuid + " does not exist. Cannot "
                            + "release an application that is not existing");
        }
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
        return application;
    }
}
