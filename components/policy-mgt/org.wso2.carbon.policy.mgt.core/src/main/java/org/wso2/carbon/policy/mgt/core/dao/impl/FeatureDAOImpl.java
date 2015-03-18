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
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.dao.*;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements FeatureDAO {

    private static final Log log = LogFactory.getLog(FeatureDAOImpl.class);


    @Override
    public Feature addFeature(Feature feature) throws FeatureManagerDAOException {
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
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, generatedKeys);
        }
        return feature;
    }


    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagerDAOException {
        return feature;
    }

    @Override
    public void deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException {
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
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }


    @Override
    public List<Feature> getAllFeatures() throws FeatureManagerDAOException {
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
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public List<Feature> getFeaturesForProfile(int profileId) throws FeatureManagerDAOException {
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
            throw new FeatureManagerDAOException(msg, e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return featureList;
    }

    @Override
    public void deleteFeature(int featureId) throws FeatureManagerDAOException {

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

    private Connection getConnection() throws FeatureManagerDAOException {
        try {
            return PolicyManagementDAOFactory.getDataSource().getConnection();
        } catch (SQLException e) {
            throw new FeatureManagerDAOException("Error occurred while obtaining a connection from the policy " +
                    "management metadata repository datasource", e);
        }
    }
}
