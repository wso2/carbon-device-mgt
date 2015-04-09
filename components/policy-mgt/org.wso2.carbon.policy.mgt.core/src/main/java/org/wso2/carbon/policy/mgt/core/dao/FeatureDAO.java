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

import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;

import java.util.List;

public interface FeatureDAO {

    Feature addFeature(Feature feature) throws FeatureManagerDAOException;

    Feature updateFeature(Feature feature) throws FeatureManagerDAOException;

    ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagerDAOException;

    List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagerDAOException;

    List<Feature> getAllFeatures() throws FeatureManagerDAOException;

    List<Feature> getAllFeatures(String deviceType) throws FeatureManagerDAOException;

    List<ProfileFeature> getFeaturesForProfile(int ProfileId) throws FeatureManagerDAOException;

    boolean deleteFeature(int featureId) throws FeatureManagerDAOException;

    boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException;

}
