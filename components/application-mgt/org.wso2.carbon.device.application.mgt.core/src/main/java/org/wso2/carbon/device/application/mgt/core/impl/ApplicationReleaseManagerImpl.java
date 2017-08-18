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
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.util.Date;

public class ApplicationReleaseManagerImpl implements ApplicationReleaseManager {
    private static Log log = LogFactory.getLog(ApplicationReleaseManagerImpl.class);

    @Override
    public ApplicationRelease createRelease(String UUID, ApplicationRelease applicationRelease) throws
            ApplicationManagementException {
        if (UUID == null) {
            throw new ApplicationManagementException("Application UUID is null. Application UUID is a required "
                    + "parameter to do the application release");
        }
        Application application = DataHolder.getInstance().getApplicationManager().getApplication(UUID);
        if (application == null) {
            throw new ApplicationManagementException("Application with UUID " + UUID + " does not exist. Cannot "
                    + "release an application that is not existing");
        }
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
    public void makeDefaultRelease(int id) throws ApplicationManagementException {

    }

    @Override
    public void updateRelease(ApplicationRelease applicationRelease) throws ApplicationManagementException {

    }
}
