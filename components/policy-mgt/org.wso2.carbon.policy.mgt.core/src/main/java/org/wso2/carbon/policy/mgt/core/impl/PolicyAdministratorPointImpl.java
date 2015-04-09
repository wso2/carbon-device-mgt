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
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.ProfileManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.impl.FeatureDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.PolicyDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.ProfileDAOImpl;

import java.util.List;

public class PolicyAdministratorPointImpl implements PolicyAdministratorPoint {

    private static final Log log = LogFactory.getLog(PolicyAdministratorPointImpl.class);

    PolicyDAOImpl policyDAO;
    FeatureDAOImpl featureDAO;
    ProfileDAOImpl profileDAO;

    public PolicyAdministratorPointImpl() {
        policyDAO = new PolicyDAOImpl();
        featureDAO = new FeatureDAOImpl();
        profileDAO = new ProfileDAOImpl();
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        try {
            policy = policyDAO.addPolicy(policy);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        try {
            policy = policyDAO.updatePolicy(policy);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while updating the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy)
            throws FeatureManagementException, PolicyManagementException {

        try {
            policy = policyDAO.addPolicyToDevice(deviceIdentifier, policy);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public Policy addPolicyToRole(String roleName, Policy policy)
            throws FeatureManagementException, PolicyManagementException {
        try {
            policy = policyDAO.addPolicyToRole(roleName, policy);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
        return policy;
    }

    @Override
    public List<Policy> getPolicies() throws PolicyManagementException {
        try {
            return policyDAO.getPolicy();
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policies.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Policy> getPoliciesOfDevice(String deviceId, String deviceType)
            throws FeatureManagementException, PolicyManagementException {
        return null;
    }

    @Override
    public List<Policy> getPoliciesOfDeviceType(String deviceType)
            throws FeatureManagementException, PolicyManagementException {
        try {
            return policyDAO.getPolicy(deviceType);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policy related to device type.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<Policy> getPoliciesOfRole(String roleName) throws FeatureManagementException, PolicyManagementException {
        try {
            return policyDAO.getPolicyOfRole(roleName);
        } catch (PolicyManagerDAOException e) {
            String msg = "Error occurred while getting the policy related to role name.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public boolean isPolicyAvailableForDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return false;
    }

    @Override
    public boolean isPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return false;
    }

    @Override
    public void setPolicyUsed(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException {

    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        try {
            profile = profileDAO.addProfile(profile);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while persisting the policy.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

        return profile;
    }

    @Override
    public boolean deleteProfile(int profileId) throws PolicyManagementException {
        return false;
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        try {
            profile = profileDAO.updateProfile(profile);
        } catch (ProfileManagerDAOException e) {
            String msg = "Error occurred while persisting the profile.";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }

        return profile;
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        try {
            feature = featureDAO.addFeature(feature);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while persisting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }

        return feature;
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        try {
            feature = featureDAO.updateFeature(feature);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while persisting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
        return feature;
    }

    @Override
    public void deleteFeature(int featureId) throws FeatureManagementException {
        try {
            featureDAO.deleteFeature(featureId);
        } catch (FeatureManagerDAOException e) {
            String msg = "Error occurred while deleting the feature.";
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }
}
