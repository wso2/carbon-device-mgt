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

package org.wso2.carbon.device.mgt.common.impl.controlqueue.mqtt;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.impl.controlqueue.ControlQueueConnector;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;

import java.io.File;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * The Class MqttControlPublisher. It is an implementation of the interface
 * ControlQueueConnector.
 * This implementation supports publishing of control signals received to an
 * MQTT end-point.
 * The configuration settings for the MQTT end-point are read from the
 * 'controller.xml' file of the project.
 * This is done using the class 'DefaultDeviceControlConfigs.java' which loads
 * the settings from the default xml org.wso2.carbon.device.mgt.iot.common.devicecloud.org.wso2.carbon.device.mgt.iot.common.config.server.configs
 * file -
 * /resources/conf/device-controls/controller.xml
 */
//TODO-Make these MQTT, XMPP publishers/ subscribers into "transport" package
public class MqttControlPublisher implements ControlQueueConnector, MqttCallback {

	private static final Log log = LogFactory.getLog(MqttControlPublisher.class);

	private String mqttEndpoint;
	private String mqttUsername;
	private String mqttPassword;
	private boolean mqttEnabled = false;

	public MqttControlPublisher() {
	}

	@Override
	public void initControlQueue() throws DeviceControllerException {
		mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
		mqttUsername = MqttConfig.getInstance().getMqttQueueUsername();
		mqttPassword = MqttConfig.getInstance().getMqttQueuePassword();
		mqttEnabled = MqttConfig.getInstance().isEnabled();
	}


	@Override
	public void enqueueControls(HashMap<String, String> deviceControls)
			throws DeviceControllerException {

		if (mqttEnabled) {
			MqttClient client;
			MqttConnectOptions options;

			String owner = deviceControls.get("owner");
			String deviceType = deviceControls.get("deviceType");
			String deviceId = deviceControls.get("deviceId");
			String key = deviceControls.get("key");
			String value = deviceControls.get("value");

			String clientId = owner + "." + deviceId;

			if (clientId.length() > 24) {
				String errorString =
						"No of characters '" + clientId.length() + "' for ClientID: '" + clientId +
								"' is invalid (should be less than 24, hence please provide a " +
								"simple " +


								"'owner' tag)";
				log.error(errorString);
				throw new DeviceControllerException(errorString);
			} else {
				log.info("No of Characters " + clientId.length() + " for ClientID : '" + clientId +
								 "' is acceptable");
			}

			String publishTopic =
					"wso2" + File.separator + "iot" + File.separator + owner + File.separator +
							deviceType + File.separator
							+ deviceId;
			String payLoad = key + ":" + value;

			log.info("Publish-Topic: " + publishTopic);
			log.info("PayLoad: " + payLoad);

			try {
				client = new MqttClient(mqttEndpoint,clientId);
				options = new MqttConnectOptions();
				options.setWill("device/clienterrors", "crashed".getBytes(UTF_8), 2, true);
				client.setCallback(this);
				client.connect(options);

				MqttMessage message = new MqttMessage();
				message.setPayload(payLoad.getBytes(UTF_8));
				client.publish(publishTopic, payLoad.getBytes(UTF_8), 0, true);

				if (log.isDebugEnabled()) {
					log.debug("MQTT Client successfully published to topic: " + publishTopic +
									  ", with payload - " + payLoad);
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
			log.warn("MQTT <Enabled> set to false in 'device-mgt-config.xml'");
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
		log.info("MQTT Message received: " + arg1.toString());
	}


}
