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


package org.wso2.carbon.policy.mgt.common;

import org.wso2.carbon.policy.mgt.common.Policy;

import java.util.List;

public interface PolicyFilter {

    void filterRolesBasedPolicies(String roles[], List<Policy> policies);

    void filterOwnershipTypeBasedPolicies(String ownershipType, List<Policy> policies);

    void filterDeviceTypeBasedPolicies(String deviceType, List<Policy> policies);

    void filterUserBasedPolicies(List<String> usernames, List<Policy> policies);

}
