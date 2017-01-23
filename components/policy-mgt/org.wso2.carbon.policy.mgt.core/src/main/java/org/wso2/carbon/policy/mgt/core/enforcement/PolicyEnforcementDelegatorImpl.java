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
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationException;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
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
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(identifier);
            if (policy != null) {
                this.addPolicyRevokeOperation(deviceIdentifiers);
                this.addPolicyOperation(deviceIdentifiers, policy);
            } else {
                //This means all the applicable policies have been removed from device. Hence calling a policy revoke.
                this.addPolicyRevokeOperation(deviceIdentifiers);
            }
        }
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException {
        try {
            PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
            PolicyAdministratorPoint policyAdministratorPoint;

            Policy policy = policyManagerService.getPEP().getEffectivePolicy(identifier);
            policyAdministratorPoint = policyManagerService.getPAP();
            if (policy != null) {
                policyAdministratorPoint.setPolicyUsed(identifier, policy);
            } else {
                policyAdministratorPoint.removePolicyUsed(identifier);
                return null;
            }
            return policy;
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
            String type = null;
            if (deviceIdentifiers.size() > 0) {
                type = deviceIdentifiers.get(0).getType();
            }
            PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                                                          PolicyManagerUtil.transformPolicy(policy), deviceIdentifiers);
        } catch (InvalidDeviceException e) {
            String msg = "Invalid DeviceIdentifiers found.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    @Override
    public void addPolicyRevokeOperation(List<DeviceIdentifier> deviceIdentifiers) throws PolicyDelegationException {
        try {
            String type = null;
            if (deviceIdentifiers.size() > 0) {
                type = deviceIdentifiers.get(0).getType();
            }
            PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                                                                   this.getPolicyRevokeOperation(), deviceIdentifiers);
        } catch (InvalidDeviceException e) {
            String msg = "Invalid DeviceIdentifiers found.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    private Operation getPolicyRevokeOperation() {
        CommandOperation policyRevokeOperation = new CommandOperation();
        policyRevokeOperation.setEnabled(true);
        policyRevokeOperation.setCode(OperationMgtConstants.OperationCodes.POLICY_REVOKE);
        policyRevokeOperation.setType(Operation.Type.COMMAND);
        return policyRevokeOperation;
    }
}
