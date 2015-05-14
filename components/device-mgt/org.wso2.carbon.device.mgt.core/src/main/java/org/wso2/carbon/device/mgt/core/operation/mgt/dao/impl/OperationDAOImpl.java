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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
            stmt = connection.prepareStatement(
                    "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, OPERATION_CODE)  " +
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
            OperationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateOperation(Operation operation) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("UPDATE DM_OPERATION O SET O.RECEIVED_TIMESTAMP=?,O.STATUS=? " +
                    "WHERE O.ID=?");

            stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            stmt.setString(2, operation.getStatus().toString());
            stmt.setInt(3, operation.getId());
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

        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE," +
                    " po.OPERATION_DETAILS, co.ENABLED from" +
                    " (SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, OPERATION_CODE FROM " +
                    " DM_OPERATION   WHERE id=?) o" +
                    " LEFT OUTER JOIN DM_PROFILE_OPERATION po on o.ID=po.OPERATION_ID" +
                    " LEFT OUTER JOIN DM_COMMAND_OPERATION co on co.OPERATION_ID=o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    bais = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(bais);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setId(rs.getInt("ID"));
                    operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                    }
                    operation.setEnabled(rs.getBoolean("ENABLED"));
                    operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                    operation.setCode(rs.getString("OPERATION_CODE"));
                }
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
    public Operation getOperationByDeviceAndId(int deviceId, int operationId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE," +
                    " po.OPERATION_DETAILS,co.ENABLED from " +
                    "(SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, " +
                    "OPERATION_CODE  FROM DM_OPERATION  WHERE id=?)  o INNER JOIN (Select * from " +
                    "DM_DEVICE_OPERATION_MAPPING dm where dm.OPERATION_ID=? AND  dm.DEVICE_ID=?)  om " +
                    "ON o.ID = om.OPERATION_ID " +
                    "LEFT OUTER JOIN DM_PROFILE_OPERATION po on  o.ID = po.OPERATION_ID " +
                    "LEFT OUTER JOIN DM_COMMAND_OPERATION co on co.OPERATION_ID=o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, operationId);
            stmt.setInt(3, deviceId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    bais = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(bais);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setId(rs.getInt("ID"));
                    operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("CREATED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    }
                    operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                    operation.setCode(rs.getString("OPERATION_CODE"));
                }
            }
        } catch (IOException ex) {
            String errorMsg = "IO error occurred while de serializing the profile operation available for the " +
                    "device:" + deviceId + "' with id '" + operationId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (ClassNotFoundException ex) {
            String errorMsg =
                    "class not found error occurred while de serializing the profile operation available for " +
                            "the device:" + deviceId + "' with id '" + operationId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + deviceId +
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
    public List<? extends Operation> getOperationsByDeviceAndStatus(int deviceId,
            Operation.Status status) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        ByteArrayInputStream bais;
        ObjectInputStream ois;
        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE, " +
                    "po.OPERATION_DETAILS,co.ENABLED from " +
                    "(SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, " +
                    "OPERATION_CODE  FROM DM_OPERATION  WHERE STATUS=?) o " +
                    "INNER JOIN (Select * from DM_DEVICE_OPERATION_MAPPING dm " +
                    "where dm.DEVICE_ID=?) om ON o.ID = om.OPERATION_ID  LEFT OUTER JOIN DM_PROFILE_OPERATION po ON " +
                    "o.ID =po.OPERATION_ID LEFT OUTER JOIN DM_COMMAND_OPERATION co ON co.OPERATION_ID=o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setInt(2, deviceId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    bais = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(bais);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setId(rs.getInt("ID"));
                    operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("CREATED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    }
                    operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                    operation.setCode(rs.getString("OPERATION_CODE"));
                    if (rs.getObject("ENABLED") != null) {
                        operation.setEnabled(rs.getBoolean("ENABLED"));
                    }
                }
                operationList.add(operation);
            }
        } catch (IOException ex) {
            String errorMsg = "IO error occurred while de serializing the profile operation available for the " +
                    "device:" + deviceId + "' and status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (ClassNotFoundException ex) {
            String errorMsg =
                    "class not found error occurred while de serializing the profile operation available for " +
                            "the device:" + deviceId + "' with status '" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
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

    @Override
    public List<? extends Operation> getOperationsForDevice(int deviceId)
            throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        ByteArrayInputStream bais;
        ObjectInputStream ois;
        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE, " +
                    "po.OPERATION_DETAILS,co.ENABLED from " +
                    "(SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, " +
                    "OPERATION_CODE  FROM DM_OPERATION) o " +
                    "INNER JOIN (Select * from DM_DEVICE_OPERATION_MAPPING dm " +
                    "where dm.DEVICE_ID=?) om ON o.ID = om.OPERATION_ID  LEFT OUTER JOIN DM_PROFILE_OPERATION po ON " +
                    "o.ID =po.OPERATION_ID LEFT OUTER JOIN DM_COMMAND_OPERATION co ON co.OPERATION_ID=o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    bais = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(bais);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setId(rs.getInt("ID"));
                    operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("CREATED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    }
                    operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                    operation.setCode(rs.getString("OPERATION_CODE"));
                }
                operationList.add(operation);
            }
        } catch (IOException ex) {
            String errorMsg = "IO error occurred while de serializing the profile operation available for the " +
                    "device:" + deviceId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (ClassNotFoundException ex) {
            String errorMsg =
                    "class not found error occurred while de serializing the profile operation available for " +
                            "the device:" + deviceId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the device'" + deviceId +
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
    public List<? extends Operation> getOperationsForStatus(Operation.Status status)
            throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;

        ByteArrayInputStream byteArrayInputStream;
        ObjectInputStream ois;
        List<Operation> operationList = new ArrayList<Operation>();

        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE,"+
                    "po.OPERATION_DETAILS,co.ENABLED from "+
                    "(SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS,"+
                    "OPERATION_CODE  FROM DM_OPERATION  WHERE STATUS=?) o "+
                    "LEFT OUTER JOIN DM_PROFILE_OPERATION po ON "+
                    "o.ID =po.OPERATION_ID LEFT OUTER JOIN DM_COMMAND_OPERATION co ON co.OPERATION_ID=o.ID";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    byteArrayInputStream = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(byteArrayInputStream);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setId(rs.getInt("ID"));
                    operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("CREATED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    }
                    operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                    operation.setCode(rs.getString("OPERATION_CODE"));
                }
                operationList.add(operation);
            }
        } catch (IOException ex) {
            String errorMsg = "IO error occurred while de serializing the profile operation available for the " +
                    "status:" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (ClassNotFoundException ex) {
            String errorMsg =
                    "class not found error occurred while de serializing the profile operation available for " +
                            "the status:" + status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the operation available for the status:'" +
                    status.toString();
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
        return operationList;
    }

    @Override
    public Operation getNextOperation(int deviceId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;

        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement(
                    "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.STATUS, o.OPERATION_CODE, " +
                            "po.OPERATION_DETAILS,co.ENABLED from " +
                            "(SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, STATUS, " +
                            "OPERATION_CODE  FROM DM_OPERATION  WHERE STATUS=?) o " +
                            "INNER JOIN (Select * from DM_DEVICE_OPERATION_MAPPING dm " +
                            "where dm.DEVICE_ID=?) om ON o.ID = om.OPERATION_ID  LEFT OUTER JOIN DM_PROFILE_OPERATION " +
                            "po ON " +
                            "o.ID =po.OPERATION_ID LEFT OUTER JOIN DM_COMMAND_OPERATION co ON co.OPERATION_ID=o.ID " +
                            "ORDER BY o.CREATED_TIMESTAMP ASC LIMIT 1");

            stmt.setString(1, Operation.Status.PENDING.toString());
            stmt.setInt(2, deviceId);

            rs = stmt.executeQuery();
            Operation operation = null;

            if (rs.next()) {
                if (rs.getBytes("OPERATION_DETAILS") != null) {
                    byte[] operationDetails;
                    operationDetails = rs.getBytes("OPERATION_DETAILS");
                    bais = new ByteArrayInputStream(operationDetails);
                    ois = new ObjectInputStream(bais);
                    operation = (ProfileOperation) ois.readObject();
                } else {
                    operation = new Operation();
                    operation.setType(this.getType(rs.getString("TYPE")));
                    operation.setStatus(this.getStatus(rs.getString("STATUS")));
                    operation.setId(rs.getInt("ID"));
                    operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
                        operation.setReceivedTimeStamp("");
                    } else {
                        operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
                    }
                    operation.setCode(rs.getString("OPERATION_CODE"));
                    if (rs.getObject("ENABLED") != null) {
                        operation.setEnabled(rs.getBoolean("ENABLED"));
                    }
                }
            }
            return operation;
        } catch (IOException ex) {
            String errorMsg = "IO error occurred while de serializing the next profile operation available for the " +
                    "device:" + deviceId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);
        } catch (ClassNotFoundException ex) {
            String errorMsg = "class not found error occurred while de serializing the profile operation available " +
                    "for the device:" + deviceId;
            log.error(errorMsg);
            throw new OperationManagementDAOException(errorMsg, ex);

        }catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
            OperationManagementDAOFactory.closeConnection();
        }
    }

    private Operation.Status getStatus(String status) {
        return Operation.Status.valueOf(status);
    }

    private Operation.Type getType(String type) {
        return Operation.Type.valueOf(type);
    }

}
