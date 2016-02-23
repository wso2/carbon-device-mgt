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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeviceTypeDAOImpl implements DeviceTypeDAO {

	@Override
	public int addDeviceType(DeviceType deviceType, int deviceTypeProviderTenantId,
							 boolean sharedWithAllTenants)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs;
		int deviceTypeId = -1;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO DM_DEVICE_TYPE (NAME,PROVIDER_TENANT_ID,SHARE_WITH_ALL_TENANTS) " +
							"VALUES (?,?,?)", new String[] {"id"});
			stmt.setString(1, deviceType.getName());
			stmt.setInt(2, deviceTypeProviderTenantId);
			stmt.setBoolean(3, sharedWithAllTenants);
			stmt.execute();
			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				deviceTypeId = rs.getInt(1);
			}
			return deviceTypeId;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException("Error occurred while registering the device type '" + deviceType.getName() + "'", e);
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
					"SELECT DT.ID AS DEVICE_TYPE_ID, DT.NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE " +
							"as  DT where DT.SHARE_WITH_ALL_TENANTS=TRUE UNION SELECT DTS.DEVICE_TYPE_ID,DT.NAME " +
							"FROM DM_SHARED_DEVICE_TYPE DTS LEFT JOIN DM_DEVICE_TYPE as DT ON DT" +
							".ID=DTS.DEVICE_TYPE_ID WHERE DTS.SHARED_TENANT_ID=?";
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
			throw new DeviceManagementDAOException(
					"Error occurred while fetching the registered device types", e);
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
			String sql =
					"SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE WHERE " +
							"ID = ?";
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
			String sql =
					"SELECT * FROM (SELECT DT.ID AS DEVICE_TYPE_ID, DT.NAME AS DEVICE_TYPE FROM " +
							"DM_DEVICE_TYPE as DT where DT.SHARE_WITH_ALL_TENANTS=TRUE UNION SELECT " +
							"DTS.DEVICE_TYPE_ID,DT.NAME FROM DM_SHARED_DEVICE_TYPE DTS LEFT JOIN " +
							"DM_DEVICE_TYPE" +
							" as DT ON DT.ID=DTS.DEVICE_TYPE_ID WHERE DTS.SHARED_TENANT_ID=?)as TB WHERE TB" +
							".DEVICE_TYPE =?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setString(2, type);
			rs = stmt.executeQuery();

			if (rs.next()) {
				deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(type);
			}
			return deviceType;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetch device type id for device type " +
							"'" + type + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public void removeDeviceType(String type, int tenantId) throws DeviceManagementDAOException {

	}

	@Override
	public void shareDeviceType(int id, int[] tenantId)
			throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		int batchSize = 10;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"INSERT INTO DM_SHARED_DEVICE_TYPE (DEVICE_TYPE_ID,SHARED_TENANT_ID) VALUES (?,?)");
			for (int i = 0; i < tenantId.length; i++) {
				stmt.setInt(1, id);
				stmt.setInt(2, tenantId[i]);

				stmt.addBatch();
				if (i % batchSize == 0) {
					stmt.executeBatch();
				}
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while registering the device type for the id " +
							"'" + id + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public List<DeviceType> getSharedDeviceType(int tenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<DeviceType> deviceTypes = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT DT.ID AS DEVICE_TYPE_ID, DT.NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE as" +
							"  DT where DT.SHARE_WITH_ALL_TENANTS=TRUE AND DT.PROVIDER_TENANT_ID != " +
							"? UNION SELECT DTS.DEVICE_TYPE_ID,DT.NAME FROM DM_SHARED_DEVICE_TYPE DTS LEFT" +
							" JOIN DM_DEVICE_TYPE as DT ON DT.ID=DTS.DEVICE_TYPE_ID WHERE DTS" +
							".SHARED_TENANT_ID=?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, tenantId);
			stmt.setInt(2, tenantId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				DeviceType deviceType = new DeviceType();
				deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
				deviceType.setName(rs.getString("DEVICE_TYPE"));
				deviceTypes.add(deviceType);
			}
			return deviceTypes;
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetching the registered device types", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	@Override
	public void removeSharedDeviceType(int id, int tenantId[]) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		int batchSize = 10;
		try {
			conn = this.getConnection();
			stmt = conn.prepareStatement(
					"DELETE FROM DM_SHARED_DEVICE_TYPE WHERE DEVICE_TYPE_ID=? AND SHARED_TENANT_ID=?;");
			for (int i = 0; i < tenantId.length; i++) {
				stmt.setInt(1, id);
				stmt.setInt(2, tenantId[i]);

				stmt.addBatch();
				if (i % batchSize == 0) {
					stmt.executeBatch();
				}
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while registering the device type for the id " +
							"'" + id + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
	}

	@Override
	public List<Integer> getSharedTenantId(String type, int providerTenantId) throws DeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> tenants = new ArrayList<>();
		try {
			conn = this.getConnection();
			String sql =
					"SELECT DISTINCT SHARED_TENANT_ID FROM DM_SHARED_DEVICE_TYPE WHERE DEVICE_TYPE_ID = " +
							"(SELECT ID from DM_DEVICE_TYPE where NAME= ?  AND " +
							"PROVIDER_TENANT_ID=?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, type);
			stmt.setInt(2, providerTenantId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				tenants.add(rs.getInt("SHARED_TENANT_ID"));
			}
			return tenants;

		} catch (SQLException e) {
			throw new DeviceManagementDAOException(
					"Error occurred while fetch device type id for device type " +
							"'" + type + "'", e);
		} finally {
			DeviceManagementDAOUtil.cleanupResources(stmt, rs);
		}
	}

	private Connection getConnection() throws SQLException {
		return DeviceManagementDAOFactory.getConnection();
	}

}
