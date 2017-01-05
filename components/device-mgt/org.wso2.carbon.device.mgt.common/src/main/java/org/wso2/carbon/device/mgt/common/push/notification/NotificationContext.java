/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common.push.notification;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import java.util.Map;

public class NotificationContext {

    private DeviceIdentifier deviceId;

    private Operation operation;

    private Map<String, String> properties;

    public NotificationContext(DeviceIdentifier deviceId) {
        this.deviceId = deviceId;
    }

    public NotificationContext(DeviceIdentifier deviceId, Operation operation) {
        this.deviceId = deviceId;
        this.operation = operation;
    }

    public DeviceIdentifier getDeviceId() {
        return deviceId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> propertiesMap) {
        properties = propertiesMap;
    }

    public Operation getOperation() {
        return operation;
    }

}
