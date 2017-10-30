/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.push.notification.mgt;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;

import java.util.List;

public class PushNotificationBasedOperationManager implements OperationManager {

    private OperationManager operationManager;
    private NotificationStrategy notificationProvider;

    public PushNotificationBasedOperationManager(
            OperationManager operationManager, NotificationStrategy notificationProvider) {
        this.operationManager = operationManager;
        this.notificationProvider = notificationProvider;
    }

    @Override
    public Activity addOperation(Operation operation,
                                 List<DeviceIdentifier> devices) throws OperationManagementException, InvalidDeviceException {
        Activity activity = this.operationManager.addOperation(operation, devices);
        for (DeviceIdentifier deviceId : devices) {
            try {
                this.notificationProvider.execute(new NotificationContext(deviceId, operation));
            } catch (PushNotificationExecutionFailedException e) {
                throw new OperationManagementException("Error occurred while sending push notification to device", e);
            }
        }
        return activity;
    }

    @Override
    public List<? extends Operation> getOperations(
            DeviceIdentifier deviceId) throws OperationManagementException {
        return this.operationManager.getOperations(deviceId);
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceId,
                                          PaginationRequest request) throws OperationManagementException {
        return this.operationManager.getOperations(deviceId, request);
    }

    @Override
    public List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException {
        return this.operationManager.getPendingOperations(deviceId);
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return this.operationManager.getNextPendingOperation(deviceId);
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceId,
                                Operation operation) throws OperationManagementException {
        this.operationManager.updateOperation(deviceId, operation);
    }

    @Override
    public void deleteOperation(int operationId) throws OperationManagementException {
        this.operationManager.deleteOperation(operationId);
    }

    @Override
    public Operation getOperationByDeviceAndOperationId(
            DeviceIdentifier deviceId, int operationId) throws OperationManagementException {
        return this.operationManager.getOperationByDeviceAndOperationId(deviceId, operationId);
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(
            DeviceIdentifier deviceId,
            Operation.Status status) throws OperationManagementException {
        try {
            return this.operationManager.getOperationsByDeviceAndStatus(deviceId, status);
        } catch (DeviceManagementException e) {
            throw new OperationManagementException("Error occurred while retrieving the list of operations by " +
                    "device and status", e);
        }
    }

    @Override
    public Operation getOperation(int operationId) throws OperationManagementException {
        return this.operationManager.getOperation(operationId);
    }

    @Override
    public Activity getOperationByActivityId(String activity) throws OperationManagementException {
        return this.operationManager.getOperationByActivityId(activity);
    }

    @Override
    public List<Operation> getOperationUpdatedAfter(long timestamp) throws OperationManagementException {
        return this.operationManager.getOperationUpdatedAfter(timestamp);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException {
        return this.operationManager.getActivitiesUpdatedAfter(timestamp);
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException {
        return this.operationManager.getActivitiesUpdatedAfter(timestamp, limit, offset);
    }

    @Override
    public int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException {
        return this.operationManager.getActivityCountUpdatedAfter(timestamp);
    }

    @Override
    public void setNotificationStrategy(NotificationStrategy notificationStrategy) {

    }

    @Override
    public NotificationStrategy getNotificationStrategy() {
        return notificationProvider;
    }

}
