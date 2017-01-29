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
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.AbstractGadgetDataServiceDAO;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOConstants;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.*;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgreSQLGadgetDataServiceDAOImpl extends AbstractGadgetDataServiceDAO {

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount, String userName)
                            throws InvalidStartIndexValueException, InvalidResultCountValueException, SQLException {

        if (startIndex < GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX) {
            throw new InvalidStartIndexValueException("Start index should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX + " or greater than that.");
        }

        if (resultCount < GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT) {
            throw new InvalidResultCountValueException("Result count should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT + " or greater than that.");
        }

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceCountByGroup> filteredNonCompliantDeviceCountsByFeatures = new ArrayList<>();
        int totalRecordsCount = 0;
        try {
            con = this.getConnection();
            String sql = "SELECT FEATURE_CODE, COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? GROUP BY FEATURE_CODE " +
                    "ORDER BY DEVICE_COUNT DESC OFFSET ? LIMIT ?";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, startIndex);
            stmt.setInt(3, resultCount);

            // executing query
            rs = stmt.executeQuery();
            // fetching query results
            DeviceCountByGroup filteredNonCompliantDeviceCountByFeature;
            while (rs.next()) {
                filteredNonCompliantDeviceCountByFeature = new DeviceCountByGroup();
                filteredNonCompliantDeviceCountByFeature.setGroup(rs.getString("FEATURE_CODE"));
                filteredNonCompliantDeviceCountByFeature.setDisplayNameForGroup(rs.getString("FEATURE_CODE"));
                filteredNonCompliantDeviceCountByFeature.setDeviceCount(rs.getInt("DEVICE_COUNT"));
                filteredNonCompliantDeviceCountsByFeatures.add(filteredNonCompliantDeviceCountByFeature);
            }
            // fetching total records count
            sql = "SELECT COUNT(FEATURE_CODE) AS NON_COMPLIANT_FEATURE_COUNT FROM " +
                "(SELECT DISTINCT FEATURE_CODE FROM " + GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_2 +
                    " WHERE TENANT_ID = ?) NON_COMPLIANT_FEATURE_CODE";

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
    public PaginationResult getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, int startIndex, int resultCount, String userName)
                            throws InvalidPotentialVulnerabilityValueException, InvalidStartIndexValueException,
                            InvalidResultCountValueException, SQLException {

        if (startIndex < GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX) {
            throw new InvalidStartIndexValueException("Start index should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX + " or greater than that.");
        }

        if (resultCount < GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT) {
            throw new InvalidResultCountValueException("Result count should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT + " or greater than that.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(extendedFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceWithDetails> filteredDevicesWithDetails = new ArrayList<>();
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
            sql = "SELECT DEVICE_ID, DEVICE_IDENTIFICATION, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ? " +
                    advancedSqlFiltering + "ORDER BY DEVICE_ID ASC OFFSET ? LIMIT ?";

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

            // fetching total records count
            sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_1 + " WHERE TENANT_ID = ?";

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
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String featureCode, BasicFilterSet basicFilterSet,
                            int startIndex, int resultCount, String userName) throws InvalidFeatureCodeValueException,
                            InvalidStartIndexValueException, InvalidResultCountValueException, SQLException {

        if (featureCode == null || featureCode.isEmpty()) {
            throw new InvalidFeatureCodeValueException("Feature code should not be either null or empty.");
        }

        if (startIndex < GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX) {
            throw new InvalidStartIndexValueException("Start index should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_START_INDEX + " or greater than that.");
        }

        if (resultCount < GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT) {
            throw new InvalidResultCountValueException("Result count should be equal to " +
                GadgetDataServiceDAOConstants.Pagination.MIN_RESULT_COUNT + " or greater than that.");
        }

        Map<String, Object> filters = this.extractDatabaseFiltersFromBean(basicFilterSet);

        Connection con;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<DeviceWithDetails> filteredDevicesWithDetails = new ArrayList<>();
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
            sql = "SELECT DEVICE_ID, DEVICE_IDENTIFICATION, PLATFORM, OWNERSHIP, CONNECTIVITY_STATUS FROM " +
                GadgetDataServiceDAOConstants.DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? AND FEATURE_CODE = ? " +
                    advancedSqlFiltering + "ORDER BY DEVICE_ID ASC OFFSET ? LIMIT ?";

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
                stmt.setInt(i, startIndex);
                stmt.setInt(++i, resultCount);
            } else {
                stmt.setInt(3, startIndex);
                stmt.setInt(4, resultCount);
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

            // fetching total records count
            sql = "SELECT COUNT(DEVICE_ID) AS DEVICE_COUNT FROM " + GadgetDataServiceDAOConstants.
                DatabaseView.DEVICES_VIEW_2 + " WHERE TENANT_ID = ? AND FEATURE_CODE = ?";

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, featureCode);

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

}
