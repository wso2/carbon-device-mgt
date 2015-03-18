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

package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;

import java.util.List;

public interface PolicyDAO {

    Policy addPolicy(Policy policy) throws PolicyManagerDAOException;

    Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToRole(String roleName, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicy(String deviceID, String deviceType, Policy policy) throws PolicyManagerDAOException;

    Policy updatePolicy(Policy policy) throws PolicyManagerDAOException;

    Policy getPolicy() throws PolicyManagerDAOException;

    Policy getPolicy(String deviceType) throws PolicyManagerDAOException;

    Policy getPolicy(String deviceID, String deviceType) throws PolicyManagerDAOException;

    void deletePolicy(Policy policy) throws PolicyManagerDAOException;

    Profile addProfile(Profile profile) throws PolicyManagerDAOException;

    Profile updateProfile(Profile profile) throws PolicyManagerDAOException;

    void deleteProfile(Profile profile) throws PolicyManagerDAOException;

    List<Profile> getAllProfiles() throws PolicyManagerDAOException;

    List<Profile> getProfilesOfDeviceType(String deviceType) throws PolicyManagerDAOException;

    List<Feature> getAllFeatures() throws PolicyManagerDAOException;

    List<Feature> getFeaturesForProfile(int ProfileId) throws PolicyManagerDAOException;

    void deleteFeature(int featureId) throws PolicyManagerDAOException;

    void deleteFeaturesOfProfile(Profile profile) throws PolicyManagerDAOException;

    Feature addFeature(Feature feature) throws PolicyManagerDAOException, FeatureManagementException;

    Feature updateFeature(Feature feature) throws PolicyManagerDAOException, FeatureManagementException;
}
