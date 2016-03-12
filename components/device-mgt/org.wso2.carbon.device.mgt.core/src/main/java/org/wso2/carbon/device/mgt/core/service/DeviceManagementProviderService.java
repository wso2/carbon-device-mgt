/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.email.EmailContext;

import java.util.List;

/**
 * Proxy class for all Device Management related operations that take the corresponding plugin type in
 * and resolve the appropriate plugin implementation
 */
public interface DeviceManagementProviderService extends OperationManager {

    List<Device> getAllDevices(String deviceType) throws DeviceManagementException;

    List<Device> getAllDevices() throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request        PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * devices.
     */
    PaginationResult getDevicesByType(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to retrieve all the devices with pagination support.
     *
     * @param request        PaginationRequest object holding the data for pagination
     * @return PaginationResult - Result including the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * devices.
     */
    PaginationResult getAllDevices(PaginationRequest request) throws DeviceManagementException;

    void sendEnrolmentInvitation(EmailContext emailContext) throws DeviceManagementException;

    void sendRegistrationEmail(EmailContext emailContext) throws DeviceManagementException;

    FeatureManager getFeatureManager(String deviceType) throws DeviceManagementException;

    /**
     * Proxy method to get the tenant configuration of a given platform.
     *
     * @param deviceType          Device platform
     * @return Tenant configuration settings of the particular tenant and platform.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * configuration.
     */
    TenantConfiguration getConfiguration(String deviceType) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user with paging information.
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    PaginationResult getDevicesOfUser(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices filtered by the ownership with paging information.
     *
     * @param request   PaginationRequest object holding the data for pagination
     * @return List of devices owned by a particular user along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    PaginationResult getDevicesByOwnership(PaginationRequest request) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName          Username of the user
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getDevicesOfUser(String userName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName          Role name of the users
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getAllDevicesOfRole(String roleName) throws DeviceManagementException;

    /**
     * Method to get the count of all types of devices.
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     * the devices
     */
    int getDeviceCount() throws DeviceManagementException;

    /**
     * Method to get the list of devices that matches with the given device name.
     *
     * @param deviceName    name of the device
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getDevicesByName(String deviceName) throws DeviceManagementException;

    /**
     * This method is used to retrieve list of devices that matches with the given device name with paging information.
     *
     * @param request PaginationRequest object holding the data for pagination
     * @return List of devices in given status along with the required parameters necessary to do pagination.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
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
     * device list
     */
    PaginationResult getDevicesByStatus(PaginationRequest request) throws DeviceManagementException;

    License getLicense(String deviceType, String languageCode) throws DeviceManagementException;

    void addLicense(String deviceType, License license) throws DeviceManagementException;

    boolean modifyEnrollment(Device device) throws DeviceManagementException;

    boolean enrollDevice(Device device) throws DeviceManagementException;

    TenantConfiguration getConfiguration() throws DeviceManagementException;

    boolean saveConfiguration(TenantConfiguration configuration) throws DeviceManagementException;

    boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException;

    Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException;

    Device getDevice(DeviceIdentifier deviceId, EnrolmentInfo.Status status) throws DeviceManagementException;

	List<DeviceType> getAvailableDeviceTypes() throws DeviceManagementException;

    boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException;

    boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException;

    boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException;

    boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                      EnrolmentInfo.Status status) throws DeviceManagementException;

    void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> deviceIds)throws DeviceManagementException;

}
