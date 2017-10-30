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

package org.wso2.carbon.policy.mgt.core.dao.impl.feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of FeatureDAO which holds generic SQL queries.
 */
public abstract class AbstractFeatureDAO implements FeatureDAO {

    private static final Log log = LogFactory.getLog(AbstractFeatureDAO.class);

    @Override
    public ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException {
        return null;
    }

    @Override
    public ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws
                                                                                      FeatureManagerDAOException {
        return null;
    }

    @Override
    public List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws
                                                                                                    FeatureManagerDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "UPDATE DM_PROFILE_FEATURES SET CONTENT = ? WHERE PROFILE_ID = ? AND FEATURE_CODE = ? AND" +
                           " TENANT_ID = ?";

            stmt = conn.prepareStatement(query);
            for (ProfileFeature feature : features) {
                stmt.setBytes(1, PolicyManagerUtil.getBytes(feature.getContent()));
                stmt.setInt(2, profileId);
                stmt.setString(3, feature.getFeatureCode());
                stmt.setInt(4, tenantId);
                stmt.addBatch();
                //Not adding the logic to check the size of the stmt and execute if the size records added is over 1000
            }
            stmt.executeBatch();

        } catch (SQLException | IOException e) {
            throw new FeatureManagerDAOException("Error occurred while adding the feature list to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
        return features;
    }

    @Override
    public boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE_FEATURES WHERE PROFILE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profile.getProfileId());
            stmt.setInt(2, tenantId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Error occurred while deleting the feature related to a profile.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean deleteFeaturesOfProfile(int profileId) throws FeatureManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE_FEATURES WHERE PROFILE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            stmt.setInt(2, tenantId);
            if (stmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Error occurred while deleting the feature related to a profile.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public boolean deleteProfileFeatures(int featureId) throws FeatureManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_PROFILE_FEATURES WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, featureId);
            stmt.setInt(2, tenantId);
            if (stmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Error occurred while deleting the feature related to a profile.", e);
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
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT ID, PROFILE_ID, FEATURE_CODE, DEVICE_TYPE, CONTENT FROM DM_PROFILE_FEATURES " +
                           "WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                ProfileFeature profileFeature = new ProfileFeature();
                profileFeature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                profileFeature.setDeviceType(resultSet.getString("DEVICE_TYPE"));
                profileFeature.setId(resultSet.getInt("ID"));
                profileFeature.setProfileId(resultSet.getInt("PROFILE_ID"));

                ByteArrayInputStream bais = null;
                ObjectInputStream ois = null;
                byte[] contentBytes;
                try {
                    contentBytes = (byte[]) resultSet.getBytes("CONTENT");
                    bais = new ByteArrayInputStream(contentBytes);
                    ois = new ObjectInputStream(bais);
                    profileFeature.setContent((Object) ois.readObject().toString());
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
            throw new FeatureManagerDAOException("Unable to get the list of the features from database.", e);
        } catch (IOException e) {
            throw new FeatureManagerDAOException("Unable to read the byte stream for content", e);
        } catch (ClassNotFoundException e) {
            throw new FeatureManagerDAOException("Class not found while converting the object", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
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
            String query = "SELECT f.ID ID, f.NAME NAME, f.CODE CODE, f.DEVICE_TYPE DEVICE_TYPE," +
                           " f.EVALUATION_RULE EVALUATION_RULE FROM DM_FEATURES f INNER JOIN DM_DEVICE_TYPE d " +
                           "ON d.ID=f.DEVICE_TYPE WHERE d.NAME = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, deviceType);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {

                Feature feature = new Feature();
                feature.setId(resultSet.getInt("ID"));
                feature.setCode(resultSet.getString("CODE"));
                feature.setName(resultSet.getString("NAME"));
                featureList.add(feature);
            }
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Unable to get the list of the features related device type " +
                                                 "from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ProfileFeature> featureList = new ArrayList<ProfileFeature>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT ID, FEATURE_CODE, DEVICE_TYPE, CONTENT FROM DM_PROFILE_FEATURES " +
                           "WHERE PROFILE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, profileId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                ProfileFeature profileFeature = new ProfileFeature();
                profileFeature.setId(resultSet.getInt("ID"));
                profileFeature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                profileFeature.setDeviceType(resultSet.getString("DEVICE_TYPE"));

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
            throw new FeatureManagerDAOException("Unable to get the list of the features from database.", e);
        } catch (IOException e) {
            throw new FeatureManagerDAOException("Unable to read the byte stream for content", e);
        } catch (ClassNotFoundException e) {
            throw new FeatureManagerDAOException("Class not found while converting the object", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public boolean deleteFeature(int featureId) throws FeatureManagerDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_FEATURES WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, featureId);
            stmt.setInt(2, tenantId);
            if(stmt.executeUpdate() > 0) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Unable to delete the feature " + featureId + " (Feature ID) " +
                                                 "from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws FeatureManagerDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }
}
