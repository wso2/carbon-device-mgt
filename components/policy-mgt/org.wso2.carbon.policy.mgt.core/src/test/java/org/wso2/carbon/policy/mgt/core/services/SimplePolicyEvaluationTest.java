/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.policy.mgt.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;

import java.util.Collections;
import java.util.List;

public class
SimplePolicyEvaluationTest implements PolicyEvaluationPoint {

    private static final Log log = LogFactory.getLog(SimplePolicyEvaluationTest.class);

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        Policy policy = new Policy();

        List<Policy> policyList;
        PolicyAdministratorPoint policyAdministratorPoint;
        PolicyInformationPoint policyInformationPoint;
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();

        try {
            if (policyManagerService != null) {

                policyInformationPoint = policyManagerService.getPIP();
                PIPDevice pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
                policyList = policyInformationPoint.getRelatedPolicies(pipDevice);
                policyAdministratorPoint = policyManagerService.getPAP();
                for(Policy pol : policyList) {
                    log.debug("Policy used in evaluation -  Name  : " + pol.getPolicyName() );
                }

                sortPolicies(policyList);
                if(!policyList.isEmpty()) {
                    policy = policyList.get(0);
                } else {
                    policyAdministratorPoint.removePolicyUsed(deviceIdentifier);
                    return null;
                }
                policyAdministratorPoint.setPolicyUsed(deviceIdentifier, policy);
            }

        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }
        return policy;
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        return null;
    }

    @Override
    public String getName() {
        return "SimplePolicy";
    }

    public void sortPolicies(List<Policy> policyList) throws PolicyEvaluationException {
        Collections.sort(policyList);
    }
}
