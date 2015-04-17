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
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.util.ArrayList;
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

        if (log.isDebugEnabled()){
            log.debug("operation:["+operation.toString()+"]");
            for(DeviceIdentifier deviceIdentifier:devices){
                log.debug("device identifier id:["+deviceIdentifier.getId()+"] type:["+deviceIdentifier.getType()+"]");
            }
        }
        try {
            OperationManagementDAOFactory.beginTransaction();

            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto = OperationDAOUtil
                    .convertOperation(operation);
            int operationId = this.lookupOperationDAO(operation).addOperation(operationDto);

            for (DeviceIdentifier deviceIdentifier : devices) {
                Device device = deviceDAO.getDevice(deviceIdentifier);
                operationMappingDAO.addOperationMapping(operationId, device.getId());
            }
            OperationManagementDAOFactory.commitTransaction();
            return true;
        } catch (OperationManagementDAOException e) {
            log.error("Error occurred while adding operation: ",e);
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e1);
            }
            throw new OperationManagementException("Error occurred while adding operation", e);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while adding operation device mapping: ",e);
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
            Device device;
            try {
                device = deviceDAO.getDevice(deviceId);
            } catch (DeviceManagementDAOException deviceDAOException) {
                String errorMsg = "Error occurred while retrieving the device " +
                        "for device Identifier type -'" + deviceId.getType() + "' and device Id '" + deviceId.getId();
                log.error(errorMsg, deviceDAOException);
                throw new OperationManagementException(errorMsg, deviceDAOException);
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
                    "operations assigned for '" + deviceId.getType() + "' device '" + deviceId.getId() + "'", e);
        }
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceId)
            throws OperationManagementException {

        if (log.isDebugEnabled()){
           log.debug("device identifier id:["+deviceId.getId()+"] type:["+deviceId.getType()+"]");
        }

        try {
            Device device;
            device = deviceDAO.getDevice(deviceId);
            List<Operation> operations = new ArrayList<Operation>();
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList =
                    operationDAO.getOperationsByDeviceAndStatus(device.getId(), org.wso2.carbon.device.mgt.core.dto
                            .operation.mgt.Operation.Status.PENDING);
            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (DeviceManagementDAOException deviceDAOException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '" + deviceId.getId();
            log.error(errorMsg, deviceDAOException);
            throw new OperationManagementException(errorMsg, deviceDAOException);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "pending operations assigned for '" + deviceId.getType() + "' device '" +
                    deviceId.getId() + "'", e);
        }
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {

        if (log.isDebugEnabled()){
            log.debug("device identifier id:["+deviceId.getId()+"] type:["+deviceId.getType()+"]");
        }
        try {
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getNextOperation(deviceId);
            Operation operation = null;
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

        if (log.isDebugEnabled()){
            log.debug("operation Id:"+operationId+" status:"+operationStatus);
        }

        try {
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO.getOperation
                    (operationId);

            if (dtoOperation == null){
                throw new OperationManagementException("Operation not found for operation id:"+operationId);
            }
            dtoOperation.setStatus(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.valueOf
                    (operationStatus.toString()));
            OperationManagementDAOFactory.beginTransaction();
            operationDAO.updateOperation(dtoOperation);
            OperationManagementDAOFactory.commitTransaction();
        } catch (OperationManagementDAOException ex) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the update operation transaction", e1);
            }
            log.error("Error occurred while updating the operation: " + operationId + " status:"+operationStatus,ex);
            throw new OperationManagementException("Error occurred while update operation", ex);
        }
    }

    @Override
    public void deleteOperation(int operationId) throws OperationManagementException {

        try {
            OperationManagementDAOFactory.beginTransaction();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operation = operationDAO.getOperation
                    (operationId);

            if (operation == null){
                throw new OperationManagementException("Operation not found for operation id:"+ operationId);
            }

            if (operation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND)) {
                commandOperationDAO.deleteOperation(operationId);
            } else if (operation.getType().equals(
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG)) {
                configOperationDAO.deleteOperation(operationId);
            } else if (operation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                    .PROFILE)) {
                profileOperationDAO.deleteOperation(operationId);
            }
            OperationManagementDAOFactory.commitTransaction();
        } catch (OperationManagementDAOException ex) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the delete operation transaction", e1);
            }
            log.error("Error occurred while deleting the operation: " + operationId, ex);
            throw new OperationManagementException("Error occurred while delete operation", ex);
        }
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {

        Device device;
        Operation operation;

        if (log.isDebugEnabled()){
            log.debug("Device Type:"+deviceId.getType()+" Id:"+deviceId.getId());
        }

        try {
            device = deviceDAO.getDevice(deviceId);
            if (device == null){
                throw new OperationManagementException("Device not found for given device identifier:"+deviceId.getId
                        ()+" type:"+device.getDeviceType());
            }
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getOperationByDeviceAndId(device.getId(), operationId);

            if (dtoOperation == null){
                throw new OperationManagementException("Operation not found for operation Id:"+ operationId +" device" +
                        " Id:"+device.getId());
            }
            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (DeviceManagementDAOException deviceDAOException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '" + deviceId.getId();
            log.error(errorMsg, deviceDAOException);
            throw new OperationManagementException(errorMsg, deviceDAOException);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() + "' device '" + deviceId.getId() + "'", e);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
            Operation.Status status) throws OperationManagementException, DeviceManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            Device device = deviceDAO.getDevice(identifier);

            if (device == null){
                throw new DeviceManagementException("Device not found for device id:"+identifier.getId()+" " +
                        "type:"+identifier.getType());
            }
            List<? extends org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList = operationDAO
                    .getOperationsByDeviceAndStatus(device.getId(),
                            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status
                                    .valueOf(status.toString()));
            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (DeviceManagementDAOException deviceDAOException) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + identifier.getType() + "' and device Id '" + identifier.getId();
            log.error(errorMsg, deviceDAOException);
            throw new OperationManagementException(errorMsg, deviceDAOException);
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
             if (dtoOperation == null){
                 throw new OperationManagementException("Operation not found for given Id:"+operationId);
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
                    operationDAO.getOperationsForStatus(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status
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
