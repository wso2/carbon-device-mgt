package org.wso2.carbon.device.mgt.common.api.controlqueue.mqtt;

import org.wso2.carbon.device.mgt.common.api.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.common.api.config.server.datasource.ControlQueue;

public class MqttConfig {
	private String mqttQueueEndpoint;
	private String mqttQueueUsername;
	private String mqttQueuePassword;
	private boolean isEnabled;

	private static final String MQTT_QUEUE_CONFIG_NAME = "MQTT";

	private ControlQueue mqttControlQueue;

	private static MqttConfig mqttConfig = new MqttConfig();

	public String getMqttQueueEndpoint() {
		return mqttQueueEndpoint;
	}

	public String getMqttQueueUsername() {
		return mqttQueueUsername;
	}

	public String getMqttQueuePassword() {
		return mqttQueuePassword;
	}

	public ControlQueue getMqttControlQueue() {
		return mqttControlQueue;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public static String getMqttQueueConfigName() {
		return MQTT_QUEUE_CONFIG_NAME;
	}

	private MqttConfig() {
		mqttControlQueue = DeviceCloudConfigManager.getInstance().getControlQueue(
				MQTT_QUEUE_CONFIG_NAME);
		mqttQueueEndpoint = mqttControlQueue.getServerURL() + ":" + mqttControlQueue.getPort();
		mqttQueueUsername = mqttControlQueue.getUsername();
		mqttQueuePassword = mqttControlQueue.getPassword();
		isEnabled = mqttControlQueue.isEnabled();
	}

	public static MqttConfig getInstance() {
		return mqttConfig;
	}

}
