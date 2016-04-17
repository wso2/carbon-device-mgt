/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util;

import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This is added to make sure that the apis are publishsed after gateway services are intiialized.
 */
public class ServerStartupListener implements ServerStartupObserver {
	private static volatile boolean serverReady = false;
	@Override
	public void completingServerStartup() {
	}

	@Override
	public void completedServerStartup() {
		ServerStartupListener.setServerReady(true);
	}

	public static boolean isServerReady() {
		return ServerStartupListener.serverReady;
	}

	public static void setServerReady(boolean serverReady) {
		ServerStartupListener.serverReady = serverReady;
	}
}
