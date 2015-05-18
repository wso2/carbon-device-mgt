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
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyFilter;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import java.util.ArrayList;
import java.util.List;

public class PolicyFilterImpl implements PolicyFilter {

    private static final Log log = LogFactory.getLog(PolicyFilterImpl.class);

    @Override
    public void filterRolesBasedPolicies(String roles[], List<Policy> policies) {

        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {

            List<String> tempRoles = policy.getRoles();
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
        policies = temp;

    }

    @Override
    public void filterOwnershipTypeBasedPolicies(String ownershipType, List<Policy> policies) {

        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {
            if (ownershipType.equalsIgnoreCase(policy.getOwnershipType())) {
                temp.add(policy);
            }
        }
        policies = temp;
    }

    @Override
    public void filterDeviceTypeBasedPolicies(String deviceType, List<Policy> policies) {
        List<Policy> temp = new ArrayList<Policy>();
        for (Policy policy : policies) {
            if (deviceType.equalsIgnoreCase(policy.getProfile().getDeviceType().getName())) {
                temp.add(policy);
            }
        }
        policies = temp;
    }


}
