package org.wso2.carbon.device.mgt.common.api.controlqueue.xmpp;

import org.wso2.carbon.device.mgt.common.api.config.server.datasource.ControlQueue;
import org.wso2.carbon.device.mgt.common.api.config.server.DeviceCloudConfigManager;

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
