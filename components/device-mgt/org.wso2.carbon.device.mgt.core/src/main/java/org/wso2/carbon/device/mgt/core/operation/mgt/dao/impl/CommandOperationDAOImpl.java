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

    private static final Log log = LogFactory.getLog(CommandOperationDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {

        int operationId = super.addOperation(operation);
        CommandOperation commandOp = (CommandOperation) operation;
        PreparedStatement stmt = null;

        try {
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
                    "UPDATE DM_COMMAND_OPERATION O SET O.ENABLED=? WHERE O.OPERATION_ID=?");

            stmt.setBoolean(1, operation.isEnabled());
            stmt.setInt(2, operation.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteOperation(int id) throws OperationManagementDAOException {

        super.deleteOperation(id);
        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("DELETE DM_COMMAND_OPERATION WHERE OPERATION_ID=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while deleting operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
            OperationManagementDAOFactory.closeConnection();
        }
    }

    public CommandOperation getOperation(int id) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        CommandOperation commandOperation = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT OPERATION_ID, ENABLED FROM DM_COMMAND_OPERATION WHERE OPERATION_ID=?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                commandOperation = new CommandOperation();
                commandOperation.setEnabled(rs.getInt("ENABLED") == 0 ? false : true);
            }

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
        return commandOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int deviceId,
            Operation.Status status) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        List<Operation> operationList = new ArrayList<Operation>();
        List<CommandOperation> commandOperationList = new ArrayList<CommandOperation>();

        CommandOperation commandOperation = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "Select co.OPERATION_ID,ENABLED from DM_COMMAND_OPERATION co " +
                    "INNER JOIN  " +
                    "(Select * From DM_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID=? " +
                    "AND STATUS=? ) dm ON dm.OPERATION_ID = co.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();
            while (rs.next()) {
                commandOperation = new CommandOperation();
                commandOperation.setEnabled(rs.getInt("ENABLED") == 0 ? false : true);
                commandOperation.setId(rs.getInt("OPERATION_ID"));
                commandOperationList.add(commandOperation);
            }

            for(CommandOperation cmOperation:commandOperationList){
               operation =  super.getOperation(cmOperation.getId());
               operation.setEnabled(cmOperation.isEnabled());
               operation.setStatus(status);
               operationList.add(operation);
            }

        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + deviceId +
                    "' with status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }
}
