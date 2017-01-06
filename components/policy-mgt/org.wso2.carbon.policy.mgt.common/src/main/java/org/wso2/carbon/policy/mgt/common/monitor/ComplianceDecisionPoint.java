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


package org.wso2.carbon.policy.mgt.common.monitor;


import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;

import java.util.List;

public interface ComplianceDecisionPoint {

    String getNoneComplianceRule(Policy policy) throws PolicyComplianceException;

    void setDevicesAsUnreachable(List<DeviceIdentifier> deviceIdentifiers) throws PolicyComplianceException;

    void setDevicesAsInactive(List<DeviceIdentifier> deviceIdentifiers) throws PolicyComplianceException;

    void setDevicesAsUnreachableWith(List<Device> devices) throws PolicyComplianceException;

    void setDeviceAsReachable(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    void reEnforcePolicy(DeviceIdentifier deviceIdentifier, NonComplianceData complianceData) throws
            PolicyComplianceException;

    void markDeviceAsNoneCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    void markDeviceAsCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    void deactivateDevice(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    void activateDevice(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException;

    void validateDevicePolicyCompliance(DeviceIdentifier deviceIdentifier, NonComplianceData complianceData) throws
            PolicyComplianceException;


}
