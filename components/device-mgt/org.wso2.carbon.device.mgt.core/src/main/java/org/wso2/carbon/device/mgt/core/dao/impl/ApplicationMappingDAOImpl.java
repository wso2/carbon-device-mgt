/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.core.dao.ApplicationMappingDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationMappingDAOImpl implements ApplicationMappingDAO {

    @Override
    public int addApplicationMapping(int deviceId, int applicationId,
                                     int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int mappingId = -1;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_APPLICATION_MAPPING (DEVICE_ID, APPLICATION_ID, " +
                    "TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, applicationId);
            stmt.setInt(3, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                mappingId = rs.getInt(1);
            }
            return mappingId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<Integer> addApplicationMappings(int deviceId, List<Integer> applicationIds,
                                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> mappingIds = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_APPLICATION_MAPPING (DEVICE_ID, APPLICATION_ID, " +
                    "TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for (int applicationId : applicationIds) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

            rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                mappingIds.add(rs.getInt(1));
            }
            return mappingIds;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mappings", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void removeApplicationMapping(int deviceId, List<Integer> appIdList,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_ID = ? AND " +
                    "APPLICATION_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            for(Integer appId:appIdList){
                stmt.setInt(1, deviceId);
                stmt.setInt(2, appId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
       } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping", e);
        }finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

}
