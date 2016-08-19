/*
 * Copyright (c) 2016a, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.operation;

import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class holds the implementation of OperationDAO which can be used to support MySQl db syntax.
 */
public class MySQLOperationDAOImpl extends GenericOperationDAOImpl {

    @Override
    public boolean updateOperationStatus(int enrolmentId, int operationId, Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        boolean isUpdated = false;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("SELECT STATUS, UPDATED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING " +
                                               "WHERE ENROLMENT_ID=? and OPERATION_ID=? FOR UPDATE");
            stmt.setString(1, status.toString());
            stmt.setLong(2, time);
            if (stmt.execute()) {
                OperationManagementDAOUtil.cleanupResources(stmt);
                stmt = connection.prepareStatement("UPDATE DM_ENROLMENT_OP_MAPPING SET STATUS=?, UPDATED_TIMESTAMP=? " +
                                                   "WHERE ENROLMENT_ID=? and OPERATION_ID=?");
                stmt.setString(1, status.toString());
                stmt.setLong(2, time);
                stmt.setInt(3, enrolmentId);
                stmt.setInt(4, operationId);
                int numOfRecordsUpdated = stmt.executeUpdate();
                if (numOfRecordsUpdated != 0) {
                    isUpdated = true;
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update device mapping operation status " +
                                                      "metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return isUpdated;
    }
}