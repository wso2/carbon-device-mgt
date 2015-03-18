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

package org.wso2.carbon.policy.mgt.common;

/**
 * This interface defines the policy management which should be implemented by the plugins
 */

public interface PolicyAdministratorPoint {

    /**
     * This method adds a policy to the platform
     *
     * @param policy
     * @return primary key (generated key)
     */

    Policy addPolicy(Policy policy) throws PolicyManagementException;


    Policy updatePolicy(Policy policy) throws PolicyManagementException;

    /**
     * This method adds a policy per device which should be implemented by the related plugins.
     *
     * @param deviceId
     * @param deviceType
     * @param policy
     * @return primary key (generated key)
     */

    Policy addPolicyToDevice(String deviceId, String deviceType, Policy policy) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method adds the policy to specific role.
     *
     * @param roleName
     * @param policy
     * @return primary key (generated key)
     */
    Policy addPolicyToRole(String roleName, Policy policy) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method returns the policy of whole platform
     *
     * @return
     */

    Policy getPolicy();

    /**
     * This method gives the device specific policy.
     *
     * @param deviceId
     * @param deviceType
     * @return Policy
     */

    Policy getPolicyOfDevice(String deviceId, String deviceType) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method returns the device type specific policy.
     *
     * @param deviceType
     * @return Policy
     */

    Policy getPolicyOfDeviceType(String deviceType) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method returns the role specific policy.
     *
     * @param roleName
     * @return
     */

    Policy getPolicyOfRole(String roleName) throws FeatureManagementException, PolicyManagementException;


    /**
     * This method checks weather a policy is available for a device.
     *
     * @param deviceId
     * @param deviceType
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyAvailableForDevice(String deviceId, String deviceType) throws PolicyManagementException;


    /**
     * This method checks weather a policy is used by a particular device.
     *
     * @param deviceId
     * @param deviceType
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyApplied(String deviceId, String deviceType) throws PolicyManagementException;


    /**
     * @param deviceId
     * @param deviceType
     * @param policy
     * @throws PolicyManagementException
     */
    void setPolicyUsed(String deviceId, String deviceType, Policy policy) throws PolicyManagementException;

    /**
     * This method will add the profile to database,
     * @param profile
     * @throws PolicyManagementException
     */
    Profile addProfile(Profile profile) throws PolicyManagementException;

    boolean deleteProfile(int profileId) throws PolicyManagementException;

    Profile updateProfile(Profile profile) throws PolicyManagementException;

    Feature addFeature(Feature feature) throws  FeatureManagementException;

    Feature updateFeature(Feature feature) throws  FeatureManagementException;

    void deleteFeature(int featureId)   throws  FeatureManagementException;

}
