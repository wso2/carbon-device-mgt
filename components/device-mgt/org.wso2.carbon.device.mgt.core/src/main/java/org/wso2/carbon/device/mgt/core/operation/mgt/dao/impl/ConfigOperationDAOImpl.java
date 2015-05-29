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
package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConfigOperationDAOImpl extends OperationDAOImpl {

    private static final Log log = LogFactory.getLog(ConfigOperationDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {

        int operationId = super.addOperation(operation);
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_CONFIG_OPERATION(OPERATION_ID, OPERATION_CONFIG) VALUES(?,?)");
            stmt.setInt(1, operationId);
            stmt.setObject(2, operation);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding command operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return operationId;
    }

    @Override
    public void deleteOperation(int id) throws OperationManagementDAOException {

        super.deleteOperation(id);
        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("DELETE DM_CONFIG_OPERATION WHERE OPERATION_ID=?") ;
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while deleting operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void updateOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;
        super.updateOperation(operation);

        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_CONFIG_OPERATION O SET O.OPERATION_CONFIG=? " +
                    "WHERE O.OPERATION_ID=?");

            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operation);

            stmt.setBytes(1, bao.toByteArray());
            stmt.setInt(2, operation.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update policy operation metadata", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing policy operation object", e);
        } finally {
            if (bao != null) {
                try {
                    bao.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ByteArrayOutputStream", e);
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ObjectOutputStream", e);
                }
            }
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        ConfigOperation configOperation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT OPERATION_ID, ENABLED, OPERATION_CONFIG FROM DM_CONFIG_OPERATION WHERE OPERATION_ID=?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                configOperation = (ConfigOperation) ois.readObject();
            }

        } catch (IOException e) {
            String errorMsg = "IO Error occurred while de serialize the policy operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Class not found error occurred while de serialize the policy operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "SQL Error occurred while retrieving the policy operation object " + "available for " +
                    "the id '"
                    + operationId;
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return configOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int deviceId,
            Operation.Status status) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        ConfigOperation configOperation;

        List<Operation> operationList = new ArrayList<Operation>();

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "Select co.OPERATION_ID, co.OPERATION_CONFIG from DM_CONFIG_OPERATION co " +
                    "INNER JOIN  " +
                    "(Select * From DM_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID=? " +
                    "AND STATUS=?) dm ON dm.OPERATION_ID = co.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_CONFIG");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                configOperation = (ConfigOperation) ois.readObject();
                operationList.add(configOperation);
            }

        } catch (IOException e) {
            String errorMsg = "IO Error occurred while de serialize the configuration operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Class not found error occurred while de serialize the configuration operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + deviceId +
                    "' with status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ByteArrayOutputStream", e);
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.warn("Error occurred while closing ObjectOutputStream", e);
                }
            }
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }
}
