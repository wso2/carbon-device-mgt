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
package org.wso2.carbon.device.mgt.core.operation.mgt;

import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OperationManagerRepository {

    private Map<DeviceTypeIdentifier, OperationManager> operationManagers;

    public OperationManagerRepository() {
        operationManagers = new ConcurrentHashMap<>();
    }

    public void addOperationManager(DeviceTypeIdentifier type, OperationManager operationManager) {
        operationManagers.put(type, operationManager);
    }

    public OperationManager getOperationManager(DeviceTypeIdentifier type) {
        return operationManagers.get(type);
    }

    public void removeOperationManager(DeviceTypeIdentifier type) {
        operationManagers.remove(type);
    }

}
