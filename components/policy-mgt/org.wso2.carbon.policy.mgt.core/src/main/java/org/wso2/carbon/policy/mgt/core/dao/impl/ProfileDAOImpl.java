/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceTypeDAO;
import org.wso2.carbon.device.mgt.core.dao.impl.DeviceTypeDAOImpl;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAOImpl implements ProfileDAO {

    private static final Log log = LogFactory.getLog(ProfileDAOImpl.class);
    FeatureDAOImpl featureDAO = new FeatureDAOImpl();
    DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAOImpl(PolicyManagementDAOFactory.getDataSource());

    public Profile addProfile(Profile profile) throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
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

            featureDAO.addProfileFeatures(profile.getProfileFeaturesList(), profile.getProfileId());
           // persistFeatures(profile);

        } catch (SQLException e) {
            String msg = "Error occurred while adding the profile to database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while adding the features to database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return profile;
    }


    public Profile updateProfile(Profile profile) throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_PROFILE SET PROFILE_NAME = ? ,TENANT_ID = ?, DEVICE_TYPE_ID = ? , UPDATED_TIME = ? " +
                    "WHERE ID = ?";
            stmt = conn.prepareStatement(query, stmt.RETURN_GENERATED_KEYS);

            stmt.setString(1, profile.getProfileName());
            stmt.setInt(2, tenantId);
            stmt.setLong(3, profile.getDeviceType().getId());
            stmt.setTimestamp(4, profile.getUpdatedDate());
            stmt.setInt(5, profile.getProfileId());

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

            // TODO : Check to update the features.
            featureDAO.updateProfileFeatures(profile.getProfileFeaturesList(), profile.getProfileId());
            //persistFeatures(profile);

        } catch (SQLException e) {
            String msg = "Error occurred while updating the profile ("+profile.getProfileName()+") in database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while updating the profile in database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }

        return profile;
    }

    @Override
    public void deleteProfile(Profile profile) throws ProfileManagerDAOException {

        // First delete the features related to the profile

        try {
            featureDAO.deleteFeaturesOfProfile(profile);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while deleting features.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        }

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
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public Profile getProfiles(int profileId) throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        Profile profile = new Profile();
        try {
            List<ProfileFeature> featureList = featureDAO.getFeaturesForProfile(profileId);
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);

            resultSet = stmt.executeQuery();

            //ID  PROFILE_NAME  TENANT_ID DEVICE_TYPE_ID CREATED_TIME UPDATED_TIME
            while (resultSet.next()) {
                profile.setProfileFeaturesList(featureList);
                profile.setProfileId(profileId);
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setDeviceType(deviceTypeDAO.getDeviceType(resultSet.getInt("DEVICE_TYPE_ID")));
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));
            }
            return profile;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred when getting the device type name by device type id.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred when getting the features related to a profile.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }

    }

    private void persistFeatures(Profile profile) throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE_FEATURES (PROFILE_ID, FEATURE_ID, CONTENT) VALUES (?, ?, ?)";

            stmt = conn.prepareStatement(query);
            for (ProfileFeature feature : profile.getProfileFeaturesList()) {
                stmt.setInt(1, profile.getProfileId());
                stmt.setInt(2, feature.getId());
                stmt.setObject(3, feature.getContent());
                stmt.addBatch();
                //Not adding the logic to check the size of the stmt and execute if the size records added is over 1000
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            String msg = "Error occurred while adding the feature list to the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }


    @Override
    public List<Profile> getAllProfiles() throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Profile> profileList = new ArrayList<Profile>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE";
            stmt = conn.prepareStatement(query);

            resultSet = stmt.executeQuery();

            //ID  PROFILE_NAME  TENANT_ID DEVICE_TYPE_ID CREATED_TIME UPDATED_TIME
            while (resultSet.next()) {
                Profile profile = new Profile();
                int profileId = resultSet.getInt("ID");
                List<ProfileFeature> featureList = featureDAO.getFeaturesForProfile(profileId);
                profile.setProfileFeaturesList(featureList);
                profile.setProfileId(profileId);
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setDeviceType(deviceTypeDAO.getDeviceType(resultSet.getInt("DEVICE_TYPE_ID")));
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));

                profileList.add(profile);
            }
            return profileList;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile list from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device type.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while getting the features related to a profile.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        }
    }

    @Override
    public List<Profile> getProfilesOfDeviceType(String deviceTypeName) throws ProfileManagerDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Profile> profileList = new ArrayList<Profile>();
        try {
            DeviceType deviceType = deviceTypeDAO.getDeviceType(deviceTypeName);
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE WHERE DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceType.getId());

            resultSet = stmt.executeQuery();

            //ID  PROFILE_NAME  TENANT_ID DEVICE_TYPE_ID CREATED_TIME UPDATED_TIME
            while (resultSet.next()) {
                Profile profile = new Profile();
                int profileId = resultSet.getInt("ID");
                List<ProfileFeature> featureList = featureDAO.getFeaturesForProfile(profileId);
                profile.setProfileFeaturesList(featureList);
                profile.setProfileId(profileId);
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setDeviceType(deviceType);
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));

                profileList.add(profile);
            }
            return profileList;
        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile list from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting the device type.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while getting the features related to a profile.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        }

    }


    private Connection getConnection() throws ProfileManagerDAOException {
        try {
            return PolicyManagementDAOFactory.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new ProfileManagerDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository datasource", e);
        }
    }

}
