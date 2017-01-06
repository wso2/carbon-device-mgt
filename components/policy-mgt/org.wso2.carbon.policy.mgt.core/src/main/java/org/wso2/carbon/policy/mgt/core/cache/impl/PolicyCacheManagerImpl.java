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
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.cache.PolicyCacheManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.PolicyManagerImpl;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.cache.Cache;
import java.util.Iterator;
import java.util.List;

public class PolicyCacheManagerImpl implements PolicyCacheManager {

    private static final Log log = LogFactory.getLog(PolicyCacheManagerImpl.class);

    private static PolicyCacheManagerImpl policyCacheManager;

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

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.put(1, policies);
    }

    @Override
    public void updateAllPolicies(List<Policy> policies) {

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.removeAll();
        lCache.put(1, policies);
    }

    @Override
    public List<Policy> getAllPolicies() throws PolicyManagementException {

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

                List<String> users = policy.getUsers();
                for (String user : users) {
                    log.debug("Users in cached policy : " + user);
                }
                List<String> roles = policy.getRoles();
                for (String role : roles) {
                    log.debug("Roles in cached policy : " + role);
                }
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

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        lCache.removeAll();
    }

    @Override
    public void addPolicy(Policy policy) {

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

        Cache<Integer, List<Policy>> lCache = getPolicyListCache();
        if (lCache.containsKey(1)) {
            PolicyManager policyManager = new PolicyManagerImpl();
            Policy policy = policyManager.getPolicy(policyId);
            this.updatePolicy(policy);
        }

    }

    @Override
    public void removePolicy(int policyId) {

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

}
