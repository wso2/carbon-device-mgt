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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class implements all the functionalities exposed as part of the OperationManager. Any transaction initiated
 * upon persisting information related to operation state, etc has to be managed, demarcated and terminated via the
 * methods available in OperationManagementDAOFactory.
 */
public class OperationManagerImpl implements OperationManager {

    private static final Log log = LogFactory.getLog(OperationManagerImpl.class);

    private OperationDAO commandOperationDAO;
    private OperationDAO configOperationDAO;
    private OperationDAO profileOperationDAO;
    private OperationMappingDAO operationMappingDAO;
    private DeviceDAO deviceDAO;
    private OperationDAO operationDAO;

    public OperationManagerImpl() {
        commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
        configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
        profileOperationDAO = OperationManagementDAOFactory.getProfileOperationDAO();
        operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        operationDAO = OperationManagementDAOFactory.getOperationDAO();
    }

    @Override
    public boolean addOperation(Operation operation,
                                List<DeviceIdentifier> devices) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.beginTransaction();
            int operationId = this.lookupOperationDAO(operation).addOperation(operation);
            for (DeviceIdentifier deviceIdentifier : devices) {
                Device device = deviceDAO.getDevice(deviceIdentifier);
                operationMappingDAO.addOperationMapping(operationId, device.getId());
            }
            OperationManagementDAOFactory.commitTransaction();
            return true;
        } catch (OperationManagementDAOException e) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while adding operation", e);
        } catch (DeviceManagementDAOException e) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while adding operation", e);
        }
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        try {
            List<Operation> operations = new ArrayList<Operation>();

            OperationManagementDAOFactory.beginTransaction();
            operations.addAll(profileOperationDAO.getOperations(deviceId));
            operations.addAll(configOperationDAO.getOperations(deviceId));
            operations.addAll(commandOperationDAO.getOperations(deviceId));
            OperationManagementDAOFactory.commitTransaction();

            return operations;
        } catch (OperationManagementDAOException e) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() + "' device '" + deviceId.getId() + "'", e);
        }
    }

    @Override
    public List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException {
        try {
            List<Operation> operations = new ArrayList<Operation>();

            OperationManagementDAOFactory.beginTransaction();
            operations.addAll(profileOperationDAO.getOperations(deviceId, Operation.Status.PENDING));
            operations.addAll(configOperationDAO.getOperations(deviceId, Operation.Status.PENDING));
            operations.addAll(commandOperationDAO.getOperations(deviceId, Operation.Status.PENDING));
            OperationManagementDAOFactory.commitTransaction();

            return operations;
        } catch (OperationManagementDAOException e) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "pending operations assigned for '" + deviceId.getType() + "' device '" +
                    deviceId.getId() + "'", e);
        }
}

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        try {
            Operation operation = operationDAO.getNextOperation(deviceId);
            return operation;
        } catch (OperationManagementDAOException e) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while retrieving next pending operation", e);
        }
    }

    @Override
    public void updateOperation(int operationId, Operation.Status operationStatus)
            throws OperationManagementException {
        try {
            OperationManagementDAOFactory.beginTransaction();
            Operation operation = operationDAO.getOperation(operationId);
            operation.setStatus(operationStatus);
            operationDAO.updateOperation(operation);
            OperationManagementDAOFactory.commitTransaction();
        }catch(OperationManagementDAOException ex){
            log.error("Error occurred while updating the operation: "+operationId);
            throw new OperationManagementException("Error occurred while update operation", ex);
        }

    }

    private OperationDAO lookupOperationDAO(Operation operation) {

        if (operation instanceof CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof ProfileOperation) {
            return profileOperationDAO;
        } else if (operation instanceof ConfigOperation) {
            return configOperationDAO;
        }else{
            return operationDAO;
        }
    }

    private OperationDAO lookupOperationDAO(Operation.Type type) {
        switch (type) {
            case CONFIG:
                return configOperationDAO;
            case PROFILE:
                return profileOperationDAO;
            case COMMAND:
                return commandOperationDAO;
            default:
                return commandOperationDAO;
        }
    }

}
