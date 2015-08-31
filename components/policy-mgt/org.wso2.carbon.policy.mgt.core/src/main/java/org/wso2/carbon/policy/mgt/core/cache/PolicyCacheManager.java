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


package org.wso2.carbon.policy.mgt.core.cache;

import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;

import java.util.List;

public interface PolicyCacheManager {

    void addAllPolicies(List<Policy> policies);

    void updateAllPolicies(List<Policy> policies);

    List<Policy> getAllPolicies() throws PolicyManagementException;

    void rePopulateCache() throws PolicyManagementException;

    void removeAllPolicies();

    void addPolicy(Policy policy);

    void updatePolicy(Policy policy);

    void updatePolicy(int policyId) throws PolicyManagementException;

    void removePolicy(int policyId);

    Policy getPolicy(int policyId) throws PolicyManagementException;

    void addPolicyToDevice(int deviceId, int policyId);

    List<Integer> getPolicyAppliedDeviceIds(int policyId);

    int getPolicyIdOfDevice(int deviceId);
}
