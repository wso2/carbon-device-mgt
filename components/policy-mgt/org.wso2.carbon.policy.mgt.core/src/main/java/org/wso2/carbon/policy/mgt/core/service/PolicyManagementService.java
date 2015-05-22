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

package org.wso2.carbon.policy.mgt.core.service;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyInformationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;

import java.util.List;

public class PolicyManagementService implements PolicyManagerService {


    PolicyManagerService policyManagerService;

    public PolicyManagementService() {
        policyManagerService = new PolicyManagerServiceImpl();
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        return policyManagerService.addFeature(feature);
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        return policyManagerService.updateFeature(feature);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        return policyManagerService.addProfile(profile);
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        return policyManagerService.updateProfile(profile);
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        return policyManagerService.addPolicy(policy);
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        return policyManagerService.updatePolicy(policy);
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        return policyManagerService.deletePolicy(policy);
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        return policyManagerService.deletePolicy(policyId);
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManagerService.getEffectivePolicy(deviceIdentifier);
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws
            FeatureManagementException {
        return policyManagerService.getEffectiveFeatures(deviceIdentifier);
    }

    @Override
    public List<Policy> getPolicies(String deviceType) throws PolicyManagementException {
        return policyManagerService.getPolicies(deviceType);
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementException {
        return policyManagerService.getFeatures();
    }

    @Override
    public PolicyAdministratorPoint getPAP() throws PolicyManagementException {
        return policyManagerService.getPAP();
    }

    @Override
    public PolicyInformationPoint getPIP() throws PolicyManagementException {
        return policyManagerService.getPIP();
    }

    @Override
    public PolicyEvaluationPoint getPEP() throws PolicyManagementException {
        return policyManagerService.getPEP();
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        return policyManagerService.getPolicyCount();
    }
}
