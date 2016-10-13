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

package org.wso2.carbon.policy.mgt.core.enforcement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.cache.impl.PolicyCacheManagerImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelegationTask implements Task {

    private static final Log log = LogFactory.getLog(DelegationTask.class);

    @Override
    public void setProperties(Map<String, String> map) {

    }

    @Override
    public void init() {

    }

    @Override
    public void execute() {

        try {
            PolicyManager policyManager = new PolicyManagerImpl();
            List<String> deviceTypes = policyManager.applyChangesMadeToPolicies();

            PolicyCacheManagerImpl.getInstance().rePopulateCache();

            if (log.isDebugEnabled()) {
                log.debug("Number of device types which policies are changed .......... : " + deviceTypes.size());
            }
            if (!deviceTypes.isEmpty()) {
                DeviceManagementProviderService service = PolicyManagementDataHolder.getInstance()
                        .getDeviceManagementService();
                List<Device> devices;
                List<Device> toBeNotified;
                for (String deviceType : deviceTypes) {
                    try {
                        devices = new ArrayList<>();
                        toBeNotified = new ArrayList<>();
                        devices.addAll(service.getAllDevices(deviceType));
                        //HashMap<Integer, Integer> deviceIdPolicy = policyManager.getAppliedPolicyIdsDeviceIds();
                        for (Device device : devices) {
                            // if (deviceIdPolicy.containsKey(device.getId())) {
                            toBeNotified.add(device);
                            // }
                        }
                        if (!toBeNotified.isEmpty()) {
                            PolicyEnforcementDelegator enforcementDelegator = new PolicyEnforcementDelegatorImpl(toBeNotified);
                            enforcementDelegator.delegate();
                        }
                    } catch (DeviceManagementException e) {
                        throw new PolicyManagementException("Error occurred while fetching the devices", e);
                    } catch (PolicyDelegationException e) {
                        throw new PolicyManagementException("Error occurred while running the delegation task on " +
                                                            "device-type : " + deviceType, e);
                    }
                }
            }
        } catch (PolicyManagementException e) {
            log.error("Error occurred while getting the policies applied to devices.", e);
        }
    }
}
