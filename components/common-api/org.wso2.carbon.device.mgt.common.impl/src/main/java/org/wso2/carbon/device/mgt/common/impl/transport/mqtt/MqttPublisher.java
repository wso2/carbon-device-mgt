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

package org.wso2.carbon.device.mgt.common.impl.transport.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The Class MqttControlPublisher. It is an implementation of the interface
 * ControlQueueConnector.
 * This implementation supports publishing of control signals received to an
 * MQTT end-point.
 * The configuration settings for the MQTT end-point are read from the
 * 'devicecloud-config.xml' file of the project.
 * This is done using the class 'DeviceCloudConfigManager.java' which loads
 * the settings from the default xml org.wso2.carbon.device.mgt.iot.common.devicecloud.org.wso2.carbon.device.mgt.iot.common.config.server.configs
 * file -
 * /resources/conf/device-controls/devicecloud-config.xml
 */
public class MqttPublisher implements MqttCallback {

	private static final Log log = LogFactory.getLog(MqttPublisher.class);

	private String mqttEndpoint;
	private String mqttUsername;
	private String mqttPassword;
	private boolean mqttEnabled = false;

	public MqttPublisher() {
	}

	public void initControlQueue() throws DeviceControllerException {
		mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
		mqttUsername = MqttConfig.getInstance().getMqttQueueUsername();
		mqttPassword = MqttConfig.getInstance().getMqttQueuePassword();
		mqttEnabled = MqttConfig.getInstance().isEnabled();
	}

	public void publish(String publishClientId, String publishTopic, byte[] payload)
			throws DeviceControllerException {

		if (mqttEnabled) {
			MqttClient client;
			MqttConnectOptions options;

			if (publishClientId.length() > 24) {
				String errorString =
						"No of characters '" + publishClientId.length() + "' for ClientID: '" + publishClientId +
								"' is invalid (should be less than 24, hence please provide a " +
								"simple " +


								"'owner' tag)";
				log.error(errorString);
				throw new DeviceControllerException(errorString);
			} else {
				log.info("No of Characters " + publishClientId.length() + " for ClientID : '" + publishClientId +
								 "' is acceptable");
			}

			try {
				client = new MqttClient(mqttEndpoint,publishClientId);
				options = new MqttConnectOptions();
				options.setWill("iotDevice/clienterrors", "crashed".getBytes(UTF_8), 2, true);
				client.setCallback(this);
				client.connect(options);

				client.publish(publishTopic, payload, 0, true);

				if (log.isDebugEnabled()) {
					log.debug("MQTT Client successfully published to topic: " + publishTopic +
									  ", with payload - " + payload);
				}
				client.disconnect();
			} catch (MqttException ex) {
				String errorMsg =
						"MQTT Client Error" + "\n\tReason:  " + ex.getReasonCode() +
								"\n\tMessage: " +
								ex.getMessage() + "\n\tLocalMsg: " + ex.getLocalizedMessage() +
								"\n\tCause: " + ex.getCause() + "\n\tException: " + ex;

				log.error(errorMsg, ex);
				throw new DeviceControllerException(errorMsg, ex);
			}
		} else {
			log.warn("MQTT <Enabled> set to false in 'devicecloud-config.xml'");
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		log.error("Connection to MQTT Endpoint Lost");
	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken topic) {
		log.info("Published topic: '" + topic.getTopics()[0] + "' successfully to client: '" +
						 topic.getClient().getClientId() + "'");
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		log.info("MQTT Message recieved: " + arg1.toString());
	}


}
