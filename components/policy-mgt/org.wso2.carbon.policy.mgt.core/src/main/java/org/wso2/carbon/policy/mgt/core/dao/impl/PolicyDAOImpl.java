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
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.dao.PolicyDAO;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDAOImpl implements PolicyDAO {

    private static final Log log = LogFactory.getLog(PolicyDAOImpl.class);


    @Override
    public Policy  addPolicy(Policy policy) throws PolicyManagerDAOException {
        persistPolicy(policy);
        return policy;
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
        return null;
    }

    @Override
    public Policy addPolicy(String deviceID, String deviceType, Policy policy) throws PolicyManagerDAOException {

        // First persist the policy to the data base.
        persistPolicy(policy);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {

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
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return policy;
    }

    @Override
    public Policy getPolicy() throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
    }

    @Override
    public Policy getPolicy(String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
    }

    @Override
    public Policy getPolicy(String deviceID, String deviceType) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (Exception e) {

        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return null;
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
        } catch (Exception e) {
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


    private void persistPolicy(Policy policy) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        // TODO : find a way to get the tenant Id.
        int tenantId = -1234;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY (NAME, PROFILE_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);

            stmt.setString(1, policy.getPolicyName());
            stmt.setInt(2, policy.getProfile().getProfileId());
            stmt.setInt(3, tenantId);

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
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
    }


    private void persistFeatures(Profile profile) throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE_FEATURES (PROFILE_ID, FEATURE_ID, CONTENT) VALUES (?, ?, ?)";

            stmt = conn.prepareStatement(query);
            for (Feature feature : profile.getFeaturesList()) {
                stmt.setInt(1, profile.getProfileId());
                stmt.setInt(2, feature.getId());
                stmt.setObject(3, feature.getAttribute());
                stmt.addBatch();
                //Not adding the logic to check the size of the stmt and execute if the size records added is over 1000
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            String msg = "Error occurred while adding the feature list to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    public Profile addProfile(Profile profile) throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        // TODO : find a way to get the tenant Id.
        int tenantId = -1234;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE (PROFILE_NAME,TENANT_ID, DEVICE_TYPE_ID, CREATED_TIME, UPDATED_TIME) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);

            stmt.setString(1, profile.getProfileName());
            stmt.setInt(2, tenantId);
            stmt.setLong(3, profile.getDeviceType().getId());
            stmt.setTimestamp(4, profile.getCreatedDate());
            stmt.setTimestamp(5, profile.getUpdatedDate());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0 && log.isDebugEnabled()) {
                String msg = "No rows are updated on the profile table.";
                log.debug(msg);
            }
            generatedKeys = stmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                profile.setProfileId(generatedKeys.getInt(1));
            }
            // Checking the profile id here, because profile id could have been passed from the calling method.
            if (profile.getProfileId() == 0) {
                throw new RuntimeException("Profile id is 0, this could be an issue.");
            }

            persistFeatures(profile);

        } catch (SQLException e) {
            String msg = "Error occurred while adding the profile to database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return profile;
    }


    public Profile updateProfile(Profile profile) throws PolicyManagerDAOException {
        return profile;
    }

    @Override
    public void deleteProfile(Profile profile) throws PolicyManagerDAOException {

        // First delete the features related to the profile
        deleteFeaturesOfProfile(profile);
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profile.getProfileId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the profile from the data base.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public void deleteFeaturesOfProfile(Profile profile) throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE_FEATURES WHERE PROFILE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profile.getProfileId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the feature related to a profile.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public Feature addFeature(Feature feature) throws PolicyManagerDAOException, FeatureManagementException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_FEATURES (NAME, CODE, DESCRIPTION, EVALUVATION_RULE) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);
            stmt.setString(1, feature.getName());
            stmt.setString(2, feature.getCode());
            stmt.setString(3, feature.getDescription());
            stmt.setString(4, feature.getRuleValue());

            int affectedRows = stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug(affectedRows + " Features are added.");
            }
            generatedKeys = stmt.getGeneratedKeys();
            while (generatedKeys.next()) {
                feature.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding feature to the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
        return feature;
    }


    @Override
    public Feature updateFeature(Feature feature) throws PolicyManagerDAOException, FeatureManagementException {
        return feature;
    }

    @Override
    public List<Profile> getAllProfiles() throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {

        }

        return null;
    }

    @Override
    public List<Profile> getProfilesOfDeviceType(String deviceType) throws PolicyManagerDAOException {


        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {

        }

        return null;
    }

    @Override
    public List<Feature> getAllFeatures() throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<Feature> featureList = new ArrayList<Feature>();

        try {
            conn = this.getConnection();
            String query = "SELECT ID, NAME, CODE, EVALUVATION_RULE FROM DM_FEATURES";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Feature feature = new Feature();
                feature.setId(resultSet.getInt("ID"));
                feature.setCode(resultSet.getString("CODE"));
                feature.setName(resultSet.getString("NAME"));
                feature.setRuleValue(resultSet.getString("EVALUVATION_RULE"));
                featureList.add(feature);
            }

        } catch (SQLException e) {
            String msg = "Unable to get the list of the features from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public List<Feature> getFeaturesForProfile(int profileId) throws PolicyManagerDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        List<Feature> featureList = new ArrayList<Feature>();

        try {
            conn = this.getConnection();
            String query = "SELECT PF.FEATURE_ID FEATURE_ID, F.NAME NAME, F.CODE CODE, " +
                    "F.EVALUVATION_RULE RULE, F.CONTENT AS CONTENT FROM DM_PROFILE_FEATURES AS PF " +
                    "JOIN DM_FEATURES AS F ON F.ID = PF.FEATURE_ID WHERE PROFILE_ID=?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Feature feature = new Feature();
                feature.setId(resultSet.getInt("FEATURE_ID"));
                feature.setCode(resultSet.getString("CODE"));
                feature.setName(resultSet.getString("NAME"));
                feature.setAttribute(resultSet.getObject("CONTENT"));
                feature.setRuleValue(resultSet.getString("RULE"));
                featureList.add(feature);
            }

        } catch (SQLException e) {
            String msg = "Unable to get the list of the features from database.";
            log.error(msg);
            throw new PolicyManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public void deleteFeature(int featureId) throws PolicyManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "";
            stmt = conn.prepareStatement(query);
        } catch (SQLException e) {

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

}
