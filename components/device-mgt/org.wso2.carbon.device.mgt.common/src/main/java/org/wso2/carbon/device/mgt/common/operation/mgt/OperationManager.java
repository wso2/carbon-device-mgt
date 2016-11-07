/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common.operation.mgt;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;

import java.util.List;

/**
 * This represents the Device Operation management functionality which should be implemented by
 * the device type plugins.
 */
public interface OperationManager {

    /**
     * Method to add a operation to a device or a set of devices.
     *
     * @param operation Operation to be added
     * @param devices   List of DeviceIdentifiers to execute the operation
     * @throws OperationManagementException If some unusual behaviour is observed while adding the operation
     *         InvalidDeviceException       If addOperation request contains Invalid DeviceIdentifiers.
     */
    Activity addOperation(Operation operation, List<DeviceIdentifier> devices) throws OperationManagementException,
            InvalidDeviceException;

    /**
     * Method to retrieve the list of all operations to a device.
     *
     * @param deviceId
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException;

    /**
     * Method to retrieve all the operations applied to a device with pagination support.
     *
     * @param deviceId DeviceIdentifier of the device
     * @param request  PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    PaginationResult getOperations(DeviceIdentifier deviceId, PaginationRequest request) throws OperationManagementException;

    /**
     * Method to retrieve the list of available operations to a device.
     *
     * @param deviceId DeviceIdentifier of the device
     * @throws OperationManagementException If some unusual behaviour is observed while fetching the
     *                                      operation list.
     */
    List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException;

    void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException;

    void deleteOperation(int operationId) throws OperationManagementException;

    Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException;

    List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
                                                             Operation.Status status)
            throws OperationManagementException, DeviceManagementException;

    Operation getOperation(int operationId) throws OperationManagementException;

    Activity getOperationByActivityId(String activity) throws OperationManagementException;

    List<Operation> getOperationUpdatedAfter(long timestamp) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException;

    /**
     * Operation manger implementation can have a push notification stratergy
     * @param notificationStrategy eg: mqtt/xmpp
     */
    void setNotificationStrategy(NotificationStrategy notificationStrategy);

    /**
     * retrive the push notification strategy.
     * @return NotificationStrategy
     */
    NotificationStrategy getNotificationStrategy();

}