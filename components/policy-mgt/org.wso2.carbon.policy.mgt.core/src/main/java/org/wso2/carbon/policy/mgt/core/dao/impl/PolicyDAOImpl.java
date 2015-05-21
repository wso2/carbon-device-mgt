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
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.sql.*;
import java.sql.Date;
import java.util.*;

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
    public boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET  PRIORITY = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);

            for (Policy policy : policies) {
                stmt.setInt(1, policy.getPriorityId());
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while updating policy priorities in database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

//    @Override
//    public Policy addDatesToPolicy(Date startDate, Date endDate, Policy policy) throws PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        try {
//            conn = this.getConnection();
//            String query = "INSERT INTO DM_DATE (START_DATE, END_DATE, POLICY_ID) VALUES (?, ?, ?)";
//            stmt = conn.prepareStatement(query);
//            stmt.setDate(1, startDate);
//            stmt.setDate(2, endDate);
//            stmt.setInt(3, policy.getId());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while adding the start date (" + startDate + ") and end date (" +
//                    endDate + ") with policy to database.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, null);
//        }
//        return policy;
//    }

//    @Override
//    public Policy addTimesToPolicy(int startTime, int endTime, Policy policy) throws PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        try {
//            conn = this.getConnection();
//            String query = "INSERT INTO DM_TIME (STARTING_TIME, ENDING_TIME, POLICY_ID) VALUES (?, ?, ?)";
//            stmt = conn.prepareStatement(query);
//            stmt.setInt(1, startTime);
//            stmt.setInt(2, endTime);
//            stmt.setInt(3, policy.getId());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while adding the start time (" + startTime + ") and end time (" +
//                    endTime + ") with policy to database.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, null);
//        }
//        return policy;
//    }

//    @Override
//    public Policy addLocationToPolicy(String latitude, String longitude, Policy policy) throws
//            PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        try {
//            conn = this.getConnection();
//            String query = "INSERT INTO DM_LOCATION (LATITUDE, LONGITUDE, POLICY_ID) VALUES (?, ?, ?)";
//            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
//            stmt.setString(1, latitude);
//            stmt.setString(2, longitude);
//            stmt.setInt(3, policy.getId());
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while adding the Location (" + latitude + ") (" +
//                    longitude + ") with policy to database.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, null);
//        }
//        return policy;
//    }

    @Override
    public Criterion addCriterion(Criterion criteria) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys;

        int tenantId = PolicyManagerUtil.getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_CRITERIA (TENANT_ID, NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, tenantId);
            stmt.setString(2, criteria.getName());
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();

            while (generatedKeys.next()) {
                criteria.setId(generatedKeys.getInt(1));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while inserting the criterion (" + criteria.getName() + ") to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return criteria;
    }

    @Override
    public Criterion updateCriterion(Criterion criteria) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PolicyManagerUtil.getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_CRITERIA SET TENANT_ID = ?,  NAME = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            stmt.setString(2, criteria.getName());
            stmt.setInt(3, criteria.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while inserting the criterion (" + criteria.getName() + ") to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return criteria;
    }

    @Override
    public Criterion getCriterion(int id) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Criterion criterion = new Criterion();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE ID= ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
            }
            return criterion;

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
    public Criterion getCriterion(String name) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Criterion criterion = new Criterion();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE NAME= ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
            }
            return criterion;

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
    public boolean checkCriterionExists(String name) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        boolean exist = false;

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA WHERE NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            resultSet = stmt.executeQuery();
            exist = resultSet.next();

        } catch (SQLException e) {
            String msg = "Error occurred while checking whether criterion (" + name + ") exists.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return exist;
    }

    @Override
    public boolean deleteCriterion(Criterion criteria) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_CRITERIA WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, criteria.getId());
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Criterion (" + criteria.getName() + ") delete from database.");
            }
            return true;
        } catch (SQLException e) {
            String msg = "Unable to delete the policy (" + criteria.getName() + ") from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<Criterion> getAllPolicyCriteria() throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Criterion> criteria = new ArrayList<Criterion>();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_CRITERIA";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Criterion criterion = new Criterion();
                criterion.setId(resultSet.getInt("ID"));
                criterion.setName(resultSet.getString("NAME"));
                criteria.add(criterion);
            }
            return criteria;

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
    public Policy addPolicyCriteria(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CRITERIA (CRITERIA_ID, POLICY_ID) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            List<PolicyCriterion> criteria = policy.getPolicyCriterias();
            for (PolicyCriterion criterion : criteria) {

                stmt.setInt(1, criterion.getCriteriaId());
                stmt.setInt(2, policy.getId());
                stmt.addBatch();
            }
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            int i = 0;

            while (generatedKeys.next()) {
                policy.getPolicyCriterias().get(i).setId(generatedKeys.getInt(1));
                i++;
            }

        } catch (SQLException e) {
            String msg = "Error occurred while inserting the criterion to policy (" + policy.getPolicyName() + ") " +
                    "to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return policy;

    }

    @Override
    public boolean addPolicyCriteriaProperties(List<PolicyCriterion> policyCriteria) throws PolicyManagerDAOException {


        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_CRITERIA_PROPERTIES (POLICY_CRITERION_ID, PROP_KEY, PROP_VALUE, CONTENT) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);

            for (PolicyCriterion criterion : policyCriteria) {

                Properties prop = criterion.getProperties();
                for (String name : prop.stringPropertyNames()) {

                    stmt.setInt(1, criterion.getId());
                    stmt.setString(2, name);
                    stmt.setString(3, prop.getProperty(name));
                    stmt.setObject(4, criterion.getObjectMap());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            //   stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while inserting the criterion properties to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

        return false;
    }

    @Override
    public List<PolicyCriterion> getPolicyCriteria(int policyId) throws PolicyManagerDAOException {


        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<PolicyCriterion> criteria = new ArrayList<PolicyCriterion>();

        try {
            conn = this.getConnection();
            String query = "SELECT DPC.ID, DPC.CRITERIA_ID, DPCP.PROP_KEY, DPCP.PROP_VALUE, DPCP.CONTENT FROM " +
                    "DM_POLICY_CRITERIA DPC LEFT JOIN DM_POLICY_CRITERIA_PROPERTIES DPCP " +
                    "ON DPCP.POLICY_CRITERION_ID = DPC.ID RIGHT JOIN DM_CRITERIA DC " +
                    "ON DC.ID=DPC.CRITERIA_ID WHERE DPC.POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            int criteriaId = 0;

            PolicyCriterion policyCriterion = null;
            Properties prop = null;
            while (resultSet.next()) {

                if (criteriaId != resultSet.getInt("ID")) {
                    if (policyCriterion != null) {
                        policyCriterion.setProperties(prop);
                        criteria.add(policyCriterion);
                    }
                    policyCriterion = new PolicyCriterion();
                    prop = new Properties();
                    criteriaId = resultSet.getInt("ID");

                    policyCriterion.setId(resultSet.getInt("ID"));
                    policyCriterion.setCriteriaId(resultSet.getInt("CRITERIA_ID"));
                } else {
                    prop.setProperty(resultSet.getString("PROP_KEY"), resultSet.getString("PROP_VALUE"));
                }
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the criteria related to policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return criteria;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY SET NAME= ?, TENANT_ID = ?, PROFILE_ID = ?, PRIORITY = ?, COMPLIANCE = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getTenantId());
            stmt.setInt(3, policy.getProfile().getProfileId());
            stmt.setInt(4, policy.getPriorityId());
            stmt.setString(5, policy.getCompliance());
            stmt.setInt(6, policy.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while updating policy (" + policy.getPolicyName() + ") in database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
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
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setProfileId(resultSet.getInt("PROFILE_ID"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
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
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
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
                policy.setPriorityId(resultSet.getInt("PRIORITY"));
                policy.setCompliance(resultSet.getString("COMPLIANCE"));
                policy.setOwnershipType(resultSet.getString("OWNERSHIP_TYPE"));
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

    @Override
    public List<String> getPolicyAppliedUsers(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<String> users = new ArrayList<String>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_USER_POLICY WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                users.add(resultSet.getString("USERNAME"));
            }
            return users;

        } catch (SQLException e) {
            String msg = "Error occurred while getting the roles related to policies.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
    }


//    public PolicyTimes getTimesOfPolicy(Policy policy) throws PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        ResultSet resultSet = null;
//        PolicyTimes times = new PolicyTimes();
//
//        try {
//            conn = this.getConnection();
//            String query = "SELECT STARTING_TIME, ENDING_TIME FROM DM_TIME WHERE POLICY_ID = ?";
//            stmt = conn.prepareStatement(query);
//            stmt.setInt(1, policy.getId());
//            resultSet = stmt.executeQuery();
//
//            while (resultSet.next()) {
//
//                times.setStartTime(resultSet.getInt("STARTING_TIME"));
//                times.setEndTime(resultSet.getInt("ENDING_TIME"));
//            }
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while getting the start time and end time related to policies.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
//            this.closeConnection();
//        }
//        return times;
//    }


//    public PolicyDates getDatesOfPolicy(Policy policy) throws PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        ResultSet resultSet = null;
//        PolicyDates dates = new PolicyDates();
//
//        try {
//            conn = this.getConnection();
//            String query = "SELECT START_DATE, END_DATE FROM DM_DATE WHERE POLICY_ID = ?";
//            stmt = conn.prepareStatement(query);
//            stmt.setInt(1, policy.getId());
//            resultSet = stmt.executeQuery();
//
//            while (resultSet.next()) {
//                dates.setStartDate(resultSet.getDate("START_DATE"));
//                dates.setEndDate(resultSet.getDate("END_DATE"));
//            }
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while getting the start date and end date related to policies.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
//            this.closeConnection();
//        }
//        return dates;
//    }


//    public PolicyLocations getLocationsOfPolicy(Policy policy) throws PolicyManagerDAOException {
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        ResultSet resultSet = null;
//        PolicyLocations locations = new PolicyLocations();
//
//        try {
//            conn = this.getConnection();
//            String query = "SELECT LATITUDE, LONGITUDE FROM DM_LOCATION WHERE POLICY_ID = ?";
//            stmt = conn.prepareStatement(query);
//            stmt.setInt(1, policy.getId());
//            resultSet = stmt.executeQuery();
//
//            while (resultSet.next()) {
//                locations.setLatitude(resultSet.getString("LATITUDE"));
//                locations.setLongitude(resultSet.getString("LONGITUDE"));
//            }
//
//        } catch (SQLException e) {
//            String msg = "Error occurred while getting the start time and end time related to policies.";
//            log.error(msg, e);
//            throw new PolicyManagerDAOException(msg, e);
//        } finally {
//            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
//            this.closeConnection();
//        }
//        return locations;
//    }

    @Override
    public void addEffectivePolicyToDevice(int deviceId, int policyId, List<ProfileFeature> profileFeatures)
            throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_DEVICE_POLICY_APPLIED " +
                    "(DEVICE_ID, POLICY_ID, POLICY_CONTENT, CREATED_TIME, UPDATED_TIME) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, policyId);
            stmt.setObject(3, profileFeatures);
            stmt.setTimestamp(4, currentTimestamp);
            stmt.setTimestamp(5, currentTimestamp);

            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the evaluated feature list to device.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void setPolicyApplied(int deviceId) throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_DEVICE_POLICY_APPLIED SET APPLIED_TIME = ?, APPLIED = ? WHERE DEVICE_ID = ? ";
            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp);
            stmt.setBoolean(2, true);
            stmt.setInt(3, deviceId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while updating applied policy to device (" + deviceId + ")";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public void updateEffectivePolicyToDevice(int deviceId, int policyId, List<ProfileFeature> profileFeatures)
            throws PolicyManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_DEVICE_POLICY_APPLIED SET POLICY_ID = ?, POLICY_CONTENT = ?, UPDATED_TIME = ?, " +
                    "APPLIED = ? WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            stmt.setObject(2, profileFeatures);
            stmt.setTimestamp(3, currentTimestamp);
            stmt.setBoolean(4, false);
            stmt.setInt(5, deviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while updating the evaluated feature list to device.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public boolean checkPolicyAvailable(int deviceId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        boolean exist = false;

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_POLICY_APPLIED WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            resultSet = stmt.executeQuery();
            exist = resultSet.next();

        } catch (SQLException e) {
            String msg = "Error occurred while checking whether device (" + deviceId + ") has a policy to apply.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return exist;
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
    public boolean deletePolicy(int policyId) throws PolicyManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyId);
            stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Policy (" + policyId + ") delete from database.");
            }
            return true;
        } catch (SQLException e) {
            String msg = "Unable to delete the policy (" + policyId + ") from database.";
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


            String deleteCriteria = "DELETE FROM DM_POLICY_CRITERIA WHERE POLICY_ID = ?";
            stmt = conn.prepareStatement(deleteCriteria);
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
        int tenantId = PolicyManagerUtil.getTenantId();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID, PRIORITY, COMPLIANCE, OWNERSHIP_TYPE) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, readHighestPriorityOfPolicies());
            stmt.setString(5, policy.getCompliance());
            stmt.setString(6, policy.getOwnershipType());

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
