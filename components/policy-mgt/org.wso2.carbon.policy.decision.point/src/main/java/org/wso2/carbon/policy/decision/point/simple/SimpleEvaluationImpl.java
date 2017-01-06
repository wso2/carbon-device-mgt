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

package org.wso2.carbon.policy.decision.point.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.decision.point.internal.PolicyDecisionPointDataHolder;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleEvaluationImpl implements SimpleEvaluation {

    private static final Log log = LogFactory.getLog(SimpleEvaluationImpl.class);
    private PolicyManagerService policyManagerService;
    private List<Policy> policyList = new ArrayList<Policy>();

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        Policy policy = new Policy();
        PolicyAdministratorPoint policyAdministratorPoint;
        PolicyInformationPoint policyInformationPoint;
        policyManagerService = getPolicyManagerService();

        try {
            if (policyManagerService != null) {

                policyInformationPoint = policyManagerService.getPIP();
                PIPDevice pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
                policyList = policyInformationPoint.getRelatedPolicies(pipDevice);
                policyAdministratorPoint = policyManagerService.getPAP();
                sortPolicies();
                if(!policyList.isEmpty()) {
                    policy = policyList.get(0);
                } else {
                    policyAdministratorPoint.removePolicyUsed(deviceIdentifier);
                    return null;
                }
                //TODO : UNCOMMENT THE FOLLOWING CASE
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
    public void sortPolicies() throws PolicyEvaluationException {
        Collections.sort(policyList);
    }

    private PolicyManagerService getPolicyManagerService() {
        return PolicyDecisionPointDataHolder.getInstance().getPolicyManagerService();
    }
}
