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

package org.wso2.carbon.device.mgt.core.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationDAO;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.util.NotificationDAOUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * This class implements the NotificationManagementService.
 */
public class NotificationManagementServiceImpl implements NotificationManagementService {

	private static final Log log = LogFactory.getLog(NotificationManagementServiceImpl.class);

	private NotificationDAO notificationDAO;
	private DeviceDAO deviceDAO;

	public NotificationManagementServiceImpl() {
		this.notificationDAO = NotificationManagementDAOFactory.getNotificationDAO();
		this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
	}

	@Override
	public boolean addNotification(Notification notification) throws NotificationManagementException {
		boolean status = false;
		int deviceId, tenantId;
		if (log.isDebugEnabled()) {
			log.debug("Adding a Notification : [" + notification.toString() + "]");
		}
		try {
			tenantId = NotificationDAOUtil.getTenantId();
			DeviceManagementDAOFactory.openConnection();
			Device device = deviceDAO.getDevice(notification.getDeviceIdentifier(), tenantId);
			deviceId = device.getId();
		} catch (SQLException e) {
			throw new NotificationManagementException("Error occurred while opening a connection to" +
			                                          " the data source", e);
		} catch (DeviceManagementDAOException e) {
			throw new NotificationManagementException("Error occurred while retriving device data for " +
			                                          " adding notification", e);
		} finally {
			DeviceManagementDAOFactory.closeConnection();
		}
		try {
			NotificationManagementDAOFactory.beginTransaction();
			int notificationId = notificationDAO.addNotification(deviceId, tenantId, notification);
			NotificationManagementDAOFactory.commitTransaction();

			if (log.isDebugEnabled()) {
				log.debug("Notification id : " + notificationId +" was added to the table.");
			}
			if(notificationId > 0) {
				status = true;
			}
		} catch (TransactionManagementException e) {
			NotificationManagementDAOFactory.rollbackTransaction();
			throw new NotificationManagementException("Error occurred while adding notification", e);
		} finally {
			NotificationManagementDAOFactory.closeConnection();
		}
		return status;
	}

	@Override
	public boolean updateNotification(Notification notification) throws NotificationManagementException {
		boolean status = false;
		if (log.isDebugEnabled()) {
			log.debug("Updating Notification : [" + notification.toString() + "]");
		}
		try {
			NotificationManagementDAOFactory.beginTransaction();
			if(notificationDAO.updateNotification(notification) > 0 ) {
				status = true;
			}
			NotificationManagementDAOFactory.commitTransaction();

			if (log.isDebugEnabled()) {
				log.debug("Notification id : " + notification.getNotificationId() +
				          " has updated successfully.");
			}
		} catch (TransactionManagementException e) {
			NotificationManagementDAOFactory.rollbackTransaction();
			throw new NotificationManagementException("Error occurred while updating notification ", e);
		} finally {
			NotificationManagementDAOFactory.closeConnection();
		}
		return status;
	}

	@Override
	public boolean updateNotificationStatus(int notificationId, Notification.Status status)
			throws NotificationManagementException {
		boolean operationStatus = false;
		if (log.isDebugEnabled()) {
			log.debug("Updating Notification id : " + notificationId);
		}
		try {
			NotificationManagementDAOFactory.beginTransaction();
			if(notificationDAO.updateNotificationStatus(notificationId, status) > 0 ) {
				operationStatus = true;
			}
			NotificationManagementDAOFactory.commitTransaction();

			if (log.isDebugEnabled()) {
				log.debug("Notification id : " + notificationId +" has updated successfully.");
			}
		} catch (TransactionManagementException e) {
			NotificationManagementDAOFactory.rollbackTransaction();
			throw new NotificationManagementException("Error occurred while updating notification", e);
		} finally {
			NotificationManagementDAOFactory.closeConnection();
		}
		return operationStatus;
	}

	@Override
	public List<Notification> getAllNotifications() throws NotificationManagementException{
	    try {
			NotificationManagementDAOFactory.openConnection();
			return notificationDAO.getAllNotifications(NotificationDAOUtil.getTenantId());
		} catch (SQLException e) {
			throw new NotificationManagementException("Error occurred while opening a connection to" +
			                                          " the data source", e);
		} finally {
			NotificationManagementDAOFactory.closeConnection();
		}
	}

	@Override
	public List<Notification> getNotificationsByStatus(Notification.Status status)
			throws NotificationManagementException{
		try {
			NotificationManagementDAOFactory.openConnection();
			return notificationDAO.getNotificationsByStatus(status, NotificationDAOUtil.getTenantId());
		} catch (SQLException e) {
			throw new NotificationManagementException("Error occurred while opening a connection " +
			                                          "to the data source", e);
		} finally {
			NotificationManagementDAOFactory.closeConnection();
		}
	}
}
