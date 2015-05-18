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

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements FeatureDAO {

    private static final Log log = LogFactory.getLog(FeatureDAOImpl.class);


    @Override
    public Feature addFeature(Feature feature) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_FEATURES (NAME, CODE, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, feature.getName());
            stmt.setString(2, feature.getCode());
            stmt.setString(3, feature.getDescription());
//            stmt.setString(4, feature.getRuleValue());
//            stmt.setInt(5, feature.getDeviceTypeId());
            int affectedRows = stmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug(affectedRows + " feature is added.");
            }
            generatedKeys = stmt.getGeneratedKeys();

            while (generatedKeys.next()) {
                feature.setId(generatedKeys.getInt(1));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding feature to the database.";
            log.error(msg, e);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return feature;
    }

    @Override
    public List<Feature> addFeatures(List<Feature> features) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        List<Feature> featureList = new ArrayList<Feature>();

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_FEATURES (NAME, CODE, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            for (Feature feature : features) {
                stmt.setString(1, feature.getName());
                stmt.setString(2, feature.getCode());
                stmt.setString(3, feature.getDescription());
//                stmt.setString(4, feature.getRuleValue());
//                stmt.setInt(5, feature.getDeviceTypeId());
                stmt.addBatch();
            }

            int[] affectedRows = stmt.executeBatch();

            generatedKeys = stmt.getGeneratedKeys();

            if (log.isDebugEnabled()) {
                log.debug(affectedRows.length + " features are added to the database.");
            }
            generatedKeys = stmt.getGeneratedKeys();
            int i = 0;

            while (generatedKeys.next()) {
                features.get(i).setId(generatedKeys.getInt(1));
                i++;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding feature to the database.";
            log.error(msg, e);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return featureList;
    }


    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_FEATURES SET NAME = ?, CODE = ?, DESCRIPTION = ? WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, feature.getName());
            stmt.setString(2, feature.getCode());
            stmt.setString(3, feature.getDescription());
//            stmt.setString(4, feature.getRuleValue());
            stmt.setInt(4, feature.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            String msg = "Error occurred while updating feature " + feature.getName() + " (Feature Name) to the database.";
            log.error(msg, e);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

        return feature;
    }

    @Override
    public ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException {
        return null;
    }

    @Override
    public ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException {
        return null;
    }

    @Override
    public List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_PROFILE_FEATURES (PROFILE_ID, FEATURE_CODE, DEVICE_TYPE_ID, CONTENT) " +
                    "VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

            for (ProfileFeature feature : features) {
                stmt.setInt(1, profileId);
                stmt.setString(2, feature.getFeatureCode());
                stmt.setInt(3, feature.getDeviceTypeId());
                if (conn.getMetaData().getDriverName().contains("H2")) {
                    stmt.setObject(4, feature.getContent(), Types.JAVA_OBJECT);
                } else {
                    stmt.setObject(4, feature.getContent());
                }
                stmt.addBatch();
                //Not adding the logic to check the size of the stmt and execute if the size records added is over 1000
            }
            stmt.executeBatch();

            generatedKeys = stmt.getGeneratedKeys();
            int i = 0;

            while (generatedKeys.next()) {
                features.get(i).setId(generatedKeys.getInt(1));
                i++;
            }

        } catch (SQLException e) {
            String msg = "Error occurred while adding the feature list to the database.";
            log.error(msg, e);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
        return features;
    }

    @Override
    public List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_PROFILE_FEATURES SET CONTENT = ? WHERE PROFILE_ID = ?, FEATURE_CODE = ?";

            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            for (ProfileFeature feature : features) {
                if (conn.getMetaData().getDriverName().contains("H2")) {
                    stmt.setObject(4, feature.getContent(), Types.JAVA_OBJECT);
                } else {
                    stmt.setObject(4, feature.getContent());
                }
                stmt.setInt(2, profileId);
                stmt.setString(3, feature.getFeatureCode());
                stmt.addBatch();
                //Not adding the logic to check the size of the stmt and execute if the size records added is over 1000
            }
            stmt.executeBatch();

        } catch (SQLException e) {
            String msg = "Error occurred while adding the feature list to the database.";
            log.error(msg, e);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return features;
    }

    @Override
    public boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE_FEATURES WHERE PROFILE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profile.getProfileId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            String msg = "Error occurred while deleting the feature related to a profile.";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public List<ProfileFeature> getAllProfileFeatures() throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ProfileFeature> featureList = new ArrayList<ProfileFeature>();

        try {
            conn = this.getConnection();
            String query = "SELECT ID, PROFILE_ID, FEATURE_CODE, DEVICE_TYPE_ID, CONTENT FROM DM_PROFILE_FEATURES";
            stmt = conn.prepareStatement(query);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                ProfileFeature profileFeature = new ProfileFeature();
//                Feature feature = new Feature();
//                feature.setId(resultSet.getInt("FEATURE_ID"));
//                feature.setCode(resultSet.getString("CODE"));
//                feature.setName(resultSet.getString("NAME"));
//                feature.setRuleValue(resultSet.getString("RULE"));

//                profileFeature.setFeature(feature);
                profileFeature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                profileFeature.setDeviceTypeId(resultSet.getInt("DEVICE_TYPE_ID"));
                profileFeature.setId(resultSet.getInt("ID"));
//                profileFeature.setContent(resultSet.getObject("CONTENT"));
                profileFeature.setProfileId(resultSet.getInt("PROFILE_ID"));

                ByteArrayInputStream bais = null;
                ObjectInputStream ois = null;
                byte[] contentBytes;
                try {
                    contentBytes = (byte[]) resultSet.getBytes("CONTENT");
                    bais = new ByteArrayInputStream(contentBytes);
                    ois = new ObjectInputStream(bais);
                    profileFeature.setContent(ois.readObject().toString());
                } finally {
                    if (bais != null) {
                        try {
                            bais.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ByteArrayOutputStream", e);
                        }
                    }
                    if (ois != null) {
                        try {
                            ois.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ObjectOutputStream", e);
                        }
                    }
                }

                featureList.add(profileFeature);
            }

        } catch (SQLException e) {
            String msg = "Unable to get the list of the features from database.";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Unable to read the byte stream for content";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "Class not found while converting the object";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return featureList;
    }

    @Override
    public List<Feature> getAllFeatures(String deviceType) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Feature> featureList = new ArrayList<Feature>();

        try {
            conn = this.getConnection();
            String query = "SELECT f.ID ID, f.NAME NAME, f.CODE CODE, f.DEVICE_TYPE_ID DEVICE_TYPE_ID," +
                    " f.EVALUATION_RULE EVALUATION_RULE FROM DM_FEATURES f INNER JOIN DM_DEVICE_TYPE d " +
                    "ON d.ID=f.DEVICE_TYPE_ID WHERE d.NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                Feature feature = new Feature();
                feature.setId(resultSet.getInt("ID"));
                feature.setCode(resultSet.getString("CODE"));
                feature.setName(resultSet.getString("NAME"));
//                feature.setDeviceTypeId(resultSet.getInt("DEVICE_TYPE_ID"));
//                feature.setRuleValue(resultSet.getString("EVALUATION_RULE"));
                featureList.add(feature);
            }

        } catch (SQLException e) {
            String msg = "Unable to get the list of the features related device type from database.";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return featureList;
    }

    @Override
    public List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ProfileFeature> featureList = new ArrayList<ProfileFeature>();

        try {
            conn = this.getConnection();
            String query = "SELECT ID, FEATURE_CODE, DEVICE_TYPE_ID, CONTENT FROM DM_PROFILE_FEATURES " +
                    "WHERE PROFILE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                ProfileFeature profileFeature = new ProfileFeature();
                profileFeature.setId(resultSet.getInt("ID"));
                profileFeature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                profileFeature.setDeviceTypeId(resultSet.getInt("DEVICE_TYPE_ID"));

                ByteArrayInputStream bais = null;
                ObjectInputStream ois = null;
                byte[] contentBytes;
                try {
                    contentBytes = resultSet.getBytes("CONTENT");
                    bais = new ByteArrayInputStream(contentBytes);
                    ois = new ObjectInputStream(bais);
                    profileFeature.setContent(ois.readObject().toString());
                } finally {
                    if (bais != null) {
                        try {
                            bais.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ByteArrayOutputStream", e);
                        }
                    }
                    if (ois != null) {
                        try {
                            ois.close();
                        } catch (IOException e) {
                            log.warn("Error occurred while closing ObjectOutputStream", e);
                        }
                    }
                }

                featureList.add(profileFeature);
            }

        } catch (SQLException e) {
            String msg = "Unable to get the list of the features from database.";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Unable to read the byte stream for content";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "Class not found while converting the object";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
            this.closeConnection();
        }
        return featureList;
    }

    @Override
    public boolean deleteFeature(int featureId) throws FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_FEATURES WHERE ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, featureId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            String msg = "Unable to delete the feature " + featureId + " (Feature ID) from database.";
            log.error(msg);
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws FeatureManagerDAOException {

        try {
            return PolicyManagementDAOFactory.getConnection();
        } catch (PolicyManagerDAOException e) {
            throw new FeatureManagerDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository datasource", e);
        }
    }

    private void closeConnection() {

        try {
            PolicyManagementDAOFactory.closeConnection();
        } catch (PolicyManagerDAOException e) {
            log.warn("Unable to close the database connection.");
        }
    }
}
