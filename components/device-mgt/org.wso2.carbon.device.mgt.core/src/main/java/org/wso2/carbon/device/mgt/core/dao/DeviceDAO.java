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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the key operations associated with persisting device related information.
 */
public interface DeviceDAO {

    /**
     * This method is used to get the device count by device-type.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns the device count of given type.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByType(String type, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by user.
     *
     * @param username username of the user.
     * @param tenantId tenant id.
     * @return returns the device count of given user.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by device name (pattern).
     *
     * @param deviceName name of the device.
     * @param tenantId tenant id.
     * @return returns the device count of given user.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByName(String deviceName, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by status.
     *
     * @param status enrollment status.
     * @param tenantId tenant id.
     * @return returns the device count of given status.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByStatus(String status, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the device count by ownership.
     *
     * @param ownerShip Ownership of devices.
     * @param tenantId tenant id.
     * @return returns the device count of given ownership.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCountByOwnership(String ownerShip, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to add a device.
     *
     * @param typeId   device type id.
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of the persisted device record.
     * @throws DeviceManagementDAOException
     */
    int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to update a given device.
     *
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of updated device.
     * @throws DeviceManagementDAOException
     */
    boolean updateDevice(Device device, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to remove a device.
     *
     * @param deviceId id of the device that should be removed.
     * @param tenantId tenant id.
     * @return returns the id of removed device.
     * @throws DeviceManagementDAOException
     */
    int removeDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param ifModifiedSince last modified time.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, Date ifModifiedSince, int tenantId) throws
                                                                                            DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given device-identifier, enrollment status and tenant-id.
     *
     * @param deviceIdentifier device id.
     * @param status enrollment status.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status,int tenantId)
            throws DeviceManagementDAOException;

    /**
     *
     * @param deviceIdentifier device id.
     * @return HashMap
     * @throws DeviceManagementDAOException
     */
    HashMap<Integer, Device> getDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve a device of a given id.
     *
     * @param deviceId device id.
     * @param tenantId tenant id.
     * @return returns the device object.
     * @throws DeviceManagementDAOException
     */
    Device getDevice(int deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve all the devices of a given tenant.
     *
     * @param tenantId tenant id.
     * @return returns a list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of a given tenant as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @param tenantId tenant id.
     * @return returns paginated list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of a given tenant and type as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and search.
     * @param tenantId tenant id.
     * @return returns paginated list of devices of provided type.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByType(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve all the devices of a given tenant and device type.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns list of devices of provided type.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException;

    List<Device> getDevices(long timestamp, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given user.
     *
     * @param username user name.
     * @param tenantId tenant id.
     * @return returns list of devices of given user.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of given user of given device type.
     * @param username user name.
     * @param type device type.
     * @param tenantId tenant id.
     * @return
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(String username, String type, int tenantId) throws DeviceManagementDAOException;


    /**
     * This method is used to retrieve devices of a given user as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and search data.
     * @param tenantId tenant id.
     * @return returns paginated list of devices in which owner matches (search) with given username.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant.
     *
     * @param username user name.
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant.
     *
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant for the given search terms.
     *
     * @param request paginated request used to search devices.
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(PaginationRequest request, int tenantId) throws DeviceManagementDAOException;

	/**
	 * This method is used to retrieve the available device types of a given tenant.
	 *
	 * @return returns list of device types.
	 * @throws DeviceManagementDAOException
	 */
	List<DeviceType> getDeviceTypes() throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given device name.
     *
     * @param deviceName device name.
     * @param tenantId   tenant id.
     * @return returns list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByNameAndType(String deviceName, String type, int tenantId, int offset, int limit)
                                                                                throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given device name as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and device search info.
     * @param tenantId   tenant id.
     * @return returns paginated list of devices which name matches (search) given device-name.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByName(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to add an enrollment information of a given device.
     *
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of the enrollment.
     * @throws DeviceManagementDAOException
     */
    int addEnrollment(Device device, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to set the current enrollment status of given device and user.
     *
     * @param deviceId     device id.
     * @param currentOwner current user name.
     * @param status       device status.
     * @param tenantId     tenant id.
     * @return returns true if success.
     * @throws DeviceManagementDAOException
     */
    boolean setEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner, Status status,
                               int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get the status of current enrollment of a given user and device.
     *
     * @param deviceId     device id.
     * @param currentOwner device owner.
     * @param tenantId     tenant id.
     * @return returns current enrollment status.
     * @throws DeviceManagementDAOException
     */
    Status getEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner,
                              int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve current enrollment of a given device and user.
     *
     * @param deviceId    device id.
     * @param currentUser user name.
     * @param tenantId    tenant id.
     * @return returns EnrolmentInfo object.
     * @throws DeviceManagementDAOException
     */
    EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, String currentUser,
                               int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve current enrollment of a given device.
     *
     * @param deviceId    device id.
     * @param tenantId    tenant id.
     * @return returns EnrolmentInfo object.
     * @throws DeviceManagementDAOException
     */
    EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve current active enrollment of a given device and tenant id.
     *
     * @param deviceId    device id.
     * @param tenantId    tenant id.
     * @return returns EnrolmentInfo object.
     * @throws DeviceManagementDAOException
     */
    EnrolmentInfo getActiveEnrolment(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given enrollment status.
     *
     * @param status   enrollment status.
     * @param tenantId tenant id.
     * @return returns list of devices of given status.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given ownership as a paginated result.
     *
     * @param request  PaginationRequest object holding the data for pagination and device search.
     * @param tenantId tenant id.
     * @return returns list of devices of given ownership.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByOwnership(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given enrollment status as a paginated result
     *
     * @param request  PaginationRequest object holding the data for pagination
     * @param tenantId tenant id.
     * @return returns paginated list of devices of given status.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByStatus(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the enrollment id of a given device and status.
     *
     * @param deviceId device id.
     * @param status   enrollment status.
     * @param tenantId tenant id.
     * @return returns the id of current enrollment.
     * @throws DeviceManagementDAOException
     */
    int getEnrolmentByStatus(DeviceIdentifier deviceId, Status status,
                             int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the enrollment info of a given list of devices and status.
     *
     * @param deviceIds A list of device identifiers.
     * @param status    enrollment status.
     * @param tenantId  tenant id.
     * @return returns a list of enrolment info objects.
     * @throws DeviceManagementDAOException
     */
    List<EnrolmentInfo> getEnrolmentsByStatus(List<DeviceIdentifier> deviceIds, Status status,
                                              int tenantId) throws DeviceManagementDAOException;
}

