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
     * Method to retrieve all the devices registered in the system.
     *
     * @return List of registered devices.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    List<Device> getAllDevices() throws DeviceManagementException;

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
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   devices.
     */
    PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException;

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
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request) throws DeviceManagementException;

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
     * This method returns the list of device owned by a user of given device type.
     * @param userName user name.
     * @param deviceType device type name
     * @return
     * @throws DeviceManagementException
     */
    List<Device> getDevicesOfUser(String userName, String deviceType) throws DeviceManagementException;

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

    /**
     * Method to get the list of devices that matches with the given device name.
     *
     * @param deviceName name of the device
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    List<Device> getDevicesByNameAndType(String deviceName, String type, int offset, int limit) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByName(PaginationRequest request) throws DeviceManagementException;

    void updateDeviceEnrolmentInfo(Device device, EnrolmentInfo.Status active) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status.
     *
     * @param status Device status
     * @return List of devices
     * @throws DeviceManagementException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices based on the device status with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     *                                   device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException;

    /**
     * This method is used to check whether the device is enrolled with the give user.
     *
     * @param deviceId identifier of the device that needs to be checked against the user.
     * @param user username of the device owner.
     *
     * @return true if the user owns the device else will return false.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the device.
     */
    boolean isEnrolled(DeviceIdentifier deviceId, String user) throws DeviceManagementException;

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

    /**
     * Returns the device of specified id.
     * @param deviceId device Id
     * @return Device returns null when device is not avaialble.
     * @throws DeviceManagementException
     */
    Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    Device getDevice(DeviceIdentifier deviceId, Date since) throws DeviceManagementException;

    HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException;

    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException;

    List<String> getAvailableDeviceTypes() throws DeviceManagementException;

    boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException;

    boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException;

    boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                      EnrolmentInfo.Status status) throws DeviceManagementException;

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

    List<Activity> getActivitiesUpdatedAfter(long timestamp) throws OperationManagementException;

    List<Activity> getActivitiesUpdatedAfter(long timestamp, int limit, int offset) throws OperationManagementException;

    int getActivityCountUpdatedAfter(long timestamp) throws OperationManagementException;

    List<MonitoringOperation> getMonitoringOperationList(String deviceType);

    int getDeviceMonitoringFrequency(String deviceType);

    boolean isDeviceMonitoringEnabled(String deviceType);

    PolicyMonitoringManager getPolicyMonitoringManager(String deviceType);

}
