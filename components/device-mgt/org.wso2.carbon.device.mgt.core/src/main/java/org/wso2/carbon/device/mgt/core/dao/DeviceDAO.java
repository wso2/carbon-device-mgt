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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.HashMap;
import java.util.List;

/**
 * This class represents the key operations associated with persisting device related information.
 */
public interface DeviceDAO {

    /**
     * This method is used to add a device.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns the device count of given type.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(String type, int tenantId) throws DeviceManagementDAOException;

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
     * @param typeId   device type id.
     * @param device   device object.
     * @param tenantId tenant id.
     * @return returns the id of updated device.
     * @throws DeviceManagementDAOException
     */
    boolean updateDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

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
     * @param index start index of result set.
     * @param limit number of records to be returned.
     * @param tenantId tenant id.
     * @return returns a PaginationResult including the requested data.
     * @throws DeviceManagementDAOException
     */
    PaginationResult getDevices(int index, int limit, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the devices of a given tenant and type as a paginated result.
     *
     * @param type device type.
     * @param index start index of result set.
     * @param limit number of records to be returned.
     * @param tenantId tenant id.
     * @return returns a PaginationResult including the requested data.
     * @throws DeviceManagementDAOException
     */
    PaginationResult getDevices(String type, int index, int limit, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve all the devices of a given tenant and device type.
     *
     * @param type device type.
     * @param tenantId tenant id.
     * @return returns list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve devices of a given user.
     *
     * @param username user name.
     * @param tenantId tenant id.
     * @return returns list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the device count of a given tenant.
     *
     * @param tenantId tenant id.
     * @return returns the device count.
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(int tenantId) throws DeviceManagementDAOException;

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
    List<Device> getDevicesByName(String deviceName, int tenantId) throws DeviceManagementDAOException;

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
     * This method is used to retrieve devices of a given enrollment status.
     *
     * @param status   enrollment status.
     * @param tenantId tenant id.
     * @return returns list of devices.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

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

