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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.util.OperationCreateTimeComparator;

import java.util.ArrayList;
import java.util.Collections;
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
    private OperationDAO policyOperationDAO;
    private OperationMappingDAO operationMappingDAO;
    private OperationDAO operationDAO;
    private DeviceDAO deviceDAO;
    private DeviceTypeDAO deviceTypeDAO;

    public OperationManagerImpl() {
        commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
        configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
        profileOperationDAO = OperationManagementDAOFactory.getProfileOperationDAO();
        policyOperationDAO = OperationManagementDAOFactory.getPolicyOperationDAO();
        operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
        operationDAO = OperationManagementDAOFactory.getOperationDAO();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
    }

    @Override
    public int addOperation(Operation operation,
                                List<DeviceIdentifier> deviceIds) throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("operation:[" + operation.toString() + "]");
            for (DeviceIdentifier deviceIdentifier : deviceIds) {
                log.debug("device identifier id:[" + deviceIdentifier.getId() + "] type:[" + deviceIdentifier.getType()
                        + "]");
            }
        }
        try {
            OperationManagementDAOFactory.beginTransaction();
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation operationDto =
                    OperationDAOUtil.convertOperation(operation);

            int operationId = this.lookupOperationDAO(operation).addOperation(operationDto);

            Device device;
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            for (DeviceIdentifier deviceId : deviceIds) {
                device = deviceDAO.getDevice(deviceId, tenantId);
                if (device == null) {
                    String errorMsg = "The operation not added for device.The device not found for " +
                            "device Identifier type -'" + deviceId.getType() + "' and device Id '" +
                            deviceId.getId();
                    log.info(errorMsg);
                } else {
                    operationMappingDAO.addOperationMapping(operationId, device.getId());
                }
            }
            OperationManagementDAOFactory.commitTransaction();
            return operationId;
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
            throw new OperationManagementException("Error occurred while retrieving device metadata", e);
        }
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            Device device = null;

            try {
                int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                device = deviceDAO.getDevice(deviceId, tenantId);
            } catch (DeviceManagementDAOException e) {
                e.printStackTrace();
            }
            if (device == null) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceId.getId() + " and given type" + deviceId.getType());
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
                    "operations assigned for '" + deviceId.getType() + "' device '" + deviceId.getId()
                    + "'", e);
        }
    }

    @Override
    public List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType()
                    + "]");
        }

        Device device;
        List<Operation> operations = new ArrayList<Operation>();

        List<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList =
                new ArrayList<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation>();

        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            device = deviceDAO.getDevice(deviceId, tenantId);

            if (device.getEnrolmentInfo().getStatus() !=null && !device.getEnrolmentInfo().getStatus().equals(
                    EnrolmentInfo.Status.ACTIVE)){
                try {
                    DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                            .updateDeviceEnrolmentInfo(device,
                                    EnrolmentInfo.Status.ACTIVE);
                }catch (DeviceManagementException deviceMgtEx){
                    String errorMsg = "Error occurred while update enrol status: "+deviceId.toString();
                    log.error(errorMsg, deviceMgtEx);
                    throw new OperationManagementException(errorMsg, deviceMgtEx);
                }
            }



            if (device == null) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceId.getId() + " and given type:" + deviceId.getType());
            }

            dtoOperationList.addAll(commandOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            dtoOperationList.addAll(configOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            dtoOperationList.addAll(profileOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            dtoOperationList.addAll(policyOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            Operation operation;
            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            Collections.sort(operations, new OperationCreateTimeComparator());
            return operations;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "pending operations assigned for '" + deviceId.getType() + "' device '" +
                    deviceId.getId() + "'", e);
        } catch (DeviceManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '"
                    + deviceId.getId();
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
        }
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("device identifier id:[" + deviceId.getId() + "] type:[" + deviceId.getType()
                    + "]");
        }
        Operation operation = null;
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            device = deviceDAO.getDevice(deviceId, tenantId);

            if (device == null) {
                throw new OperationManagementException("Device not found for given device " +
                        "Identifier:" + deviceId.getId() + " and given type" + deviceId.getType());
            }
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getNextOperation(device.getId());

            if (device.getEnrolmentInfo().getStatus() !=null && !device.getEnrolmentInfo().getStatus().equals(
                    EnrolmentInfo.Status.ACTIVE)){
                try {
                    DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                            .updateDeviceEnrolmentInfo(device,
                                    EnrolmentInfo.Status.ACTIVE);
                }catch (DeviceManagementException deviceMgtEx){
                    String errorMsg = "Error occurred while update enrol status: "+deviceId.toString();
                    log.error(errorMsg, deviceMgtEx);
                    throw new OperationManagementException(errorMsg, deviceMgtEx);
                }
            }

            if (dtoOperation != null) {
                if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND
                        .equals(dtoOperation.getType())) {
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                    commandOperation =
                            (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO
                                    .getOperation(dtoOperation.getId());
                    dtoOperation.setEnabled(commandOperation.isEnabled());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG
                        .equals(dtoOperation.getType())) {
                    dtoOperation = configOperationDAO.getOperation(dtoOperation.getId());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.PROFILE
                        .equals(dtoOperation.getType())) {
                    dtoOperation = profileOperationDAO.getOperation(dtoOperation.getId());
                } else if (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                        .POLICY.equals(dtoOperation.getType())) {
                    dtoOperation = policyOperationDAO.getOperation(dtoOperation.getId());
                }
                operation = OperationDAOUtil.convertOperation(dtoOperation);
            }
            return operation;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving next pending operation", e);
        } catch (DeviceManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '"
                    + deviceId.getId();
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
        }
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException {

        int operationId = operation.getId();

        if (log.isDebugEnabled()) {
            log.debug("operation Id:" + operationId + " status:" + operation.getStatus());
        }

        try {

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            Device device = deviceDAO.getDevice(deviceId, tenantId);

            if (operation.getStatus() !=null) {
                OperationManagementDAOFactory.beginTransaction();
                operationDAO.updateOperationStatus(device.getId(), operationId,
                        org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status
                                .valueOf(operation.getStatus().toString()));
                OperationManagementDAOFactory.commitTransaction();
            }

            if (operation.getOperationResponse() != null){
                OperationManagementDAOFactory.beginTransaction();
                operationDAO.addOperationResponse(device.getId(), operationId, operation.getOperationResponse());
                OperationManagementDAOFactory.commitTransaction();
            }
        } catch (OperationManagementDAOException ex) {
            try {
                OperationManagementDAOFactory.rollbackTransaction();
            } catch (OperationManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the update operation transaction", e1);
            }
            log.error("Error occurred while updating the operation: " + operationId + " status:" + operation.getStatus(), ex);
            throw new OperationManagementException("Error occurred while update operation", ex);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while fetch the device for device identifier: " + deviceId.getId() + " " +
                    "type:" + deviceId.getType(), e);
            throw new OperationManagementException("Error occurred while update operation", e);
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
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException {
        Device device;
        Operation operation;

        if (log.isDebugEnabled()) {
            log.debug("Operation Id:" + operationId + " Device Type:" + deviceId.getType() + " Device Identifier:" +
                    deviceId.getId());
        }

        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            device = deviceDAO.getDevice(deviceId, tenantId);
            if (device == null) {
                throw new OperationManagementException("Device not found for given device identifier:" +
                        deviceId.getId() + " type:" + deviceId.getType());
            }
            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation = operationDAO
                    .getOperationByDeviceAndId(device.getId(), operationId);

            if (dtoOperation.getType()
                    .equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND)) {
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                commandOperation = (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO
                        .getOperation(dtoOperation.getId());
                dtoOperation.setEnabled(commandOperation.isEnabled());
            } else if (dtoOperation.getType()
                    .equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG)) {
                dtoOperation = configOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                    .PROFILE)) {
                dtoOperation = profileOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                    .POLICY)) {

                dtoOperation = policyOperationDAO.getOperation(dtoOperation.getId());
            }

            if (dtoOperation == null) {
                throw new OperationManagementException("Operation not found for operation Id:" + operationId +
                        " device" + " Id:" + device.getId());
            }
            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() + "' device '" + deviceId.getId()
                    + "'", e);
        } catch (DeviceManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '"
                    + deviceId.getId();
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId, Operation.Status status) throws OperationManagementException {

        try {
            List<Operation> operations = new ArrayList<Operation>();
            List<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation> dtoOperationList =
                    new ArrayList<org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation>();

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            Device device = deviceDAO.getDevice(deviceId, tenantId);

            if (device == null) {
                throw new OperationManagementException("Device not found for device id:" + deviceId.getId() + " " +
                        "type:" + deviceId.getType());
            }

            org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status dtoOpStatus = org.wso2.carbon.device
                    .mgt.core.dto.operation.mgt.Operation.Status.valueOf(status.toString());
            dtoOperationList.addAll(commandOperationDAO.getOperationsByDeviceAndStatus(device.getId(), dtoOpStatus));

            dtoOperationList.addAll(configOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            dtoOperationList.addAll(profileOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            dtoOperationList.addAll(policyOperationDAO.getOperationsByDeviceAndStatus(device.getId(),
                    org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Status.PENDING));

            Operation operation;

            for (org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation dtoOperation : dtoOperationList) {
                operation = OperationDAOUtil.convertOperation(dtoOperation);
                operations.add(operation);
            }
            return operations;
        } catch (OperationManagementDAOException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of " +
                    "operations assigned for '" + deviceId.getType() + "' device '" +
                    deviceId.getId() + "' and status:" + status.toString(), e);
        } catch (DeviceManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the device " +
                    "for device Identifier type -'" + deviceId.getType() + "' and device Id '" + deviceId.getId();
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
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

            if (dtoOperation.getType()
                    .equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.COMMAND)) {
                org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation commandOperation;
                commandOperation = (org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation) commandOperationDAO
                        .getOperation(dtoOperation.getId());
                dtoOperation.setEnabled(commandOperation.isEnabled());
            } else if (dtoOperation.getType()
                    .equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type.CONFIG)) {
                dtoOperation = configOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                    .PROFILE)) {
                dtoOperation = profileOperationDAO.getOperation(dtoOperation.getId());
            } else if (dtoOperation.getType().equals(org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation.Type
                    .POLICY)) {

                dtoOperation = policyOperationDAO.getOperation(dtoOperation.getId());
            }

            operation = OperationDAOUtil.convertOperation(dtoOperation);
        } catch (OperationManagementDAOException e) {
            String errorMsg = "Error occurred while retrieving the operation with operation Id '" + operationId;
            log.error(errorMsg, e);
            throw new OperationManagementException(errorMsg, e);
        }
        return operation;
    }

    private OperationDAO lookupOperationDAO(Operation operation) {

        if (operation instanceof CommandOperation) {
            return commandOperationDAO;
        } else if (operation instanceof ProfileOperation) {
            return profileOperationDAO;
        } else if (operation instanceof ConfigOperation) {
            return configOperationDAO;
        } else if (operation instanceof PolicyOperation) {
            return policyOperationDAO;
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
