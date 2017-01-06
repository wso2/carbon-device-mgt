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
package org.wso2.carbon.policy.mgt.core.enforcement;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;

import java.util.List;

public interface PolicyEnforcementDelegator {

    void delegate() throws PolicyDelegationException;

    Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException;

    void addPolicyOperation(List<DeviceIdentifier> deviceIdentifiers, Policy policy) throws PolicyDelegationException;

    void addPolicyRevokeOperation(List<DeviceIdentifier> deviceIdentifiers) throws PolicyDelegationException;

}
