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

package org.wso2.carbon.device.mgt.analytics.dashboard.dao.impl;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAO;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.*;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GadgetDataServiceDAOImpl implements GadgetDataServiceDAO {

    @Override
    public int getTotalDeviceCount() throws SQLException {
        try {
            return this.getDeviceCount(null);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int getActiveDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setConnectivityStatus(ConnectivityStatus.active.toString());
        try {
            return this.getDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int getInactiveDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setConnectivityStatus(ConnectivityStatus.inactive.toString());
        try {
            return this.getDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int getRemovedDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setConnectivityStatus(ConnectivityStatus.removed.toString());
        try {
            return this.getDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int getNonCompliantDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setPotentialVulnerability(PotentialVulnerability.non_compliant.toString());
        try {
            return this.getDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int getUnmonitoredDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setPotentialVulnerability(PotentialVulnerability.unmonitored.toString());
        try {
            return this.getDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                                                  throws InvalidParameterValueException, SQLException {

        if (startIndex < 0) {
            throw new InvalidParameterValueException("Start index should be equal to 0 or greater than that.");
        }

        if (resultCount < 5) {
            throw new InvalidParameterValueException("Result count should be equal to 5 or greater than that.");
        }

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Map<String, Object>> filteredNonCompliantDeviceCountsByFeatures = new ArrayList<>();
        int totalRecordsCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT FEATURE_CODE, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 " +
                "WHERE TENANT_ID = ? GROUP BY FEATURE_CODE ORDER BY DEVICE_COUNT DESC LIMIT ?, ?";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, startIndex);
            stmt.setInt(3, resultCount);

            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            Map<String, Object> filteredNonCompliantDeviceCountByFeature;
            while (rs.next()) {
                filteredNonCompliantDeviceCountByFeature = new HashMap<>();
                filteredNonCompliantDeviceCountByFeature.put("FEATURE_CODE", rs.getString("FEATURE_CODE"));
                filteredNonCompliantDeviceCountByFeature.put("DEVICE_COUNT", rs.getInt("DEVICE_COUNT"));
                filteredNonCompliantDeviceCountsByFeatures.add(filteredNonCompliantDeviceCountByFeature);
            }
            // fetching total records count
            sql = "SELECT COUNT(FEATURE_CODE) AS NON_COMPLIANT_FEATURE_COUNT FROM " +
                "(SELECT DISTINCT FEATURE_CODE FROM DEVICES_VIEW_2 WHERE TENANT_ID = ?)";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);

            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                totalRecordsCount = rs.getInt("NON_COMPLIANT_FEATURE_COUNT");
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        PaginationResult paginationResult = new PaginationResult();
        paginationResult.setData(filteredNonCompliantDeviceCountsByFeatures);
        paginationResult.setRecordsTotal(totalRecordsCount);
        return paginationResult;
    }

    public int getDeviceCount(FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int filteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ?";
            // appending filters to support advanced filtering options
            // [1] appending filter columns
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
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
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCount;
    }

    public int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode, FilterSet filterSet)
                                                        throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int filteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 " +
                "WHERE TENANT_ID = ? AND FEATURE_CODE = ?";
            // appending filters to support advanced filtering options
            // [1] appending filter columns
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);
            if (filters != null && filters.values().size() > 0) {
                int i = 3;
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
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCount;
    }

    public Map<String, Integer> getDeviceCountsByPlatforms(FilterSet filterSet)
                                                           throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByPlatforms = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ? " +
                advancedSqlFiltering + "GROUP BY PLATFORM";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
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
                filteredDeviceCountsByPlatforms.put(rs.getString("PLATFORM"), rs.getInt("DEVICE_COUNT"));
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByPlatforms;
    }

    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                        FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByPlatforms = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 WHERE TENANT_ID = ? " +
                "AND FEATURE_CODE = ? " + advancedSqlFiltering + "GROUP BY PLATFORM";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);
            if (filters != null && filters.values().size() > 0) {
                int i = 3;
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
                filteredDeviceCountsByPlatforms.put(rs.getString("PLATFORM"), rs.getInt("DEVICE_COUNT"));
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByPlatforms;
    }

    public Map<String, Integer> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                                                throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByOwnershipTypes = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT OWNERSHIP, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ? " +
                advancedSqlFiltering + "GROUP BY OWNERSHIP";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
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
                filteredDeviceCountsByOwnershipTypes.put(rs.getString("OWNERSHIP"), rs.getInt("DEVICE_COUNT"));
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByOwnershipTypes;
    }

    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                            FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, Integer> filteredDeviceCountsByOwnershipTypes = new HashMap<>();
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT OWNERSHIP, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 WHERE TENANT_ID = ? " +
                "AND FEATURE_CODE = ? " + advancedSqlFiltering + "GROUP BY OWNERSHIP";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);
            if (filters != null && filters.values().size() > 0) {
                int i = 3;
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
                filteredDeviceCountsByOwnershipTypes.put(rs.getString("OWNERSHIP"), rs.getInt("DEVICE_COUNT"));
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByOwnershipTypes;
    }

    public PaginationResult getDevicesWithDetails(FilterSet filterSet, int startIndex, int resultCount)
                                                                throws InvalidParameterValueException, SQLException {

        if (startIndex < 0) {
            throw new InvalidParameterValueException("Start index should be equal to 0 or greater than that.");
        }

        if (resultCount < 5) {
            throw new InvalidParameterValueException("Result count should be equal to 5 or greater than that.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Map<String, Object>> filteredDevicesWithDetails = new ArrayList<>();
        int totalRecordsCount = 0;
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_1 " +
                  "WHERE TENANT_ID = ? " + advancedSqlFiltering + "ORDER BY DEVICE_ID ASC LIMIT ?, ?";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
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
                stmt.setInt(i, startIndex);
                stmt.setInt(++i, resultCount);
            } else {
                stmt.setInt(2, startIndex);
                stmt.setInt(3, resultCount);
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            Map<String, Object> filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new HashMap<>();
                filteredDeviceWithDetails.put("device-id", rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.put("platform", rs.getString("PLATFORM"));
                filteredDeviceWithDetails.put("ownership", rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.put("connectivity-details", rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
            // fetching total records count
            sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 WHERE TENANT_ID = ?";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);

            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                totalRecordsCount = rs.getInt("DEVICE_COUNT");
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        PaginationResult paginationResult = new PaginationResult();
        paginationResult.setData(filteredDevicesWithDetails);
        paginationResult.setRecordsTotal(totalRecordsCount);
        return paginationResult;
    }

    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                        FilterSet filterSet, int startIndex, int resultCount)
                                                                throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        if (startIndex < 0) {
            throw new InvalidParameterValueException("Start index should be equal to 0 or greater than that.");
        }

        if (resultCount < 5) {
            throw new InvalidParameterValueException("Result count should be equal to 5 or greater than that.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Map<String, Object>> filteredDevicesWithDetails = new ArrayList<>();
        int totalRecordsCount = 0;
        try {
            con = this.getConnection();
            String sql, advancedSqlFiltering = "";
            // appending filters if exist, to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    advancedSqlFiltering = advancedSqlFiltering + "AND " + column + " = ? ";
                }
            }
            sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_2 " +
                  "WHERE TENANT_ID = ? AND FEATURE_CODE = ? " + advancedSqlFiltering + "ORDER BY DEVICE_ID ASC LIMIT ?, ?";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);
            if (filters != null && filters.values().size() > 0) {
                int i = 3;
                for (Object value : filters.values()) {
                    if (value instanceof Integer) {
                        stmt.setInt(i, (Integer) value);
                    } else if (value instanceof String) {
                        stmt.setString(i, (String) value);
                    }
                    i++;
                }
                stmt.setInt(i, startIndex);
                stmt.setInt(++i, resultCount);
            } else {
                stmt.setInt(3, startIndex);
                stmt.setInt(4, resultCount);
            }
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            Map<String, Object> filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new HashMap<>();
                filteredDeviceWithDetails.put("device-id", rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.put("platform", rs.getString("PLATFORM"));
                filteredDeviceWithDetails.put("ownership", rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.put("connectivity-details", rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
            // fetching total records count
            sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_2 " +
                  "WHERE TENANT_ID = ? AND FEATURE_CODE = ?";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);

            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            while (rs.next()) {
                totalRecordsCount = rs.getInt("DEVICE_COUNT");
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        PaginationResult paginationResult = new PaginationResult();
        paginationResult.setData(filteredDevicesWithDetails);
        paginationResult.setRecordsTotal(totalRecordsCount);
        return paginationResult;
    }

    public List<Map<String, Object>> getDevicesWithDetails(FilterSet filterSet)
                                            throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Map<String, Object>> filteredDevicesWithDetails = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql;
            sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_1 WHERE TENANT_ID = ?";
            // appending filters to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
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
            Map<String, Object> filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new HashMap<>();
                filteredDeviceWithDetails.put("device-id", rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.put("platform", rs.getString("PLATFORM"));
                filteredDeviceWithDetails.put("ownership", rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.put("connectivity-details", rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDevicesWithDetails;
    }

    public List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                           FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Map<String, Object>> filteredDevicesWithDetails = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql;
            sql = "SELECT DEVICE_ID, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM DEVICES_VIEW_2 " +
                  "WHERE TENANT_ID = ? AND FEATURE_CODE = ?";
            // appending filters to support advanced filtering options
            // [1] appending filter columns, if exist
            if (filters != null && filters.size() > 0) {
                for (String column : filters.keySet()) {
                    sql = sql + " AND " + column + " = ?";
                }
            }
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, nonCompliantFeatureCode);
            if (filters != null && filters.values().size() > 0) {
                int i = 3;
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
            Map<String, Object> filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new HashMap<>();
                filteredDeviceWithDetails.put("device-id", rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.put("platform", rs.getString("PLATFORM"));
                filteredDeviceWithDetails.put("ownership", rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.put("connectivity-details", rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDevicesWithDetails;
    }

    private Map<String, Object> extractDatabaseFiltersFromBean(FilterSet filterSet)
                                                        throws InvalidParameterValueException {
        if (filterSet == null) {
            return null;
        }

        Map<String, Object> filters = new LinkedHashMap<>();

        String connectivityStatus = filterSet.getConnectivityStatus();
        if (connectivityStatus != null) {
            if (ConnectivityStatus.active.toString().equals(connectivityStatus) ||
                    ConnectivityStatus.inactive.toString().equals(connectivityStatus) ||
                        ConnectivityStatus.removed.toString().equals(connectivityStatus)) {
                filters.put("CONNECTIVITY_STATUS", connectivityStatus.toUpperCase());
            } else {
                throw new InvalidParameterValueException("Invalid use of value for platform. " +
                    "Value of platform could only be either android, ios or windows.");
            }
        }

        String potentialVulnerability = filterSet.getPotentialVulnerability();
        if (potentialVulnerability != null) {
            if (PotentialVulnerability.non_compliant.toString().equals(potentialVulnerability) ||
                    PotentialVulnerability.unmonitored.toString().equals(potentialVulnerability)) {
                if (PotentialVulnerability.non_compliant.toString().equals(potentialVulnerability)) {
                    filters.put("IS_COMPLIANT", 0);
                } else {
                    filters.put("POLICY_ID", -1);
                }
            } else {
                throw new InvalidParameterValueException("Invalid use of value for potential vulnerability. " +
                    "Value of potential vulnerability could only be non_compliant or unmonitored.");
            }
        }

        String platform = filterSet.getPlatform();
        if (platform != null) {
            if (Platform.android.toString().equals(platform) ||
                    Platform.ios.toString().equals(platform) ||
                        Platform.windows.toString().equals(platform)) {
                filters.put("PLATFORM", platform);
            } else {
                throw new InvalidParameterValueException("Invalid use of value for platform. " +
                    "Value of platform could only be either android, ios or windows.");
            }
        }

        String ownership = filterSet.getOwnership();
        if (ownership != null) {
            if (Ownership.byod.toString().equals(ownership) ||
                    Ownership.cope.toString().equals(ownership)) {
                filters.put("OWNERSHIP", ownership.toUpperCase());
            } else {
                throw new InvalidParameterValueException("Invalid use of value for ownership. " +
                    "Value of ownership could only be either BYOD or COPE.");
            }
        }

        return filters;
    }

    private Connection getConnection() throws SQLException {
        return GadgetDataServiceDAOFactory.getConnection();
    }

}
