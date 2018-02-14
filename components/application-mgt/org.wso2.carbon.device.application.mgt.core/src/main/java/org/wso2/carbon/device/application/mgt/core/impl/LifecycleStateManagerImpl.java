/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.ValidateApplicationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of Lifecycle state management.
 */
public class LifecycleStateManagerImpl implements LifecycleStateManager {

    private static final Log log = LogFactory.getLog(LifecycleStateManagerImpl.class);

    @Override
    public LifecycleState getLifecycleState(int applicationId, String applicationUuid) throws LifecycleManagementException {
        LifecycleState lifecycleState;
        try {
            ConnectionManagerUtil.openDBConnection();
            LifecycleStateDAO lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
            Application application = ValidateApplicationUtil.validateApplication(applicationId);
            //todo applicationUuid and applicationId should be passed and util method has to be changed
            ApplicationRelease applicationRelease = ValidateApplicationUtil.validateApplicationRelease(applicationUuid);
            lifecycleState = lifecycleStateDAO.getLatestLifeCycleStateByReleaseID(applicationRelease.getId());
            lifecycleState.setNextStates(getNextLifecycleStates(lifecycleState.getCurrentState()));
        } catch (ApplicationManagementDAOException e) {
            throw new LifecycleManagementException("Failed to get lifecycle state", e);
        } catch (DBConnectionException e) {
            throw new LifecycleManagementException("Failed to connect with Database", e);
        } catch (ApplicationManagementException e) {
            throw new LifecycleManagementException("Failed to get application and application management", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleState;
    }

    @Override
    public void addLifecycleState(int applicationId, String applicationUuid, LifecycleState state) throws LifecycleManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            Application application = ValidateApplicationUtil.validateApplication(applicationId);
            //todo applicationUuid and applicationId should be passed and util method has to be changed
            ApplicationRelease applicationRelease = ValidateApplicationUtil.validateApplicationRelease(applicationUuid);
            LifecycleStateDAO lifecycleStateDAO;

            if (application != null) {
                state.setAppId(applicationId);
            }
            if (applicationRelease != null) {
                state.setReleaseId(applicationRelease.getId());
            }
            if (state.getCurrentState() != null && state.getPreviousState() != null && state.getUpdatedBy() != null) {
                validateLifecycleState(state);
                lifecycleStateDAO = ApplicationManagementDAOFactory.getLifecycleStateDAO();
                lifecycleStateDAO.addLifecycleState(state);
            }
        } catch (LifeCycleManagementDAOException | DBConnectionException e) {
            throw new LifecycleManagementException("Failed to add lifecycle state", e);
        } catch (ApplicationManagementException e) {
            throw new LifecycleManagementException("Lifecycle State Validation failed", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private List<String> getNextLifecycleStates(String currentLifecycleState) {
        List<String> nextLifecycleStates = new ArrayList<>();
        if (AppLifecycleState.CREATED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.IN_REVIEW.toString());
        }
        if (AppLifecycleState.IN_REVIEW.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.APPROVED.toString());
            nextLifecycleStates.add(AppLifecycleState.REJECTED.toString());
        }
        if (AppLifecycleState.REJECTED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.IN_REVIEW.toString());
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        if (AppLifecycleState.APPROVED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.PUBLISHED.toString());
        }
        if (AppLifecycleState.PUBLISHED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.UNPUBLISHED.toString());
            nextLifecycleStates.add(AppLifecycleState.DEPRECATED.toString());
        }
        if (AppLifecycleState.UNPUBLISHED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.PUBLISHED.toString());
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        if (AppLifecycleState.DEPRECATED.toString().equals(currentLifecycleState)) {
            nextLifecycleStates.add(AppLifecycleState.REMOVED.toString());
        }
        return nextLifecycleStates;
    }

    private void validateLifecycleState(LifecycleState state) throws LifecycleManagementException {

        if (AppLifecycleState.CREATED.toString().equals(state.getCurrentState())) {
            throw new LifecycleManagementException("Current State Couldn't be " + state.getCurrentState());
        }
        if (AppLifecycleState.IN_REVIEW.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.CREATED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.REJECTED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.CREATED.toString() + " or " +
                        AppLifecycleState.REJECTED.toString());
            }
        }
        if (AppLifecycleState.APPROVED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.IN_REVIEW.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.IN_REVIEW.toString());
            }
        }
        if (AppLifecycleState.PUBLISHED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.APPROVED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.UNPUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.APPROVED.toString() + " or " +
                        AppLifecycleState.UNPUBLISHED.toString());
            }
        }
        if (AppLifecycleState.UNPUBLISHED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.PUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.PUBLISHED.toString());
            }
        }
        if (AppLifecycleState.REJECTED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.IN_REVIEW.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.IN_REVIEW.toString());
            }
        }
        if (AppLifecycleState.DEPRECATED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.PUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be " + AppLifecycleState.PUBLISHED.toString());
            }
        }
        if (AppLifecycleState.REMOVED.toString().equals(state.getCurrentState())) {
            if (!AppLifecycleState.DEPRECATED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.REJECTED.toString().equals(state.getPreviousState()) &&
                    !AppLifecycleState.UNPUBLISHED.toString().equals(state.getPreviousState())) {
                throw new LifecycleManagementException("If Current State is " + state.getCurrentState() +
                        "Previous State should be either " + AppLifecycleState.DEPRECATED.toString() + " or " +
                        AppLifecycleState.REJECTED.toString() + " or " + AppLifecycleState.UNPUBLISHED.toString());
            }
        }
    }
}
