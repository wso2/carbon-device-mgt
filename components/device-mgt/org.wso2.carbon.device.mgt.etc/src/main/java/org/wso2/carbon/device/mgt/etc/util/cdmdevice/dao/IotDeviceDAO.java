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

package org.wso2.carbon.device.mgt.etc.util.cdmdevice.dao;

import org.wso2.carbon.device.mgt.etc.util.cdmdevice.dto.IotDevice;

import java.util.List;

/**
 * This class represents the key operations associated with persisting iot-device related
 * information.
 */
public interface IotDeviceDAO {

	/**
	 * Fetches a IotDevice from Iot database.
	 *
	 * @param iotDeviceId Id of the Iot-Device.
	 * @return IotDevice corresponding to given device-id.
	 * @throws IotDeviceManagementDAOException
	 */
	IotDevice getIotDevice(String iotDeviceId) throws IotDeviceManagementDAOException;

	/**
	 * Adds a new IotDevice to the MDM database.
	 *
	 * @param iotDevice IotDevice to be added.
	 * @return The status of the operation.
	 * @throws IotDeviceManagementDAOException
	 */
	boolean addIotDevice(IotDevice iotDevice) throws IotDeviceManagementDAOException;

	/**
	 * Updates IotDevice information in MDM database.
	 *
	 * @param iotDevice IotDevice to be updated.
	 * @return The status of the operation.
	 * @throws IotDeviceManagementDAOException
	 */
	boolean updateIotDevice(IotDevice iotDevice) throws IotDeviceManagementDAOException;

	/**
	 * Deletes a given IotDevice from MDM database.
	 *
	 * @param mblDeviceId Id of IotDevice to be deleted.
	 * @return The status of the operation.
	 * @throws IotDeviceManagementDAOException
	 */
	boolean deleteIotDevice(String mblDeviceId) throws IotDeviceManagementDAOException;

	/**
	 * Fetches all IotDevices from MDM database.
	 *
	 * @return List of IotDevices.
	 * @throws IotDeviceManagementDAOException
	 */
	List<IotDevice> getAllIotDevices() throws IotDeviceManagementDAOException;

}
