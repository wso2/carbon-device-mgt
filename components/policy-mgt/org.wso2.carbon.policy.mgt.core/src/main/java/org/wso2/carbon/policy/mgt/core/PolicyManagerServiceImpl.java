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
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.policy.mgt.common.*;
import org.wso2.carbon.policy.mgt.core.impl.PolicyAdministratorPointImpl;
import org.wso2.carbon.policy.mgt.core.impl.PolicyInformationPointImpl;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PolicyManagerServiceImpl implements PolicyManagerService {

    private static final Log log = LogFactory.getLog(PolicyManagerServiceImpl.class);

    PolicyAdministratorPointImpl policyAdministratorPoint;

    public PolicyManagerServiceImpl() {
        policyAdministratorPoint = new PolicyAdministratorPointImpl();
    }

    @Override
    public Feature addFeature(Feature feature) throws FeatureManagementException {
        return policyAdministratorPoint.addFeature(feature);
    }

    @Override
    public Feature updateFeature(Feature feature) throws FeatureManagementException {
        return policyAdministratorPoint.updateFeature(feature);
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


            Policy policy = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint().
                    getEffectivePolicy(deviceIdentifier);

            if (policy != null) {
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<DeviceIdentifier>();
                deviceIdentifiers.add(deviceIdentifier);

                List<ProfileFeature> effectiveFeatures = policy.getProfile().getProfileFeaturesList();
                List<ProfileOperation> profileOperationList = new ArrayList<ProfileOperation>();

                PolicyOperation policyOperation = new PolicyOperation();
                policyOperation.setEnabled(true);
                policyOperation.setType(Operation.Type.POLICY);
                policyOperation.setCode(PolicyOperation.POLICY_OPERATION_CODE);

                for (ProfileFeature feature : effectiveFeatures) {
                    ProfileOperation profileOperation = new ProfileOperation();

                    profileOperation.setCode(feature.getFeatureCode());
                    profileOperation.setEnabled(true);
                    profileOperation.setStatus(Operation.Status.PENDING);
                    profileOperation.setType(Operation.Type.PROFILE);
                    profileOperation.setPayLoad(feature.getContent());
                    profileOperationList.add(profileOperation);
                }
                policyOperation.setProfileOperations(profileOperationList);
                PolicyManagementDataHolder.getInstance().getDeviceManagementService().
                        addOperation(policyOperation, deviceIdentifiers);
                policyOperation.setPayLoad(policyOperation.getProfileOperations());
            } else {
                return null;
            }

            return policy;
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

            List<ProfileFeature> effectiveFeatures = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint().
                    getEffectiveFeatures(deviceIdentifier);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<DeviceIdentifier>();
            deviceIdentifiers.add(deviceIdentifier);
            List<ProfileOperation> profileOperationList = new ArrayList<ProfileOperation>();

            if (!effectiveFeatures.isEmpty()) {
                for (ProfileFeature feature : effectiveFeatures) {
                    ProfileOperation operation = new ProfileOperation();

                    operation.setCode(feature.getFeatureCode());
                    operation.setPayLoad(feature.getContent());
                    operation.setStatus(Operation.Status.PENDING);
                    operation.setType(Operation.Type.PROFILE);
                    operation.setEnabled(true);
                    profileOperationList.add(operation);
                    PolicyManagementDataHolder.getInstance().getDeviceManagementService().
                            addOperation(operation, deviceIdentifiers);
                }
            } else {
                return null;
            }

            return effectiveFeatures;
        } catch (PolicyEvaluationException e) {
            String msg = "Error occurred while getting the effective features from the PEP service " +
                    deviceIdentifier.getId() + " - " + deviceIdentifier.getType();
            log.error(msg, e);
            throw new FeatureManagementException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the effective feature to database." +
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
    public int getPolicyCount() throws PolicyManagementException {
        return policyAdministratorPoint.getPolicyCount();
    }
}
