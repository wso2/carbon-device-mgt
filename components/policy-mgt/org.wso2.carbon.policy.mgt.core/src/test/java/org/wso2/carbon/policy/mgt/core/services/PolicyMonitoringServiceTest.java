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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;

import java.util.ArrayList;
import java.util.List;

public class PolicyMonitoringServiceTest implements PolicyMonitoringService {

    private static final Log log = LogFactory.getLog(PolicyMonitoringServiceTest.class);

    @Override
    public void notifyDevices(List<Device> devices) throws PolicyComplianceException {

        log.debug("Device notifying is called by the task.");
    }

    @Override
    public ComplianceData checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy, Object response)
            throws PolicyComplianceException {

        log.debug("Check compliance is called.");

        log.debug(policy.getPolicyName());
        log.debug(policy.getId());

        log.debug(deviceIdentifier.getId());
        log.debug(deviceIdentifier.getType());

        ComplianceData data = new ComplianceData();

        List<ComplianceFeature> complianceFeatures = new ArrayList<>();

//        List<ProfileFeature> profileFeatures = policy.getProfile().getProfileFeaturesList();

//        for (ProfileFeature pf : profileFeatures) {
//            log.debug(pf.getFeatureCode());
//            ComplianceFeature comf = new ComplianceFeature();
//
//            comf.setFeatureCode(pf.getFeatureCode());
//            comf.setCompliance(false);
//            comf.setMessage("This is a test....");
//
//            complianceFeatures.add(comf);
//        }
        data.setComplianceFeatures(complianceFeatures);
        data.setStatus(false);

        return data;
    }

    @Override
    public String getType() {
        return "android";
    }
}
