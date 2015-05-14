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

package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.common.ProfileManagementException;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.FeatureManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.ProfileManagerImpl;

import java.util.List;

public class PolicyAdministratorPointImpl implements PolicyAdministratorPoint {

    private static final Log log = LogFactory.getLog(PolicyAdministratorPointImpl.class);


    private PolicyManager policyManager;
    private ProfileManager profileManager;
    private FeatureManager featureManager;

    public PolicyAdministratorPointImpl() {

        policyManager = new PolicyManagerImpl();
        profileManager = new ProfileManagerImpl();
        featureManager = new FeatureManagerImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        return policyManager.addPolicy(policy);
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        return policyManager.updatePolicy(policy);
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        return policyManager.deletePolicy(policy);
    }

    @Override
    public Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws PolicyManagementException {
        return policyManager.addPolicyToDevice(deviceIdentifierList, policy);
    }

    @Override
    public Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException {
        return policyManager.addPolicyToRole(roleNames, policy);
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {
        return policyManager.getPolicies();
    }

    @Override public Policy getPolicy(int policyId) throws PolicyManagementException {
        return policyManager.getPolicy(policyId);
    }

    @Override
    public List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.getPoliciesOfDevice(deviceIdentifier);
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceType) throws PolicyManagementException {
        return policyManager.getPoliciesOfDeviceType(deviceType);
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException {
        return policyManager.getPoliciesOfRole(roleName);
    }

    @Override
    public List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException {
        return policyManager.getPoliciesOfUser(username);
    }

    @Override
    public boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.checkPolicyAvailable(deviceIdentifier);
    }

    @Override
    public boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.setPolicyApplied(deviceIdentifier);
    }

    @Override
    public void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException {
        policyManager.addAppliedPolicyToDevice(deviceIdentifier, policy.getId(), policy.getProfile().getProfileFeaturesList());
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.addProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public boolean deleteProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.deleteProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while deleting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        try {
            return profileManager.updateProfile(profile);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while persisting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Profile getProfile(int profileId) throws PolicyManagementException {
        try {
            return profileManager.getProfile(profileId);
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while retriving profile";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Profile> getProfiles() throws PolicyManagementException {
        try {
            return profileManager.getAllProfiles();
        } catch (ProfileManagementException e) {
            String msg = "Error occurred while obtaining list of profiles.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        try {
            return featureManager.addFeature(feature);
        } catch (FeatureManagementException e) {
            String msg = "Error occurred while persisting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        try {
            return featureManager.updateFeature(feature);
        } catch (FeatureManagementException e) {
            String msg = "Error occurred while persisting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }

    @Override
    public boolean deleteFeature(int featureId) throws FeatureManagementException {
        try {
            return featureManager.deleteFeature(featureId);
        } catch (FeatureManagementException e) {
            String msg = "Error occurred while deleting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }
}
