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

package org.wso2.carbon.device.mgt.core.notification.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationDAO;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.util.NotificationDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of NotificationDAO which includes the methods to do CRUD operations on notification.
 */
public class NotificationDAOImpl implements NotificationDAO {

	private static final Log log = LogFactory.getLog(NotificationDAOImpl.class);

	@Override
	public int addNotification(int deviceId, int tenantId, Notification notification) throws
	                                                               NotificationManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs;
		int notificationId = -1;
		try {
			conn = NotificationManagementDAOFactory.getConnection();
			String sql =
					"INSERT INTO DM_NOTIFICATION(DEVICE_ID, OPERATION_ID, STATUS, DESCRIPTION, TENANT_ID) " +
					"VALUES (?, ?, ?, ?, ?)";
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, deviceId);
			stmt.setInt(2, notification.getOperationId());
			stmt.setString(3, notification.getStatus().toString());
			stmt.setString(4, notification.getDescription());
			stmt.setInt(5, tenantId);
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				notificationId = rs.getInt(1);
			}
		} catch (Exception e) {
			throw new NotificationManagementException("Error occurred while adding the " +
			                                          "Notification for device id : " + deviceId,
			                                          e);
		} finally {
			NotificationDAOUtil.cleanupResources(stmt, null);
		}
		return notificationId;
	}

	@Override
	public int updateNotification(Notification notification)
			throws NotificationManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = NotificationManagementDAOFactory.getConnection();
			String sql = "UPDATE DM_NOTIFICATION SET OPERATION_ID = ?, STATUS = ?, DESCRIPTION = ? " +
			             "WHERE NOTIFICATION_ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, notification.getOperationId());
			stmt.setString(2, notification.getStatus().toString());
			stmt.setString(3, notification.getDescription());
			stmt.setInt(4, notification.getNotificationId());
			rows = stmt.executeUpdate();
		} catch (Exception e) {
			throw new NotificationManagementException("Error occurred while updating the " +
			                            "Notification id : " + notification.getNotificationId(), e);
		} finally {
			NotificationDAOUtil.cleanupResources(stmt, null);
		}
		return rows;
	}

	@Override
	public int updateNotificationStatus(int notificationId, Notification.Status status)
			throws NotificationManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		int rows;
		try {
			conn = NotificationManagementDAOFactory.getConnection();
			String sql = "UPDATE DM_NOTIFICATION SET STATUS = ? WHERE NOTIFICATION_ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, status.toString());
			stmt.setInt(2, notificationId);
			rows = stmt.executeUpdate();
		} catch (Exception e) {
			throw new NotificationManagementException("Error occurred while updating the status of " +
			                                          "Notification id : " + notificationId, e);
		} finally {
			NotificationDAOUtil.cleanupResources(stmt, null);
		}
		return rows;
	}

	@Override
	public List<Notification> getAllNotifications(int tenantId)
			throws NotificationManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Notification> notifications = null;
		try {
			conn = NotificationManagementDAOFactory.getConnection();
			String sql =
					"SELECT n1.NOTIFICATION_ID, n1.DEVICE_ID, n1.OPERATION_ID, n1.STATUS, n1.DESCRIPTION," +
					" d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, DM_DEVICE_TYPE t, (SELECT " +
					"NOTIFICATION_ID, DEVICE_ID, OPERATION_ID, STATUS, DESCRIPTION FROM DM_NOTIFICATION WHERE " +
					"TENANT_ID = ?) n1 WHERE n1.DEVICE_ID = d.ID AND d.DEVICE_TYPE_ID=t.ID AND TENANT_ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setInt(2, tenantId);
			rs = stmt.executeQuery();
			notifications = new ArrayList<>();
			while (rs.next()) {
				notifications.add(this.getNotification(rs));
			}
		} catch (SQLException e) {
			throw new NotificationManagementException(
					"Error occurred while retrieving information of all notifications", e);
		} finally {
			NotificationDAOUtil.cleanupResources(stmt, rs);
		}
		return notifications;
	}

	@Override
	public List<Notification> getNotificationsByStatus(Notification.Status status, int tenantId)
			throws NotificationManagementException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Notification> notifications = null;
		try {
			conn = NotificationManagementDAOFactory.getConnection();
			String sql = "SELECT n1.NOTIFICATION_ID, n1.DEVICE_ID, n1.OPERATION_ID, n1.STATUS," +
			             " n1.DESCRIPTION, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM " +
			             "DM_DEVICE d, DM_DEVICE_TYPE t, (SELECT NOTIFICATION_ID, DEVICE_ID, " +
			             "OPERATION_ID, STATUS, DESCRIPTION FROM DM_NOTIFICATION WHERE " +
			             "TENANT_ID = ? AND STATUS = ?) n1 WHERE n1.DEVICE_ID = d.ID AND d.DEVICE_TYPE_ID=t.ID " +
			             "AND TENANT_ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setString(2, status.toString());
			stmt.setInt(3, tenantId);
			rs = stmt.executeQuery();
			notifications = new ArrayList<>();
			while (rs.next()) {
				notifications.add(this.getNotification(rs));
			}
		} catch (SQLException e) {
			throw new NotificationManagementException(
					"Error occurred while retrieving information of all " +
					"notifications by status : " + status, e);
		} finally {
			NotificationDAOUtil.cleanupResources(stmt, rs);
		}
		return notifications;
	}

	private Notification getNotification(ResultSet rs) throws SQLException {
		Notification notification = new Notification();
		notification.setNotificationId(rs.getInt("NOTIFICATION_ID"));
		notification.setDeviceIdentifier(this.getDeviceIdentifier(rs));
		notification.setOperationId(rs.getInt("OPERATION_ID"));
		notification.setDescription(rs.getString("DESCRIPTION"));
		notification.setStatus(rs.getString("STATUS"));
		return notification;
	}

	private DeviceIdentifier getDeviceIdentifier(ResultSet rs) throws SQLException {
		DeviceIdentifier identifier = new DeviceIdentifier();
		identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
		identifier.setType(rs.getString("DEVICE_TYPE"));
		return identifier;
	}
}