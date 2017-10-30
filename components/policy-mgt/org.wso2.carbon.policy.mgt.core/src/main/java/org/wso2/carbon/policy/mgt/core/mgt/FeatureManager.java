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


package org.wso2.carbon.policy.mgt.core.mgt;


import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;

import java.util.List;

public interface FeatureManager {

    /*Feature addFeature(Feature feature) throws FeatureManagementException;

    public List<Feature> addFeatures(List<Feature> features) throws FeatureManagementException;

    Feature updateFeature(Feature feature) throws FeatureManagementException;*/

    boolean deleteFeature(Feature feature) throws FeatureManagementException;

    ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagementException;

    ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagementException;

    List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagementException;

    List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws FeatureManagementException;


    List<Feature> getAllFeatures(String deviceType) throws FeatureManagementException;

    List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagementException;

    boolean deleteFeature(int featureId) throws FeatureManagementException;

    boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagementException;
}
