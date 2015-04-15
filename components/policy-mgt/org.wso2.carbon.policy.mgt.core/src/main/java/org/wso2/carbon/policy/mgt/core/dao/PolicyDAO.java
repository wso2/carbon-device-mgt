/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.policy.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.Profile;

import java.util.List;

public interface PolicyDAO {

    Policy addPolicy(Policy policy) throws PolicyManagerDAOException;

    Policy addPolicy(String deviceType, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToRole(String roleName, Policy policy) throws PolicyManagerDAOException;

    Policy addPolicyToDevice(DeviceIdentifier deviceIdentifier, Policy policy) throws PolicyManagerDAOException;

    Policy updatePolicy(Policy policy) throws PolicyManagerDAOException;

    Policy getPolicy(int policyId) throws PolicyManagerDAOException;

    Policy getPolicyByProfileID(int profileId) throws PolicyManagerDAOException;

    List<Policy> getPolicy() throws PolicyManagerDAOException;

    List<Policy> getPolicy(String deviceType) throws PolicyManagerDAOException;

    List<Policy> getPolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagerDAOException;

    List<Policy> getPolicyOfRole(String roleName) throws PolicyManagerDAOException;

    void deletePolicy(Policy policy) throws PolicyManagerDAOException;

}
