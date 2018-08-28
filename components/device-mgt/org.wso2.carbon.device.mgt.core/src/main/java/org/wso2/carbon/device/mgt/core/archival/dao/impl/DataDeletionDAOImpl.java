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
import org.wso2.carbon.device.mgt.core.archival.dao.DataDeletionDAO;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAOException;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAOUtil;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import org.wso2.carbon.device.mgt.core.task.impl.ArchivedDataDeletionTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataDeletionDAOImpl implements DataDeletionDAO {
    private static Log log = LogFactory.getLog(DataDeletionDAOImpl.class);

    private int retentionPeriod = DataDeletionDAO.DEFAULT_RETENTION_PERIOD;

    public DataDeletionDAOImpl(int retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
        if (log.isDebugEnabled()) {
            log.debug("Using retention period as " + retentionPeriod);
        }
    }

    @Override
    public void deleteOperationResponses() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE_ARCH " +
                    "WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting operation responses", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteNotifications() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_NOTIFICATION_ARCH" +
                    "  WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting notifications", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteCommandOperations() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_COMMAND_OPERATION_ARCH" +
                    "  WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting command operations", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteProfileOperations() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_PROFILE_OPERATION_ARCH" +
                    "  WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting profile operations", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteEnrolmentMappings() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING_ARCH WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting enrolment mappings", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void deleteOperations() throws ArchivalDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "DELETE FROM DM_OPERATION_ARCH WHERE ARCHIVED_AT < DATE_SUB(NOW(), INTERVAL ? DAY)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.retentionPeriod);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new ArchivalDAOException("Error occurred while deleting operations", e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

}
