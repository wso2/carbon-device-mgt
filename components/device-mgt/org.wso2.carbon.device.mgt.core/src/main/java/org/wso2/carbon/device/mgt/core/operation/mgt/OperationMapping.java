/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.core.operation.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;

/**
 * Class for represent operation mapping
 */
public class OperationMapping {

    private DeviceIdentifier deviceIdentifier;
    private int operationId;
    private int enrollmentId;
    private int tenantId;
    private Operation.Status status;
    private Operation.PushNotificationStatus pushNotificationStatus;

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Operation.Status getStatus() {
        return status;
    }

    public void setStatus(Operation.Status status) {
        this.status = status;
    }

    public Operation.PushNotificationStatus getPushNotificationStatus() {
        return pushNotificationStatus;
    }

    public void setPushNotificationStatus(Operation.PushNotificationStatus pushNotificationStatus) {
        this.pushNotificationStatus = pushNotificationStatus;
    }
}
