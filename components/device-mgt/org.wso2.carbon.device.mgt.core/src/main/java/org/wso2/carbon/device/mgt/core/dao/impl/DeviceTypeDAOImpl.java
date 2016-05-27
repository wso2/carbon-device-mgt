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
package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceTypeDAOImpl implements DeviceTypeDAO {

	@Override
	public void addDeviceType(DeviceType deviceType, int providerTenantId, boolean isSharedWithAllTenants)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO DM_DEVICE_TYPE (NAME,PROVIDER_TENANT_ID,SHARED_WITH_ALL_TENANTS) VALUES (?,?,?)");
			stmt.setString(1, deviceType.getName());
			stmt.setInt(2, providerTenantId);
			stmt.setBoolean(3, isSharedWithAllTenants);
			stmt.execute();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while registering the device type '" + deviceType.getName() + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public void updateDeviceType(DeviceType deviceType, int tenantId)
			throws DeviceManagementDAOException {
	}

	@Override
	public List<DeviceType> getDeviceTypes(int tenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE where PROVIDER_TENANT_ID =" +
							"? OR SHARED_WITH_ALL_TENANTS = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setBoolean(2, true);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceType deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
				deviceTypes.add(deviceType);
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<DeviceType> getDeviceTypesByProvider(int tenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE where PROVIDER_TENANT_ID =?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceType deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
				deviceTypes.add(deviceType);
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public List<DeviceType> getSharedDeviceTypes() throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE where  " +
							"SHARED_WITH_ALL_TENANTS = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, true);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceType deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
				deviceTypes.add(deviceType);
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public DeviceType getDeviceType(int id) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE WHERE ID = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			DeviceType deviceType = null;
			while (rs.next()) {
				deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
			}
			return deviceType;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetching the registered device type", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public DeviceType getDeviceType(String type, int tenantId) throws
															   DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DeviceType deviceType = null;
		try {
			conn = this.getConnection();
			String sql = "SELECT ID AS DEVICE_TYPE_ID FROM DM_DEVICE_TYPE WHERE (PROVIDER_TENANT_ID =? OR " +
							"SHARED_WITH_ALL_TENANTS = ?) AND NAME =?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setBoolean(2, true);
			stmt.setString(3, type);
			rs = stmt.executeQuery();
			if (rs.next()) {
				deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(type);
			}
			return deviceType;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetch device type id for device type '" + type + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public void removeDeviceType(String type, int tenantId) throws DeviceManagementDAOException {

	}

	private Connection getConnection() throws SQLException {
		return DeviceManagementDAOFactory.getConnection();
	}

}
