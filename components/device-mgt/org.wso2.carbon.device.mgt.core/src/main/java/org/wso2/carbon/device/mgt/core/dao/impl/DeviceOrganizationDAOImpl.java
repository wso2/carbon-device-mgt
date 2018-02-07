/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceOrganizationDAOException;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DeviceOrganizationDAOImpl implements DeviceOrganizationDAO {

    /**
     * Add a new device to organization
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     * @param parent     parent that device is child to in the network
     * @param pingMins   number of minutes since last ping from device
     * @param state      state of activity of device
     * @return true if device added successfully
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public boolean addDeviceOrganization(String deviceId, String deviceName, String parent,
                                         int pingMins, int state, int isGateway) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        boolean isSuccess = false;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DEVICE_ORGANIZATION_MAP(DEVICE_ID, DEVICE_NAME, DEVICE_PARENT, " +
                    "MINUTES_SINCE_LAST_PING, STATE, IS_GATEWAY) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            stmt.setString(2, deviceName);
            stmt.setString(3, parent);
            stmt.setInt(4, pingMins);
            stmt.setInt(5, state);
            stmt.setInt(6, isGateway);
            stmt.executeUpdate();
            isSuccess = true;
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while adding device '" + deviceId +
                    "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return isSuccess;
        }
    }

    /**
     * Remove a device from organization. Not implemented since device not removed from DB
     *
     * @param deviceId unique device identifier
     * @return true if device removed successfully
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public boolean removeDeviceOrganization(String deviceId) throws DeviceOrganizationDAOException {
        return false;
    }

    /**
     * Return device state by device path
     *
     * @param deviceId unique device identifier
     * @return device state
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public int getDeviceOrganizationStateById(String deviceId) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceState = -1;
        try {
            conn = this.getConnection();
            String sql = "SELECT STATE FROM DEVICE_ORGANIZATION_MAP WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceState = rs.getInt("STATE");
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred for device with ID: " + deviceId +
                    " while getting the device state.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return deviceState;
        }
    }

    /**
     * Get the device organization parent
     *
     * @param deviceId unique device identifier
     * @return the device organization parent
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public String getDeviceOrganizationParent(String deviceId) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String deviceOrgParent = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT DEVICE_PARENT FROM DEVICE_ORGANIZATION_MAP WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceOrgParent = rs.getString("DEVICE_PARENT");
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred for device with ID: " + deviceId +
                    " while getting the device organization path.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return deviceOrgParent;
        }
    }

    /**
     * This method allows us to check whether any device in the organization is a gateway
     *
     * @param deviceId unique device identifier
     * @return returns 1 if device is a gateway
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public int getDeviceOrganizationIsGateway(String deviceId) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int isGatewayState = -1;
        try {
            conn = this.getConnection();
            String sql = "SELECT IS_GATEWAY FROM DEVICE_ORGANIZATION_MAP WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                isGatewayState = rs.getInt("IS_GATEWAY");
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred for device with ID: " + deviceId +
                    " while checking if device is a gateway.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return isGatewayState;
        }
    }

    /**
     * This method allows to get the Children connected to a parent
     *
     * @param parentId unique device identifier, in this case the parents' ID
     * @return DeviceOrganizationMetadataHolder ArrayList with IDs of children
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public List<DeviceOrganizationMetadataHolder> getChildrenByParentId(String parentId)
            throws DeviceOrganizationDAOException {
        List<DeviceOrganizationMetadataHolder> children = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceOrganizationMetadataHolder deviceMetadataHolder;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM DEVICE_ORGANIZATION_MAP WHERE DEVICE_PARENT = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, parentId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceMetadataHolder = this.loadOrganization(rs);
                children.add(deviceMetadataHolder);
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred for device with ID: " + parentId +
                    " while retrieving children.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return children;
        }
    }


    /**
     * This method allows to get the Device Organization name by ID
     *
     * @param deviceId unique device identifier
     * @return returns the Device Name
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public String getDeviceOrganizationNameById(String deviceId) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String deviceOrgName = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT DEVICE_NAME FROM DEVICE_ORGANIZATION_MAP WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceOrgName = rs.getString("DEVICE_NAME");
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred for device with ID: " + deviceId +
                    " while getting the device organization Name.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return deviceOrgName;
        }
    }

    /**
     * Get all the devices in the Device Organization
     *
     * @return arraylist with all devices in Organization
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public List<DeviceOrganizationMetadataHolder> getDevicesInOrganization() throws DeviceOrganizationDAOException {
        List<DeviceOrganizationMetadataHolder> devicesInOrganization = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceOrganizationMetadataHolder deviceMetadataHolder;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM DEVICE_ORGANIZATION_MAP";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceMetadataHolder = this.loadOrganization(rs);
                devicesInOrganization.add(deviceMetadataHolder);
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while obtaining device list in organization", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return devicesInOrganization;
        }
    }

    /**
     * Update the device organization name
     *
     * @param deviceId   unique device identifier
     * @param deviceName identifier name given to device
     * @return updated device name if updated successfully
     * @throws DeviceManagementDAOException
     */
    @Override
    public String updateDeviceOrganizationName(String deviceId, String deviceName)
            throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        String updatedName = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DEVICE_ORGANIZATION_MAP SET DEVICE_NAME = ? WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, deviceName);
            stmt.setString(2, deviceId);
            rows = stmt.executeUpdate();
            if (rows > 0) {
                updatedName = deviceName;
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while updating Name of device '" +
                    deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return updatedName;
        }
    }

    /**
     * Update device organization path
     *
     * @param deviceId unique device identifier
     * @param parent   parent that device is child to in the network
     * @return the updated device path
     * @throws DeviceManagementDAOException
     */
    @Override
    public String updateDeviceOrganizationParent(String deviceId, String parent) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        String updatedParent = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DEVICE_ORGANIZATION_MAP SET DEVICE_PARENT = ? WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"id "});
            stmt.setString(1, parent);
            stmt.setString(2, deviceId);
            rows = stmt.executeUpdate();
            if (rows > 0) {
                updatedParent = parent;
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while path of updating device '" +
                    deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return updatedParent;
        }
    }

    /**
     * This method allows to update the no. of minutes since last contact with device
     *
     * @param deviceId    unique device identifier
     * @param newPingMins number of minutes since last ping from device
     * @return
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public int updateDevicePingMins(String deviceId, int newPingMins) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        int updatedPingMins = -1;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DEVICE_ORGANIZATION_MAP SET MINUTES_SINCE_LAST_PING = ? WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, newPingMins);
            stmt.setString(2, deviceId);
            rows = stmt.executeUpdate();
            if (rows > 0) {
                updatedPingMins = newPingMins;
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while " +
                    "updating No. of mins since last ping of device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return updatedPingMins;
        }
    }

    /**
     * This method allows to update the Device Organization state
     *
     * @param deviceId unique device identifier
     * @param newState tate of activity of device
     * @return will return -1 if there's an error. Else the new value
     * @throws DeviceOrganizationDAOException
     */
    @Override
    public int updateDeviceOrganizationState(String deviceId, int newState) throws DeviceOrganizationDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int rows;
        int updatedState = -1;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DEVICE_ORGANIZATION_MAP SET STATE = ? WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
            stmt.setInt(1, newState);
            stmt.setString(2, deviceId);
            rows = stmt.executeUpdate();
            if (rows > 0) {
                updatedState = newState;
            }
        } catch (SQLException e) {
            throw new DeviceOrganizationDAOException("Error occurred while updating state of device '" +
                    deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return updatedState;
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    //This method is used to load the contents of one record in table to an object in the array
    private DeviceOrganizationMetadataHolder loadOrganization(ResultSet rs) throws SQLException {
        DeviceOrganizationMetadataHolder metadataHolder = new DeviceOrganizationMetadataHolder();
        metadataHolder.setDeviceId(rs.getString("DEVICE_ID"));
        metadataHolder.setDeviceName(rs.getString("DEVICE_NAME"));
        metadataHolder.setParent(rs.getString("DEVICE_PARENT"));
        metadataHolder.setPingMins(rs.getInt("MINUTES_SINCE_LAST_PING"));
        metadataHolder.setState(rs.getInt("STATE"));
        metadataHolder.setIsGateway(rs.getInt("IS_GATEWAY"));
        return metadataHolder;
    }
}
