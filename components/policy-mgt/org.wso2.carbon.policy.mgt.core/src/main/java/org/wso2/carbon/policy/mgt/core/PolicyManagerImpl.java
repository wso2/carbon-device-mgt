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

package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.Profile;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;

import java.util.List;

public class PolicyManagerImpl implements PolicyManager {

    private static final Log log = LogFactory.getLog(PolicyManagerImpl.class);

    PolicyAdministratorPointImpl policyAdministratorPoint;

    public PolicyManagerImpl() {
        policyAdministratorPoint = new PolicyAdministratorPointImpl();
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        return policyAdministratorPoint.addFeature(feature);
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        return policyAdministratorPoint.updateFeature(feature);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        return policyAdministratorPoint.addProfile(profile);
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        return policyAdministratorPoint.updateProfile(profile);
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        return policyAdministratorPoint.addPolicy(policy);
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        return policyAdministratorPoint.updatePolicy(policy);
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return null;
    }

    @Override
    public Policy getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws FeatureManagementException {
        return null;
    }

    @Override
    public List<Policy> getPolicies(String deviceType) throws PolicyManagementException {
        return null;
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementException {
        return null;
    }
}
