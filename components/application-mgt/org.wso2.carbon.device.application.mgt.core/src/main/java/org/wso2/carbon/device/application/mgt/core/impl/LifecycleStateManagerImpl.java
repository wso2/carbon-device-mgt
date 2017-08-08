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
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.DAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.util.List;

public class LifecycleStateManagerImpl implements LifecycleStateManager {

    private static final Log log = LogFactory.getLog(LifecycleStateManagerImpl.class);

    @Override
    public List<LifecycleState> getLifecycleStates() throws LifecycleManagementException {
        List<LifecycleState> lifecycleStates = null;
        try {
            ConnectionManagerUtil.openDBConnection();
            LifecycleStateDAO lifecycleStateDAO = DAOFactory.getLifecycleStateDAO();
            lifecycleStates = lifecycleStateDAO.getLifecycleStates();
        } catch (DAOException | DBConnectionException e) {
            throw new LifecycleManagementException("Failed get lifecycle states.", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return lifecycleStates;
    }

    @Override
    public void addLifecycleState(LifecycleState state) throws LifecycleManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            LifecycleStateDAO lifecycleStateDAO = DAOFactory.getLifecycleStateDAO();
            lifecycleStateDAO.addLifecycleState(state);
        } catch (DAOException | DBConnectionException e) {
            throw new LifecycleManagementException("Failed to add lifecycle state", e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void deleteLifecycleState(String identifier) throws LifecycleManagementException {

        try {
            ConnectionManagerUtil.openDBConnection();
            LifecycleStateDAO lifecycleStateDAO = DAOFactory.getLifecycleStateDAO();
            lifecycleStateDAO.deleteLifecycleState(identifier);
        } catch (DAOException | DBConnectionException e) {
            throw new LifecycleManagementException("Failed to add lifecycle state: " + identifier, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
