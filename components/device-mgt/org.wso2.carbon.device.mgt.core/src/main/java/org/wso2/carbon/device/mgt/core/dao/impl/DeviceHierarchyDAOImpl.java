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

import org.wso2.carbon.device.mgt.common.DeviceHierarchyMetadataHolder;
import org.wso2.carbon.device.mgt.core.dao.DeviceHierarchyDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceHierarchyDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceHierarchyDAOImpl implements DeviceHierarchyDAO {
    @Override
    public boolean addDeviceToHierarchy(String deviceId, String deviceParent, int isParent, int tenantId)
            throws DeviceHierarchyDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        boolean isSuccess = false;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_HIERARCHY(DEVICE_ID, DEVICE_PARENT, IS_PARENT, TENANT_ID) " +
                    "VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId);
            stmt.setString(2, deviceParent);
            stmt.setInt(3, isParent);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();
            isSuccess = true;
        } catch (SQLException e) {
            throw new DeviceHierarchyDAOException("Error occurred while adding device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            return isSuccess;
        }
    }

    @Override
    public boolean removeDeviceFromHierarchy(String deviceId) throws DeviceHierarchyDAOException {
        return false;
    }

    @Override
    public List<DeviceHierarchyMetadataHolder> getDevicesInHierarchy() throws DeviceHierarchyDAOException {
        List<DeviceHierarchyMetadataHolder> devicesInHierarchy = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceHierarchyMetadataHolder deviceHierarchyMetadataHolder;
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM DM_DEVICE_HIERARCHY";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceHierarchyMetadataHolder = this.loadHierarchy(rs);
                devicesInHierarchy.add(deviceHierarchyMetadataHolder);
            }
        } catch (SQLException e) {
            throw new DeviceHierarchyDAOException("Error occurred while obtaining devices in hierarchy", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
            return devicesInHierarchy;
        }
    }

    @Override
    public boolean updateDeviceHierarchyParent(String deviceId, String newParentId)
            throws DeviceHierarchyDAOException {
        return false;
    }

    @Override
    public boolean updateDeviceHierarchyParencyState(String deviceId, int newParencyState)
            throws DeviceHierarchyDAOException {
        return false;
    }

    @Override
    public boolean updateDeviceHierarchyTenantId(String deviceId, int newTenantId) throws DeviceHierarchyDAOException {
        return false;
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    //This method is used to load the contents of one record in table to an object in the array
    private DeviceHierarchyMetadataHolder loadHierarchy(ResultSet rs) throws SQLException {
        DeviceHierarchyMetadataHolder metadataHolder = new DeviceHierarchyMetadataHolder();
        metadataHolder.setId(rs.getInt("ID"));
        metadataHolder.setDeviceId(rs.getString("DEVICE_ID"));
        metadataHolder.setDeviceParent(rs.getString("DEVICE_PARENT"));
        metadataHolder.setIsParent(rs.getInt("IS_PARENT"));
        metadataHolder.setTenantId(rs.getInt("TENANT_ID"));
        return metadataHolder;
    }
}
