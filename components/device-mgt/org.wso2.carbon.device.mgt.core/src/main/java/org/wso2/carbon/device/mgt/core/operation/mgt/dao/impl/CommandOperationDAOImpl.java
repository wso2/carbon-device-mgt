/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;

import org.wso2.carbon.device.mgt.core.dto.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandOperationDAOImpl extends OperationDAOImpl {

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        int operationId;
        CommandOperation commandOp = (CommandOperation) operation;
        PreparedStatement stmt = null;
        try {
            operationId = super.addOperation(operation);
            Connection conn = OperationManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_COMMAND_OPERATION(OPERATION_ID, ENABLED) VALUES(?, ?)");
            stmt.setInt(1, operationId);
            stmt.setBoolean(2, commandOp.isEnabled());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding command operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return operationId;
    }

    @Override
    public void updateOperation(Operation operation) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(
                    "UPDATE DM_COMMAND_OPERATION O SET O.ENABLED = ? WHERE O.OPERATION_ID = ?");
            stmt.setBoolean(1, operation.isEnabled());
            stmt.setInt(2, operation.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteOperation(int id) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            super.deleteOperation(id);
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("DELETE DM_COMMAND_OPERATION WHERE OPERATION_ID = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while deleting operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    public CommandOperation getOperation(int id) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        CommandOperation commandOperation = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT OPERATION_ID, ENABLED FROM DM_COMMAND_OPERATION WHERE OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                commandOperation = new CommandOperation();
                commandOperation.setEnabled(rs.getInt("ENABLED") != 0);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the command operation " +
                    "object available for the id '" + id, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return commandOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            int enrolmentId, Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        CommandOperation commandOperation;
        List<CommandOperation> commandOperations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, co1.ENABLED, co1.STATUS, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "o.OPERATION_CODE FROM (SELECT co.OPERATION_ID, co.ENABLED, dm.STATUS " +
                    "FROM DM_COMMAND_OPERATION co INNER JOIN (SELECT ENROLMENT_ID, OPERATION_ID, STATUS " +
                    "FROM DM_ENROLMENT_OPERATION_MAPPING WHERE ENROLMENT_ID = ? AND STATUS = ?) dm " +
                    "ON dm.OPERATION_ID = co.OPERATION_ID) co1 INNER JOIN DM_OPERATION o ON co1.OPERATION_ID = o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();
            while (rs.next()) {
                commandOperation = new CommandOperation();
                commandOperation.setId(rs.getInt("ID"));
                commandOperation.setEnabled(rs.getInt("ENABLED") != 0);
                commandOperation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                commandOperation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                commandOperation.setCreatedTimeStamp(rs.getString("CREATED_TIMESTAMP"));
                commandOperation.setReceivedTimeStamp(rs.getString("RECEIVED_TIMESTAMP"));
                commandOperation.setCode(rs.getString("OPERATION_CODE"));
                commandOperations.add(commandOperation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation available " +
                    "for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return commandOperations;
    }

}
