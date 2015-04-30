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
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements all the functionality exposed as part of the OperationManager. Any transaction initiated
 * upon persisting information related to operation state, etc has to be managed, demarcated and terminated via the
 * methods available in OperationManagementDAOFactory.
 */
public class OperationManagerImpl implements OperationManager {

    private static final Log log = LogFactory.getLog(OperationManagerImpl.class);

    private OperationDAO commandOperationDAO;
    private OperationDAO configOperationDAO;
    private OperationDAO profileOperationDAO;
    private OperationMappingDAO operationMappingDAO;
    private OperationDAO operationDAO;
    private DeviceManagementService deviceManagementService;

    public OperationManagerImpl() {
        commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
        configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
        profileOperationDAO = OperationManagementDAOFactory.getProfileOperationDAO();
        operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
        operationDAO = OperationManagementDAOFactory.getOperationDAO();
        deviceManagementService = new DeviceManagementServiceImpl();
    }

    @Override
    public boolean addOperation(Operation operation, List<DeviceIdentifier> devices) throws
            OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("operation:[" + operation.toString() + "]");
            for (DeviceIdentifier deviceIdentifier : devices) {
                log.debug("device identifier id:[" + deviceIdentifier.getId() + "] type:[" + deviceIdentifier.getType()
                        + "]");
            }
        }
        try {
            OperationManagementDAOFactory.beginTransaction();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto =
                    OperationDAOUtil.convertOperation(operation);
            int operationId = this.lookupOperationDAO(operation).addOperation(operationDto);
            org.wso2.carbon.device.mgt.common.Device device;

            for (DeviceIdentifier deviceIdentifier : devices) {
                device = deviceManagementService.getCoreDevice(deviceIdentifier);
                if (device == null) {
                    String errorMsg = "The operation not added for device.The device not found for " +
                            "device Identifier type -'" + deviceIdentifier.getType() + "' and device Id '" +
                            deviceIdentifier.getId();
                    log.info(errorMsg);
                } else {
                    operationMappingDAO.addOperationMapping(operationId, device.getId());
                }
            }
            OperationManagementDAOFactory.commitTransaction();
            return true;
        } catch (OperationManagementDAOException e) {
            log.error("Error occurred while adding operation: ", e);
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while adding operation", e);
        } catch (DeviceManagementException deviceMgtEx) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            String errorMsg = "Error occurred fetching devices ";
            log.error(deviceMgtEx.getErrorMessage(), deviceMgtEx);
            throw new OperationManagementException(errorMsg, deviceMgtEx);
        }
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            org.wso2.carbon.device.mgt.common.Device device;

            try {
                device = deviceManagementService.getCoreDevice(deviceIdentifier);
            } catch (DeviceManagementException deviceMgtEx) {
                String errorMsg = "Error occurred while retrieving the device " +
                        "for device Identifier type -'" + deviceIdentifier.getType() + "' and device Id '" +
                        deviceIdentifier.getId();
                log.error(errorMsg, deviceMgtEx);
                throw new OperationManagementException(errorMsg, deviceMgtEx);
            }
            if (device == null) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceIdentifier.getId() + " and given type" + deviceIdentifier.getType());
            }
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> operationList = operationDAO
                    .getOperationsForDevice(device.getId());
            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : operationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceIdentifier.getType() + "' device '" + deviceIdentifier.getId()
                    + "'", e);
        }
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Device identifier id:[" + deviceIdentifier.getId() + "] type:[" + deviceIdentifier.getType()
                    + "]");
        }

        org.wso2.carbon.device.mgt.common.Device device;
        List<Operation> operations;
        List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList;

        try {

            device = deviceManagementService.getCoreDevice(deviceIdentifier);

            if (device == null) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceIdentifier.getId() + " and given type" + deviceIdentifier.getType());
            }
            operations = new ArrayList<Operation>();
            dtoOperationList = operationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING);

            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (DeviceManagementException deviceMgtException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceIdentifier.getType() + "' and device Id '"
                    + deviceIdentifier.getId();
            log.error(errorMsg, deviceMgtException);
            throw new OperationManagementException(errorMsg, deviceMgtException);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "pending operations assigned for '" + deviceIdentifier.getType() + "' device '" +
                    deviceIdentifier.getId() + "'", e);
        }
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType() + "]");
        }
        Operation operation = null;
        try {
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getNextOperation(deviceId);
            if (dtoOperation != null) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
            }
            return operation;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving next pending operation", e);
        }
    }

    @Override
    public void updateOperation(int operationId, Operation.Status operationStatus)
            throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("operation Id:" + operationId + " status:" + operationStatus);
        }

        try {
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation =
                    operationDAO.getOperation(operationId);

            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for operation id:" + operationId);
            }
            dtoOperation.setStatus(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.valueOf
                    (operationStatus.toString()));
            OperationManagementDAOFactory.beginTransaction();
            lookupOperationDAO(dtoOperation).updateOperation(dtoOperation);
            OperationManagementDAOFactory.commitTransaction();
        } catch (OperationManagementDAOException ex) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the update operation transaction", e1);
            }
            log.error("Error occurred while updating the operation: " + operationId + " status:" + operationStatus, ex);
            throw new OperationManagementException("Error occurred while update operation", ex);
        }
    }

    @Override
    public void deleteOperation(int operationId) throws OperationManagementException {

        try {
            OperationManagementDAOFactory.beginTransaction();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operation = operationDAO.getOperation
                    (operationId);

            if (operation == null) {
                throw new OperationManagementException("Operation not found for operation id:" + operationId);
            }

            lookupOperationDAO(operation).deleteOperation(operationId);
            OperationManagementDAOFactory.commitTransaction();

        } catch (OperationManagementDAOException ex) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e) {
                log.warn("Error occurred while roll-backing the delete operation transaction", e);
            }
            log.error("Error occurred while deleting the operation: " + operationId, ex);
            throw new OperationManagementException("Error occurred while delete operation", ex);
        }
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceIdentifier, int operationId)
            throws OperationManagementException {

        org.wso2.carbon.device.mgt.common.Device device;
        Operation operation;

        if (log.isDebugEnabled()) {
            log.debug(
                    "Operation Id:" + operationId + " Device Type:" + deviceIdentifier.getType() + " Device Identifier:"
                            +
                            deviceIdentifier.getId());
        }

        try {
            device = deviceManagementService.getCoreDevice(deviceIdentifier);
            if (device == null) {
                throw new OperationManagementException("Device not found for given device identifier:" +
                        deviceIdentifier.getId() + " type:" + deviceIdentifier.getType());
            }
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getOperationByDeviceAndId(device.getId(), operationId);

            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for operation Id:" + operationId +
                        " device" + " Id:" + device.getId());
            }
            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (DeviceManagementException deviceMgtException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceIdentifier.getType() + "' and device Id '"
                    + deviceIdentifier.getId();
            log.error(errorMsg, deviceMgtException);
            throw new OperationManagementException(errorMsg, deviceMgtException);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceIdentifier.getType() + "' device '" + deviceIdentifier.getId()
                    + "'", e);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            org.wso2.carbon.device.mgt.common.Device device = deviceManagementService.getCoreDevice(identifier);

            if (device == null) {
                throw new DeviceManagementException("Device not found for device id:" + identifier.getId() + " " +
                        "type:" + identifier.getType());
            }
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList =
                    operationDAO.getOperationsByDeviceAndStatus(device.getId(),
                            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status
                                    .valueOf(status.toString()));

            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (DeviceManagementException deviceMgtException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + identifier.getType() + "' and device Id '" + identifier.getId();
            log.error(errorMsg, deviceMgtException);
            throw new OperationManagementException(errorMsg, deviceMgtException);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + identifier.getType() + "' device '" +
                    identifier.getId() + "' and status:" + status.toString(), e);
        }
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementException {

        Operation operation;
        try {
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO.getOperation
                    (operationId);
            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for given Id:" + operationId);
            }
            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (OperationManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the operation with operation Id '" + operationId;
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsForStatus(Operation.Status status)
            throws OperationManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList =
                    operationDAO.getOperationsForStatus(
                            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status
                                    .valueOf(status.toString()));
            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations for status:'" + status.toString(), e);
        }
    }

    private OperationDAO lookupOperationDAO(Operation operation) {

        if (operation instanceof CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof ProfileOperation) {
            return profileOperationDAO;
        } else if (operation instanceof ConfigOperation) {
            return configOperationDAO;
        } else {
            return operationDAO;
        }
    }

    private OperationDAO lookupOperationDAO(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operation) {

        if (operation instanceof org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation) {
            return profileOperationDAO;
        } else if (operation instanceof org.wso2.carbon.device.mgt.core.dto.operation.mgt.ConfigOperation) {
            return configOperationDAO;
        } else {
            return operationDAO;
        }
    }
}
