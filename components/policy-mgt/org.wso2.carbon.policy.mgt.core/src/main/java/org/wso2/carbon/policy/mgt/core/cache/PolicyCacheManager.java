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

import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;

import java.util.List;

/**
 * This interface for manage caching for policies. This is using the cache implementation comes in kernel, which will
 * automatically sync with the cluster. So all the nodes will receive the updated policies.
 */
public interface PolicyCacheManager {

    /**
     * This method add policies to cache.
     * @param policies - List of policies
     */
    void addAllPolicies(List<Policy> policies);

    /**
     * This method will update the policies in the cache.
     * @param policies - List of policies
     */
    void updateAllPolicies(List<Policy> policies);

    /**
     * This method will return the all policies.
     * @return - list of policies
     * @throws PolicyManagementException
     */
    List<Policy> getAllPolicies() throws PolicyManagementException;

    /**
     * This method will repopulate the cache, this will be called when there is a change of the policies.
     * @throws PolicyManagementException
     */
    void rePopulateCache() throws PolicyManagementException;

    /**
     * This will remove all the policies from the cache.
     */
    void removeAllPolicies();

    /**
     * This method will the policy to the cache.
     * @param policy - policy to add
     */
    void addPolicy(Policy policy);

    /**
     * This method will update the policy in the cache.
     * @param policy - policy to be updated.
     */
    void updatePolicy(Policy policy);

    /**
     * This method will update the policy by reading the cache. If it is not available it will read from policy manager.
     * @param policyId  - integer, policy id.
     * @throws PolicyManagementException
     */
    void updatePolicy(int policyId) throws PolicyManagementException;

    /**
     * Remove policy from the cache.
     * @param policyId - Id of the policy to be removed.
     */
    void removePolicy(int policyId);

    /**
     * This will return the policy from cache. If it not available in the cache, it will get from the policy manager.
     * @param policyId - Id of the policy to be returned.
     * @return - Policy
     * @throws PolicyManagementException
     */
    Policy getPolicy(int policyId) throws PolicyManagementException;

    /**
     * This method will add a policy id against a device id.
     * @param deviceId - Id of the device.
     * @param policyId - Id of the policy
     */
    void addPolicyToDevice(int deviceId, int policyId);

    /**
     * This method will return the device ids, a certain policy is applied.
     * @param policyId  - Id of the policu.
     * @return - List of device ids.
     */
    List<Integer> getPolicyAppliedDeviceIds(int policyId);

    /**
     * This method will return the applied policy id of the device.
     * @param deviceId - Id of the device
     * @return - Id of the policy.
     */
    int getPolicyIdOfDevice(int deviceId);
}
