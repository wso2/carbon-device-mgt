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
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
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
        int operationId = super.addOperation(operation);
        CommandOperation commandOp = (CommandOperation) operation;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_COMMAND_OPERATION(OPERATION_ID, ENABLED) VALUES(?, ?)");
            stmt.setInt(1, operationId);
            stmt.setBoolean(2, commandOp.isEnabled());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding command operation", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationId;
    }

    public List<? extends Operation> getOperations(DeviceIdentifier deviceId,
                                         Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT po.OPERATION_ID, po.TYPE, po.CREATED_TIMESTAMP, po.RECEIVED_TIMESTAMP, po.STATUS, " +
                    "co.ENABLED FROM DM_COMMAND_OPERATION co INNER JOIN (SELECT o.ID AS OPERATION_ID, o.TYPE, " +
                    "o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS FROM DM_OPERATION o INNER JOIN (" +
                    "SELECT dom.OPERATION_ID AS OP_ID FROM (SELECT d.ID FROM DM_DEVICE d INNER JOIN " +
                    "DM_DEVICE_TYPE dm ON d.DEVICE_TYPE_ID = dm.ID AND dm.NAME = ? AND " +
                    "d.DEVICE_IDENTIFICATION = ?) d1 INNER JOIN DM_DEVICE_OPERATION_MAPPING dom ON d1.ID = " +
                    "dom.DEVICE_ID) ois ON o.ID = ois.OP_ID AND o.STATUS = ? ORDER BY " +
                    "o.CREATED_TIMESTAMP ASC) po ON co.OPERATION_ID = po.OPERATION_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(1, deviceId.getId());
            stmt.setString(1, status.toString());
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while retrieving the list of " +
                    "operations with the status '" + status + "' available for the '" + deviceId.getType() +
                    "' device '" + deviceId.getId() + "'");
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }

        return null;
    }

    public List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT po.OPERATION_ID, po.TYPE, po.CREATED_TIMESTAMP, po.RECEIVED_TIMESTAMP, po.STATUS, " +
                    "co.ENABLED FROM DM_COMMAND_OPERATION co INNER JOIN (SELECT o.ID AS OPERATION_ID, o.TYPE, " +
                    "o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS FROM DM_OPERATION o INNER JOIN (" +
                    "SELECT dom.OPERATION_ID AS OP_ID FROM (SELECT d.ID FROM DM_DEVICE d INNER JOIN " +
                    "DM_DEVICE_TYPE dm ON d.DEVICE_TYPE_ID = dm.ID AND dm.NAME = ? AND " +
                    "d.DEVICE_IDENTIFICATION = ?) d1 INNER JOIN DM_DEVICE_OPERATION_MAPPING dom ON d1.ID = " +
                    "dom.DEVICE_ID) ois ON o.ID = ois.OP_ID ORDER BY " +
                    "o.CREATED_TIMESTAMP ASC) po ON co.OPERATION_ID = po.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(1, deviceId.getId());
            rs = stmt.executeQuery();

            List<CommandOperation> operations = new ArrayList<CommandOperation>();
            while (rs.next()) {
                CommandOperation operation = new CommandOperation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                operation.setEnabled(Boolean.parseBoolean(rs.getString("ENABLED")));
                operations.add(operation);
            }
            return operations;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while retrieving the list of " +
                    "operations available for the '" + deviceId.getType() + "' device '" + deviceId.getId() + "'");
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

}
