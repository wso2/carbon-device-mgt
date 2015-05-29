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
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileOperationDAOImpl extends OperationDAOImpl {

    private static final Log log = LogFactory.getLog(ProfileOperationDAOImpl.class);

    public int addOperation(Operation operation) throws OperationManagementDAOException {

        int operationId = super.addOperation(operation);
        operation.setCreatedTimeStamp(new Timestamp(new java.util.Date().getTime()).toString());
        operation.setId(operationId);
        operation.setEnabled(true);
        ProfileOperation profileOp = (ProfileOperation) operation;
        Connection conn = OperationManagementDAOFactory.getConnection();

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement("INSERT INTO DM_PROFILE_OPERATION(OPERATION_ID, OPERATION_DETAILS) " +
                    "VALUES(?, ?)");
            stmt.setInt(1, operationId);
            stmt.setObject(2, profileOp);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding profile operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return operationId;
    }

    @Override
    public void updateOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;
        super.updateOperation(operation);

        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_PROFILE_OPERATION O SET O.OPERATION_DETAILS=? " +
                    "WHERE O.OPERATION_ID=?");

            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operation);

            stmt.setBytes(1, bao.toByteArray());
            stmt.setInt(2, operation.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update operation metadata", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing profile operation object", e);
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
    public void deleteOperation(int id) throws OperationManagementDAOException {

        super.deleteOperation(id);
        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("DELETE DM_PROFILE_OPERATION WHERE OPERATION_ID=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while deleting operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    public Operation getOperation(int id) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProfileOperation profileOperation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT OPERATION_ID, ENABLED, OPERATION_DETAILS FROM DM_PROFILE_OPERATION WHERE OPERATION_ID=?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                profileOperation = (ProfileOperation) ois.readObject();
            }

        } catch (IOException e) {
            String errorMsg = "IO Error occurred while de serialize the profile operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Class not found error occurred while de serialize the profile operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "SQL Error occurred while retrieving the command operation object " + "available for " +
                    "the id '"
                    + id;
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return profileOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int deviceId,
            Operation.Status status) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        ProfileOperation profileOperation;

        List<Operation> operationList = new ArrayList<Operation>();

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "Select po.OPERATION_ID, ENABLED, OPERATION_DETAILS from DM_PROFILE_OPERATION po " +
                    "INNER JOIN  " +
                    "(Select * From DM_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID=? " +
                    "AND STATUS=?) dm ON dm.OPERATION_ID = po.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                profileOperation = (ProfileOperation) ois.readObject();
                profileOperation.setStatus(status);
                operationList.add(profileOperation);
            }

        } catch (IOException e) {
            String errorMsg = "IO Error occurred while de serialize the profile operation object";
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Class not found error occurred while de serialize the profile operation object";
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
