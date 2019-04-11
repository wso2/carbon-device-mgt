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
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolicyOperationDAOImpl extends GenericOperationDAOImpl {

    private static final Log log = LogFactory.getLog(PolicyOperationDAOImpl.class);

    @Override
    public int addOperation(Operation operation) throws OperationManagementDAOException {
        int operationId;
        PreparedStatement stmt = null;
        try (ByteArrayOutputStream bao = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bao)) {
            operationId = super.addOperation(operation);
            operation.setCreatedTimeStamp(new Timestamp(new java.util.Date().getTime()).toString());
            operation.setId(operationId);
            operation.setEnabled(true);
            PolicyOperation policyOperation = (PolicyOperation) operation;
            Connection conn = OperationManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_POLICY_OPERATION(OPERATION_ID, OPERATION_DETAILS) " +
                                         "VALUES(?, ?)");

            oos.writeObject(operation);

            stmt.setInt(1, operationId);
            stmt.setBytes(2, bao.toByteArray());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding policy operation", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing policy operation object", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }

        return operationId;
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PolicyOperation policyOperation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT OPERATION_ID, ENABLED, OPERATION_DETAILS FROM DM_POLICY_OPERATION WHERE OPERATION_ID=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                policyOperation = (PolicyOperation) ois.readObject();
            }
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the policy operation " +
                    "object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the " +
                    "policy operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the policy operation " +
                    "object available for the id '" + operationId + "'", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return policyOperation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId,
            Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        PolicyOperation policyOperation;
        List<Operation> operations = new ArrayList<>();

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT po.OPERATION_ID, ENABLED, OPERATION_DETAILS FROM DM_POLICY_OPERATION po " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND STATUS = ?) dm ON dm.OPERATION_ID = po.OPERATION_ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] operationDetails = rs.getBytes("OPERATION_DETAILS");
                bais = new ByteArrayInputStream(operationDetails);
                ois = new ObjectInputStream(bais);
                policyOperation = (PolicyOperation) ois.readObject();
                policyOperation.setStatus(status);
                operations.add(policyOperation);
            }
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO Error occurred while de serialize the profile " +
                    "operation object", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Class not found error occurred while de serialize the " +
                    "profile operation object", e);
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
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
        }
        return operations;
    }

}
