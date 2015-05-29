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

import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OperationMappingDAOImpl implements OperationMappingDAO {

    @Override
    public void addOperationMapping(int operationId, Integer deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_DEVICE_OPERATION_MAPPING(DEVICE_ID, OPERATION_ID,STATUS) VALUES(?, ?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, operationId);
            stmt.setString(3, Operation.Status.PENDING.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeOperationMapping(int operationId,
                                       Integer deviceIds) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ? AND OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, 0);
            stmt.setInt(2, operationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

}
