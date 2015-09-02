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
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PolicyCacheManagerImpl implements PolicyCacheManager {

    private static final Log log = LogFactory.getLog(PolicyCacheManagerImpl.class);

    private static HashMap<Integer, HashMap<Integer, Policy>> tenantedPolicyMap = new HashMap<>();

    private static PolicyCacheManagerImpl policyCacheManager;

//    private static Cache<Integer, Policy> getPolicyCache() {
//        return PolicyManagerUtil.getPolicyCache(PolicyManagementConstants.DM_CACHE);
//    }

    private static Cache<Integer, List<Policy>> getPolicyListCache() {
        return PolicyManagerUtil.getPolicyListCache(PolicyManagementConstants.DM_CACHE_LIST);
    }

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
        // HashMap<Integer, Policy> map = this.getTenantRelatedMap();

//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (cache.isEmpty()) {

//        for (Policy policy : policies) {
//            cache.put(policy.getId(), policy);
//        }
//        }

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.put(1, policies);
    }

    @Override
    public void updateAllPolicies(List<Policy> policies) {
////        HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        cache.removeAll();
////        map.clear();
////        if (map.isEmpty()) {
//        for (Policy policy : policies) {
////                map.put(policy.getId(), policy);
//            cache.put(policy.getId(), policy);
//        }
////        }

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.removeAll();
        lCache.put(1, policies);
    }

    @Override
    public List<Policy> getAllPolicies() throws PolicyManagementException {
//       // HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        Iterator iterator = cache.iterator();
////        iterator.hasNext()
//        if (!iterator.hasNext()) {
//            PolicyManager policyManager = new PolicyManagerImpl();
//            this.addAllPolicies(policyManager.getPolicies());
//        }
//        if (log.isDebugEnabled()) {
//            //log.debug("No of policies stored in the cache .. : " + map.size());
//
//            //Set<Integer> keySet = map.keySet();
//            Iterator iterator2 = cache.iterator();
//            while (iterator2.hasNext()) {
//                org.wso2.carbon.caching.impl.CacheEntry thisEntry = (org.wso2.carbon.caching.impl.CacheEntry)
// iterator2.next();
//                log.debug("Policy id in maps .. : " + thisEntry.getKey() + " policy name : " + ((Policy) thisEntry
// .getValue()).
//                        getPolicyName() + " Activated : " + ((Policy) thisEntry.getValue()).isActive());
//            }
//        }
//
//        List<Policy> policies = new ArrayList<>();
//        while (iterator.hasNext()){
//            CacheEntry thisEntry = (CacheEntry) iterator.next();
//            policies.add((Policy) thisEntry.getValue());
//        }
//
//        return policies;
////        return new ArrayList<>(map.values());

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (!lCache.containsKey(1)) {
            PolicyManager policyManager = new PolicyManagerImpl();
            this.addAllPolicies(policyManager.getPolicies());
        }

        if (log.isDebugEnabled()) {
            List<Policy> cachedPolicy = lCache.get(1);
            for (Policy policy : cachedPolicy) {
                log.debug("Policy id in cache .. : " + policy.getId() + " policy name : " + policy.
                        getPolicyName() + " Activated : " + policy.isActive());
            }
        }

        return lCache.get(1);

    }

    @Override
    public void rePopulateCache() throws PolicyManagementException {

        this.removeAllPolicies();
        this.getAllPolicies();
    }

    @Override
    public void removeAllPolicies() {
//        Cache<Integer, Policy> cache = getPolicyCache();
//        cache.removeAll();
        //HashMap<Integer, Policy> map = this.getTenantRelatedMap();
        //map.clear();

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.removeAll();
    }

    @Override
    public void addPolicy(Policy policy) {
        //HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (!cache.containsKey(policy.getId())) {
//            cache.put(policy.getId(), policy);
//        } else {
//            log.warn("Policy id (" + policy.getId() + ") already exist in the map. hence not attempted to store.");
//        }

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (lCache.containsKey(1)) {
            List<Policy> cachedPolicy = lCache.get(1);

            for (Policy pol : cachedPolicy) {
                if (pol.getId() == policy.getId()) {
                    return;
                }
            }
            cachedPolicy.add(policy);
        }

    }

    @Override
    public void updatePolicy(Policy policy) {
        // HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (cache.containsKey(policy.getId())) {
//            cache.remove(policy.getId());
//            cache.put(policy.getId(), policy);
//        }

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (lCache.containsKey(1)) {
            List<Policy> cachedPolicy = lCache.get(1);
            Iterator iterator = cachedPolicy.iterator();
            while (iterator.hasNext()) {
                Policy pol = (Policy) iterator.next();
                if (pol.getId() == policy.getId()) {
                    iterator.remove();
                    break;
                }
            }
            cachedPolicy.add(policy);
            lCache.replace(1, cachedPolicy);
        }

    }

    @Override
    public void updatePolicy(int policyId) throws PolicyManagementException {
        // HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (cache.containsKey(policyId)) {
//            this.removePolicy(policyId);
//        }
//        PolicyManager policyManager = new PolicyManagerImpl();
//        Policy policy = policyManager.getPolicy(policyId);
//        cache.put(policyId, policy);

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (lCache.containsKey(1)) {
            PolicyManager policyManager = new PolicyManagerImpl();
            Policy policy = policyManager.getPolicy(policyId);
            this.updatePolicy(policy);
        }

    }

    @Override
    public void removePolicy(int policyId) {
        // HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (cache.containsKey(policyId)) {
//            cache.remove(policyId);
//        } else {
//            log.warn("Policy id (" + policyId + ") does not exist in the cache. Hence not removed.");
//        }

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (lCache.containsKey(1)) {
            List<Policy> cachedPolicy = lCache.get(1);
            Iterator iterator = cachedPolicy.iterator();
            while (iterator.hasNext()) {
                Policy pol = (Policy) iterator.next();
                if (pol.getId() == policyId) {
                    iterator.remove();
                    break;
                }
            }
            lCache.replace(1, cachedPolicy);
        }
    }

    @Override
    public Policy getPolicy(int policyId) throws PolicyManagementException {
        //HashMap<Integer, Policy> map = this.getTenantRelatedMap();
//        Cache<Integer, Policy> cache = getPolicyCache();
//        if (!cache.containsKey(policyId)) {
//            this.removeAllPolicies();
//            this.getAllPolicies();
//        }
//        return cache.get(policyId);

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (!lCache.containsKey(1)) {
            this.removeAllPolicies();
            this.getAllPolicies();
        }

        Policy policy = null;
        List<Policy> cachedPolicy = lCache.get(1);
        Iterator iterator = cachedPolicy.iterator();
        while (iterator.hasNext()) {
            Policy pol = (Policy) iterator.next();
            if (pol.getId() == policyId) {
                policy = pol;
            }
        }
        return policy;
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

    private HashMap<Integer, Policy> getTenantRelatedMap() {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!tenantedPolicyMap.containsKey(tenantId)) {
            HashMap<Integer, Policy> policyMap = new HashMap<>();
            tenantedPolicyMap.put(tenantId, policyMap);
        }
        return tenantedPolicyMap.get(tenantId);
    }
}
