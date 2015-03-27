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

import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CommandOperationDAOImpl extends AbstractOperationDAO {

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        int operationId = super.addOperation(operation);
        CommandOperation commandOp = (CommandOperation) operation;
        Connection conn = OperationManagementDAOFactory.getConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
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

    @Override
    public int updateOperation(Operation operation) throws OperationManagementDAOException {
        return 0;
    }

    @Override
    public int deleteOperation(int id) throws OperationManagementDAOException {
        return 0;
    }

    @Override
    public Operation getOperation(int id) throws OperationManagementDAOException {
        return null;
    }

    @Override
    public List<Operation> getOperations() throws OperationManagementDAOException {
        return null;
    }

    @Override
    public List<Operation> getOperations(String status) throws OperationManagementDAOException {
        return null;
    }

}
