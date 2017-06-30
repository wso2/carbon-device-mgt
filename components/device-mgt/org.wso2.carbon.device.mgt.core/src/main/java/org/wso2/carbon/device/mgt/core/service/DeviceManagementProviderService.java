/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Proxy class for all Device Management related operations that take the corresponding plugin type in
 * and resolve the appropriate plugin implementation
 */
public interface DeviceManagementProviderService {

    /**
     * Method to retrieve all the devices of a given device type.
     *
     * @param deviceType Device-type of the required devices
     * @return List of devices of given device-type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(String deviceType) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices of a given device type.
     *
     * @param deviceType Device-type of the required devices
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices of given device-type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(String deviceType, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices() throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices(boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param since - Date value where the resource was last modified
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getDevices(Date since) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices registered in the system.
     *
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getDevices(Date since, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getDevicesByType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getAllDevices(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDeviceWithTypeProperties(DeviceIdentifier deviceId) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param since - Date value where the resource was last modified
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, Date since) throws DeviceManagementException;

    /**
     * Returns the device of specified id.
     *
     * @param deviceId device Id
     * @param since - Date value where the resource was last modified
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, Date since, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Returns the device of specified id with the given status.
     *
     * @param deviceId device Id
     * @param status - Status of the device
     *
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * Returns the device of specified id with the given status.
     *
     * @param deviceId device Id
     * @param status - Status of the device
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return Device returns null when device is not available.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesOfUser(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesOfUser(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName Username of the user
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName Username of the user
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method returns the list of device owned by a user of given device type.
     *
     * @param userName   user name.
     * @param deviceType device type name
     * @return List of device owned by the given user and type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, String deviceType) throws DeviceManagementException;

    /**
     * This method returns the list of device owned by a user of given device type.
     *
     * @param userName   user name.
     * @param deviceType device type name
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of device owned by the given user and type.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesOfUser(String userName, String deviceType, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName Role name of the users
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getAllDevicesOfRole(String roleName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName Role name of the users
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getAllDevicesOfRole(String roleName, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the list of devices that matches with the given device name.
     *
     * @param request PaginationRequest object holding the data for pagination and filter info
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesByNameAndType(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByName(PaginationRequest request) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByName(PaginationRequest request, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status.
     *
     * @param status Device status
     * @return List of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status.
     *
     * @param status Device status
     * @param requireDeviceInfo - A boolean indicating whether the device-info (location, app-info etc) is also required
     *                          along with the device data.
     * @return List of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, boolean requireDeviceInfo) throws DeviceManagementException;

    /**
     * Method to get the device count of user.
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount(String username) throws DeviceManagementException;

    /**
     * Method to get the count of all types of devices.
     *
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     *                                   the devices
     */
    int getDeviceCount() throws DeviceManagementException;

    HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException;

    void sendEnrolmentInvitation(String templateName, EmailMetaInfo metaInfo) throws DeviceManagementException;

    void sendRegistrationEmail(EmailMetaInfo metaInfo) throws DeviceManagementException;

    FeatureManager getFeatureManager(String deviceType) throws DeviceManagementException;

    /**
     * Proxy method to get the tenant configuration of a given platform.
     *
     * @param deviceType Device platform
     * @return Tenant configuration settings of the particular tenant and platform.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   configuration.
     */
    PlatformConfiguration getConfiguration(String deviceType) throws DeviceManagementException;

    void updateDeviceEnrolmentInfo(Device device, EnrolmentInfo.Status active) throws DeviceManagementException;

    /**
     * This method is used to check whether the device is enrolled with the give user.
     *
     * @param deviceId identifier of the device that needs to be checked against the user.
     * @param user     username of the device owner.
     * @return true if the user owns the device else will return false.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the device.
     */
    boolean isEnrolled(DeviceIdentifier deviceId, String user) throws DeviceManagementException;

    /**
     * This method is used to get notification strategy for given device type
     *
     * @param deviceType Device type
     * @return Notification Strategy for device type
     * @throws DeviceManagementException
     */
    NotificationStrategy getNotificationStrategyByDeviceType(String deviceType) throws DeviceManagementException;

    License getLicense(String deviceType, String languageCode) throws DeviceManagementException;

    void addLicense(String deviceType, License license) throws DeviceManagementException;

    boolean modifyEnrollment(Device device) throws DeviceManagementException;

    boolean enrollDevice(Device device) throws DeviceManagementException;

    PlatformConfiguration getConfiguration() throws DeviceManagementException;

    boolean saveConfiguration(PlatformConfiguration configuration) throws DeviceManagementException;

    boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException;

    List<String> getAvailableDeviceTypes() throws DeviceManagementException;

    boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException;

    boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException;

    boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                      EnrolmentInfo.Status status) throws DeviceManagementException;

    boolean setStatus(String currentOwner, EnrolmentInfo.Status status) throws DeviceManagementException;

    void notifyOperationToDevices(Operation operation,
                                  List<DeviceIdentifier> deviceIds) throws DeviceManagementException;

    Activity addOperation(String type, Operation operation,
                          List<DeviceIdentifier> devices) throws OperationManagementException, InvalidDeviceException;

    List<? extends Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException;

    PaginationResult getOperations(DeviceIdentifier deviceId,
                                   PaginationRequest request) throws OperationManagementException;

    List<? extends Operation> getPendingOperations(
            DeviceIdentifier deviceId) throws OperationManagementException;

    Operation getNextPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException;

    void updateOperation(DeviceIdentifier deviceId, Operation operation) throws OperationManagementException;

    void deleteOperation(String type, int operationId) throws OperationManagementException;

    Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceId, int operationId)
            throws OperationManagementException;

    List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier identifier,
                                                             Operation.Status status)
            throws OperationManagementException, DeviceManagementException;

    Operation getOperation(String type, int operationId) throws OperationManagementException;

    Activity getOperationByActivityId(String activity) throws OperationManagementException;

    Activity getOperationByActivityIdAndDevice(String activity, DeviceIdentifier deviceId) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException;

    List<MonitoringOperation> getMonitoringOperationList(String deviceType);

    int getDeviceMonitoringFrequency(String deviceType);

    boolean isDeviceMonitoringEnabled(String deviceType);

    PolicyMonitoringManager getPolicyMonitoringManager(String deviceType);

    /**
     * Change device status.
     *
     * @param deviceIdentifier {@link DeviceIdentifier} object
     * @param newStatus        New status of the device
     * @return Whether status is changed or not
     * @throws DeviceManagementException on errors while trying to change device status
     */
    boolean changeDeviceStatus(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status newStatus)
            throws DeviceManagementException;
    
    /**
     * This will handle add and update of device type services.
     * @param deviceManagementService
     */
    void registerDeviceType(DeviceManagementService deviceManagementService) throws DeviceManagementException;

    /**
     * This retrieves the device type info for the given type
     * @param deviceType name of the type.
     * @throws DeviceManagementException
     */
    DeviceType getDeviceType(String deviceType) throws DeviceManagementException;

    /**
     * This retrieves the device type info for the given type
     * @throws DeviceManagementException
     */
    List<DeviceType> getDeviceTypes() throws DeviceManagementException;

    /**
     * This retrieves the device pull notification payload and passes to device type pull notification subscriber.
     * @throws PullNotificationExecutionFailedException
     */
    void notifyPullNotificationSubscriber(DeviceIdentifier deviceIdentifier, Operation operation)
            throws PullNotificationExecutionFailedException;

    List<Integer> getDeviceEnrolledTenants() throws DeviceManagementException;
}
