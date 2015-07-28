/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.policy.mgt.core.enforcement;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.util.ArrayList;
import java.util.List;

public class PolicyEnforcementDelegatorImpl implements PolicyEnforcementDelegator {

    private OperationManager operationManager = new OperationManagerImpl();

    @Override
    public void delegate(Policy policy, List<Device> devices) throws PolicyDelegationException {
        try {
            List<DeviceIdentifier> deviceIds = new ArrayList<>();
            for (Device device : devices) {
                deviceIds.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            }
            operationManager.addOperation(PolicyManagerUtil.transformPolicy(policy), deviceIds);
        } catch (OperationManagementException e) {
            throw new PolicyDelegationException("Error occurred while delegating policy information to " +
                    "the respective enforcement points", e);
        }
    }

}
