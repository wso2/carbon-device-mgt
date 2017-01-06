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


package org.wso2.carbon.policy.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyFilter;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyFilterImpl implements PolicyFilter {

    private static final Log log = LogFactory.getLog(PolicyFilterImpl.class);

    @Override
    public List<Policy> filterActivePolicies(List<Policy> policies) {

        if (log.isDebugEnabled()) {
            log.debug("No of policies went in to filterActivePolicies : " + policies.size());
            for (Policy policy : policies) {
                log.debug("Names of policy went in to  filterActivePolicies : " + policy.getPolicyName());
            }
        }

        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {
            if (policy.isActive()) {
                temp.add(policy);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies returned from filterActivePolicies :" + policies.size());
            for (Policy policy : temp) {
                log.debug("Names of policy filtered in filterActivePolicies : " + policy.getPolicyName());
            }
        }

        return temp;
    }

    @Override
    public List<Policy> filterDeviceGroupsPolicies(Map<Integer, DeviceGroup> groupMap, List<Policy> policies) {

        List<Policy> temp = new ArrayList<Policy>();
        Map<Integer, Policy> policyMap = new HashMap<>();
        for (Policy policy : policies) {
            List<DeviceGroupWrapper> wrappers = policy.getDeviceGroups();
            if (wrappers.isEmpty()) {
                temp.add(policy);
                continue;
            } else if (PolicyManagementConstants.ANY.equalsIgnoreCase(wrappers.get(0).getName())) {
                temp.add(policy);
                policyMap.put(policy.getId(), policy);
                continue;
            } else {
                for (DeviceGroupWrapper deviceGroupWrapper : wrappers) {
                    if (groupMap.containsKey(deviceGroupWrapper.getId()) && !policyMap.containsKey(policy.getId())) {
                        temp.add(policy);
                        policyMap.put(policy.getId(), policy);
                    }
                }
            }
        }
        return temp;
    }

    @Override
    public List<Policy> filterRolesBasedPolicies(String roles[], List<Policy> policies) {

        if (log.isDebugEnabled()) {
            log.debug("No of policies went in to filterRolesBasedPolicies : " + policies.size());
            for (Policy policy : policies) {
                log.debug("Names of policy  went in to filterRolesBasedPolicies : " + policy.getPolicyName());
            }
            log.debug("Roles passed to match.");
            for (String role : roles) {
                log.debug("Role name passed : " + role);
            }
        }

        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {

            List<String> tempRoles = policy.getRoles();
            if (tempRoles.isEmpty()) {

                if (log.isDebugEnabled()) {
                    log.debug("Roles list is empty.");
                }
                temp.add(policy);
                continue;
            }
            if (PolicyManagementConstants.ANY.equalsIgnoreCase(tempRoles.get(0))) {
                temp.add(policy);
                continue;
            }

            for (String role : roles) {
                for (String policyRole : tempRoles) {
                    if (role.equalsIgnoreCase(policyRole)) {
                        temp.add(policy);
                        continue;
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies returned from filterRolesBasedPolicies : " + policies.size());
            for (Policy policy : temp) {
                log.debug("Names of policy filtered in filterRolesBasedPolicies : " + policy.getPolicyName());
            }
        }

        return temp;
    }

    @Override
    public List<Policy> filterOwnershipTypeBasedPolicies(String ownershipType, List<Policy> policies) {

        if (ownershipType == null) {
            return policies;
        }
        if (log.isDebugEnabled()) {
            log.debug("No of policies went in to filterOwnershipTypeBasedPolicies : " + policies.size());
            log.debug("Ownership type : " + ownershipType);
            for (Policy policy : policies) {
                log.debug("Names of policy  went in to filterOwnershipTypeBasedPolicies : " + policy.getPolicyName());
            }
        }

        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {
            if (policy.getOwnershipType() == null || ownershipType.equalsIgnoreCase(policy.getOwnershipType()) ||
                    PolicyManagementConstants.ANY.equalsIgnoreCase(policy.getOwnershipType())) {
                temp.add(policy);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies returned from filterOwnershipTypeBasedPolicies : " + policies.size());
            for (Policy policy : temp) {
                log.debug("Names of policy filtered in filterOwnershipTypeBasedPolicies : " + policy.getPolicyName());
            }
        }
        return temp;
    }

    @Override
    public List<Policy> filterDeviceTypeBasedPolicies(String deviceType, List<Policy> policies) {

        if (log.isDebugEnabled()) {
            log.debug("No of policies went in to filterDeviceTypeBasedPolicies : " + policies.size());
            log.debug("Device type  : " + deviceType);
            for (Policy policy : policies) {
                log.debug("Names of policy  went in to filterDeviceTypeBasedPolicies : " + policy.getPolicyName());
            }
        }


        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {
            if (deviceType.equalsIgnoreCase(policy.getProfile().getDeviceType())) {
                temp.add(policy);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies returned from filterDeviceTypeBasedPolicies : " + policies.size());
            for (Policy policy : temp) {
                log.debug("Names of policy filtered in filterDeviceTypeBasedPolicies : " + policy.getPolicyName());
            }
        }

        return temp;
    }

    @Override
    public List<Policy> filterUserBasedPolicies(String username, List<Policy> policies) {

        if (log.isDebugEnabled()) {
            log.debug("No of policies went in to filterUserBasedPolicies : " + policies.size());
            log.debug("User name : " + username);
            for (Policy policy : policies) {
                log.debug("Names of policy  went in to filterUserBasedPolicies : " + policy.getPolicyName());
            }
        }

        List<Policy> temp = new ArrayList<Policy>();

        for (Policy policy : policies) {

            List<String> users = policy.getUsers();

            if (users.isEmpty()) {
                temp.add(policy);
                continue;
            }
            if (users.contains(PolicyManagementConstants.ANY)) {
                temp.add(policy);
                continue;
            }
            for (String user : users) {
                if (username.equalsIgnoreCase(user)) {
                    temp.add(policy);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("No of policies returned from filterUserBasedPolicies : " + policies.size());
            for (Policy policy : temp) {
                log.debug("Names of policy filtered in filterUserBasedPolicies : " + policy.getPolicyName());
            }
        }

        return temp;
    }
}
