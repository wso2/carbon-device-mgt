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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationException;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.util.ArrayList;
import java.util.List;

public class PolicyEnforcementDelegatorImpl implements PolicyEnforcementDelegator{

    private static final Log log = LogFactory.getLog(PolicyEnforcementDelegatorImpl.class);

    private List<Device> devices;

    public PolicyEnforcementDelegatorImpl(List<Device> devices) {

        log.info("Policy re-enforcing stared due to change of the policies.");

        if (log.isDebugEnabled()) {
            for (Device device : devices) {
                log.debug("Policy re-enforcing for device :" + device.getDeviceIdentifier() + " - Type : "
                        + device.getType());
            }
        }
        this.devices = devices;

    }

    @Override
    public void delegate() throws PolicyDelegationException {

        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());

            Policy policy = this.getEffectivePolicy(identifier);

            if (policy != null) {
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                deviceIdentifiers.add(identifier);
                this.addPolicyOperation(deviceIdentifiers, policy);
            }

        }
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException {

        try {
            PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
            return policyManagerService.getPEP().getEffectivePolicy(identifier);
            //return PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint().getEffectivePolicy(identifier);
        } catch (PolicyEvaluationException e) {
            String msg = "Error occurred while retrieving the effective policy for devices.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the effective policy for devices.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    @Override
    public void addPolicyOperation(List<DeviceIdentifier> deviceIdentifiers, Policy policy) throws
            PolicyDelegationException {

        try {
            OperationManager operationManager = new OperationManagerImpl();
            operationManager.addOperation(PolicyManagerUtil.transformPolicy(policy), deviceIdentifiers);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }

    }

}
