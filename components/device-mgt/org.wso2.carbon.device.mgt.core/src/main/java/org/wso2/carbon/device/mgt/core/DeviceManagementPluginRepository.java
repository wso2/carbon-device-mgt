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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagerStartupListener;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceManagementPluginRepository implements DeviceManagerStartupListener {

    private Map<String, DeviceManagementService> providers;
    private boolean isInited;
    private static final Log log = LogFactory.getLog(DeviceManagementPluginRepository.class);

    public DeviceManagementPluginRepository() {
        providers = Collections.synchronizedMap(new HashMap<String, DeviceManagementService>());
        DeviceManagementServiceComponent.registerStartupListener(this);
    }

    public void addDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
            String deviceType = provider.getType();
        synchronized (providers) {
            try {
                if (isInited) {
                    /* Initializing Device Management Service Provider */
                    provider.init();
                    DeviceManagerUtil.registerDeviceType(deviceType);
                    DeviceManagementDataHolder.getInstance().setRequireDeviceAuthorization(deviceType,
                                                            provider.getDeviceManager().requireDeviceAuthorization());

                }
            } catch (DeviceManagementException e) {
                throw new DeviceManagementException("Error occurred while adding device management provider '" +
                        deviceType + "'", e);
            }
            providers.put(deviceType, provider);
        }
    }

    public void removeDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
        providers.remove(provider.getType());
    }

    public DeviceManagementService getDeviceManagementService(String type) {
        return providers.get(type);
    }

    public Map<String, DeviceManagementService> getAllDeviceManagementServices() {
        return providers;
    }

    @Override
    public void notifyObserver() {
        synchronized (providers) {
            for (DeviceManagementService provider : providers.values()) {
                try {
                    provider.init();
                    DeviceManagerUtil.registerDeviceType(provider.getType());
                    //TODO:
                    //This is a temporory fix.
                    //windows and IOS cannot resolve user info by extracting certs
                    //until fix that, use following variable to enable and disable of checking user authorization.

                    DeviceManagementDataHolder.getInstance().setRequireDeviceAuthorization(provider.getType(),
                                             provider.getDeviceManager().requireDeviceAuthorization());
                } catch (Throwable e) {
                    /* Throwable is caught intentionally as failure of one plugin - due to invalid start up parameters,
                        etc - should not block the initialization of other device management providers */
                    log.error("Error occurred while initializing device management provider '" +
                            provider.getType() + "'", e);
                }
            }
            this.isInited = true;
        }
    }

}
