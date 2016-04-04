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

package org.wso2.carbon.device.mgt.etc.controlqueue.xmpp;

import org.wso2.carbon.device.mgt.etc.config.server.datasource.ControlQueue;
import org.wso2.carbon.device.mgt.etc.config.server.DeviceCloudConfigManager;

public class XmppConfig {
	private String xmppServerIP;
	private int xmppServerPort;
	private String xmppEndpoint;
	private String xmppUsername;
	private String xmppPassword;
	private boolean isEnabled;

	private static final String XMPP_QUEUE_CONFIG_NAME = "XMPP";
	private final int SERVER_CONNECTION_PORT = 5222;

	private ControlQueue xmppControlQueue;

	private static XmppConfig xmppConfig = new XmppConfig();

	public String getXmppServerIP() {
		return xmppServerIP;
	}

	public int getXmppServerPort() {
		return xmppServerPort;
	}

	public String getXmppEndpoint() {
		return xmppEndpoint;
	}

	public String getXmppUsername() {
		return xmppUsername;
	}

	public String getXmppPassword() {
		return xmppPassword;
	}

	public ControlQueue getXmppControlQueue() {
		return xmppControlQueue;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public static String getXmppQueueConfigName() {
		return XMPP_QUEUE_CONFIG_NAME;
	}

	private XmppConfig() {
		xmppControlQueue = DeviceCloudConfigManager.getInstance().getControlQueue(
				XMPP_QUEUE_CONFIG_NAME);

		xmppServerIP = xmppControlQueue.getServerURL();
		int indexOfChar = xmppServerIP.lastIndexOf('/');
		if (indexOfChar != -1) {
			xmppServerIP = xmppServerIP.substring((indexOfChar + 1), xmppServerIP.length());
		}

		xmppServerPort = xmppControlQueue.getPort();
		xmppEndpoint = xmppControlQueue.getServerURL() + ":" + xmppServerPort;
		xmppUsername = xmppControlQueue.getUsername();
		xmppPassword = xmppControlQueue.getPassword();
		isEnabled = xmppControlQueue.isEnabled();
	}

	public static XmppConfig getInstance() {
		return xmppConfig;
	}

	public int getSERVER_CONNECTION_PORT() {
		return SERVER_CONNECTION_PORT;
	}
}
