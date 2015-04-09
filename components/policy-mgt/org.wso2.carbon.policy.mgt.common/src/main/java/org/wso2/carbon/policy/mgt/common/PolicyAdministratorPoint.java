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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

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
     * @param deviceIdentifierr
     * @param policy
     * @return primary key (generated key)
     */

    Policy addPolicyToDevice(DeviceIdentifier deviceIdentifierr, Policy policy) throws FeatureManagementException, PolicyManagementException;

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

    List<Policy> getPolicies() throws PolicyManagementException;

    /**
     * This method gives the device specific policy.
     *
     * @param deviceId
     * @param deviceType
     * @return Policy
     */

    List<Policy> getPoliciesOfDevice(String deviceId, String deviceType) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method returns the device type specific policy.
     *
     * @param deviceType
     * @return Policy
     */

    List<Policy> getPoliciesOfDeviceType(String deviceType) throws FeatureManagementException, PolicyManagementException;

    /**
     * This method returns the role specific policy.
     *
     * @param roleName
     * @return
     */

    List<Policy> getPoliciesOfRole(String roleName) throws FeatureManagementException, PolicyManagementException;


    /**
     * This method checks weather a policy is available for a device.
     *
     * @param deviceIdentifier
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;


    /**
     * This method checks weather a policy is used by a particular device.
     *
     * @param deviceIdentifier
     * @return
     * @throws PolicyManagementException
     */
    boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;


    /**
     * @param deviceIdentifier
     * @param policy
     * @throws PolicyManagementException
     */
    void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException;

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
