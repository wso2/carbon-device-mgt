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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.ProfileDAO;
import org.wso2.carbon.policy.mgt.core.dao.ProfileManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAOImpl implements ProfileDAO {

    private static final Log log = LogFactory.getLog(ProfileDAOImpl.class);

    public Profile addProfile(Profile profile) throws ProfileManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE " +
                    "(PROFILE_NAME, TENANT_ID, DEVICE_TYPE, CREATED_TIME, UPDATED_TIME) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, new String[] {"id"});

            stmt.setString(1, profile.getProfileName());
            stmt.setInt(2, tenantId);
            stmt.setString(3, profile.getDeviceType());
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

        } catch (SQLException e) {
            String msg = "Error occurred while adding the profile to database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return profile;
    }


    public Profile updateProfile(Profile profile) throws ProfileManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
      //  ResultSet generatedKeys = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_PROFILE SET PROFILE_NAME = ? , DEVICE_TYPE = ? , UPDATED_TIME = ? " +
                    "WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, profile.getProfileName());
            stmt.setString(2, profile.getDeviceType());
            stmt.setTimestamp(3, profile.getUpdatedDate());
            stmt.setInt(4, profile.getProfileId());
            stmt.setInt(5, tenantId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0 && log.isDebugEnabled()) {
                String msg = "No rows are updated on the profile table.";
                log.debug(msg);
            }
//            generatedKeys = stmt.getGeneratedKeys();
//
//            if (generatedKeys.next()) {
//                profile.setProfileId(generatedKeys.getInt(1));
//            }
//            // Checking the profile id here, because profile id could have been passed from the calling method.
//            if (profile.getProfileId() == 0) {
//                throw new RuntimeException("Profile id is 0, this could be an issue.");
//            }

        } catch (SQLException e) {
            String msg = "Error occurred while updating the profile (" + profile.getProfileName() + ") in database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return profile;
    }

    @Override
    public boolean deleteProfile(Profile profile) throws ProfileManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profile.getProfileId());
            if (stmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the profile from the data base.";
            log.error(msg);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean deleteProfile(int profileId) throws ProfileManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            if (stmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the profile from the data base.";
            log.error(msg);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public Profile getProfile(int profileId) throws ProfileManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Profile profile = new Profile();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                profile.setProfileId(profileId);
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setDeviceType(resultSet.getString("DEVICE_TYPE"));
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return profile;
    }


    @Override
    public List<Profile> getAllProfiles() throws ProfileManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Profile> profileList = new ArrayList<>();

        try {
            //TODO : Fix with TenantID.
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                Profile profile = new Profile();
                profile.setProfileId(resultSet.getInt("ID"));
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));
                profile.setDeviceType(resultSet.getString("DEVICE_TYPE"));

                profileList.add(profile);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile list from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return profileList;
    }

    @Override
    public List<Profile> getProfilesOfDeviceType(String deviceType) throws ProfileManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Profile> profileList = new ArrayList<>();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_PROFILE WHERE DEVICE_TYPE = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Profile profile = new Profile();
                profile.setProfileId(resultSet.getInt("ID"));
                profile.setProfileName(resultSet.getString("PROFILE_NAME"));
                profile.setTenantId(resultSet.getInt("TENANT_ID"));
                profile.setDeviceType(resultSet.getString("DEVICE_TYPE"));
                profile.setCreatedDate(resultSet.getTimestamp("CREATED_TIME"));
                profile.setUpdatedDate(resultSet.getTimestamp("UPDATED_TIME"));

                profileList.add(profile);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while reading the profile list from the database.";
            log.error(msg, e);
            throw new ProfileManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return profileList;
    }


    private Connection getConnection() throws ProfileManagerDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }

}
