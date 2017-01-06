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
package org.wso2.carbon.policy.mgt.core.mgt;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;

import java.util.HashMap;
import java.util.List;

public interface PolicyManager {

    Policy addPolicy(Policy policy) throws PolicyManagementException;

    Policy updatePolicy(Policy policy) throws PolicyManagementException;

    boolean updatePolicyPriorities(List<Policy> policies) throws PolicyManagementException;

    boolean deletePolicy(Policy policy) throws PolicyManagementException;

    boolean deletePolicy(int policyId) throws PolicyManagementException;

    void activatePolicy(int policyId) throws PolicyManagementException;

    void inactivatePolicy(int policyId) throws PolicyManagementException;

    Policy addPolicyToDevice(List<DeviceIdentifier> deviceIdentifierList, Policy policy) throws
                                                                                         PolicyManagementException;

    Policy addPolicyToRole(List<String> roleNames, Policy policy) throws PolicyManagementException;

    Policy addPolicyToUser(List<String> usernameList, Policy policy) throws PolicyManagementException;

    Policy getPolicyByProfileID(int profileId) throws PolicyManagementException;

    Policy getPolicy(int policyId) throws PolicyManagementException;

    List<Policy> getPolicies() throws PolicyManagementException;

    List<Policy> getPoliciesOfDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    List<Policy> getPoliciesOfDeviceType(String deviceType) throws PolicyManagementException;

    List<Policy> getPoliciesOfRole(String roleName) throws PolicyManagementException;

    List<Policy> getPoliciesOfUser(String username) throws PolicyManagementException;

    List<Device> getPolicyAppliedDevicesIds(int policyId) throws PolicyManagementException;

    void addAppliedPolicyFeaturesToDevice(DeviceIdentifier deviceIdentifier, Policy policy)
            throws PolicyManagementException;

    List<String> applyChangesMadeToPolicies() throws PolicyManagementException;

    void addAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagementException;

    void removeAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    boolean checkPolicyAvailable(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    boolean setPolicyApplied(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    int getPolicyCount() throws PolicyManagementException;

    Policy getAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException;

    HashMap<Integer, Integer> getAppliedPolicyIdsDeviceIds() throws PolicyManagementException;
}
