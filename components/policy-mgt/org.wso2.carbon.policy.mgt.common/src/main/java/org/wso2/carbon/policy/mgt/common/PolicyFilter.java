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

import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;

import java.util.List;
import java.util.Map;

public interface PolicyFilter {

    List<Policy> filterActivePolicies(List<Policy> policies);

    List<Policy> filterDeviceGroupsPolicies(Map<Integer, DeviceGroup> groupMap, List<Policy> policies);

    List<Policy> filterRolesBasedPolicies(String roles[], List<Policy> policies);

    List<Policy> filterOwnershipTypeBasedPolicies(String ownershipType, List<Policy> policies);

    List<Policy> filterDeviceTypeBasedPolicies(String deviceType, List<Policy> policies);

    List<Policy> filterUserBasedPolicies(String username, List<Policy> policies);

}
