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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidFeatureCodeValueException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidPotentialVulnerabilityValueException;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractGadgetDataServiceDAO implements GadgetDataServiceDAO {

    @Override
    public DeviceCountByGroup getTotalDeviceCount() throws SQLException {
        int totalDeviceCount;
        try {
            totalDeviceCount = this.getFilteredDeviceCount(null);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            throw new AssertionError(e);
        }

        DeviceCountByGroup deviceCountByGroup = new DeviceCountByGroup();
        deviceCountByGroup.setGroup("total");
        deviceCountByGroup.setDisplayNameForGroup("Total");
        deviceCountByGroup.setDeviceCount(totalDeviceCount);

        return deviceCountByGroup;
    }

    @Override
    public DeviceCountByGroup getDeviceCount(ExtendedFilterSet extendedFilterSet)
                                                  throws InvalidPotentialVulnerabilityValueException, SQLException {

        int filteredDeviceCount = this.getFilteredDeviceCount(extendedFilterSet);

        DeviceCountByGroup deviceCountByGroup = new DeviceCountByGroup();
        deviceCountByGroup.setGroup("filtered");
        deviceCountByGroup.setDisplayNameForGroup("Filtered");
        deviceCountByGroup.setDeviceCount(filteredDeviceCount);

        return deviceCountByGroup;
    }

    private int getFilteredDeviceCount(ExtendedFilterSet extendedFilterSet)
                                       throws InvalidPotentialVulnerabilityValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(extendedFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int filteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ?";
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
    public DeviceCountByGroup getFeatureNonCompliantDeviceCount(String featureCode,
                            BasicFilterSet basicFilterSet) throws InvalidFeatureCodeValueException, SQLException {

        if (featureCode == null || featureCode.isEmpty()) {
            throw new InvalidFeatureCodeValueException("Feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(basicFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        int filteredDeviceCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? AND FEATURE_CODE = ?";
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
            stmt.setString(2, featureCode);
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

        DeviceCountByGroup deviceCountByGroup = new DeviceCountByGroup();
        deviceCountByGroup.setGroup("feature-non-compliant-and-filtered");
        deviceCountByGroup.setDisplayNameForGroup("Feature-non-compliant-and-filtered");
        deviceCountByGroup.setDeviceCount(filteredDeviceCount);

        return deviceCountByGroup;
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByConnectivityStatuses() throws SQLException {
        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> deviceCountsByConnectivityStatuses = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql = "SELECT CONNECTIVITY_STATUS, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_1 +
                    " WHERE TENANT_ID = ? GROUP BY CONNECTIVITY_STATUS";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            DeviceCountByGroup deviceCountByConnectivityStatus;
            while (rs.next()) {
                deviceCountByConnectivityStatus = new DeviceCountByGroup();
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
    public List<DeviceCountByGroup> getDeviceCountsByPotentialVulnerabilities() throws SQLException {
        // getting non-compliant device count
        DeviceCountByGroup nonCompliantDeviceCount = new DeviceCountByGroup();
        nonCompliantDeviceCount.setGroup(GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT);
        nonCompliantDeviceCount.setDisplayNameForGroup("Non-compliant");
        nonCompliantDeviceCount.setDeviceCount(getNonCompliantDeviceCount());

        // getting unmonitored device count
        DeviceCountByGroup unmonitoredDeviceCount = new DeviceCountByGroup();
        unmonitoredDeviceCount.setGroup(GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED);
        unmonitoredDeviceCount.setDisplayNameForGroup("Unmonitored");
        unmonitoredDeviceCount.setDeviceCount(getUnmonitoredDeviceCount());

        List<DeviceCountByGroup> deviceCountsByPotentialVulnerabilities = new ArrayList<>();
        deviceCountsByPotentialVulnerabilities.add(nonCompliantDeviceCount);
        deviceCountsByPotentialVulnerabilities.add(unmonitoredDeviceCount);

        return deviceCountsByPotentialVulnerabilities;
    }

    private int getNonCompliantDeviceCount() throws SQLException {
        ExtendedFilterSet extendedFilterSet = new ExtendedFilterSet();
        extendedFilterSet.setPotentialVulnerability(GadgetDataServiceDAOConstants.
            PotentialVulnerability.NON_COMPLIANT);
        try {
            return this.getFilteredDeviceCount(extendedFilterSet);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            throw new AssertionError(e);
        }
    }

    private int getUnmonitoredDeviceCount() throws SQLException {
        ExtendedFilterSet extendedFilterSet = new ExtendedFilterSet();
        extendedFilterSet.setPotentialVulnerability(GadgetDataServiceDAOConstants.
            PotentialVulnerability.UNMONITORED);
        try {
            return this.getFilteredDeviceCount(extendedFilterSet);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByPlatforms(ExtendedFilterSet extendedFilterSet)
                                                  throws InvalidPotentialVulnerabilityValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(extendedFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> filteredDeviceCountsByPlatforms = new ArrayList<>();
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
            sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ? " + advancedSqlFiltering + "GROUP BY PLATFORM";
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
            DeviceCountByGroup filteredDeviceCountByPlatform;
            while (rs.next()) {
                filteredDeviceCountByPlatform = new DeviceCountByGroup();
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
    public List<DeviceCountByGroup>
                         getFeatureNonCompliantDeviceCountsByPlatforms(String featureCode,
                                BasicFilterSet basicFilterSet) throws InvalidFeatureCodeValueException, SQLException {

        if (featureCode == null || featureCode.isEmpty()) {
            throw new InvalidFeatureCodeValueException("Feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(basicFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> filteredDeviceCountsByPlatforms = new ArrayList<>();
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
            sql = "SELECT PLATFORM, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? AND FEATURE_CODE = ? " +
                    advancedSqlFiltering + "GROUP BY PLATFORM";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, featureCode);
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
            DeviceCountByGroup filteredDeviceCountByPlatform;
            while (rs.next()) {
                filteredDeviceCountByPlatform = new DeviceCountByGroup();
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
    public List<DeviceCountByGroup> getDeviceCountsByOwnershipTypes(ExtendedFilterSet extendedFilterSet)
                                                    throws InvalidPotentialVulnerabilityValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(extendedFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> filteredDeviceCountsByOwnershipTypes = new ArrayList<>();
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
            sql = "SELECT OWNERSHIP, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ? " +
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
            DeviceCountByGroup filteredDeviceCountByOwnershipType;
            while (rs.next()) {
                filteredDeviceCountByOwnershipType = new DeviceCountByGroup();
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
    public List<DeviceCountByGroup>
                  getFeatureNonCompliantDeviceCountsByOwnershipTypes(String featureCode,
                               BasicFilterSet basicFilterSet) throws InvalidFeatureCodeValueException, SQLException {

        if (featureCode == null || featureCode.isEmpty()) {
            throw new InvalidFeatureCodeValueException("Feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(basicFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> filteredDeviceCountsByOwnershipTypes = new ArrayList<>();
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
            sql = "SELECT OWNERSHIP, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? AND FEATURE_CODE = ? " +
                    advancedSqlFiltering + "GROUP BY OWNERSHIP";
            stmt = con.prepareStatement(sql);
            // [2] appending filter column values, if exist
            stmt.setInt(1, tenantId);
            stmt.setString(2, featureCode);
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
            DeviceCountByGroup filteredDeviceCountByOwnershipType;
            while (rs.next()) {
                filteredDeviceCountByOwnershipType = new DeviceCountByGroup();
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
    public List<DeviceWithDetails> getDevicesWithDetails(ExtendedFilterSet extendedFilterSet)
                                                    throws InvalidPotentialVulnerabilityValueException, SQLException {

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(extendedFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceWithDetails> filteredDevicesWithDetails = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql;
            sql = "SELECT DEVICE_ID, DEVICE_IDENTIFICATION, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ?";
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
            DeviceWithDetails filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DeviceWithDetails();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setDeviceIdentification(rs.getString("DEVICE_IDENTIFICATION"));
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
    public List<DeviceWithDetails> getFeatureNonCompliantDevicesWithDetails(String featureCode,
                               BasicFilterSet basicFilterSet) throws InvalidFeatureCodeValueException, SQLException {

        if (featureCode == null || featureCode.isEmpty()) {
            throw new InvalidFeatureCodeValueException("Feature code should not be either null or empty.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(basicFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceWithDetails> filteredDevicesWithDetails = new ArrayList<>();
        try {
            con = this.getConnection();
            String sql;
            sql = "SELECT DEVICE_ID, DEVICE_IDENTIFICATION, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_2 +
                    " WHERE TENANT_ID = ? AND FEATURE_CODE = ?";
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
            stmt.setString(2, featureCode);
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
            DeviceWithDetails filteredDeviceWithDetails;
            while (rs.next()) {
                filteredDeviceWithDetails = new DeviceWithDetails();
                filteredDeviceWithDetails.setDeviceId(rs.getInt("DEVICE_ID"));
                filteredDeviceWithDetails.setDeviceIdentification(rs.getString("DEVICE_IDENTIFICATION"));
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

    protected Map<String, Object> extractDatabaseFiltersFromBean(BasicFilterSet basicFilterSet) {
        if (basicFilterSet == null) {
            return null;
        }

        Map<String, Object> filters = new LinkedHashMap<>();

        String connectivityStatus = basicFilterSet.getConnectivityStatus();
        if (connectivityStatus != null && !connectivityStatus.isEmpty()) {
            filters.put("CONNECTIVITY_STATUS", connectivityStatus);
        }

        String platform = basicFilterSet.getPlatform();
        if (platform != null && !platform.isEmpty()) {
            filters.put("PLATFORM", platform);
        }

        String ownership = basicFilterSet.getOwnership();
        if (ownership != null && !ownership.isEmpty()) {
            filters.put("OWNERSHIP", ownership);
        }

        return filters;
    }

    protected Map<String, Object> extractDatabaseFiltersFromBean(ExtendedFilterSet extendedFilterSet)
                                                                 throws InvalidPotentialVulnerabilityValueException {
        if (extendedFilterSet == null) {
            return null;
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean((BasicFilterSet) extendedFilterSet);

        String potentialVulnerability = extendedFilterSet.getPotentialVulnerability();
        if (potentialVulnerability != null && !potentialVulnerability.isEmpty()) {
            if (GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT.equals(potentialVulnerability) ||
                    GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED.equals(potentialVulnerability)) {
                if (GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT.equals(potentialVulnerability)) {
                    filters.put("IS_COMPLIANT", 0);
                } else {
                    filters.put("POLICY_ID", -1);
                }
            } else {
                throw new InvalidPotentialVulnerabilityValueException("Invalid use of value for potential " +
                    "vulnerability. Value of potential vulnerability could only be either " +
                        GadgetDataServiceDAOConstants.PotentialVulnerability.NON_COMPLIANT + " or " +
                            GadgetDataServiceDAOConstants.PotentialVulnerability.UNMONITORED + ".");
            }
        }

        return filters;
    }

    protected Connection getConnection() throws SQLException {
        return GadgetDataServiceDAOFactory.getConnection();
    }

}
