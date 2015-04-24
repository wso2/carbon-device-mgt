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
package org.wso2.carbon.device.mgt.core;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.HashMap;
import java.util.Map;

public class DeviceManagementRepository {

    private Map<String, DeviceManager> providers;

    public DeviceManagementRepository() {
        providers = new HashMap<String, DeviceManager>();
    }

    public void addDeviceManagementProvider(DeviceManager provider) throws DeviceManagementException {
        String deviceType = provider.getProviderType();
        try {
            DeviceManagerUtil.registerDeviceType(deviceType);
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while adding device management provider '" +
                    deviceType + "'");
        }
        providers.put(deviceType, provider);
    }

    public void removeDeviceManagementProvider(DeviceManager provider) throws DeviceManagementException {
        String deviceType = provider.getProviderType();
        try {
            DeviceManagerUtil.unregisterDeviceType(deviceType);
        } catch (DeviceManagementException e) {
            throw new DeviceManagementException("Error occurred while removing device management provider '" +
                    deviceType + "'", e);
        }
        providers.remove(deviceType);
    }

    public DeviceManager getDeviceManagementProvider(String type) {
        return providers.get(type);
    }

}
