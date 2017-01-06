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
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;

import java.util.ArrayList;
import java.util.List;

public class PolicyMonitoringManagerTest implements PolicyMonitoringManager {

    private static final Log log = LogFactory.getLog(PolicyMonitoringManagerTest.class);


    @Override
    public NonComplianceData checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy, Object response)
            throws PolicyComplianceException {

        log.debug("Check compliance is called.");

        log.debug(policy.getPolicyName());
        log.debug(policy.getId());

        log.debug(deviceIdentifier.getId());
        log.debug(deviceIdentifier.getType());

        NonComplianceData data = new NonComplianceData();

        List<ComplianceFeature> complianceFeatures = new ArrayList<>();

        List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();

        for (ProfileFeature pf : profileFeatures) {
            log.debug(pf.getFeatureCode());
            ComplianceFeature comf = new ComplianceFeature();

            comf.setFeatureCode(pf.getFeatureCode());
            comf.setCompliance(false);
            comf.setMessage("This is a test....");

            complianceFeatures.add(comf);
        }
        data.setComplianceFeatures(complianceFeatures);
        data.setStatus(false);

        return data;
    }
}
