/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.policy.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyDates;
import org.wso2.carbon.policy.mgt.common.PolicyLocations;
import org.wso2.carbon.policy.mgt.common.PolicyTimes;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAOImpl implements PolicyDAO {

    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagerDAOException {
        return persistPolicy(policy);
    }

    @Override
    public Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_TYPE_POLICY (DEVICE_TYPE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, getDeviceTypeId(deviceType));
            stmt.setInt(2, policy.getId());
            stmt.executeQuery();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the device type policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;

    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_ROLE_POLICY (ROLE_NAME, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (String role : roleNames) {
                stmt.setString(1, role);
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the role name with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_USER_POLICY (POLICY_ID, USERNAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (String username : usernameList) {
                stmt.setInt(1, policy.getId());
                stmt.setString(2, username);
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the user name with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToDevice(List<Device> devices, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_POLICY (DEVICE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            for (Device device : devices) {
                stmt.setInt(1, device.getId());
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            String msg = "Error occurred while adding the device ids  with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addDatesToPolicy(Date startDate, Date endDate, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DATE (START_DATE, END_DATE, POLICY_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);
            stmt.setInt(3, policy.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the start date (" + startDate + ") and end date (" +
                    endDate + ") with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addTimesToPolicy(int startTime, int endTime, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_TIME (STARTING_TIME, ENDING_TIME, POLICY_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, startTime);
            stmt.setInt(2, endTime);
            stmt.setInt(3, policy.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the start time (" + startTime + ") and end time (" +
                    endTime + ") with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy addLocationToPolicy(String latitude, String longitude, Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_LOCATION (LATITUDE, LONGITUDE, POLICY_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, latitude);
            stmt.setString(2, longitude);
            stmt.setInt(3, policy.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the Location (" + latitude + ") (" +
                    longitude + ") with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET NAME= ?, TENANT_ID = ?, PROFILE_ID = ?, PRIORITY = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getTenantId());
            stmt.setInt(3, policy.getProfile().getProfileId());
            stmt.setInt(4, policy.getPriorityId());
            stmt.setInt(5, policy.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while updating policy (" + policy.getPolicyName() + ") in database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Policy policy = new Policy();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY WHERE ID= ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
            }
            return policy;

        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }


    @Override
    public Policy getPolicyByProfileID(int profileId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Policy policy = new Policy();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY WHERE PROFILE_ID= ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                policy.setId(resultSet.getInt("ID"));
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
            }
            return policy;

        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }

    @Override
    public List<Policy> getAllPolicies() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Policy> policies = new ArrayList<Policy>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Policy policy = new Policy();

                policy.setId(resultSet.getInt("ID"));
                policy.setProfileId(resultSet.getInt("PROFILE_ID"));
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policies.add(policy);
            }
            return policies;

        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }

    @Override
    public List<Policy> getPolicyOfDeviceType(String deviceTypeName) throws PolicyManagerDAOException {
        return null;
    }

    @Override
    public List<Integer> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceIdList = new ArrayList<Integer>();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                deviceIdList.add(resultSet.getInt("DEVICE_ID"));
            }
            return deviceIdList;
        } catch (SQLException e) {
            String msg = "Error occurred while getting the device related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }

    }


    public List<String> getPolicyAppliedRoles(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<String> roleNames = new ArrayList<String>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_ROLE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                roleNames.add(resultSet.getString("ROLE_NAME"));
            }
            return roleNames;

        } catch (SQLException e) {
            String msg = "Error occurred while getting the roles related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }


    public PolicyTimes getTimesOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        PolicyTimes times = new PolicyTimes();

        try {
            conn = this.getConnection();
            String query = "SELECT STARTING_TIME, ENDING_TIME FROM DM_TIME WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                //TODO:
                policy.setStartTime(resultSet.getInt("STARTING_TIME"));
                policy.setEndTime(resultSet.getInt("ENDING_TIME"));

                times.setStartTime(resultSet.getInt("STARTING_TIME"));
                times.setEndTime(resultSet.getInt("ENDING_TIME"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start time and end time related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return times;
    }


    public PolicyDates getDatesOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        PolicyDates dates = new PolicyDates();

        try {
            conn = this.getConnection();
            String query = "SELECT START_DATE, END_DATE FROM DM_DATE WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                //TODO:
                policy.setStartDate(resultSet.getDate("START_DATE"));
                policy.setEndDate(resultSet.getDate("END_DATE"));

                dates.setStartDate(resultSet.getDate("START_DATE"));
                dates.setEndDate(resultSet.getDate("END_DATE"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start date and end date related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return dates;
    }


    public PolicyLocations getLocationsOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        PolicyLocations locations = new PolicyLocations();

        try {
            conn = this.getConnection();
            String query = "SELECT LATITUDE, LONGITUDE FROM DM_LOCATION WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                //TODO:
                policy.setLatitude(resultSet.getString("LATITUDE"));
                policy.setLongitude(resultSet.getString("LONGITUDE"));

                locations.setLatitude(resultSet.getString("LATITUDE"));
                locations.setLongitude(resultSet.getString("LONGITUDE"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start time and end time related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return locations;
    }

    @Override
    public List<Integer> getPolicyIdsOfDevice(Device device) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE DEVICE_ID =  ? ";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, device.getId());
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the device policy table.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return policyIds;
    }

    @Override
    public List<Integer> getPolicyOfRole(String roleName) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();

        try {
            conn = this.getConnection();
            String query = "SELECT *  FROM DM_ROLE_POLICY WHERE ROLE_NAME = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roleName);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the role policy table.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return policyIds;
    }

    @Override
    public List<Integer> getPolicyOfUser(String username) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> policyIds = new ArrayList<Integer>();

        try {
            conn = this.getConnection();
            String query = "SELECT *  FROM DM_USER_POLICY WHERE USERNAME = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policyIds.add(resultSet.getInt("POLICY_ID"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the user policy table.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return policyIds;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policy.getPolicyName() + ") delete from database.");
            }
            return true;
        } catch (SQLException e) {
            String msg = "Unable to delete the policy (" + policy.getPolicyName() + ") from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean deleteAllPolicyRelatedConfigs(int policyId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();

            String userPolicy = "DELETE FROM DM_USER_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(userPolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            String rolePolicy = "DELETE FROM DM_ROLE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(rolePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();


            String devicePolicy = "DELETE FROM DM_DEVICE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(devicePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();


            String locationPolicy = "DELETE FROM DM_LOCATION WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(locationPolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();


            String timePolicy = "DELETE FROM DM_TIME WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(timePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();


            String datePolicy = "DELETE FROM DM_DATE WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(datePolicy);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policyId + ") related configs deleted from database.");
            }
            return true;
        } catch (SQLException e) {
            String msg = "Unable to delete the policy (" + policyId + ") related configs from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws PolicyManagerDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }

    private void closeConnection() {
        try {
            PolicyManagementDAOFactory.closeConnection();
        } catch (PolicyManagerDAOException e) {
            log.warn("Unable to close the database connection.");
        }
    }


    private Policy persistPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID, PRIORITY) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, readHighestPriorityOfPolicies());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0 && log.isDebugEnabled()) {
                String msg = "No rows are updated on the policy table.";
                log.debug(msg);
            }
            generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                policy.setId(generatedKeys.getInt(1));
            }
            // checking policy id here, because it object could have passed with id from the calling method.
            if (policy.getId() == 0) {
                throw new RuntimeException("No rows were inserted, policy id cannot be null.");
            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding policy to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return policy;
    }


    /**
     * This method returns the device type id when supplied with device type name.
     *
     * @param deviceType device type.
     * @return integer value
     * @throws PolicyManagerDAOException
     */
    private int getDeviceTypeId(String deviceType) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int deviceTypeId = -1;
        try {
            conn = this.getConnection();
            String query = "SELECT ID FROM DM_DEVICE_TYPE WHERE NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                deviceTypeId = resultSet.getInt("ID");
            }

        } catch (SQLException e) {
            String msg = "Error occurred while selecting the device type id.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return deviceTypeId;
    }


    private int readHighestPriorityOfPolicies() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        int priority = 0;

        try {
            conn = this.getConnection();
            String query = "SELECT MAX(PRIORITY) PRIORITY FROM DM_POLICY;";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                priority = resultSet.getInt("PRIORITY") + 1;
            }
            if (log.isDebugEnabled()) {
                log.debug("Priority of the new policy added is (" + priority + ")");
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the highest priority of the policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return priority;
    }

}
