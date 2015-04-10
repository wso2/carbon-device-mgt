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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceDAOImpl;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceTypeDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAOImpl implements PolicyDAO {

    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);
    DeviceDAOImpl deviceDAO = new DeviceDAOImpl(PolicyManagementDAOFactory.getDataSource());
    DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAOImpl(PolicyManagementDAOFactory.getDataSource());
    ProfileDAO profileDAO = new ProfileDAOImpl();


    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagerDAOException {
        return persistPolicy(policy);
    }

    @Override
    public Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException {

        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
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
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

        return policy;

    }

    @Override
    public Policy addPolicyToRole(String roleName, Policy policy) throws PolicyManagerDAOException {
        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_ROLE_POLICY (ROLE_NAME, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roleName);
            stmt.setInt(2, policy.getId());

            stmt.executeQuery();
            policy.getRoleList().add(roleName);
        } catch (SQLException e) {
            String msg = "Error occurred while adding the role name with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

        return policy;
    }

    @Override
    public Policy addPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagerDAOException {

        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            Device device = deviceDAO.getDevice(deviceIdentifier);
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_POLICY (DEVICE_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, device.getId());
            stmt.setInt(2, policy.getId());

            stmt.executeUpdate();
            policy.getDeviceList().add(device);
        } catch (SQLException e) {
            String msg = "Error occurred while adding the device ids  with policy to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while reading the device data from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return policy;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET NAME= ?, TENANT_ID = ?, PROFILE_ID = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getTenantId());
            stmt.setInt(3, policy.getProfile().getProfileId());
            stmt.setInt(4, policy.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while updating policy " + policy.getPolicyName() + " (Policy Name) in database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return policy;
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagerDAOException {

        Connection conn = null;
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

                Profile profile = profileDAO.getProfiles(resultSet.getInt("PROFILE_ID"));
                List<Device> deviceList = getPolicyAppliedDevices(policyId);
                List<String> roleNames = getPolicyAppliedRoles(policyId);

                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setProfile(profile);
                policy.setDeviceList(deviceList);
                policy.setRoleList(roleNames);

                setDatesOfPolicy(policy);
                setTimesOfPolicy(policy);
                setLocationsOfPolicy(policy);
            }

            return policy;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profiles.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }


    @Override
    public Policy getPolicyByProfileID(int profileId) throws PolicyManagerDAOException {

        Connection conn = null;
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

                int policyId = resultSet.getInt("ID");
                Profile profile = profileDAO.getProfiles(profileId);
                List<Device> deviceList = getPolicyAppliedDevices(policyId);
                List<String> roleNames = getPolicyAppliedRoles(policyId);

                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setProfile(profile);
                policy.setDeviceList(deviceList);
                policy.setRoleList(roleNames);

                setDatesOfPolicy(policy);
                setTimesOfPolicy(policy);
                setLocationsOfPolicy(policy);
            }

            return policy;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profiles.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }

    @Override
    public List<Policy> getPolicy() throws PolicyManagerDAOException {

        Connection conn = null;
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

                int policyId = resultSet.getInt("ID");
                Profile profile = profileDAO.getProfiles(resultSet.getInt("PROFILE_ID"));
                List<Device> deviceList = getPolicyAppliedDevices(policyId);
                List<String> roleNames = getPolicyAppliedRoles(policyId);

                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("NAME"));
                policy.setTenantId(resultSet.getInt("TENANT_ID"));
                policy.setProfile(profile);
                policy.setDeviceList(deviceList);
                policy.setRoleList(roleNames);

                setDatesOfPolicy(policy);
                setTimesOfPolicy(policy);
                setLocationsOfPolicy(policy);

                policies.add(policy);
            }
            return policies;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profiles.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

    }

    @Override
    public List<Policy> getPolicy(String deviceTypeName) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Policy> policies = new ArrayList<Policy>();
        try {
            DeviceType deviceType = deviceTypeDAO.getDeviceType(deviceTypeName);

            // This gives the profiles related to given device types.
            List<Profile> profileList = profileDAO.getProfilesOfDeviceType(deviceTypeName);

            for (Profile profile : profileList) {
                policies.add(getPolicyByProfileID(profile.getProfileId()));
            }

           /* conn = this.getConnection();

            // TODO : Change the query for  device type
            String query = "SELECT dp.ID PID, dp.NAME PNAME, dp.TENANT_ID PTD, dp.PROFILE_ID PPID FROM DM_POLICY dp " +
                    "INNER JOIN DM_PROFILE dpr ON dpr.ID = dp.PROFILE_ID WHERE dpr.ID = ?";
            stmt = conn.prepareStatement(query);

            resultSet = stmt.executeQuery();

            //ID NAME TENANT_ID PROFILE_ID
            while (resultSet.next()) {
                Policy policy = new Policy();

                int policyId = resultSet.getInt("PID");
                Profile profile = profileDAO.getProfiles(resultSet.getInt("PID"));
                List<Device> deviceList = getPolicyAppliedDevices(policyId);
                List<String> roleNames = getPolicyAppliedRoles(policyId);

                policy.setId(policyId);
                policy.setPolicyName(resultSet.getString("PNAME"));
                policy.setTenantId(resultSet.getInt("PTD"));
                policy.setProfile(profile);
                policy.setDeviceList(deviceList);
                policy.setRoleList(roleNames);

                setDatesOfPolicy(policy);
                setTimesOfPolicy(policy);
                setLocationsOfPolicy(policy);

                policies.add(policy);
            }*/

            return policies;
      /*  } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);*/
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while getting the profiles.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device type.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

    }

    // TODO :
    private List<Device> getPolicyAppliedDevices(int policyId) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<Device> deviceList = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                deviceList.add(deviceDAO.getDevice(resultSet.getInt("DEVICE_ID")));
            }
            return deviceList;
        } catch (SQLException e) {
            String msg = "Error occurred while getting the device related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting device data.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

    }

    // TODO :
    private List<String> getPolicyAppliedRoles(int policyId) throws PolicyManagerDAOException {
        Connection conn = null;
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
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }

    // TODO :
    private void setTimesOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = this.getConnection();
            String query = "SELECT STARTING_TIME, ENDING_TIME FROM DM_TIME WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policy.setStartTime(resultSet.getInt("STARTING_TIME"));
                policy.setEndTime(resultSet.getInt("ENDING_TIME"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start time and end time related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }

    // TODO :
    private void setDatesOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = this.getConnection();
            String query = "SELECT START_DATE, END_DATE FROM DM_DATE WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policy.setStartDate(resultSet.getDate("START_DATE"));
                policy.setEndDate(resultSet.getDate("END_DATE"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start date and end date related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }

    // TODO:
    private void setLocationsOfPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        try {
            conn = this.getConnection();
            String query = "SELECT LAT, LONG FROM DM_LOCATION WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                policy.setLatitude(resultSet.getString("LAT"));
                policy.setLongitude(resultSet.getString("LONG"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting the start time and end time related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
    }

    @Override
    public List<Policy> getPolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        List<Policy> policies = new ArrayList<Policy>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY WHERE DEVICE_ID =  ? ";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceDAO.getDevice(deviceIdentifier).getId().intValue());

            generatedKeys = stmt.executeQuery();

            while (generatedKeys.next()){
                policies.add(getPolicy(generatedKeys.getInt("POLICY_ID")));
            }

           return  policies;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the device policy table.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting device data from the device identifier.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

    }

    @Override
    public List<Policy> getPolicyOfRole(String roleName) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        List<Policy> policies = new ArrayList<Policy>();
        try {
            conn = this.getConnection();
            String query = "SELECT *  FROM DM_ROLE_POLICY WHERE ROLE_NAME = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, roleName);

            generatedKeys = stmt.executeQuery();

            while (generatedKeys.next()){
                policies.add(getPolicy(generatedKeys.getInt("POLICY_ID")));
            }

            return  policies;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the role policy table.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
    }

    @Override
    public void deletePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policy.getId());
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policy.getPolicyName() + ") delete from database.");
            }
        } catch (SQLException e) {
            String msg = "Unable to delete the policy (" + policy.getPolicyName() + ") from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

    }

    private Connection getConnection() throws PolicyManagerDAOException {
        try {
            return PolicyManagementDAOFactory.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new PolicyManagerDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository datasource", e);
        }
    }


    private Policy persistPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        // TODO : find a way to get the tenant Id.
        int tenantId = -1234;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID, PRIORITY) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);

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

            profileDAO.addProfile(policy.getProfile());

            return policy;
        } catch (SQLException e) {
            String msg = "Error occurred while adding policy to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while adding profile to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
    }


    /**
     * This method returns the device type id when supplied with device type name.
     *
     * @param deviceType
     * @return
     * @throws PolicyManagerDAOException
     */
    private int getDeviceTypeId(String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
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
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return deviceTypeId;
    }


    private int readHighestPriorityOfPolicies()  throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = this.getConnection();
            String query = "SELECT MAX(PRIORITY) PRIORITY FROM DM_POLICY;";
            stmt = conn.prepareStatement(query);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                return resultSet.getInt("PRIORITY");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while reading the highest priority of the policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return 0;
    }

}
