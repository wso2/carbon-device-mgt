/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.util.DeviceTypeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements CRUD for Devices.
 */
public class DeviceTypePluginDAO {

	private static final Log log = LogFactory.getLog(DeviceTypePluginDAO.class);
	private DeviceTypePluginDAOManager deviceTypePluginDAOManager;
	public DeviceTypePluginDAO(DeviceManagementConfiguration deviceManagementConfiguration) {
		deviceTypePluginDAOManager = new DeviceTypePluginDAOManager(deviceManagementConfiguration);
	}

	public Device getDevice(String deviceId) throws DeviceTypeMgtPluginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		Device device = null;
		ResultSet resultSet = null;
		try {
			conn = deviceTypePluginDAOManager.getConnection();
			String selectDBQuery =
					"SELECT VIRTUAL_FIREALARM_DEVICE_ID, DEVICE_NAME" +
					" FROM VIRTUAL_FIREALARM_DEVICE WHERE VIRTUAL_FIREALARM_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, deviceId);
			resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				device = new Device();
				if (log.isDebugEnabled()) {
					log.debug("Virtual Firealarm device " + deviceId + " data has been fetched from " +
							  "Virtual Firealarm database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Virtual Firealarm device : '" + deviceId + "'";
			log.error(msg, e);
			throw new DeviceTypeMgtPluginException(msg, e);
		} finally {
			DeviceTypeUtils.cleanupResources(stmt, resultSet);
			deviceTypePluginDAOManager.closeConnection();
		}

		return device;
	}

	public boolean addDevice(Device device) throws DeviceTypeMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = deviceTypePluginDAOManager.getConnection();
			String createDBQuery =
					"INSERT INTO VIRTUAL_FIREALARM_DEVICE(VIRTUAL_FIREALARM_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, device.getDeviceIdentifier());
			stmt.setString(2, device.getName());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Virtual Firealarm device " + device.getDeviceIdentifier() + " data has been" +
							  " added to the Virtual Firealarm database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Virtual Firealarm device '" +
						 device.getDeviceIdentifier() + "' to the Virtual Firealarm db.";
			log.error(msg, e);
			throw new DeviceTypeMgtPluginException(msg, e);
		} finally {
			DeviceTypeUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean updateDevice(Device device) throws DeviceTypeMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DeviceTypePluginDAOManager.getConnection();
			String updateDBQuery =
					"UPDATE VIRTUAL_FIREALARM_DEVICE SET  DEVICE_NAME = ? WHERE VIRTUAL_FIREALARM_DEVICE_ID = ?";

			stmt = conn.prepareStatement(updateDBQuery);
			stmt.setString(1, device.getName());
			stmt.setString(2, device.getDeviceIdentifier());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Virtualm Firealarm device " + device.getDeviceIdentifier() + " data has been" +
							  " modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Virtual Firealarm device '" +
						 device.getDeviceIdentifier() + "' data.";
			log.error(msg, e);
			throw new DeviceTypeMgtPluginException(msg, e);
		} finally {
			DeviceTypeUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean deleteDevice(String iotDeviceId) throws DeviceTypeMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = deviceTypePluginDAOManager.getConnection();
			String deleteDBQuery = "DELETE FROM VIRTUAL_FIREALARM_DEVICE WHERE VIRTUAL_FIREALARM_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, iotDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Virtual Firealarm device " + iotDeviceId + " data has deleted" +
							  " from the Virtual Firealarm database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting Virtual Firealarm device " + iotDeviceId;
			log.error(msg, e);
			throw new DeviceTypeMgtPluginException(msg, e);
		} finally {
			DeviceTypeUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<Device> getAllDevices() throws DeviceTypeMgtPluginException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Device device;
		List<Device> devices = new ArrayList<>();
		try {
			conn = deviceTypePluginDAOManager.getConnection();
			String selectDBQuery =
					"SELECT VIRTUAL_FIREALARM_DEVICE_ID, DEVICE_NAME FROM VIRTUAL_FIREALARM_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				device = new Device();
				device.setDeviceIdentifier(resultSet.getString(VirtualFireAlarmConstants.DEVICE_PLUGIN_DEVICE_ID));
				device.setName(resultSet.getString(VirtualFireAlarmConstants.DEVICE_PLUGIN_DEVICE_NAME));
				devices.add(device);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Virtual Firealarm device details have fetched from Firealarm database.");
			}
			return devices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Virtual Firealarm device data'";
			log.error(msg, e);
			throw new DeviceTypeMgtPluginException(msg, e);
		} finally {
			DeviceTypeUtils.cleanupResources(stmt, resultSet);
			deviceTypePluginDAOManager.closeConnection();
		}
	}
}