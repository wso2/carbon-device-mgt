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

import com.google.gson.Gson;
import org.wso2.carbon.device.mgt.common.DeviceType;
import org.wso2.carbon.device.mgt.common.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Device Type
 */
public class DeviceTypeDAOImpl implements DeviceTypeDAO {

    @Override
    public DeviceType addDeviceType(DeviceType deviceType) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs;
        int deviceTypeId = -1;
        try {
            conn = DeviceManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement(
                    "INSERT INTO DM_DEVICE_TYPE (NAME,DEVICE_TYPE_META) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            stmt.setString(1, deviceType.getName());
            String deviceMeta = null;
            if (deviceType.getDeviceTypeMetaDefinition() != null) {
                Gson gson = new Gson();
                deviceMeta = gson.toJson(deviceType.getDeviceTypeMetaDefinition());
            }
            stmt.setString(2, deviceMeta);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                deviceTypeId = rs.getInt(1);
            }
            deviceType.setId(deviceTypeId);
            return deviceType;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException(
                    "Error occurred while registering the device type '" + deviceType.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceType updateDeviceType(DeviceType deviceType) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DeviceManagementDAOFactory.getConnection();
            stmt = conn.prepareStatement("UPDATE DM_DEVICE_TYPE SET DEVICE_TYPE_META = ? WHERE NAME = ?");
            String deviceMeta = null;
            if (deviceType.getDeviceTypeMetaDefinition() != null) {
                Gson gson = new Gson();
                deviceMeta = gson.toJson(deviceType.getDeviceTypeMetaDefinition());
            }
            stmt.setString(1, deviceMeta);
            stmt.setString(3, deviceType.getName());
            stmt.executeUpdate();
            return deviceType;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating device type'" +
                                                           deviceType.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<DeviceType> getDeviceTypes() throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DeviceType> deviceTypes = new ArrayList<>();
        try {
            conn = DeviceManagementDAOFactory.getConnection();
            String sql = "SELECT ID AS DEVICE_TYPE_ID, NAME AS DEVICE_TYPE, DEVICE_TYPE_META, LAST_UPDATED_TIMESTAMP " +
                            "FROM DM_DEVICE_TYPE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                DeviceType deviceType = new DeviceType();
                deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
                deviceType.setName(rs.getString("DEVICE_TYPE"));
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta,
                                                                         DeviceTypeMetaDefinition.class)
                    );
                }
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
            conn = DeviceManagementDAOFactory.getConnection();
            String sql = "SELECT ID AS DEVICE_TYPE_ID, DEVICE_TYPE_META, NAME AS DEVICE_TYPE FROM DM_DEVICE_TYPE WHERE "
            + "ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            DeviceType deviceType = null;
            while (rs.next()) {
                deviceType = new DeviceType();
                deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
                deviceType.setName(rs.getString("DEVICE_TYPE"));
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }

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
    public DeviceType getDeviceType(String type) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceType deviceType = null;
        try {
            conn = DeviceManagementDAOFactory.getConnection();
            String sql = "SELECT ID AS DEVICE_TYPE_ID, DEVICE_TYPE_META FROM DM_DEVICE_TYPE WHERE AND NAME =?";
            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, true);
            stmt.setString(2, type);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceType = new DeviceType();
                deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
                deviceType.setName(type);
                String devicetypeMeta = rs.getString("DEVICE_TYPE_META");
                if (devicetypeMeta != null && devicetypeMeta.length() > 0) {
                    Gson gson = new Gson();
                    deviceType.setDeviceTypeMetaDefinition(gson.fromJson(devicetypeMeta
                            , DeviceTypeMetaDefinition.class));
                }
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
    public void removeDeviceType(String type) throws DeviceManagementDAOException {
        //do nothing
    }

}
