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


package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;

import java.util.List;

/**
 * This interface represents the key operations related to profile features of device policies.
 */
public interface FeatureDAO {

    /**
     * This method is used to add a feature related to given profile.
     *
     * @param feature consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns ProfileFeature object.
     * @throws FeatureManagerDAOException
     */
    ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to update a feature related to given profile.
     * @param feature consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns updated ProfileFeature object.
     * @throws FeatureManagerDAOException
     */
    ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to add set of features to a given profile.
     *
     * @param features consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagerDAOException;

    /**
     * This method is used to update set of features to a given profile.
     *
     * @param features consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features.
     *
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> getAllProfileFeatures() throws FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features based on device type.
     *
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<Feature> getAllFeatures(String deviceType) throws FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features of given profile.
     *
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used remove a feature.
     *
     * @param featureId id of the removing feature.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteFeature(int featureId) throws FeatureManagerDAOException;

    /**
     * This method is used to remove set of features of given profile.
     *
     * @param profile that contains features to be removed.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException;

    /**
     * This method is used to remove set of features of given profile id.
     *
     * @param profileId id of the profile.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteFeaturesOfProfile(int profileId) throws FeatureManagerDAOException;

    boolean deleteProfileFeatures(int featureId) throws FeatureManagerDAOException;

}
