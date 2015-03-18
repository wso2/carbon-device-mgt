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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.PolicyManager;
import org.wso2.carbon.policy.mgt.core.PolicyManagerImpl;

import java.util.List;

public class PolicyManagementService implements PolicyManager {


    PolicyManager policyManager;

    public PolicyManagementService() {
        policyManager = new PolicyManagerImpl();
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        return policyManager.addFeature(feature);
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        return policyManager.updateFeature(feature);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        return policyManager.addProfile(profile);
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        return policyManager.updateProfile(profile);
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
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.getEffectivePolicy(deviceIdentifier);
    }

    @Override
    public Policy getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws FeatureManagementException {
        return policyManager.getEffectiveFeatures(deviceIdentifier);
    }

    @Override
    public List<Policy> getPolicies(String deviceType) throws PolicyManagementException {
        return policyManager.getPolicies(deviceType);
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementException {
        return policyManager.getFeatures();
    }
}
