/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.archival.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.archival.beans.*;
import org.wso2.carbon.device.mgt.core.archival.dao.*;

import java.sql.*;
import java.util.*;

public class ArchivalDAOImpl implements ArchivalDAO {

    private static final Log log = LogFactory.getLog(ArchivalDAOImpl.class);

    private int retentionPeriod;
    private int batchSize = ArchivalDAO.DEFAULT_BATCH_SIZE;
    private Timestamp currentTimestamp;


    public ArchivalDAOImpl(int retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public ArchivalDAOImpl(int retentionPeriod, int batchSize) {
        this.retentionPeriod = retentionPeriod;
        this.batchSize = batchSize;
        this.currentTimestamp = new Timestamp(new java.util.Date().getTime());
        if (log.isDebugEnabled()) {
            log.debug("Using batch size of " + this.batchSize + " with retention period " + this.retentionPeriod);
        }
    }

    @Override
    public List<Integer> getAllOperations() throws ArchivalDAOException {
        List<Integer> operationIds = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE CREATED_TIMESTAMP < DATE_SUB(NOW(), INTERVAL " + this.retentionPeriod + " DAY)";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);
            if (log.isDebugEnabled()) {
                log.debug("Selected Operation Ids from Enrolment OP Mapping");
            }
            while (rs.next()) {
                operationIds.add(rs.getInt("OPERATION_ID"));
            }
        } catch (SQLException e) {
            String msg = "An error occurred while getting a list operation Ids to archive";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        if (log.isDebugEnabled()) {
            log.debug(operationIds.size() + " operations found for the archival");
            log.debug(operationIds.size() + "[" + operationIds.get(0) + "," + operationIds.get(batchSize - 1) + "]");
        }
        return operationIds;
    }

    @Override
    public List<Integer> getPendingAndInProgressOperations() throws ArchivalDAOException {
        List<Integer> operationIds = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT DISTINCT OPERATION_ID " +
                    " FROM DM_ENROLMENT_OP_MAPPING WHERE STATUS='PENDING' OR STATUS='IN_PROGRESS' " +
                    " AND CREATED_TIMESTAMP < DATE_SUB(NOW(), INTERVAL " + this.retentionPeriod + " DAY)";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);
            if (log.isDebugEnabled()) {
                log.debug("Selected Pending or In Progress Operation IDs");
            }
            while (rs.next()) {
                operationIds.add(rs.getInt("OPERATION_ID"));
            }
        } catch (SQLException e) {
            String msg = "An error occurred while getting a list pending or in progress operation Ids to archive";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        if (log.isDebugEnabled()) {
            log.debug(operationIds.size() + " operations found for the archival");
            log.debug(operationIds.size() + "[" + operationIds.get(0) + "," + operationIds.get(batchSize - 1) + "]");
        }
        return operationIds;
    }

    @Override
    public void copyOperationIDsForArchival(List<Integer> operationIds) throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ARCHIVED_OPERATIONS(ID,CREATED_TIMESTAMP) VALUES (?,NOW())";
            stmt = conn.prepareStatement(sql);

            int count = 0;
            for (int i = 0; i < operationIds.size(); i++) {
                stmt.setInt(1, operationIds.get(i));
                stmt.addBatch();

                if (++count % this.batchSize == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " Records copied to the temporary table.");
            }
        } catch (SQLException e) {
            String msg = "Error while copying operation Ids for archival";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public List<ArchiveOperationResponse> selectOperationResponses() throws ArchivalDAOException {
        Statement stmt = null;
        ResultSet rs = null;

        List<ArchiveOperationResponse> operationResponses = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    o.ID,\n" +
                    "    o.ENROLMENT_ID,\n" +
                    "    o.OPERATION_ID,\n" +
                    "    o.EN_OP_MAP_ID,\n" +
                    "    o.OPERATION_RESPONSE,\n" +
                    "    o.RECEIVED_TIMESTAMP\n" +
                    "FROM\n" +
                    "    DM_DEVICE_OPERATION_RESPONSE o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ArchiveOperationResponse rep = new ArchiveOperationResponse();
                rep.setId(rs.getInt("ID"));
                rep.setEnrolmentId(rs.getInt("ENROLMENT_ID"));
                rep.setOperationId(rs.getInt("OPERATION_ID"));
                rep.setOperationResponse(rs.getBytes("OPERATION_RESPONSE"));
                rep.setReceivedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP"));
                operationResponses.add(rep);
            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Operation Response");
            }

        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operation responses";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }

        return operationResponses;

    }

    @Override
    public void moveOperationResponses(List<ArchiveOperationResponse> archiveOperationResponse) throws ArchivalDAOException {
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();


            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();
            String sql = "INSERT INTO DM_DEVICE_OPERATION_RESPONSE_ARCH VALUES(?, ?, ?, ?, ?,?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
            for (ArchiveOperationResponse rs : archiveOperationResponse) {
                stmt2.setInt(1, rs.getId());
                stmt2.setInt(2, rs.getEnrolmentId());
                stmt2.setInt(3, rs.getOperationId());
                stmt2.setBytes(4, (byte[]) rs.getOperationResponse());
                stmt2.setTimestamp(5, rs.getReceivedTimeStamp());
                stmt2.setTimestamp(6, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Executing Operation Responses batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [OPERATION_RESPONSES] Records copied to the archival table. Starting deletion");
            }
            //try the deletion now
            sql = "DELETE o.* FROM DM_DEVICE_OPERATION_RESPONSE o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.OPERATION_ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operation responses";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public List<ArchiveNotification> selectNotifications() throws ArchivalDAOException {

        Statement stmt = null;
        ResultSet rs = null;
        List<ArchiveNotification> notifications = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    o.NOTIFICATION_ID,\n" +
                    "    o.DEVICE_ID,\n" +
                    "    o.OPERATION_ID,\n" +
                    "    o.TENANT_ID,\n" +
                    "    o.STATUS,\n" +
                    "    o.DESCRIPTION\n" +
                    "FROM\n" +
                    "    DM_NOTIFICATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                ArchiveNotification note = new ArchiveNotification();
                note.setNotificationId(rs.getInt("NOTIFICATION_ID"));
                note.setDeviceId(rs.getInt("DEVICE_ID"));
                note.setOperationId(rs.getInt("OPERATION_ID"));
                note.setTenantId(rs.getInt("TENANT_ID"));
                note.setStatus(rs.getString("STATUS"));
                note.setDescription(rs.getString("DESCRIPTION"));
                notifications.add(note);
            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Notification");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the notifications";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return notifications;
    }


    @Override
    public void moveNotifications(List<ArchiveNotification> archiveNotifications) throws ArchivalDAOException {
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO DM_NOTIFICATION_ARCH VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
//            while (rs.next()) {
            for (ArchiveNotification rs : archiveNotifications) {
                stmt2.setInt(1, rs.getNotificationId());
                stmt2.setInt(2, rs.getDeviceId());
                stmt2.setInt(3, rs.getOperationId());
                stmt2.setInt(4, rs.getTenantId());
                stmt2.setString(5, rs.getStatus());
                stmt2.setString(6, rs.getDescription());
                stmt2.setTimestamp(7, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Executing Notifications batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [NOTIFICATIONS] Records copied to the archival table. Starting deletion");
            }
            sql = "DELETE o.* FROM DM_NOTIFICATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.OPERATION_ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the notifications";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public List<ArchiveCommandOperation> selectCommandOperations() throws ArchivalDAOException {
        Statement stmt = null;
        ResultSet rs = null;

        List<ArchiveCommandOperation> commandOperations = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    *\n" +
                    "FROM\n" +
                    "    DM_COMMAND_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ArchiveCommandOperation op = new ArchiveCommandOperation();
                op.setOperationId(rs.getInt("OPERATION_ID"));
                op.setEnabled(rs.getInt("ENABLED"));

                commandOperations.add(op);
            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Command Operation");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the command operation";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return commandOperations;
    }

    @Override
    public void moveCommandOperations(List<ArchiveCommandOperation> commandOperations) throws ArchivalDAOException {
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO DM_COMMAND_OPERATION_ARCH VALUES(?,?,?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
            for (ArchiveCommandOperation rs : commandOperations) {
                stmt2.setInt(1, rs.getOperationId());
                stmt2.setInt(2, rs.getEnabled());
                stmt2.setTimestamp(3, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Executing Command Operations batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [COMMAND_OPERATION] Records copied to the archival table. Starting deletion");
            }
            sql = "DELETE o.* FROM DM_COMMAND_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.OPERATION_ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the command operation";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public List<ArchiveProfileOperation> selectProfileOperations() throws ArchivalDAOException {
        Statement stmt = null;
        ResultSet rs = null;
        List<ArchiveProfileOperation> profileOperations = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    *\n" +
                    "FROM\n" +
                    "    DM_PROFILE_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                ArchiveProfileOperation op = new ArchiveProfileOperation();

                op.setOperationId(rs.getInt("OPERATION_ID"));
                op.setEnabled(rs.getInt("ENABLED"));
                op.setOperationDetails(rs.getBytes("OPERATION_DETAILS"));
                profileOperations.add(op);

            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Profile Operation");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the profile operation";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return profileOperations;
    }

    @Override
    public void moveProfileOperations(List<ArchiveProfileOperation> profileOperations) throws ArchivalDAOException {
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();

            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO DM_PROFILE_OPERATION_ARCH VALUES(?, ?, ?, ?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
            for (ArchiveProfileOperation rs : profileOperations) {
                stmt2.setInt(1, rs.getOperationId());
                stmt2.setInt(2, rs.getEnabled());
                stmt2.setBytes(3, (byte[]) rs.getOperationDetails());
                stmt2.setTimestamp(4, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Executing Profile Operations batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [PROFILE_OPERATION] Records copied to the archival table. Starting deletion");
            }
            sql = "DELETE o.* FROM DM_PROFILE_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.OPERATION_ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the profile operation";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public List<ArchiveEnrolmentOperationMap> selectEnrolmentMappings() throws ArchivalDAOException {
        Statement stmt = null;
        ResultSet rs = null;
        List<ArchiveEnrolmentOperationMap> operationMaps = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    o.ID,\n" +
                    "    o.ENROLMENT_ID,\n" +
                    "    o.OPERATION_ID,\n" +
                    "    o.STATUS,\n" +
                    "    o.CREATED_TIMESTAMP,\n" +
                    "    o.UPDATED_TIMESTAMP\n" +
                    "FROM\n" +
                    "    DM_ENROLMENT_OP_MAPPING o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                ArchiveEnrolmentOperationMap eom = new ArchiveEnrolmentOperationMap();
                eom.setId(rs.getInt("ID"));
                eom.setEnrolmentId(rs.getInt("ENROLMENT_ID"));
                eom.setOperationId(rs.getInt("OPERATION_ID"));
                eom.setStatus(rs.getString("STATUS"));
                eom.setCreatedTimestamp(rs.getInt("CREATED_TIMESTAMP"));
                eom.setUpdatedTimestamp(rs.getInt("UPDATED_TIMESTAMP"));
                operationMaps.add(eom);
            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Enrolment OP Mapping");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the enrolment op mappings";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }

        return operationMaps;
    }

    @Override
    public void moveEnrolmentMappings(List<ArchiveEnrolmentOperationMap> operationMaps) throws ArchivalDAOException {
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO DM_ENROLMENT_OP_MAPPING_ARCH VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
            for (ArchiveEnrolmentOperationMap rs : operationMaps) {
                stmt2.setInt(1, rs.getId());
                stmt2.setInt(2, rs.getEnrolmentId());
                stmt2.setInt(3, rs.getOperationId());
                stmt2.setString(4, rs.getStatus());
                stmt2.setInt(5, rs.getCreatedTimestamp());
                stmt2.setInt(6, rs.getUpdatedTimestamp());
                stmt2.setTimestamp(7, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Executing Enrolment Mappings batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [ENROLMENT_OP_MAPPING] Records copied to the archival table. Starting deletion");
            }
            sql = "DELETE o.* FROM DM_ENROLMENT_OP_MAPPING o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.OPERATION_ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.OPERATION_ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the enrolment op mappings";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public List<ArchiveOperation> selectOperations() throws ArchivalDAOException {
        Statement stmt = null;
        ResultSet rs = null;
        List<ArchiveOperation> operations = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT \n" +
                    "    o.ID,\n" +
                    "    o.TYPE,\n" +
                    "    o.CREATED_TIMESTAMP,\n" +
                    "    o.RECEIVED_TIMESTAMP,\n" +
                    "    o.OPERATION_CODE\n" +
                    "FROM\n" +
                    "    DM_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.ID = da.ID;";
            stmt = this.createMemoryEfficientStatement(conn);
            rs = stmt.executeQuery(sql);

            while (rs.next()) {

                ArchiveOperation op = new ArchiveOperation();
                op.setId(rs.getInt("ID"));
                op.setType(rs.getString("TYPE"));
                op.setCreatedTimeStamp(rs.getTimestamp("CREATED_TIMESTAMP"));
                op.setRecievedTimeStamp(rs.getTimestamp("RECEIVED_TIMESTAMP"));
                op.setOperationCode(rs.getString("OPERATION_CODE"));

                operations.add(op);

            }

            if (log.isDebugEnabled()) {
                log.debug("Selecting done for the Operation");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operations";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return operations;
    }

    @Override
    public void moveOperations(List<ArchiveOperation> operations) throws ArchivalDAOException {
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        Statement stmt3 = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();
            String sql = "INSERT INTO DM_OPERATION_ARCH VALUES(?, ?, ?, ?, ?, ?)";
            stmt2 = conn2.prepareStatement(sql);

            int count = 0;
            for (ArchiveOperation rs : operations) {
                stmt2.setInt(1, rs.getId());
                stmt2.setString(2, rs.getType());
                stmt2.setTimestamp(3, rs.getCreatedTimeStamp());
                stmt2.setTimestamp(4, rs.getRecievedTimeStamp());
                stmt2.setString(5, rs.getOperationCode());
                stmt2.setTimestamp(6, this.currentTimestamp);
                stmt2.addBatch();

                if (++count % batchSize == 0) {
                    stmt2.executeBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Final Execution of Operations batch " + count);
                    }
                }
            }
            stmt2.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug(count + " [OPERATIONS] Records copied to the archival table. Starting deletion");
            }
            sql = "DELETE o.* FROM DM_OPERATION o\n" +
                    "        INNER JOIN\n" +
                    "    DM_ARCHIVED_OPERATIONS da ON o.ID = da.ID \n" +
                    "WHERE\n" +
                    "    o.ID = da.ID;";
            stmt3 = conn.createStatement();
            int affected = stmt3.executeUpdate(sql);
            if (log.isDebugEnabled()) {
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operations";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt2);
            ArchivalDAOUtil.cleanupResources(stmt3);
        }
    }

    @Override
    public void truncateOperationIDsForArchival() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "TRUNCATE DM_ARCHIVED_OPERATIONS";
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while truncating operation Ids";
            log.error(msg, e);
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    private Statement createMemoryEfficientStatement(Connection conn) throws ArchivalDAOException, SQLException {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        return stmt;
    }

    private String buildWhereClause(String[] statuses) {
        StringBuilder whereClause = new StringBuilder("WHERE ");
        for (int i = 0; i < statuses.length; i++) {
            whereClause.append("STATUS ='");
            whereClause.append(statuses[i]);
            whereClause.append("' ");
            if (i != (statuses.length - 1))
                whereClause.append(" OR ");
        }
        return whereClause.toString();
    }

    private void copyOperationIDsForArchival() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        Statement createStmt = null;
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
//            conn.setAutoCommit(false);
//            String sql = "INSERT INTO DM_ARCHIVED_OPERATIONS(ID,CREATED_TIMESTAMP)" +
//                    " SELECT DISTINCT op.ID as OPERATION_ID, NOW()" +
//                    " FROM DM_ENROLMENT_OP_MAPPING AS opm" +
//                    " LEFT JOIN DM_OPERATION AS op ON opm.OPERATION_ID = op.ID" +
//                    " WHERE opm.STATUS='ERROR' OR opm.STATUS='COMPLETED'" +
//                    " AND op.RECEIVED_TIMESTAMP < DATE_SUB(NOW(), INTERVAL ? DAY);";
//            stmt = conn.prepareStatement(sql);
//            stmt.setInt(1, this.retentionPeriod);
//            stmt.addBatch();
//            stmt.executeBatch();
//            conn.commit();

            //Create the temporary table first
//            String sql = "CREATE TEMPORARY TABLE DM_ARCHIVED_OPERATIONS (ID INTEGER NOT NULL," +
//                    "    CREATED_TIMESTAMP TIMESTAMP NOT NULL, PRIMARY KEY (ID))" ;
//            createStmt = conn.createStatement();
//            createStmt.execute(sql);
//            if(log.isDebugEnabled()) {
//                log.debug("Temporary table DM_ARCHIVED_OPERATIONS has been created ");
//            }
            //Copy eligible operations into DM_ARCHIVED_OPERATIONS
            String sql = "INSERT INTO DM_ARCHIVED_OPERATIONS(ID,CREATED_TIMESTAMP)" +
                    " SELECT DISTINCT OPERATION_ID, NOW()" +
                    " FROM DM_ENROLMENT_OP_MAPPING" +
                    " WHERE STATUS='ERROR' OR STATUS='COMPLETED' OR STATUS='REPEATED'" +
                    " AND CREATED_TIMESTAMP < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            int affected = stmt.executeUpdate();
            log.info(affected + " Eligible operations found for archival");
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while copying operation Ids for archival", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
            ArchivalDAOUtil.cleanupResources(createStmt);
        }
    }
}