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

package org.wso2.carbon.simple.policy.decision.point;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.PIPDevice;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationException;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManager;
import org.wso2.carbon.simple.policy.decision.point.internal.PolicyDecisionPointDataHolder;

import java.util.Collections;
import java.util.List;

public class SimpleEvaluationImpl implements SimpleEvaluation {

    private static final Log log = LogFactory.getLog(SimpleEvaluationImpl.class);
    private PolicyManager policyManager;
    private List<Policy> policyList;

    public SimpleEvaluationImpl() {
        policyManager = PolicyDecisionPointDataHolder.getInstance().getPolicyManager();
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {

        try {
            if (policyManager == null && policyList == null) {
                PIPDevice pipDevice = policyManager.getPIP().getDeviceData(deviceIdentifier);
                policyList = policyManager.getPIP().getRelatedPolicies(pipDevice);
            }
            sortPolicy();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }

        return policyList.get(0);
    }


    @Override
    public void sortPolicy() throws PolicyEvaluationException {
        Collections.sort(policyList);
    }
}
