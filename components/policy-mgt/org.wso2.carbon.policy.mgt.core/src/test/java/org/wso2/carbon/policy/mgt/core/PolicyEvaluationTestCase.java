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


package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.services.SimplePolicyEvaluationTest;

import java.util.Collections;
import java.util.List;

public class PolicyEvaluationTestCase extends BasePolicyManagementDAOTest {

    private static final String ANDROID = "android";
    private static final Log log = LogFactory.getLog(PolicyEvaluationTestCase.class);


    @BeforeClass
    @Override
    public void init() throws Exception {

        PolicyEvaluationPoint evaluationPoint = new SimplePolicyEvaluationTest();
        PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint(evaluationPoint);
    }

    @Test
    public void activatePolicies() throws PolicyManagementException {
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
        PolicyAdministratorPoint administratorPoint = policyManagerService.getPAP();
        List<Policy> policies = policyManagerService.getPolicies(ANDROID);

        for (Policy policy : policies) {
            log.debug("Policy status : " + policy.getPolicyName() + "  - " + policy.isActive() + " - " + policy
                    .isUpdated());

            if (!policy.isActive()) {
                administratorPoint.activatePolicy(policy.getId());
            }
        }
        administratorPoint.publishChanges();
    }

    @Test(dependsOnMethods = ("activatePolicies"))
    public void getEffectivePolicy() throws DeviceManagementException, PolicyEvaluationException {

        log.debug("Getting effective policy for device started ..........");

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        List<Device> devices = service.getAllDevices(ANDROID);

        PolicyEvaluationPoint evaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();

        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setType(device.getType());
            identifier.setId(device.getDeviceIdentifier());
            Policy policy = evaluationPoint.getEffectivePolicy(identifier);

            if (policy != null) {
                log.debug("Name of the policy applied to device is " + policy.getPolicyName());
            } else {
                log.debug("No policy is applied to device.");
            }
        }
    }


    @Test(dependsOnMethods = ("getEffectivePolicy"))
    public void updatePriorities() throws PolicyManagementException {

        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
        PolicyAdministratorPoint administratorPoint = policyManagerService.getPAP();

        List<Policy> policies = administratorPoint.getPolicies();

        log.debug("Re-enforcing policy started....");

        int sixe = policies.size();

        sortPolicies(policies);
        int x = 0;
        for (Policy policy : policies) {
            policy.setPriorityId(sixe - x);
            x++;
        }



        administratorPoint.updatePolicyPriorities(policies);
        administratorPoint.publishChanges();
    }

    public void sortPolicies(List<Policy> policyList)  {
        Collections.sort(policyList);
    }
}
