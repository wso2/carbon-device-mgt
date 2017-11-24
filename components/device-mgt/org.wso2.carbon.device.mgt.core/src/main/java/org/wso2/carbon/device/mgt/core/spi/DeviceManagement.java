/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.spi;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.device.mgt.core.manager.DeviceAgentManager;
import org.wso2.carbon.device.mgt.core.manager.DeviceManager;
import org.wso2.carbon.device.mgt.core.manager.DeviceTypeManager;

/**
 * The interface for the device management component.
 */
public interface DeviceManagement {
    /**
     * Get device type manager.
     * @return
     */
    DeviceTypeManager getDeviceTypeManager();

    /**
     * Get config provider
     * @return
     */
    ConfigProvider getConfigProvider();

    /**
     * Get device agent manager.
     * @return
     */
    DeviceAgentManager getDeviceAgentManager();


    /**
     * Get device manager.
     * @return
     */
    DeviceManager getDeviceManager();
}
