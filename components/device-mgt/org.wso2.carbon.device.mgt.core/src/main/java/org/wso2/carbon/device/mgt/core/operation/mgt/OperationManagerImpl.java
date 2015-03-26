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
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;

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
    private OperationDAO simpleOperationDAO;
    private OperationMappingDAO operationMappingDAO;
    private DeviceDAO deviceDAO;

    public OperationManagerImpl() {
        commandOperationDAO = OperationManagementDAOFactory.getCommandOperationDAO();
        configOperationDAO = OperationManagementDAOFactory.getConfigOperationDAO();
        simpleOperationDAO = OperationManagementDAOFactory.getSimpleOperationDAO();
        operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
    }

    @Override
    public boolean addOperation(Operation operation,
                                List<DeviceIdentifier> devices) throws OperationManagementException {
        try {
            OperationManagementDAOFactory.beginTransaction();
            int operationId = this.lookupOperationDAO(operation).addOperation(operation);
            for(Iterator<DeviceIdentifier> i = devices.iterator(); i.hasNext(); ) {
                DeviceIdentifier deviceIdentifier = i.next();
                org.wso2.carbon.device.mgt.core.dto.Device device = deviceDAO.getDevice(deviceIdentifier);
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
    public List<Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
        try{
            this.lookupOperationDAO(operation)


        }catch(OperationManagementDAOException ex){}
    }

    @Override
    public List<Operation> getPendingOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
    }

    @Override
    public Operation getPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
    }

    @Override
    public Operation updateOperation(Long operationId, DeviceIdentifier deviceIdentifier,
            String responsePayLoad) throws OperationManagementException {
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
