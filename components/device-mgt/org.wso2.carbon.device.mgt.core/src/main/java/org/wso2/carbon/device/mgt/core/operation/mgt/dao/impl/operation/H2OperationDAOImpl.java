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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationResponse;
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
import java.util.List;

/**
 * This class holds the implementation of OperationDAO which can be used to support H2 db syntax.
 */
public class H2OperationDAOImpl extends GenericOperationDAOImpl {

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Activity> activities = new ArrayList<>();
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "SELECT feom.ENROLMENT_ID, feom.OPERATION_ID, feom.CREATED_TIMESTAMP, o.TYPE AS OPERATION_TYPE, " +
                    "o.OPERATION_CODE, orsp.OPERATION_RESPONSE, orsp.LATEST_RECEIVED_TIMESTAMP AS RECEIVED_TIMESTAMP, " +
                    "orsp.ID AS OP_RES_ID, feom.STATUS, feom.UPDATED_TIMESTAMP, feom.DEVICE_IDENTIFICATION, " +
                    "feom.DEVICE_TYPE FROM (SELECT eom.ENROLMENT_ID, eom.OPERATION_ID, eom.STATUS, eom.CREATED_TIMESTAMP, " +
                    "eom.UPDATED_TIMESTAMP, fe.DEVICE_IDENTIFICATION, fe.DEVICE_TYPE FROM " +
                    "(SELECT ENROLMENT_ID, OPERATION_ID, STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP " +
                    "FROM DM_ENROLMENT_OP_MAPPING WHERE UPDATED_TIMESTAMP > ? ORDER BY OPERATION_ID LIMIT ? OFFSET ?) eom " +
                    "LEFT OUTER JOIN (SELECT e.ID AS ENROLMENT_ID, d.ID AS DEVICE_ID, d.DEVICE_IDENTIFICATION, " +
                    "t.NAME AS DEVICE_TYPE FROM DM_ENROLMENT e LEFT OUTER JOIN DM_DEVICE d ON e.DEVICE_ID = d.ID " +
                    "LEFT OUTER JOIN DM_DEVICE_TYPE t ON d.DEVICE_TYPE_ID = t.ID WHERE d.TENANT_ID = ? AND " +
                    "e.TENANT_ID = ?) fe ON fe.ENROLMENT_ID = eom.ENROLMENT_ID) feom LEFT OUTER JOIN DM_OPERATION o " +
                    "ON feom.OPERATION_ID = o.ID LEFT OUTER JOIN (SELECT ID, ENROLMENT_ID, OPERATION_ID, " +
                    "OPERATION_RESPONSE, MAX(RECEIVED_TIMESTAMP) LATEST_RECEIVED_TIMESTAMP " +
                    "FROM DM_DEVICE_OPERATION_RESPONSE GROUP BY ENROLMENT_ID , OPERATION_ID) orsp " +
                    "ON o.ID = orsp.OPERATION_ID AND feom.ENROLMENT_ID = orsp.ENROLMENT_ID GROUP BY feom.ENROLMENT_ID, " +
                    "feom.OPERATION_ID, feom.CREATED_TIMESTAMP, o.TYPE, o.OPERATION_CODE, orsp.OPERATION_RESPONSE, " +
                    "orsp.LATEST_RECEIVED_TIMESTAMP, orsp.ID, feom.STATUS, feom.UPDATED_TIMESTAMP, " +
                    "feom.DEVICE_IDENTIFICATION, feom.DEVICE_TYPE";

            stmt = conn.prepareStatement(sql);

            stmt.setLong(1, timestamp);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            stmt.setInt(4, tenantId);
            stmt.setInt(5, tenantId);

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
}