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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperationDAOImpl implements OperationDAO {

    public int addOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(
                    "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS,OPERATIONCODE)  " +
                            "VALUES (?, ?, ?, ?,?)");
            stmt.setString(1, operation.getType().toString());
            stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(3, null);
            stmt.setString(4, Operation.Status.PENDING.toString());
            stmt.setString(5, operation.getCode());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int updateOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(
                    "UPDATE DM_OPERATION O SET O.RECEIVED_TIMESTAMP=?,O.STATUS=? WHERE O.ID=?");

            stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            stmt.setString(2, operation.getStatus().toString());
            stmt.setInt(3, operation.getId());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            return id;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int deleteOperation(int id) throws OperationManagementDAOException {
        return 0;
    }

    public Operation getOperation(int id) throws OperationManagementDAOException {
        return null;
    }

    @Override
    public Operation getOperation(DeviceIdentifier deviceId, int operationId) throws OperationManagementDAOException {

        return null;
    }

    @Override
    public Operation getOperation(DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementDAOException {
        return null;
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementDAOException {
        List<Operation> operations;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql =
                    "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATIONCODE FROM DM_OPERATION o " +
                            "INNER JOIN (SELECT dom.OPERATION_ID AS OP_ID FROM (SELECT d.ID FROM DM_DEVICE d INNER " +
                            "JOIN " +
                            "DM_DEVICE_TYPE dm ON d.DEVICE_TYPE_ID = dm.ID AND dm.NAME = ? AND d" +
                            ".DEVICE_IDENTIFICATION = ?) d1 " +
                            "INNER JOIN DM_DEVICE_OPERATION_MAPPING dom ON d1.ID = dom.DEVICE_ID) ois  " +
                            "ON o.ID = ois.OP_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());
            rs = stmt.executeQuery();

            operations = new ArrayList<Operation>();
            while (rs.next()) {
                Operation operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("CREATED_TIMESTAMP") == null){
                    operation.setReceivedTimeStamp("");
                }else{
                    operation.setReceivedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                }
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                operation.setCode(rs.getString("OPERATIONCODE"));
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while retrieving the operation list " +
                    "available for the '" + deviceId.getType() + "' with id '" + deviceId.getId() + "'", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementDAOException {
        return null;
    }

    @Override
    public List<? extends Operation> getOperations(Operation.Status status) throws OperationManagementDAOException {
        return null;
    }

    @Override
    public Operation getNextOperation(DeviceIdentifier deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(
                    "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS,o.OPERATIONCODE " +
                            " FROM DM_OPERATION o " +
                            "INNER JOIN (SELECT dom.OPERATION_ID AS OP_ID FROM (SELECT d.ID " +
                            "FROM DM_DEVICE d INNER JOIN DM_DEVICE_TYPE dm ON d.DEVICE_TYPE_ID = dm.ID AND " +
                            "dm.NAME = ? AND d.DEVICE_IDENTIFICATION = ?) d1 INNER JOIN " +
                            "DM_DEVICE_OPERATION_MAPPING dom ON d1.ID = dom.DEVICE_ID) ois ON o.ID = ois.OP_ID " +
                            "ORDER BY o.CREATED_TIMESTAMP ASC LIMIT 1");
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());
            rs = stmt.executeQuery();

            Operation operation = null;
            if (rs.next()) {

                operation = new Operation();
                operation.setType(this.getType(rs.getString("TYPE")));
                operation.setStatus(this.getStatus(rs.getString("STATUS")));
                operation.setId(rs.getInt("ID"));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null){
                    operation.setReceivedTimeStamp("");
                }else{
                    operation.setReceivedTimeStamp(rs.getString("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATIONCODE"));
            }
            return operation;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private Operation.Status getStatus(String status) {
        return Operation.Status.valueOf(status);
    }

    private Operation.Type getType(String type) {
        return Operation.Type.valueOf(type);
    }

}
