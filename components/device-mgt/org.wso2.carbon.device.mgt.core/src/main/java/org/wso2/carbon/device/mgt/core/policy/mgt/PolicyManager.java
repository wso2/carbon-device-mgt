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
package org.wso2.carbon.device.mgt.core.policy.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.policy.mgt.policy.Policy;

import java.util.List;

public interface PolicyManager {

    public enum Type {
        USER_BASED, ROLE_BASED, PLATFORM_BASED
    }

    boolean addPolicy(Policy policy) throws PolicyManagementException;

    boolean removePolicy(String policyId) throws PolicyManagementException;

    boolean updatePolicy(Policy policy) throws PolicyManagementException;

    Policy getPolicy(String policyId) throws PolicyManagementException;

    List<Policy> getPolicies() throws PolicyManagementException;

    List<Policy> getUserBasedPolicies(String user) throws PolicyManagementException;

    List<Policy> getRoleBasedPolicies(String role) throws PolicyManagementException;

    List<Policy> getPlatformBasedPolicies(String platform) throws PolicyManagementException;

    boolean assignRoleBasedPolicy(String policyId, String role) throws PolicyManagementException;

    boolean assignRoleBasedPolicy(String policyId, List<String> roles) throws PolicyManagementException;

    boolean assignUserBasedPolicy(String policyId, String user) throws PolicyManagementException;

    boolean assignUserBasedPolicy(String policyId, List<String> users) throws PolicyManagementException;

    boolean assignPlatformBasedPolicy(String policyId, String platform) throws PolicyManagementException;

    Profile getEffectiveProfile(DeviceIdentifier deviceId) throws PolicyManagementException;

}
