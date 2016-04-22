/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GadgetDataServiceDAOImpl implements GadgetDataServiceDAO {
    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(GadgetDataServiceDAOImpl.class);

    @Override
    public int getTotalDeviceCount(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public int getFeatureNonCompliantDeviceCount(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 2;
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public int getActiveDeviceCount() throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        Map<String, Object> filters = new HashMap<>();
        filters.put("CONNECTIVITY_STATUS", "ACTIVE");
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public int getInactiveDeviceCount() throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        Map<String, Object> filters = new HashMap<>();
        filters.put("CONNECTIVITY_STATUS", "INACTIVE");
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public int getRemovedDeviceCount() throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        Map<String, Object> filters = new HashMap<>();
        filters.put("CONNECTIVITY_STATUS", "REMOVED");
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public int getNonCompliantDeviceCount() throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        Map<String, Object> filters = new HashMap<>();
        filters.put("IS_COMPLIANT", 0);
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public Map<String, Integer> getNonCompliantDeviceCountsByFeatures() throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredNonCompliantDeviceCountsByFeatures = new HashMap<>();
        try {
            con = this.getConnection();
            String sql = "SELECT FEATURE_CODE, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 " +
                    "WHERE TENANT_ID = ? GROUP BY FEATURE_CODE";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                filteredNonCompliantDeviceCountsByFeatures.
                    put(rs.getString("FEATURE_CODE"), rs.getInt("DEVICE_COUNT"));
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while executing a selection query to the database", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredNonCompliantDeviceCountsByFeatures;
    }

    @Override
    public int getUnmonitoredDeviceCount() throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        Map<String, Object> filters = new HashMap<>();
        filters.put("POLICY_ID", -1);
        return this.getDeviceCount(filteringViewID, filters);
    }

    @Override
    public Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        return this.getDeviceCountsByPlatforms(filteringViewID, filters);
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 2;
        return this.getDeviceCountsByPlatforms(filteringViewID, filters);
    }

    @Override
    public Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        return this.getDeviceCountsByOwnershipTypes(filteringViewID, filters);
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 2;
        return this.getDeviceCountsByOwnershipTypes(filteringViewID, filters);
    }

    @Override
    public List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 1;
        return this.getDevicesWithDetails(filteringViewID, filters);
    }

    @Override
    public List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(Map<String, Object> filters) throws GadgetDataServiceDAOException {
        int filteringViewID = 2;
        return this.getDevicesWithDetails(filteringViewID, filters);
    }

    private int getDeviceCount(int filteringViewID, Map<String, Object> filters) throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int filteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql;
            if (filteringViewID == 1) {
                sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ?";
            } else {
                // if filteringViewID == 2
                sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 WHERE TENANT_ID = ?";
            }
            // appending filters to support advanced filtering options
            // [1] appending filter columns
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values
            stmt.setInt(1, tenantId);
            if (filters != null && filters.values().size() > 0) {
                int i = 2;
                for (Object value : filters.values()) {
                    if (value instanceof Integer) {
                        stmt.setInt(i, (Integer) value);
                    } else if (value instanceof String) {
                        stmt.setString(i, (String) value);
                    }
                    i++;
                }
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                filteredDeviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while executing a selection query to the database", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCount;
    }

    private Map<String, Integer> getDeviceCountsByPlatforms(int filteringViewID, Map<String, Object> filters) throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByPlatforms = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns
            if (filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            if (filteringViewID == 1) {
                sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ? " +
                        advancedSqlFiltering + "GROUP BY PLATFORM";
            } else {
                // if filteringViewID == 2
                sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 WHERE TENANT_ID = ? " +
                        advancedSqlFiltering + "GROUP BY PLATFORM";
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values
            stmt.setInt(1, tenantId);
            int i = 2;
            for (Object value : filters.values()) {
                if (value instanceof Integer) {
                    stmt.setInt(i, (Integer) value);
                } else if (value instanceof String) {
                    stmt.setString(i, (String) value);
                }
                i++;
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                filteredDeviceCountsByPlatforms.put(rs.getString("PLATFORM"), rs.getInt("DEVICE_COUNT"));
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while executing a selection query to the database", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByPlatforms;
    }

    private Map<String, Integer> getDeviceCountsByOwnershipTypes(int filteringViewID, Map<String, Object> filters) throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByOwnershipTypes = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns
            if (filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            if (filteringViewID == 1) {
                sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ? " +
                        advancedSqlFiltering + "GROUP BY OWNERSHIP";
            } else {
                // if filteringViewID == 2
                sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 WHERE TENANT_ID = ? " +
                        advancedSqlFiltering + "GROUP BY OWNERSHIP";
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values
            stmt.setInt(1, tenantId);
            int i = 2;
            for (Object value : filters.values()) {
                if (value instanceof Integer) {
                    stmt.setInt(i, (Integer) value);
                } else if (value instanceof String) {
                    stmt.setString(i, (String) value);
                }
                i++;
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                filteredDeviceCountsByOwnershipTypes.put(rs.getString("PLATFORM"), rs.getInt("DEVICE_COUNT"));
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while executing a selection query to the database", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByOwnershipTypes;
    }

    private List<Map<String, Object>> getDevicesWithDetails(int filteringViewID, Map<String, Object> filters) throws GadgetDataServiceDAOException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Object> filteredDeviceWithDetails = new HashMap<>();
        List<Map<String, Object>> filteredDevicesWithDetails = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql;
            if (filteringViewID == 1) {
                sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_1 WHERE TENANT_ID = ?";
            } else {
                // if filteringViewID == 2
                sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_2 WHERE TENANT_ID = ?";
            }
            // appending filters to support advanced filtering options
            // [1] appending filter columns
            if (filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values
            stmt.setInt(1, tenantId);
            int i = 2;
            for (Object value : filters.values()) {
                if (value instanceof Integer) {
                    stmt.setInt(i, (Integer) value);
                } else if (value instanceof String) {
                    stmt.setString(i, (String) value);
                }
                i++;
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                filteredDeviceWithDetails.put("Device-ID", rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.put("Platform", rs.getString("PLATFORM"));
                filteredDeviceWithDetails.put("Ownership", rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.put("Connectivity-Details", rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
        } catch (SQLException e) {
            throw new GadgetDataServiceDAOException("Error occurred while executing a selection query to the database", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDevicesWithDetails;
    }

    private Connection getConnection() throws SQLException {
        return GadgetDataServiceDAOFactory.getConnection();
    }

}
