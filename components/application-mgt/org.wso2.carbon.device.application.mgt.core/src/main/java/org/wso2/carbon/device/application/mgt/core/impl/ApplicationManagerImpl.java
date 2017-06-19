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

import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.PlatformDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.ValidationException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.HelperUtil;

import java.util.Date;

public class ApplicationManagerImpl implements ApplicationManager {


    public static final String CREATED = "created";

    @Override
    public Application createApplication(Application application) throws ApplicationManagementException {

        validateApplication(application, false);

        try {
            ConnectionManagerUtil.openConnection();
            ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();

            application.setUuid(HelperUtil.generateApplicationUuid());

            application.setCreatedAt(new Date());
            application.setModifiedAt(new Date());

            LifecycleStateDAO lifecycleStateDAO = DAOFactory.getLifecycleStateDAO();
            LifecycleState lifecycleState = lifecycleStateDAO.getLifeCycleStateByIdentifier(CREATED);
            if (lifecycleState == null) {
                throw new NotFoundException("Invalid lifecycle state.");
            }

            Lifecycle lifecycle = new Lifecycle();
            lifecycle.setLifecycleState(lifecycleState);
            lifecycle.setLifecycleState(lifecycleState);
            lifecycle.setLifecycleStateModifiedAt(new Date());
            lifecycle.setGetLifecycleStateModifiedBy(application.getUser().getUserName());
            application.setCurrentLifecycle(lifecycle);

            PlatformDAO platformDAO = DAOFactory.getPlatformDAO();
            Platform platform = platformDAO.getPlatform(application.getUser().getTenantId(), application.getPlatform().getIdentifier());
            if (platform == null) {
                throw new NotFoundException("Invalid platform");
            }
            application.setPlatform(platform);

            return applicationDAO.createApplication(application);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }

    }


    @Override
    public Application editApplication(Application application) throws ApplicationManagementException {

        if (application.getUuid() == null) {
            throw new ValidationException("Application UUID cannot be empty");
        }

        try {
            ConnectionManagerUtil.openConnection();
            ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();

            if (application.getCategory() == null) {
                application.setCategory(new Category());
            }

            if (application.getPlatform() == null) {
                application.setPlatform(new Platform());
            } else {
                PlatformDAO platformDAO = DAOFactory.getPlatformDAO();
                Platform platform = platformDAO.getPlatform(application.getUser().getTenantId(), application.getPlatform()
                        .getIdentifier());
                application.setPlatform(platform);
                if (platform == null) {
                    throw new NotFoundException("Invalid platform");
                }
            }

            if (application.getPayment() == null) {
                application.setPayment(new Payment());
            }

            application.setModifiedAt(new Date());

            return applicationDAO.editApplication(application);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }

    }

    @Override
    public void deleteApplication(int uuid) throws ApplicationManagementException {

    }

    @Override
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {

        try {
            ConnectionManagerUtil.openConnection();
            ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
            return applicationDAO.getApplications(filter);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }

    }

    private void validateApplication(Application application, boolean isEdit) throws ValidationException {

        if (application.getName() == null) {
            throw new ValidationException("Application name cannot be empty");
        }

        if (application.getUser() == null || application.getUser().getUserName() == null ||
                application.getUser().getTenantId() == 0) {
            throw new ValidationException("Username and tenant Id cannot be empty");
        }

        if (application.getCategory() == null || application.getCategory().getId() == 0) {
            throw new ValidationException("Category id cannot be empty");
        }

        if (application.getPlatform() == null || application.getPlatform().getIdentifier() == null) {
            throw new ValidationException("Platform identifier cannot be empty");
        }


    }
}
