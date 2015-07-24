/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.policy.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAO;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonitoringDAOImpl implements MonitoringDAO {

    private static final Log log = LogFactory.getLog(MonitoringDAOImpl.class);

    @Override
    public int setDeviceAsNoneCompliance(int deviceId, int policyId) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_STATUS (DEVICE_ID, POLICY_ID, STATUS, LAST_FAILED_TIME, " +
                    "ATTEMPTS) VALUES (?, ?, ?, ?, ?) ";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, policyId);
            stmt.setInt(3, 0);
            stmt.setTimestamp(4, currentTimestamp);
            stmt.setInt(5, 0);
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding the none compliance to the database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }

    }

    @Override
    public void setDeviceAsCompliance(int deviceId, int policyId) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET STATUS = ?, ATTEMPTS=0, LAST_SUCCESS_TIME = ?" +
                    " WHERE  DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1);
            stmt.setTimestamp(2, currentTimestamp);
            stmt.setInt(3, deviceId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while deleting the none compliance to the database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addNoneComplianceFeatures(int policyComplianceStatusId, int deviceId, List<ComplianceFeature>
            complianceFeatures) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_FEATURES (COMPLIANCE_STATUS_ID, FEATURE_CODE, STATUS) " +
                    "VALUES (?, ?, ?) ";

            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            for (ComplianceFeature feature : complianceFeatures) {
                stmt.setInt(1, policyComplianceStatusId);
                stmt.setString(2, feature.getFeatureCode());
                stmt.setString(3, String.valueOf(feature.isCompliance()));
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the none compliance features to the database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public ComplianceData getCompliance(int deviceId) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        ComplianceData complianceData = null;
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_STATUS WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                complianceData.setId(resultSet.getInt("ID"));
                complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                complianceData.setStatus(resultSet.getBoolean("STATUS"));
                complianceData.setAttempts(resultSet.getInt("ATTEMPTS"));
                complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));
            }
            return complianceData;

        } catch (SQLException e) {
            String msg = "Unable to retrieve compliance data from database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }

    @Override
    public List<ComplianceData> getCompliance(List<Integer> deviceIds) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ComplianceData> complianceDataList = null;

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_STATUS WHERE DEVICE_ID IN (?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, makeString(deviceIds));

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                ComplianceData complianceData = new ComplianceData();

                complianceData.setId(resultSet.getInt("ID"));
                complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                complianceData.setStatus(resultSet.getBoolean("STATUS"));
                complianceData.setAttempts(resultSet.getInt("ATTEMPTS"));
                complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));

                complianceDataList.add(complianceData);
            }
            return complianceDataList;

        } catch (SQLException e) {
            String msg = "Unable to retrieve compliance data from database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }

    @Override
    public List<ComplianceFeature> getNoneComplianceFeatures(int policyComplianceStatusId) throws
            MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ComplianceFeature> complianceFeatures = new ArrayList<ComplianceFeature>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_FEATURES WHERE COMPLIANCE_STATUS_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyComplianceStatusId);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                ComplianceFeature feature = new ComplianceFeature();
                feature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                feature.setMessage(resultSet.getString("STATUS"));
                complianceFeatures.add(feature);
            }
            return complianceFeatures;

        } catch (SQLException e) {
            String msg = "Unable to retrieve compliance features data from database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }

    }

    @Override
    public void deleteNoneComplianceData(int policyComplianceStatusId) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY_COMPLIANCE_FEATURES WHERE COMPLIANCE_STATUS_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyComplianceStatusId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Unable to delete compliance  data from database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void updateAttempts(int deviceId, boolean reset) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {

            conn = this.getConnection();
            String query = "";
            if (reset) {
                query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET ATTEMPTS = 0, LAST_REQUESTED_TIME = ? " +
                        "WHERE DEVICE_ID = ?";
            } else {
                query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET ATTEMPTS = ATTEMPTS + 1, LAST_REQUESTED_TIME = ? " +
                        "WHERE DEVICE_ID = ?";
            }
            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp);
            stmt.setInt(2, deviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Unable to update the attempts  data in database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void updateAttempts(List<Integer> deviceIds, boolean reset) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {

            conn = this.getConnection();
            String query = "";
            if (reset) {
                query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET ATTEMPTS = 0, LAST_REQUESTED_TIME = ? " +
                        "WHERE DEVICE_ID = ?";
            } else {
                query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET ATTEMPTS = ATTEMPTS + 1, LAST_REQUESTED_TIME = ? " +
                        "WHERE DEVICE_ID = ?";
            }
            stmt = conn.prepareStatement(query);
            for (int deviceId : deviceIds) {
                stmt.setTimestamp(1, currentTimestamp);
                stmt.setInt(2, deviceId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Unable to update the attempts  data in database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    private Connection getConnection() throws MonitoringDAOException {
        try {
            return PolicyManagementDAOFactory.getConnection();
        } catch (PolicyManagerDAOException e) {
            throw new MonitoringDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository config.datasource", e);
        }
    }


    private void closeConnection() {
        try {
            PolicyManagementDAOFactory.closeConnection();
        } catch (PolicyManagerDAOException e) {
            log.warn("Unable to close the database connection.");
        }
    }

    private String makeString(List<Integer> values) {

        StringBuilder buff = new StringBuilder();
        for (int value : values) {
            buff.append(value).append(",");
        }
        buff.deleteCharAt(buff.length() - 1);
        return buff.toString();
    }
}
