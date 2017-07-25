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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EntityDoesNotExistException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationDAO;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.util.NotificationDAOUtil;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.sql.SQLException;
import java.util.ArrayList;
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
    public boolean addNotification(DeviceIdentifier deviceId,
                                   Notification notification) throws NotificationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Adding a Notification : [" + notification.toString() + "]");
        }
        int notificationId;
        int tenantId = NotificationDAOUtil.getTenantId();

        Device device = this.getDevice(deviceId);
        if (device == null) {
            throw new EntityDoesNotExistException("No device is found with type '" + deviceId.getType() +
                    "' and id '" + deviceId.getId() + "'");
        }

        try {
            NotificationManagementDAOFactory.beginTransaction();
            notificationId = notificationDAO.addNotification(device.getId(), tenantId, notification);
            NotificationManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            throw new NotificationManagementException("Error occurred while adding notification", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Notification id : " + notificationId + " was added to the table.");
        }
        return true;
    }

    private Device getDevice(DeviceIdentifier deviceId) throws NotificationManagementException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId, false);
        } catch (DeviceManagementException e) {
            throw new NotificationManagementException("Error occurred while retrieving device data for " +
                    " adding notification", e);
        }
        return device;
    }

    @Override
    public boolean updateNotification(Notification notification) throws NotificationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating Notification : [" + notification.toString() + "]");
        }
        try {
            NotificationManagementDAOFactory.beginTransaction();
            notificationDAO.updateNotification(notification);
            NotificationManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            throw new NotificationManagementException("Error occurred while updating notification ", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Notification id : " + notification.getNotificationId() +
                    " has updated successfully.");
        }
        return true;
    }

    @Override
    public boolean updateNotificationStatus(int notificationId, Notification.Status status)
            throws NotificationManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating Notification id : " + notificationId);
        }
        try {
            NotificationManagementDAOFactory.beginTransaction();
            notificationDAO.updateNotificationStatus(notificationId, status);
            NotificationManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            NotificationManagementDAOFactory.rollbackTransaction();
            throw new NotificationManagementException("Error occurred while updating notification", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
        if (log.isDebugEnabled()) {
            log.debug("Notification id : " + notificationId + " has updated successfully.");
        }
        return true;
    }

    @Override
    public List<Notification> getAllNotifications() throws NotificationManagementException {
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
    public Notification getNotification(int notificationId) throws NotificationManagementException {
        try {
            NotificationManagementDAOFactory.openConnection();
            return notificationDAO.getNotification(NotificationDAOUtil.getTenantId(), notificationId);
        } catch (SQLException e) {
            throw new NotificationManagementException("Error occurred while opening a connection to" +
                    " the data source", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getAllNotifications(PaginationRequest request) throws NotificationManagementException {
        PaginationResult paginationResult = new PaginationResult();
        List<Notification> notifications = new ArrayList<>();
        request = DeviceManagerUtil.validateNotificationListPageSize(request);
        int count =0;
        try {
            NotificationManagementDAOFactory.openConnection();
            notifications = notificationDAO.getAllNotifications(request, NotificationDAOUtil.getTenantId());
            count = notificationDAO.getNotificationCount(NotificationDAOUtil.getTenantId());
            paginationResult.setData(notifications);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (SQLException e) {
            throw new NotificationManagementException("Error occurred while opening a connection to" +
                    " the data source", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getNotificationsByStatus(Notification.Status status,
                                                     PaginationRequest request) throws NotificationManagementException{
        PaginationResult paginationResult = new PaginationResult();
        List<Notification> notifications = new ArrayList<>();
        request = DeviceManagerUtil.validateNotificationListPageSize(request);
        int count =0;
        try {
            NotificationManagementDAOFactory.openConnection();
            notifications = notificationDAO.getNotificationsByStatus(request, status, NotificationDAOUtil.getTenantId());
            count = notificationDAO.getNotificationCountByStatus(status, NotificationDAOUtil.getTenantId());
            paginationResult.setData(notifications);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (SQLException e) {
            throw new NotificationManagementException("Error occurred while opening a connection " +
                    "to the data source", e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }


    @Override
    public List<Notification> getNotificationsByStatus(Notification.Status status)
            throws NotificationManagementException {
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
