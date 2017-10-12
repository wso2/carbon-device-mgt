/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationResponse;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class holds the generic implementation of OperationDAO which can be used to support ANSI db syntax.
 */
public class GenericOperationDAOImpl implements OperationDAO {

    private static final Log log = LogFactory.getLog(GenericOperationDAOImpl.class);

    public int addOperation(Operation operation) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION(TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE)  " +
                    "VALUES (?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql, new String[]{"id"});
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

    public boolean updateOperationStatus(int enrolmentId, int operationId, Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        boolean isUpdated = false;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection connection = OperationManagementDAOFactory.getConnection();
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
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while update device mapping operation status " +
                    "metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
        return isUpdated;
    }

    @Override
    public void updateEnrollmentOperationsStatus(int enrolmentId, String operationCode, Operation.Status existingStatus,
                                                 Operation.Status newStatus) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String query = "SELECT EOM.ID FROM DM_ENROLMENT_OP_MAPPING EOM INNER JOIN DM_OPERATION DM "
                    + "ON DM.ID = EOM.OPERATION_ID  WHERE EOM.ENROLMENT_ID = ? AND DM.OPERATION_CODE = ? "
                    + "AND EOM.STATUS = ?";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, operationCode);
            stmt.setString(3, existingStatus.toString());
            // This will return only one result always.
            rs = stmt.executeQuery();
            int id = 0;
            while (rs.next()) {
                id = rs.getInt("ID");
            }
            if (id != 0) {
                stmt = connection.prepareStatement(
                        "UPDATE DM_ENROLMENT_OP_MAPPING SET STATUS = ?, " + "UPDATED_TIMESTAMP = ?  WHERE ID = ?");
                stmt.setString(1, newStatus.toString());
                stmt.setLong(2, System.currentTimeMillis() / 1000);
                stmt.setInt(3, id);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while update device mapping operation status " + "metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public boolean updateTaskOperation(int enrolmentId, String operationCode)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            String query = "SELECT EOM.ID FROM DM_ENROLMENT_OP_MAPPING EOM INNER JOIN DM_OPERATION DM "
                    + "ON DM.ID = EOM.OPERATION_ID WHERE EOM.ENROLMENT_ID = ? AND DM.OPERATION_CODE = ? AND "
                    + "EOM.STATUS = ?";
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, operationCode);
            stmt.setString(3, Operation.Status.PENDING.toString());
            // This will return only one result always.
            rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while update device mapping operation status " + "metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return result;
    }

    @Override
    public void addOperationResponse(int enrolmentId, int operationId, Object operationResponse)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ByteArrayOutputStream bao = null;
        ObjectOutputStream oos = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();

            stmt = connection.prepareStatement("SELECT ID FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? " +
                    "AND OPERATION_ID = ?");
            stmt.setInt(1, enrolmentId);
            stmt.setInt(2, operationId);

            rs = stmt.executeQuery();
            int enPrimaryId = 0;
            if(rs.next()){
                enPrimaryId = rs.getInt("ID");
            }
            stmt = connection.prepareStatement("INSERT INTO DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID, " +
                    "EN_OP_MAP_ID, OPERATION_RESPONSE, RECEIVED_TIMESTAMP) VALUES(?, ?, ?, ?, ?)");
            bao = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bao);
            oos.writeObject(operationResponse);

            stmt.setInt(1, operationId);
            stmt.setInt(2, enrolmentId);
            stmt.setInt(3, enPrimaryId);
            stmt.setBytes(4, bao.toByteArray());
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while inserting operation response", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("Error occurred while serializing policy operation object", e);
        } finally {
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
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Activity getActivity(int operationId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity = null;
        List<ActivityStatus> activityStatusList = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT eom.ENROLMENT_ID, eom.OPERATION_ID, eom.ID AS EOM_MAPPING_ID, dor.ID AS OP_RES_ID,\n" +
                    "de.DEVICE_ID, d.DEVICE_IDENTIFICATION, \n" +
                    "d.DEVICE_TYPE_ID, dt.NAME AS DEVICE_TYPE_NAME, eom.STATUS, eom.CREATED_TIMESTAMP, \n" +
                    "eom.UPDATED_TIMESTAMP, op.OPERATION_CODE, op.TYPE AS OPERATION_TYPE, dor.OPERATION_RESPONSE, \n" +
                    "dor.RECEIVED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING eom \n" +
                    "INNER JOIN DM_OPERATION op ON op.ID=eom.OPERATION_ID\n" +
                    "INNER JOIN DM_ENROLMENT de ON de.ID=eom.ENROLMENT_ID\n" +
                    "INNER JOIN DM_DEVICE d ON d.ID=de.DEVICE_ID \n" +
                    "INNER JOIN DM_DEVICE_TYPE dt ON dt.ID=d.DEVICE_TYPE_ID\n" +
                    "LEFT JOIN DM_DEVICE_OPERATION_RESPONSE dor ON dor.ENROLMENT_ID=de.id \n" +
                    "AND dor.OPERATION_ID = eom.OPERATION_ID\n" +
                    "WHERE eom.OPERATION_ID = ? AND de.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();

            int enrolmentId = 0;
            ActivityStatus activityStatus = null;

            while (rs.next()) {
                if (enrolmentId == 0) {
                    activity = new Activity();
                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                }
                if (enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                        operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                    }
                    activityStatus.setResponses(operationResponses);

                    activityStatusList.add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                    activity.setActivityStatus(activityStatusList);
                } else {
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database.", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Error occurred while converting the operation response to string.", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO exception occurred while converting the operations responses.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activity;
    }

    public Activity getActivityByDevice(int operationId, int deviceId) throws OperationManagementDAOException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Activity activity = null;
        List<ActivityStatus> activityStatusList = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT eom.ENROLMENT_ID, eom.OPERATION_ID, eom.ID AS EOM_MAPPING_ID, dor.ID AS OP_RES_ID,\n" +
                    "de.DEVICE_ID, d.DEVICE_IDENTIFICATION, \n" +
                    "d.DEVICE_TYPE_ID, dt.NAME AS DEVICE_TYPE_NAME, eom.STATUS, eom.CREATED_TIMESTAMP, \n" +
                    "eom.UPDATED_TIMESTAMP, op.OPERATION_CODE, op.TYPE AS OPERATION_TYPE, dor.OPERATION_RESPONSE, \n" +
                    "dor.RECEIVED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING AS eom \n" +
                    "INNER JOIN DM_OPERATION AS op ON op.ID=eom.OPERATION_ID\n" +
                    "INNER JOIN DM_ENROLMENT AS de ON de.ID=eom.ENROLMENT_ID\n" +
                    "INNER JOIN DM_DEVICE AS d ON d.ID=de.DEVICE_ID \n" +
                    "INNER JOIN DM_DEVICE_TYPE AS dt ON dt.ID=d.DEVICE_TYPE_ID\n" +
                    "LEFT JOIN DM_DEVICE_OPERATION_RESPONSE AS dor ON dor.ENROLMENT_ID=de.id \n" +
                    "AND dor.OPERATION_ID = eom.OPERATION_ID\n" +
                    "WHERE eom.OPERATION_ID = ? AND de.device_id = ? AND de.TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, operationId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();

            int enrolmentId = 0;
            ActivityStatus activityStatus = null;

            while (rs.next()) {
                if (enrolmentId == 0) {
                    activity = new Activity();
                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));
                }
                if (enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                        operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                    }
                    activityStatus.setResponses(operationResponses);

                    activityStatusList.add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                    activity.setActivityStatus(activityStatusList);
                } else {
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database.", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Error occurred while converting the operation response to string.", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO exception occurred while converting the operations responses.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activity;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit,
                                                    int offset) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String sql = "SELECT " +
                    "    opr.ENROLMENT_ID, " +
                    "    opr.CREATED_TIMESTAMP, " +
                    "    opr.UPDATED_TIMESTAMP, " +
                    "    opr.OPERATION_ID, " +
                    "    opr.OPERATION_CODE, " +
                    "    opr.OPERATION_TYPE, " +
                    "    opr.STATUS, " +
                    "    opr.DEVICE_ID, " +
                    "    opr.DEVICE_IDENTIFICATION, " +
                    "    opr.DEVICE_TYPE, " +
                    "    ops.RECEIVED_TIMESTAMP, " +
                    "    ops.ID OP_RES_ID, " +
                    "    ops.OPERATION_RESPONSE " +
                    " FROM " +
                    "    (SELECT " +
                    "            opm.ID MAPPING_ID, " +
                    "            opm.ENROLMENT_ID, " +
                    "            opm.CREATED_TIMESTAMP, " +
                    "            opm.UPDATED_TIMESTAMP, " +
                    "            opm.OPERATION_ID, " +
                    "            op.OPERATION_CODE, " +
                    "            op.TYPE  OPERATION_TYPE, " +
                    "            opm.STATUS, " +
                    "            en.DEVICE_ID, " +
                    "            de.DEVICE_IDENTIFICATION, " +
                    "            dt.NAME  DEVICE_TYPE, " +
                    "            de.TENANT_ID " +
                    "    FROM" +
                    "        DM_ENROLMENT_OP_MAPPING  opm " +
                    "        INNER JOIN DM_OPERATION  op ON opm.OPERATION_ID = op.ID " +
                    "        INNER JOIN DM_ENROLMENT  en ON opm.ENROLMENT_ID = en.ID " +
                    "        INNER JOIN DM_DEVICE  de ON en.DEVICE_ID = de.ID " +
                    "        INNER JOIN DM_DEVICE_TYPE  dt ON dt.ID = de.DEVICE_TYPE_ID " +
                    "    WHERE " +
                    "        opm.UPDATED_TIMESTAMP > ? " +
                    "            AND de.TENANT_ID = ? " +
                    "    ORDER BY opm.UPDATED_TIMESTAMP " +
                    "    LIMIT ? OFFSET ?) opr " +
                    " LEFT JOIN DM_DEVICE_OPERATION_RESPONSE ops ON opr.MAPPING_ID = ops.EN_OP_MAP_ID " +
                    " WHERE " +
                    "    opr.UPDATED_TIMESTAMP > ? " +
                    "    AND opr.TENANT_ID = ? ";

            stmt = conn.prepareStatement(sql);

            stmt.setLong(1, timestamp);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            stmt.setLong(5, timestamp);
            stmt.setInt(6, tenantId);

            rs = stmt.executeQuery();

            int operationId = 0;
            int enrolmentId = 0;
            int responseId = 0;
            Activity activity = null;
            ActivityStatus activityStatus = null;
            while (rs.next()) {

                if (operationId != rs.getInt("OPERATION_ID")) {
                    activity = new Activity();
                    activities.add(activity);
                    List<ActivityStatus> statusList = new ArrayList<>();
                    activityStatus = new ActivityStatus();

                    operationId = rs.getInt("OPERATION_ID");
                    enrolmentId = rs.getInt("ENROLMENT_ID");

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(
                                rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
                        operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        responseId = rs.getInt("OP_RES_ID");
                    }
                    activityStatus.setResponses(operationResponses);
                    statusList.add(activityStatus);
                    activity.setActivityStatus(statusList);
                    activity.setActivityId(OperationDAOUtil.getActivityId(rs.getInt("OPERATION_ID")));

                }

                if (operationId == rs.getInt("OPERATION_ID") && enrolmentId != rs.getInt("ENROLMENT_ID")) {
                    activityStatus = new ActivityStatus();

                    activity.setType(Activity.Type.valueOf(rs.getString("OPERATION_TYPE")));
                    activity.setCreatedTimeStamp(new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(new java.util.Date(
                                rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
                    }
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
                        operationResponses.add(OperationDAOUtil.getOperationResponse(rs));
                        responseId = rs.getInt("OP_RES_ID");
                    }
                    activityStatus.setResponses(operationResponses);
                    activity.getActivityStatus().add(activityStatus);

                    enrolmentId = rs.getInt("ENROLMENT_ID");
                }

                if (rs.getInt("OP_RES_ID") != 0 && responseId != rs.getInt("OP_RES_ID")) {
                    if (rs.getTimestamp("RECEIVED_TIMESTAMP") != (null)) {
                        activityStatus.getResponses().add(OperationDAOUtil.getOperationResponse(rs));
                        responseId = rs.getInt("OP_RES_ID");
                    }
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operation details from " +
                    "the database.", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException("Error occurred while converting the operation response to string.", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException("IO exception occurred while converting the operations responses.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    @Override
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(*) AS COUNT FROM DM_ENROLMENT_OP_MAPPING m \n"
                    + "INNER JOIN DM_ENROLMENT d ON m.ENROLMENT_ID = d.ID \n"
                    + "WHERE m.UPDATED_TIMESTAMP > ? AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, timestamp);
            stmt.setInt(2, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("COUNT");
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while getting the activity count from " + "the database.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return 0;
    }

    @Override
    public Operation getOperation(int id) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE FROM " +
                    "DM_OPERATION WHERE id = ?";
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
            throw new OperationManagementDAOException("SQL Error occurred while retrieving the operation object " +
                    "available for the id '" + id, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
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
            String sql = "SELECT o.ID, o.TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, om.STATUS, o.OPERATION_CODE, " +
                    "om.ID AS OM_MAPPING_ID, " +
                    "om.UPDATED_TIMESTAMP FROM (SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP," +
                    "OPERATION_CODE  FROM DM_OPERATION  WHERE id = ?) o INNER JOIN (SELECT * FROM " +
                    "DM_ENROLMENT_OP_MAPPING dm where dm.OPERATION_ID = ? AND dm.ENROLMENT_ID = ?) om " +
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
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with id '" + operationId, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operation;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            int enrolmentId, Operation.Status status) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, om.ID AS OM_MAPPING_ID," +
                    "om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(int enrolmentId, PaginationRequest request,
                                                                    Operation.Status status)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, " +
                    "om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY " +
                    "o.CREATED_TIMESTAMP DESC LIMIT ?,?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, status.toString());
            stmt.setInt(3, request.getStartIndex());
            stmt.setInt(4, request.getRowCount());
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
//                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
//                    operation.setReceivedTimeStamp("");
//                } else {
//                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
//                }
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                OperationDAOUtil.setActivityId(operation, rs.getInt("OM_MAPPING_ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE, om.STATUS, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<? extends Operation> getOperationsForDevice(int enrolmentId, PaginationRequest request)
            throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE, om.STATUS, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP DESC LIMIT ?,?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setInt(2, request.getStartIndex());
            stmt.setInt(3, request.getRowCount());
            rs = stmt.executeQuery();

            while (rs.next()) {
                operation = new Operation();
                operation.setId(rs.getInt("ID"));
                operation.setType(Operation.Type.valueOf(rs.getString("TYPE")));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(Operation.Status.valueOf(rs.getString("STATUS")));
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation " +
                    "available for the device'" + enrolmentId + "' with status '", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public int getOperationCountForDevice(int enrolmentId) throws OperationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int operationCount = 0;
        try {
            conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS OPERATION_COUNT FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                operationCount = rs.getInt("OPERATION_COUNT");
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while getting the operations count for enrolment : "
                    + enrolmentId, e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationCount;
    }

    @Override
    public Operation getNextOperation(int enrolmentId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection connection = OperationManagementDAOFactory.getConnection();
            stmt = connection.prepareStatement("SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, " +
                    "OPERATION_CODE, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID " +
                    "ORDER BY o.CREATED_TIMESTAMP ASC LIMIT 1");
            stmt.setInt(1, enrolmentId);
            stmt.setString(2, Operation.Status.PENDING.toString());
            rs = stmt.executeQuery();

            Operation operation = null;
            if (rs.next()) {
                operation = new Operation();
                operation.setType(OperationDAOUtil.getType(rs.getString("TYPE")));
                operation.setId(rs.getInt("ID"));
                operation.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP").toString());
//                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
//                    operation.setReceivedTimeStamp("");
//                } else {
//                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
//                }
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(Operation.Status.PENDING);
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
            }
            return operation;
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while adding operation metadata", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }


    public List<? extends Operation> getOperationsByDeviceStatusAndType(
            int enrolmentId, Operation.Status status, Operation.Type type) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Operation operation;
        List<Operation> operations = new ArrayList<Operation>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, OPERATION_CODE, om.ID AS OM_MAPPING_ID, " +
                    "om.UPDATED_TIMESTAMP FROM (SELECT o.ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE " +
                    "FROM DM_OPERATION o WHERE o.TYPE = ?) o " +
                    "INNER JOIN (SELECT * FROM DM_ENROLMENT_OP_MAPPING dm " +
                    "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP ASC";

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
//                if (rs.getTimestamp("RECEIVED_TIMESTAMP") == null) {
//                    operation.setReceivedTimeStamp("");
//                } else {
//                    operation.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP").toString());
//                }
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                OperationDAOUtil.setActivityId(operation, rs.getInt("ID"));
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operation available " +
                    "for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public Map<Integer, List<OperationMapping>> getOperationMappingsByStatus(Operation.Status opStatus, Operation.PushNotificationStatus pushNotificationStatus,
                                                                             int limit) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn;
        OperationMapping operationMapping;
        Map<Integer, List<OperationMapping>> operationMappingsTenantMap = new HashMap<>();
        try {
            conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT op.ENROLMENT_ID, op.OPERATION_ID, d.DEVICE_IDENTIFICATION, dt.NAME as DEVICE_TYPE, " +
                    "d.TENANT_ID FROM DM_DEVICE d, DM_ENROLMENT_OP_MAPPING op, DM_DEVICE_TYPE dt  WHERE op.STATUS = ?" +
                    " AND op.PUSH_NOTIFICATION_STATUS = ? AND d.DEVICE_TYPE_ID = dt.ID AND d.ID=op.ENROLMENT_ID ORDER" +
                    " BY op.OPERATION_ID LIMIT ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, opStatus.toString());
            stmt.setString(2, pushNotificationStatus.toString());
            stmt.setInt(3, limit);
            rs = stmt.executeQuery();
            while (rs.next()) {
                int tenantID = rs.getInt("TENANT_ID");
                List<OperationMapping> operationMappings = operationMappingsTenantMap.get(tenantID);
                if (operationMappings == null) {
                    operationMappings = new LinkedList<>();
                    operationMappingsTenantMap.put(tenantID, operationMappings);
                }
                operationMapping = new OperationMapping();
                operationMapping.setOperationId(rs.getInt("OPERATION_ID"));
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                operationMapping.setDeviceIdentifier(deviceIdentifier);
                operationMapping.setEnrollmentId(rs.getInt("ENROLMENT_ID"));
                operationMapping.setTenantId(tenantID);
                operationMappings.add(operationMapping);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error while getting operation mappings from database. ", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operationMappingsTenantMap;
    }
}
