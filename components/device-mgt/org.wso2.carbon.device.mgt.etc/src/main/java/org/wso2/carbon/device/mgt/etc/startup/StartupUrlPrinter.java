/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.etc.startup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.etc.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

public class StartupUrlPrinter implements ServerStartupObserver {
	private static final Log log = LogFactory.getLog(StartupUrlPrinter.class);
	@Override
	public void completingServerStartup() {

	}

	@Override
	public void completedServerStartup() {
		printUrl();


	}
	private void printUrl() {
		// Hostname
		String hostName = "localhost";
		try {
			hostName = NetworkUtils.getMgtHostName();
		} catch (Exception ignored) {
		}
		// HTTPS port
		String mgtConsoleTransport = CarbonUtils.getManagementTransport();
		ConfigurationContextService configContextService = DeviceManagementServiceComponent.configurationContextService;

		int httpsPort = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);

		log.info("CDM - Device Common API Service: https://" + hostName + ":" + httpsPort + "/common");
	}

}
