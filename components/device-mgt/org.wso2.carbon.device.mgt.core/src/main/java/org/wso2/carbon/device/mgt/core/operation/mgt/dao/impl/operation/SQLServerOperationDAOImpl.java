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

package org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.operation;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationResponse;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.impl.GenericOperationDAOImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.util.OperationDAOUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class holds the implementation of OperationDAO which can be used to support SQLServer db syntax.
 */
public class SQLServerOperationDAOImpl extends GenericOperationDAOImpl {

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
                         "o.OPERATION_CODE, om.STATUS, om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                         "INNER JOIN (SELECT dm.OPERATION_ID, dm.ID, dm.STATUS, dm.UPDATED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING dm " +
                         "WHERE dm.ENROLMENT_ID = ?) om ON o.ID = om.OPERATION_ID ORDER BY o.CREATED_TIMESTAMP DESC " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operations " +
                                                      "available for the device '" + enrolmentId + "'", e);
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
        List<Operation> operations = new ArrayList<Operation>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT o.ID, TYPE, o.CREATED_TIMESTAMP, o.RECEIVED_TIMESTAMP, o.OPERATION_CODE, " +
                         "om.ID AS OM_MAPPING_ID, om.UPDATED_TIMESTAMP FROM DM_OPERATION o " +
                         "INNER JOIN (SELECT dm.OPERATION_ID, dm.ID, dm.STATUS, dm.UPDATED_TIMESTAMP FROM DM_ENROLMENT_OP_MAPPING dm " +
                         "WHERE dm.ENROLMENT_ID = ? AND dm.STATUS = ?) om ON o.ID = om.OPERATION_ID ORDER BY " +
                         "o.CREATED_TIMESTAMP DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
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
                if (rs.getLong("UPDATED_TIMESTAMP") == 0) {
                    operation.setReceivedTimeStamp("");
                } else {
                    operation.setReceivedTimeStamp(
                            new java.sql.Timestamp((rs.getLong("UPDATED_TIMESTAMP") * 1000)).toString());
                }
                operation.setCode(rs.getString("OPERATION_CODE"));
                operation.setStatus(status);
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("SQL error occurred while retrieving the operations " +
                                                      "available for the device'" + enrolmentId + "' with status '" + status.toString(), e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            /*String sql = "SELECT opm.ENROLMENT_ID, opm.CREATED_TIMESTAMP, opm.UPDATED_TIMESTAMP, opm.OPERATION_ID,\n"
                    + "op.OPERATION_CODE, op.TYPE OPERATION_TYPE, opm.STATUS, en.DEVICE_ID,\n"
                    + "ops.RECEIVED_TIMESTAMP, ops.ID OP_RES_ID, ops.OPERATION_RESPONSE,\n"
                    + "de.DEVICE_IDENTIFICATION, dt.NAME DEVICE_TYPE\n" + "FROM DM_ENROLMENT_OP_MAPPING opm\n"
                    + "LEFT JOIN DM_OPERATION op ON opm.OPERATION_ID = op.ID \n"
                    + "LEFT JOIN DM_ENROLMENT en ON opm.ENROLMENT_ID = en.ID \n"
                    + "LEFT JOIN DM_DEVICE de ON en.DEVICE_ID = de.ID \n"
                    + "LEFT JOIN DM_DEVICE_TYPE  dt ON dt.ID = de.DEVICE_TYPE_ID \n"
                    + "LEFT JOIN DM_DEVICE_OPERATION_RESPONSE ops ON \n"
                    + "opm.ENROLMENT_ID = ops.ENROLMENT_ID AND opm.OPERATION_ID = ops.OPERATION_ID \n"
                    + "WHERE opm.UPDATED_TIMESTAMP > ? \n" + "AND de.TENANT_ID = ? \n";

            if (timestamp == 0) {
                sql += "ORDER BY opm.OPERATION_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            } else {
                sql += "ORDER BY opm.UPDATED_TIMESTAMP asc OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            }*/

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
                    "    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY) opr " +
                    " LEFT JOIN DM_DEVICE_OPERATION_RESPONSE ops ON opr.MAPPING_ID = ops.EN_OP_MAP_ID " +
                    " WHERE" +
                    "    opr.UPDATED_TIMESTAMP > ? " +
                    "    AND opr.TENANT_ID = ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, timestamp);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, offset);
            stmt.setInt(4, limit);
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
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());

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
                    activity.setCreatedTimeStamp(
                            new java.util.Date(rs.getLong(("CREATED_TIMESTAMP")) * 1000).toString());
                    activity.setCode(rs.getString("OPERATION_CODE"));

                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
                    deviceIdentifier.setType(rs.getString("DEVICE_TYPE"));
                    activityStatus.setDeviceIdentifier(deviceIdentifier);

                    activityStatus.setStatus(ActivityStatus.Status.valueOf(rs.getString("STATUS")));

                    List<OperationResponse> operationResponses = new ArrayList<>();
                    if (rs.getInt("UPDATED_TIMESTAMP") != 0) {
                        activityStatus.setUpdatedTimestamp(
                                new java.util.Date(rs.getLong(("UPDATED_TIMESTAMP")) * 1000).toString());
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
            throw new OperationManagementDAOException(
                    "Error occurred while getting the operation details from " + "the database.", e);
        } catch (ClassNotFoundException e) {
            throw new OperationManagementDAOException(
                    "Error occurred while converting the operation response to string.", e);
        } catch (IOException e) {
            throw new OperationManagementDAOException(
                    "IO exception occurred while converting the operations responses.", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return activities;
    }

    @Override
    public Map<Integer, List<OperationMapping>> getOperationMappingsByStatus(Operation.Status opStatus, Operation.PushNotificationStatus pushNotificationStatus,
                                                                             int limit) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        OperationMapping operationMapping;
        Map<Integer, List<OperationMapping>> operationMappingsTenantMap = new HashMap<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT op.ENROLMENT_ID, op.OPERATION_ID, d.DEVICE_IDENTIFICATION, dt.NAME as DEVICE_TYPE, d" +
                    ".TENANT_ID FROM DM_DEVICE d, DM_ENROLMENT_OP_MAPPING op, DM_DEVICE_TYPE dt  WHERE op.STATUS = ? " +
                    "AND op.PUSH_NOTIFICATION_STATUS = ? AND d.DEVICE_TYPE_ID = dt.ID " +
                    "AND d.ID=op.ENROLMENT_ID ORDER BY op.OPERATION_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, opStatus.toString());
            stmt.setString(2, pushNotificationStatus.toString());
            stmt.setInt(3, 0);
            stmt.setInt(4, limit);
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
