/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.operation.mgt;

public class OperationMgtConstants {

    public final class DeviceConstants {
        private DeviceConstants() {
            throw new AssertionError();
        }

        public static final String DEVICE_ID_NOT_FOUND = "Device not found for device id: %s";
        public static final String DEVICE_ID_SERVICE_NOT_FOUND =
                "Issue in retrieving device management service instance for device found at %s";
    }

    public final class OperationCodes {
        private OperationCodes() {
            throw new AssertionError();
        }
        public static final String POLICY_REVOKE = "POLICY_REVOKE";
    }
}
