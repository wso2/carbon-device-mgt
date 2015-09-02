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


package org.wso2.carbon.policy.mgt.core.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.cache.PolicyCacheManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PolicyCacheManagerImpl implements PolicyCacheManager {

    private static final Log log = LogFactory.getLog(PolicyCacheManagerImpl.class);

    private static HashMap<Integer, HashMap<Integer, Policy>> tenantedPolicyMap = new HashMap<>();

    private static PolicyCacheManagerImpl policyCacheManager;

    private PolicyCacheManagerImpl() {
    }

    public static PolicyCacheManager getInstance() {
        if (policyCacheManager == null) {
            synchronized (PolicyCacheManagerImpl.class) {
                if (policyCacheManager == null) {
                    policyCacheManager = new PolicyCacheManagerImpl();
                }
            }
        }
        return policyCacheManager;
    }

    @Override
    public void addAllPolicies(List<Policy> policies) {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (map.isEmpty()) {
            for (Policy policy : policies) {
                map.put(policy.getId(), policy);
            }
        }

    }

    @Override
    public void updateAllPolicies(List<Policy> policies) {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        map.clear();
        if (map.isEmpty()) {
            for (Policy policy : policies) {
                map.put(policy.getId(), policy);
            }
        }
    }

    @Override
    public List<Policy> getAllPolicies() throws PolicyManagementException {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (map.isEmpty()) {
            PolicyManager policyManager = new PolicyManagerImpl();
            this.addAllPolicies(policyManager.getPolicies());
        }
        if (log.isDebugEnabled()) {
            log.debug("No of policies stored in the cache .. : " + map.size());

            Set<Integer> keySet = map.keySet();
            for (Integer x : keySet) {
                log.debug("Policy id in maps .. : " + map.get(x).getId() + " policy name : " + map.get(x).
                        getPolicyName() + " Activated : " + map.get(x).isActive());
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public void rePopulateCache() throws PolicyManagementException {

        this.removeAllPolicies();
        this.getAllPolicies();
    }

    @Override
    public void removeAllPolicies() {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        map.clear();
    }

    @Override
    public void addPolicy(Policy policy) {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (!map.containsKey(policy.getId())) {
            map.put(policy.getId(), policy);
        } else {
            log.warn("Policy id (" + policy.getId() + ") already exist in the map. hence not attempted to store.");
        }
    }

    @Override
    public void updatePolicy(Policy policy) {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (map.containsKey(policy.getId())) {
            map.remove(policy.getId());
            map.put(policy.getId(), policy);
        }
    }

    @Override
    public void updatePolicy(int policyId) throws PolicyManagementException {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (map.containsKey(policyId)) {
            this.removePolicy(policyId);
        }
        PolicyManager policyManager = new PolicyManagerImpl();
        Policy policy = policyManager.getPolicy(policyId);
        map.put(policyId, policy);
    }

    @Override
    public void removePolicy(int policyId) {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (map.containsKey(policyId)) {
            map.remove(policyId);
        } else {
            log.warn("Policy id (" + policyId + ") does not exist in the cache. Hence not removed.");
        }
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {
        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        if (!map.containsKey(policyId)) {
            this.removeAllPolicies();
            this.getAllPolicies();
        }
        return map.get(policyId);

    }

    @Override
    public void addPolicyToDevice(int deviceId, int policyId) {

    }

    @Override
    public List<Integer> getPolicyAppliedDeviceIds(int policyId) {
        return null;
    }

    @Override
    public int getPolicyIdOfDevice(int deviceId) {
        return 0;
    }

    private HashMap<Integer, Policy> getTenantRelatedMap(){

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if(!tenantedPolicyMap.containsKey(tenantId)){
            HashMap<Integer, Policy> policyMap = new HashMap<>();
            tenantedPolicyMap.put(tenantId, policyMap);
        }
        return tenantedPolicyMap.get(tenantId);
    }
}
