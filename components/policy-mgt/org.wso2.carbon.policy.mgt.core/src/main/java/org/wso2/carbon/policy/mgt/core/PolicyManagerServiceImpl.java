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

package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;
import org.wso2.carbon.policy.mgt.core.impl.PolicyInformationPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.MonitoringManagerImpl;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleServiceImpl;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.util.ArrayList;
import java.util.List;

public class PolicyManagerServiceImpl implements PolicyManagerService {

    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    PolicyAdministratorPoint policyAdministratorPoint;
    MonitoringManager monitoringManager;
    private PolicyManager policyManager;

    public PolicyManagerServiceImpl() {
        policyAdministratorPoint = new PolicyAdministratorPointImpl();
        monitoringManager = new MonitoringManagerImpl();
        policyManager = new PolicyManagerImpl();
        PolicyManagementDataHolder.getInstance().setMonitoringManager(monitoringManager);
        PolicyManagementDataHolder.getInstance().setPolicyManager(policyManager);
    }

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        return policyAdministratorPoint.addProfile(profile);
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        return policyAdministratorPoint.updateProfile(profile);
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        return policyAdministratorPoint.addPolicy(policy);
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        return policyAdministratorPoint.updatePolicy(policy);
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        return policyAdministratorPoint.deletePolicy(policy);
    }

    @Override
    public boolean deletePolicy(int policyId) throws PolicyManagementException {
        return policyAdministratorPoint.deletePolicy(policyId);
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        try {
            PolicyEvaluationPoint policyEvaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();
            Policy policy;

            if (policyEvaluationPoint != null) {
                policy = policyEvaluationPoint.
                        getEffectivePolicy(deviceIdentifier);
                if (policy == null) {
                    policyAdministratorPoint.removePolicyUsed(deviceIdentifier);
                    return null;
                }
                this.getPAP().setPolicyUsed(deviceIdentifier, policy);
            } else {
                throw new PolicyEvaluationException("Error occurred while getting the policy evaluation point " +
                        deviceIdentifier.getId() + " - " + deviceIdentifier.getType());
            }
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<DeviceIdentifier>();
            deviceIdentifiers.add(deviceIdentifier);

            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIdentifiers.size() > 0) {
                type = deviceIdentifiers.get(0).getType();
            }
            PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                    PolicyManagerUtil.transformPolicy(policy), deviceIdentifiers);
            return policy;
        } catch (InvalidDeviceException e) {
            String msg = "Error occurred while getting the effective policies for invalid DeviceIdentifiers";
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (PolicyEvaluationException e) {
            String msg = "Error occurred while getting the effective policies from the PEP service for device " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the effective feature to database." +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new PolicyManagementException(msg, e);
        }
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws
            FeatureManagementException {
        try {
            PolicyEvaluationPoint policyEvaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();
            if (policyEvaluationPoint != null) {
                return policyEvaluationPoint.getEffectiveFeatures(deviceIdentifier);
            } else {
                throw new FeatureManagementException("Error occurred while getting the policy evaluation point " +
                        deviceIdentifier.getId() + " - " + deviceIdentifier.getType());
            }
        } catch (PolicyEvaluationException e) {
            String msg = "Error occurred while getting the effective features from the PEP service " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        }
    }

    @Override
    public List<Policy> getPolicies(String deviceType) throws PolicyManagementException {
        return policyAdministratorPoint.getPoliciesOfDeviceType(deviceType);
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementException {
        return null;
    }

    @Override
    public PolicyAdministratorPoint getPAP() throws PolicyManagementException {
        return new PolicyAdministratorPointImpl();
    }

    @Override
    public PolicyInformationPoint getPIP() throws PolicyManagementException {
        return new PolicyInformationPointImpl();
    }

    @Override
    public PolicyEvaluationPoint getPEP() throws PolicyManagementException {
        return PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();
    }

    @Override
    public TaskScheduleService getTaskScheduleService() throws PolicyMonitoringTaskException {
        return new TaskScheduleServiceImpl();
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        return policyAdministratorPoint.getPolicyCount();
    }

    @Override
    public Policy getAppliedPolicyToDevice(
            DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return policyManager.getAppliedPolicyToDevice(deviceIdentifier);
    }

    @Override
    public List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Object
            deviceResponse) throws PolicyComplianceException {
        return monitoringManager.checkPolicyCompliance(deviceIdentifier, deviceResponse);
    }

    @Override
    public boolean checkCompliance(DeviceIdentifier deviceIdentifier, Object response) throws
            PolicyComplianceException {

        List<ComplianceFeature> complianceFeatures =
                monitoringManager.checkPolicyCompliance(deviceIdentifier, response);
        return !(complianceFeatures == null || complianceFeatures.isEmpty());
    }

    @Override
    public NonComplianceData getDeviceCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        return monitoringManager.getDevicePolicyCompliance(deviceIdentifier);
    }

    @Override
    public boolean isCompliant(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        return monitoringManager.isCompliant(deviceIdentifier);
    }
}