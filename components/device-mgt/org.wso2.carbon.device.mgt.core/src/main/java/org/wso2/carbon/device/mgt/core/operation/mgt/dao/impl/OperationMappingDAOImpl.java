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

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationEnrolmentMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationMappingDAOImpl implements OperationMappingDAO {

    @Override
    public void addOperationMapping(int operationId, Integer deviceId, boolean isScheduled) throws
            OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            long time = System.currentTimeMillis() / 1000;
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, OPERATION_ID, STATUS, " +
                    "PUSH_NOTIFICATION_STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, operationId);
            stmt.setString(3, Operation.Status.PENDING.toString());
            if (isScheduled) {
                stmt.setString(4, Operation.PushNotificationStatus.SCHEDULED.toString());
            } else {
                stmt.setString(4, Operation.PushNotificationStatus.COMPLETED.toString());
            }
            stmt.setLong(5, time);
            stmt.setLong(6, time);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeOperationMapping(int operationId,
                                       Integer deviceId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ? AND OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, operationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while persisting device operation mappings", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateOperationMapping(int operationId, Integer deviceId, Operation.PushNotificationStatus pushNotificationStatus) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_ENROLMENT_OP_MAPPING SET PUSH_NOTIFICATION_STATUS = ? WHERE ENROLMENT_ID = ? and " +
                    "OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, pushNotificationStatus.toString());
            stmt.setInt(2, deviceId);
            stmt.setInt(3, operationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while updating device operation mappings", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateOperationMapping(List<OperationMapping> operationMappingList) throws
            OperationManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_ENROLMENT_OP_MAPPING SET PUSH_NOTIFICATION_STATUS = ? WHERE ENROLMENT_ID = ? and " +
                    "OPERATION_ID = ?";
            stmt = conn.prepareStatement(sql);
            if (conn.getMetaData().supportsBatchUpdates()) {
                for (OperationMapping operationMapping : operationMappingList) {
                    stmt.setString(1, operationMapping.getPushNotificationStatus().toString());
                    stmt.setInt(2, operationMapping.getEnrollmentId());
                    stmt.setInt(3, operationMapping.getOperationId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } else {
                for (OperationMapping operationMapping : operationMappingList) {
                    stmt.setString(1, operationMapping.getPushNotificationStatus().toString());
                    stmt.setInt(2, operationMapping.getEnrollmentId());
                    stmt.setInt(3, operationMapping.getOperationId());
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while updating device operation mappings as " +
                    "batch ", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<OperationEnrolmentMapping> getFirstPendingOperationMappingsForActiveEnrolments(long minDuration,
                                   long maxDuration, int deviceTypeId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<OperationEnrolmentMapping> enrolmentOperationMappingList = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            //We are specifically looking for operation mappings in 'Pending' & 'Repeated' states. Further we want
            //devices to be active at that moment. Hence filtering by 'ACTIVE' & 'UNREACHABLE' device states.
            String sql = "SELECT ENROLMENT_ID, D.DEVICE_IDENTIFICATION AS DEVICE_IDENTIFIER, MIN(CREATED_TIMESTAMP) " +
                    "AS CREATED_TIMESTAMP, E.STATUS AS ENROLMENT_STATUS, E.TENANT_ID FROM " +
                    "DM_ENROLMENT_OP_MAPPING OP INNER JOIN DM_ENROLMENT E ON OP.ENROLMENT_ID = E.ID INNER JOIN " +
                    "DM_DEVICE D ON E.DEVICE_ID = D.ID WHERE " +
                    "OP.STATUS IN ('"+ Operation.Status.PENDING.name() + "','" + Operation.Status.REPEATED.name() + "') " +
                    "AND OP.CREATED_TIMESTAMP BETWEEN ? AND ? AND E.STATUS IN ('" + EnrolmentInfo.Status.ACTIVE.name() +
                    "','" + EnrolmentInfo.Status.UNREACHABLE.name() + "') AND D.DEVICE_TYPE_ID = ? GROUP BY ENROLMENT_ID," +
                    " D.DEVICE_IDENTIFICATION, E.STATUS, E.TENANT_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, maxDuration);
            stmt.setLong(2, minDuration);
            stmt.setInt(3, deviceTypeId);
            rs = stmt.executeQuery();
            enrolmentOperationMappingList = new ArrayList<>();
            while (rs.next()) {
                OperationEnrolmentMapping enrolmentOperationMapping = this.getEnrolmentOpMapping(rs);
                enrolmentOperationMappingList.add(enrolmentOperationMapping);
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while fetching pending operation mappings for " +
                    "active devices of type '" + deviceTypeId + "'", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return enrolmentOperationMappingList;
    }

    @Override
    public Map<Integer, Long> getLastConnectedTimeForActiveEnrolments(long timeStamp, int deviceTypeId) throws OperationManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<Integer, Long> lastConnectedTimeMap = null;
        try {
            Connection conn = OperationManagementDAOFactory.getConnection();
            //We are specifically looking for operation mappings in 'Pending' & 'Repeated' states. Further we want
            //devices to be active at that moment. Hence filtering by 'ACTIVE' & 'UNREACHABLE' device states.
            String sql = "SELECT OP.ENROLMENT_ID AS EID, MAX(OP.UPDATED_TIMESTAMP) AS LAST_CONNECTED_TIME FROM " +
                    "DM_ENROLMENT_OP_MAPPING OP INNER JOIN DM_ENROLMENT E ON OP.ENROLMENT_ID = E.ID INNER JOIN " +
                    "DM_DEVICE D ON E.DEVICE_ID = D.ID WHERE " +
                    "OP.STATUS = '" + Operation.Status.COMPLETED.name() + "'" +
                    "AND OP.UPDATED_TIMESTAMP >= ? AND E.STATUS IN ('" + EnrolmentInfo.Status.ACTIVE.name() +
                    "','" + EnrolmentInfo.Status.UNREACHABLE.name() + "') AND D.DEVICE_TYPE_ID = ? GROUP BY ENROLMENT_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, timeStamp);
            stmt.setInt(2, deviceTypeId);
            rs = stmt.executeQuery();
            lastConnectedTimeMap = new HashMap<>();
            while (rs.next()) {
                lastConnectedTimeMap.put(rs.getInt("EID"), rs.getLong("LAST_CONNECTED_TIME"));
            }
        } catch (SQLException e) {
            throw new OperationManagementDAOException("Error occurred while fetching last connected time for " +
                    "active devices of type '" + deviceTypeId + "'", e);
        } finally {
            OperationManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return lastConnectedTimeMap;
    }

    private OperationEnrolmentMapping getEnrolmentOpMapping(ResultSet rs) throws SQLException {
        OperationEnrolmentMapping enrolmentOperationMapping = new OperationEnrolmentMapping();
        enrolmentOperationMapping.setEnrolmentId(rs.getInt("ENROLMENT_ID"));
        enrolmentOperationMapping.setDeviceId(rs.getString("DEVICE_IDENTIFIER"));
        enrolmentOperationMapping.setTenantId(rs.getInt("TENANT_ID"));
        enrolmentOperationMapping.setCreatedTime(rs.getLong("CREATED_TIMESTAMP"));
        enrolmentOperationMapping.setDeviceStatus(rs.getString("ENROLMENT_STATUS"));
        return enrolmentOperationMapping;
    }
}
