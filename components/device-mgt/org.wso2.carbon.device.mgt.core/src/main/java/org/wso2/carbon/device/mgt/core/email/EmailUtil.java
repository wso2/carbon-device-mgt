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
package org.wso2.carbon.device.mgt.core.email;

import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

public class EmailUtil {

    public static String getServerBaseHttpsUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return "https://" + hostName + ":" + port;
    }

    public static String getServerBaseHttpUrl() {
        String hostName = "localhost";
        try {
            hostName = NetworkUtils.getMgtHostName();
        } catch (Exception ignored) {
        }
        ConfigurationContextService configContextService =
                DeviceManagementDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, "http");
        int httpProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                        "http");
        if (httpProxyPort > 0) {
            port = httpProxyPort;
        }
        return "http://" + hostName + ":" + port;
    }

}
