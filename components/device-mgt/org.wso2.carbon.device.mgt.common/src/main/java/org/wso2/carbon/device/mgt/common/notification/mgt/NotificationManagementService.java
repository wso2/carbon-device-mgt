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

package org.wso2.carbon.device.mgt.common.notification.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.util.List;

/**
 * Defines the contract of NotificationManagementService.
 */
public interface NotificationManagementService {

    /**
     * Method to add a notification to the database.
     *
     * @param notification - Notification to be added to database.
     * @return boolean status of the operation.
     * @throws NotificationManagementException
     *          if something goes wrong while adding the Notification.
     */
    boolean addNotification(DeviceIdentifier deviceId,
                            Notification notification) throws NotificationManagementException;

    /**
     * Method to update a notification in the database.
     *
     * @param notification - Notification to be updated in the database.
     * @return boolean status of the operation.
     * @throws NotificationManagementException
     *          if something goes wrong while updating the Notification.
     */
    boolean updateNotification(Notification notification) throws NotificationManagementException;

    /**
     * Method to update the notification status of a Notification in the database.
     *
     * @param notificationId - Notification id of the notification to be updated.
     * @param status         - New notification status.
     * @return boolean status of the operation.
     * @throws NotificationManagementException
     *          if something goes wrong while updating the Notification.
     */
    boolean updateNotificationStatus(int notificationId, Notification.Status status) throws
            NotificationManagementException;

    /**
     * Method to fetch all the notifications in the database.
     *
     * @return List of all Notifications in the database.
     * @throws NotificationManagementException
     *
     */
    List<Notification> getAllNotifications() throws NotificationManagementException;

    Notification getNotification(int notificationId) throws NotificationManagementException;

    PaginationResult getAllNotifications(PaginationRequest request) throws NotificationManagementException;

    /**
     * @param status - Status of the notifications to be fetched from database.
     * @return A list of notifications matching the given status.
     * @throws NotificationManagementException
     *          if something goes wrong while fetching the Notification.
     */
    List<Notification> getNotificationsByStatus(Notification.Status status) throws
            NotificationManagementException;

    PaginationResult getNotificationsByStatus(Notification.Status status,
                                   PaginationRequest request) throws NotificationManagementException;

}
