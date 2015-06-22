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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;

import java.util.List;

/**
 * This class represents the key operations associated with persisting device related information.
 */
public interface DeviceDAO {

	void addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

	void updateDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException;

	void updateDeviceStatus(DeviceIdentifier deviceId, Status status, int tenantId) throws DeviceManagementDAOException;

	void deleteDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

	Device getDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException;

	List<Device> getDevices(int tenantId) throws DeviceManagementDAOException;

    List<Integer> getDeviceIds(List<DeviceIdentifier> devices,
                               int tenantId) throws DeviceManagementDAOException;

	/**
	 * @param type - The device type id.
	 * @return a list of devices based on the type id.
	 * @throws DeviceManagementDAOException
	 */
	List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException;

	/**
	 * Get the list of devices belongs to a user.
	 * @param username Requested user.
	 * @return List of devices of the user.
	 * @throws DeviceManagementDAOException
	 */
	List<Device> getDeviceListOfUser(String username, int tenantId) throws DeviceManagementDAOException;

    /**
     * Get the count of devices
     *
     * @return device count
     * @throws DeviceManagementDAOException
     */
    int getDeviceCount(int tenantId) throws DeviceManagementDAOException;

    /**
     * Get the list of devices that matches with the given device name.
     *
     * @param deviceName Name of the device
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementDAOException
     */
    List<Device> getDevicesByName(String deviceName, int tenantId) throws DeviceManagementDAOException;

    void addDeviceApplications(int id, Object applications)  throws DeviceManagementDAOException;
}
