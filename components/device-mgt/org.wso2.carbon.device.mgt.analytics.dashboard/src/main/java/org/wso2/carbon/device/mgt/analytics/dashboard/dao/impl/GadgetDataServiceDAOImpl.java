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
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.DetailedDeviceEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.DeviceCountByGroupEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.FilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GadgetDataServiceDAOImpl implements GadgetDataServiceDAO {

    @Override
    public DeviceCountByGroupEntry getTotalDeviceCount() throws SQLException {
        int totalDeviceCount;
        try {
            totalDeviceCount = this.getFilteredDeviceCount(null);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }

        DeviceCountByGroupEntry deviceCountByGroupEntry = new DeviceCountByGroupEntry();
        deviceCountByGroupEntry.setGroup("total");
        deviceCountByGroupEntry.setDisplayNameForGroup("Total");
        deviceCountByGroupEntry.setDeviceCount(totalDeviceCount);

        return deviceCountByGroupEntry;
    }

    public DeviceCountByGroupEntry getDeviceCount(FilterSet filterSet)
                                                  throws InvalidParameterValueException, SQLException {

        int filteredDeviceCount = this.getFilteredDeviceCount(filterSet);

        DeviceCountByGroupEntry deviceCountByGroupEntry = new DeviceCountByGroupEntry();
        deviceCountByGroupEntry.setGroup("non-specific");
        deviceCountByGroupEntry.setDisplayNameForGroup("Non-specific");
        deviceCountByGroupEntry.setDeviceCount(filteredDeviceCount);

        return deviceCountByGroupEntry;
    }

    private int getFilteredDeviceCount(FilterSet filterSet) throws InvalidParameterValueException, SQLException {

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

    @Override
    public DeviceCountByGroupEntry getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                                             FilterSet filterSet) throws InvalidParameterValueException, SQLException {

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

        DeviceCountByGroupEntry deviceCountByGroupEntry = new DeviceCountByGroupEntry();
        deviceCountByGroupEntry.setGroup("feature-non-compliant");
        deviceCountByGroupEntry.setDisplayNameForGroup("Feature-non-compliant");
        deviceCountByGroupEntry.setDeviceCount(filteredDeviceCount);

        return deviceCountByGroupEntry;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByConnectivityStatuses() throws SQLException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroupEntry> deviceCountsByConnectivityStatuses = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql = "SELECT CONNECTIVITY_STATUS, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM DEVICES_VIEW_1 " +
                "WHERE TENANT_ID = ? GROUP BY CONNECTIVITY_STATUS";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            DeviceCountByGroupEntry deviceCountByConnectivityStatus;
            while (rs.next()) {
                deviceCountByConnectivityStatus = new DeviceCountByGroupEntry();
                deviceCountByConnectivityStatus.setGroup(rs.getString("CONNECTIVITY_STATUS"));
                deviceCountByConnectivityStatus.setDisplayNameForGroup(rs.getString("CONNECTIVITY_STATUS"));
                deviceCountByConnectivityStatus.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                deviceCountsByConnectivityStatuses.add(deviceCountByConnectivityStatus);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCountsByConnectivityStatuses;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPotentialVulnerabilities() throws SQLException {
        // getting non-compliant device count
        DeviceCountByGroupEntry nonCompliantDeviceCount = new DeviceCountByGroupEntry();
        nonCompliantDeviceCount.setGroup(GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT);
        nonCompliantDeviceCount.setDisplayNameForGroup("Non-compliant");
        nonCompliantDeviceCount.setDeviceCount(getNonCompliantDeviceCount());

        // getting unmonitored device count
        DeviceCountByGroupEntry unmonitoredDeviceCount = new DeviceCountByGroupEntry();
        unmonitoredDeviceCount.setGroup(GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED);
        unmonitoredDeviceCount.setDisplayNameForGroup("Unmonitored");
        unmonitoredDeviceCount.setDeviceCount(getUnmonitoredDeviceCount());

        List<DeviceCountByGroupEntry> deviceCountsByPotentialVulnerabilities = new ArrayList<>();
        deviceCountsByPotentialVulnerabilities.add(nonCompliantDeviceCount);
        deviceCountsByPotentialVulnerabilities.add(unmonitoredDeviceCount);

        return deviceCountsByPotentialVulnerabilities;
    }

    private int getNonCompliantDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setPotentialVulnerability(GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT);
        try {
            return this.getFilteredDeviceCount(filterSet);
        } catch (InvalidParameterValueException e) {
            throw new AssertionError(e);
        }
    }

    private int getUnmonitoredDeviceCount() throws SQLException {
        FilterSet filterSet = new FilterSet();
        filterSet.setPotentialVulnerability(GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED);
        try {
            return this.getFilteredDeviceCount(filterSet);
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
        List<DeviceCountByGroupEntry> filteredNonCompliantDeviceCountsByFeatures = new ArrayList<>();
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
            DeviceCountByGroupEntry filteredNonCompliantDeviceCountByFeature;
            while (rs.next()) {
                filteredNonCompliantDeviceCountByFeature = new DeviceCountByGroupEntry();
                filteredNonCompliantDeviceCountByFeature.setGroup(rs.getString("FEATURE_CODE"));
                filteredNonCompliantDeviceCountByFeature.setDisplayNameForGroup(rs.getString("FEATURE_CODE"));
                filteredNonCompliantDeviceCountByFeature.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredNonCompliantDeviceCountsByFeatures.add(filteredNonCompliantDeviceCountByFeature);
            }
            // fetching total records count
            sql = "SELECT COUNT(FEATURE_CODE) AS NON_COMPLIANT_FEATURE_COUNT FROM " +
                "(SELECT DISTINCT FEATURE_CODE FROM DEVICES_VIEW_2 WHERE TENANT_ID = ?) NON_COMPLIANT_FEATURE_CODE";

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

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPlatforms(FilterSet filterSet)
                                                           throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroupEntry> filteredDeviceCountsByPlatforms = new ArrayList<>();
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
            DeviceCountByGroupEntry filteredDeviceCountByPlatform;
            while (rs.next()) {
                filteredDeviceCountByPlatform = new DeviceCountByGroupEntry();
                filteredDeviceCountByPlatform.setGroup(rs.getString("PLATFORM"));
                filteredDeviceCountByPlatform.setDisplayNameForGroup(rs.getString("PLATFORM").toUpperCase());
                filteredDeviceCountByPlatform.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredDeviceCountsByPlatforms.add(filteredDeviceCountByPlatform);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                        FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroupEntry> filteredDeviceCountsByPlatforms = new ArrayList<>();
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
            DeviceCountByGroupEntry filteredDeviceCountByPlatform;
            while (rs.next()) {
                filteredDeviceCountByPlatform = new DeviceCountByGroupEntry();
                filteredDeviceCountByPlatform.setGroup(rs.getString("PLATFORM"));
                filteredDeviceCountByPlatform.setDisplayNameForGroup(rs.getString("PLATFORM").toUpperCase());
                filteredDeviceCountByPlatform.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredDeviceCountsByPlatforms.add(filteredDeviceCountByPlatform);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                                                throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroupEntry> filteredDeviceCountsByOwnershipTypes = new ArrayList<>();
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
            DeviceCountByGroupEntry filteredDeviceCountByOwnershipType;
            while (rs.next()) {
                filteredDeviceCountByOwnershipType = new DeviceCountByGroupEntry();
                filteredDeviceCountByOwnershipType.setGroup(rs.getString("OWNERSHIP"));
                filteredDeviceCountByOwnershipType.setDisplayNameForGroup(rs.getString("OWNERSHIP"));
                filteredDeviceCountByOwnershipType.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredDeviceCountsByOwnershipTypes.add(filteredDeviceCountByOwnershipType);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByOwnershipTypes;
    }

    @Override
    public List<DeviceCountByGroupEntry>
                        getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                            FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroupEntry> filteredDeviceCountsByOwnershipTypes = new ArrayList<>();
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
            DeviceCountByGroupEntry filteredDeviceCountByOwnershipType;
            while (rs.next()) {
                filteredDeviceCountByOwnershipType = new DeviceCountByGroupEntry();
                filteredDeviceCountByOwnershipType.setGroup(rs.getString("OWNERSHIP"));
                filteredDeviceCountByOwnershipType.setDisplayNameForGroup(rs.getString("OWNERSHIP"));
                filteredDeviceCountByOwnershipType.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredDeviceCountsByOwnershipTypes.add(filteredDeviceCountByOwnershipType);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDeviceCountsByOwnershipTypes;
    }

    @Override
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
        List<DetailedDeviceEntry> filteredDevicesWithDetails = new ArrayList<>();
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
            DetailedDeviceEntry filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DetailedDeviceEntry();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setPlatform(rs.getString("PLATFORM"));
                filteredDeviceWithDetails.setOwnershipType(rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.setConnectivityStatus(rs.getString("CONNECTIVITY_STATUS"));
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

    @Override
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
        List<DetailedDeviceEntry> filteredDevicesWithDetails = new ArrayList<>();
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
            DetailedDeviceEntry filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DetailedDeviceEntry();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setPlatform(rs.getString("PLATFORM"));
                filteredDeviceWithDetails.setOwnershipType(rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.setConnectivityStatus(rs.getString("CONNECTIVITY_STATUS"));
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

    @Override
    public List<DetailedDeviceEntry> getDevicesWithDetails(FilterSet filterSet)
                                            throws InvalidParameterValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DetailedDeviceEntry> filteredDevicesWithDetails = new ArrayList<>();
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
            DetailedDeviceEntry filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DetailedDeviceEntry();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setPlatform(rs.getString("PLATFORM"));
                filteredDeviceWithDetails.setOwnershipType(rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.setConnectivityStatus(rs.getString("CONNECTIVITY_STATUS"));
                filteredDevicesWithDetails.add(filteredDeviceWithDetails);
            }
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return filteredDevicesWithDetails;
    }

    @Override
    public List<DetailedDeviceEntry> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                           FilterSet filterSet) throws InvalidParameterValueException, SQLException {

        if (nonCompliantFeatureCode == null || "".equals(nonCompliantFeatureCode)) {
            throw new InvalidParameterValueException("Non-compliant feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(filterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DetailedDeviceEntry> filteredDevicesWithDetails = new ArrayList<>();
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
            DetailedDeviceEntry filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DetailedDeviceEntry();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setPlatform(rs.getString("PLATFORM"));
                filteredDeviceWithDetails.setOwnershipType(rs.getString("OWNERSHIP"));
                filteredDeviceWithDetails.setConnectivityStatus(rs.getString("CONNECTIVITY_STATUS"));
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
            filters.put("CONNECTIVITY_STATUS", connectivityStatus);
        }

        String potentialVulnerability = filterSet.getPotentialVulnerability();
        if (potentialVulnerability != null) {
            if (GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT.equals(potentialVulnerability) ||
                    GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED.equals(potentialVulnerability)) {
                if (GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT.equals(potentialVulnerability)) {
                    filters.put("IS_COMPLIANT", 0);
                } else {
                    filters.put("POLICY_ID", -1);
                }
            } else {
                throw new InvalidParameterValueException("Invalid use of value for potential vulnerability. " +
                    "Value of potential vulnerability could only be either " +
                        GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT + " or " +
                            GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED + ".");
            }
        }

        String platform = filterSet.getPlatform();
        if (platform != null) {
            filters.put("PLATFORM", platform);
        }

        String ownership = filterSet.getOwnership();
        if (ownership != null) {
            filters.put("OWNERSHIP", ownership);
        }

        return filters;
    }

    private Connection getConnection() throws SQLException {
        return GadgetDataServiceDAOFactory.getConnection();
    }

}
