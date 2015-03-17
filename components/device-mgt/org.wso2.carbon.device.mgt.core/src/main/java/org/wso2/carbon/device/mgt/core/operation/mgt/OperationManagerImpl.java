/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.operation.mgt;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;

import java.util.List;

public class OperationManagerImpl implements OperationManager {

    private OperationDAO commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
    private OperationDAO configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
    private OperationDAO simpleOperationDAO = OperationManagementDAOFactory.getSimpleOperationDAO();

    @Override
    public boolean addOperation(Operation operation,
                                List<DeviceIdentifier> devices) throws OperationManagementException {
        try {
            return this.lookupOperationDAO(operation).addOperation(operation);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while adding operation", e);
        }
    }

    @Override
    public List<Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
    }

    @Override
    public List<Operation> getPendingOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
    }

    @Override
    public List<Feature> getFeaturesForDeviceType(String deviceType) throws FeatureManagementException {
        return null;
    }

    private OperationDAO lookupOperationDAO(Operation operation) {
        if (operation instanceof CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof ConfigOperation) {
            return configOperationDAO;
        } else {
            return simpleOperationDAO;
        }
    }

}
