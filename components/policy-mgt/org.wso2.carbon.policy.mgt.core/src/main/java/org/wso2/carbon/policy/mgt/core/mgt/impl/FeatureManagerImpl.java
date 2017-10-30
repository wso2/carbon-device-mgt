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

package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.dao.FeatureDAO;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;

import java.sql.SQLException;
import java.util.List;

public class FeatureManagerImpl implements FeatureManager {

    private FeatureDAO featureDAO;
    private static Log log = LogFactory.getLog(FeatureManagerImpl.class);

    public FeatureManagerImpl() {
        featureDAO = PolicyManagementDAOFactory.getFeatureDAO();
    }

    /*@Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            feature = featureDAO.addFeature(feature);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while adding feature (" + feature.getName() + ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        } catch (FeatureManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while adding feature (" + feature.getName() + ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
        return feature;
    }*/

    /*@Override
    public List<Feature> addFeatures(List<Feature> features) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            features = featureDAO.addFeatures(features);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while adding feature (" + features.size()+ ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        } catch (FeatureManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while adding feature (" + features.size() + ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }

        return features;
    }*/

   /* @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            feature = featureDAO.updateFeature(feature);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (PolicyManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while updating feature (" + feature.getName() + ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        } catch (FeatureManagerDAOException e) {
            try {
                PolicyManagementDAOFactory.rollbackTransaction();
            } catch (PolicyManagerDAOException e1) {
                log.warn("Unable to roll back the transaction");
            }
            String msg = "Error occurred while updating feature (" + feature.getName() + ")";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
        return feature;
    }*/

    @Override
    public boolean deleteFeature(Feature feature) throws FeatureManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            bool =  featureDAO.deleteFeature(feature.getId());
            PolicyManagementDAOFactory.commitTransaction();
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature (" + feature.getName() +
                    ")", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature (" + feature.getName() +
                    ") from database", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return bool;
    }

    @Override
    public ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            feature = featureDAO.addProfileFeature(feature, profileId);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (PolicyManagerDAOException | FeatureManagerDAOException e) {
            throw new FeatureManagementException("Error occurred while adding profile feature (" +
                    feature.getFeatureCode() + " - " + profileId + ")", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return feature;
    }

    @Override
    public ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws
            FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            feature = featureDAO.updateProfileFeature(feature, profileId);
            PolicyManagementDAOFactory.commitTransaction();

        } catch (FeatureManagerDAOException | PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while updating feature (" +
                    feature.getFeatureCode() + " - " + profileId + ") in database.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return feature;
    }

    @Override
    public List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            features = featureDAO.addProfileFeatures(features, profileId);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while adding the features to profile id (" +
                    profileId + ")", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while adding the features to profile id (" +
                    profileId + ") to the database", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return features;
    }

    @Override
    public List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagementException {
        try {
            PolicyManagementDAOFactory.beginTransaction();
            features = featureDAO.updateProfileFeatures(features, profileId);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while updating the features to profile id (" +
                    profileId + ")", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while updating the features to profile id (" +
                    profileId + ") to the database", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return features;
    }
    

    @Override
    public List<Feature> getAllFeatures(String deviceType) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return featureDAO.getAllFeatures(deviceType);
        } catch (FeatureManagerDAOException e) {
            throw new FeatureManagementException("Error occurred while retrieving the features", e);
        } catch (SQLException e) {
            throw new FeatureManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagementException {
        try {
            PolicyManagementDAOFactory.openConnection();
            return featureDAO.getFeaturesForProfile(profileId);
        } catch (FeatureManagerDAOException e) {
            throw new FeatureManagementException("Error occurred while getting the features", e);
        } catch (SQLException e) {
            throw new FeatureManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deleteFeature(int featureId) throws FeatureManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            bool = featureDAO.deleteFeature(featureId);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature - id (" +
                    featureId + ")", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature - id (" + featureId +
                    ") from database.", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return bool;
    }

    @Override
    public boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagementException {
        boolean bool;
        try {
            PolicyManagementDAOFactory.beginTransaction();
            bool = featureDAO.deleteFeaturesOfProfile(profile);
            PolicyManagementDAOFactory.commitTransaction();
        } catch (FeatureManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature of - profile (" +
                    profile.getProfileName() + ")", e);
        } catch (PolicyManagerDAOException e) {
            PolicyManagementDAOFactory.rollbackTransaction();
            throw new FeatureManagementException("Error occurred while deleting the feature of - profile (" +
                    profile.getProfileName() + ") from database", e);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
        return bool;
    }
}
