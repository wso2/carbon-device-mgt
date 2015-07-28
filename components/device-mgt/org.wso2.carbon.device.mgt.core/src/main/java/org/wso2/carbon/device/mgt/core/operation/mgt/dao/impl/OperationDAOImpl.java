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
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperationDAOImpl implements OperationDAO {

    private static final Log log = LogFactory.getLog(OperationDAOImpl.class);

    public int addOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE)  " +
                    "VALUES (?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, operation.getType().toString());
            stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(3, null);
            stmt.setString(4, operation.getCode());
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
    public void updateOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_OPERATION O SET O.RECEIVED_TIMESTAMP=? " +
                    "WHERE O.ID=?");

            stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            stmt.setInt(2, operation.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    public void updateOperationStatus(int enrolmentId, int operationId, Operation.Status status)
            throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_ENROLMENT_OPERATION_MAPPING O SET O.STATUS=? " +
                    "WHERE O.ENROLMENT_ID=? and O.OPERATION_ID=?");

            stmt.setString(1, status.toString());
            stmt.setInt(2, enrolmentId);
            stmt.setInt(3, operationId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update device mapping operation status " +
                    "metadata",
                    e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }

    }

    @Override
    public void addOperationResponse(int enrolmentId, int operationId, Object operationResponse)
            throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;

        try {
            Connection connection = OperationManagementDAOFactory.getConnection();

            stmt = connection.prepareStatement("INSERT INTO DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID,DEVICE_ID," +
                    "OPERATION_RESPONSE) VALUES(?, ?, ?)");

            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operationResponse);

            stmt.setInt(1, operationId);
            stmt.setInt(2, enrolmentId);
            stmt.setBytes(3, bao.toByteArray());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while inserting operation response", e);
        }catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing policy operation object", e);
        }finally {
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

        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("DELETE DM_OPERATION WHERE ID=?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while deleting operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public Operation getOperation(int id) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE FROM " +
                    "DM_OPERATION WHERE id=?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
            }

        } catch (SQLException e) {
            String errorMsg = "SQL Error occurred while retrieving the operation object " + "available for the id '"
                    + id;
            log.error(errorMsg, e);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operation;
    }

    @Override
    public Operation getOperationByDeviceAndId(int enrolmentId, int operationId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE " +
                    " From (SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS," +
                    "OPERATION_CODE  FROM DM_OPERATION  WHERE id=?)  o INNER JOIN (Select * from " +
                    "DM_ENROLMENT_OPERATION_MAPPING dm where dm.OPERATION_ID=? AND  dm.ENROLMENT_ID=?)  om " +
                    "ON o.ID = om.OPERATION_ID ";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, operationId);
            stmt.setInt(3, enrolmentId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + enrolmentId +
                    "' with id '" + operationId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId,
                                                                    Operation.Status status) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE " +
                    "FROM DM_OPERATION o " +
                    "INNER JOIN (Select * from DM_ENROLMENT_OPERATION_MAPPING dm " +
                    "where dm.ENROLMENT_ID=? and dm.STATUS=?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP ASC";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                operationList.add(operation);
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + enrolmentId +
                    "' with status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId)
            throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE,dm.STATUS  FROM DM_OPERATION o " +
                    "INNER JOIN (Select * from DM_ENROLMENT_OPERATION_MAPPING dm " +
                    "where dm.ENROLMENT_ID=?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP ASC";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                operationList.add(operation);
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + enrolmentId +
                    "' with status '";
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }

    @Override
    public Operation getNextOperation(int enrolmentId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE  FROM DM_OPERATION o " +
                    "INNER JOIN (Select * from DM_ENROLMENT_OPERATION_MAPPING dm " +
                    "where dm.ENROLMENT_ID=? AND dm.STATUS=?) om ON o.ID = om.OPERATION_ID " +
                    "ORDER BY o.CREATED_TIMESTAMP ASC LIMIT 1");

            stmt.setInt(1, enrolmentId);
            stmt.setString(2, Operation.Status.PENDING.toString());

            rs = stmt.executeQuery();
            Operation operation = null;

            if (rs.next()) {
                operation = new Operation();
                operation.setType(this.getType(rs.getString("TYPE")));
                operation.setId(rs.getInt("ID"));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(Operation.Status.PENDING);
            }
            return operation;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
    }


    public List<? extends Operation> getOperationsByDeviceStatusAndType(int enrolmentId,
                                                                        Operation.Status status, Operation.Type type) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE FROM " +
                    "(SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE " +
                    "FROM DM_OPERATION o WHERE o.TYPE=?) o " +
                    "INNER JOIN (Select * from DM_ENROLMENT_OPERATION_MAPPING dm " +
                    "where dm.ENROLMENT_ID=? and dm.STATUS=?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP ASC";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, type.toString());
            stmt.setInt(2, enrolmentId);
            stmt.setString(3, status.toString());

            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operationList.add(operation);
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + enrolmentId +
                    "' with status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }

    private Operation.Status getStatus(String status) {
        return Operation.Status.valueOf(status);
    }

    private Operation.Type getType(String type) {
        return Operation.Type.valueOf(type);
    }

}
