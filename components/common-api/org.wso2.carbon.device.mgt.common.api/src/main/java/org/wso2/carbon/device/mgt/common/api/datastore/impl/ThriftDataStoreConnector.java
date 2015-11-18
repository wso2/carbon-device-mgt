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

package org.wso2.carbon.device.mgt.common.api.datastore.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception
		.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.device.mgt.common.api.config.server.datasource.DataStore;
import org.wso2.carbon.device.mgt.common.api.datastore.DataStoreConnector;
import org.wso2.carbon.device.mgt.common.api.exception.DeviceControllerException;

import java.net.MalformedURLException;
import java.util.HashMap;


public class ThriftDataStoreConnector implements DataStoreConnector {

	private static final Log log = LogFactory.getLog(ThriftDataStoreConnector.class);

	private String dataStoreEndpoint;
	private String dataStoreUsername;
	private String dataStorePassword;
	private boolean enabled = false;

	public final class DataStoreConstants {
		public final static String BAM = "WSO2-BAM";
		public final static String CEP = "WSO2-CEP";
	}

	@Override
	public void initDataStore(DataStore config) throws DeviceControllerException {
		dataStoreEndpoint = config.getServerURL() + ":" + config.getPort();
		dataStoreUsername = config.getUsername();
		dataStorePassword = config.getPassword();
		enabled = config.isEnabled();
	}

	private DataPublisher getDataPublisher() throws DeviceControllerException {

		try {
			DataPublisher dataPublisher = new DataPublisher(dataStoreEndpoint, dataStoreUsername,
															dataStorePassword);
			if (log.isDebugEnabled()) {
				log.info("data publisher created for endpoint " + dataStoreEndpoint);
			}
			return dataPublisher;
		} catch (MalformedURLException | AgentException | AuthenticationException
				| TransportException e) {
			String error = "Error creating data publisher for  endpoint: " + dataStoreEndpoint +
					"with credentials, username-" + dataStoreUsername + " and password-" +
					dataStorePassword + ": ";
			log.error(error);
			throw new DeviceControllerException(error);
		}
	}


	@Override
	public void publishIoTData(HashMap<String, String> deviceData) throws
																   DeviceControllerException {
		//TODO: Create a threadpool and publish the Data or  use a queue and publish to it and
		// have a data retreiver.
		DataPublisher dataPublisher = getDataPublisher();
		if (!enabled || dataPublisher == null) {
			throw new DeviceControllerException();
		}

		String owner = deviceData.get("owner");
		String deviceType = deviceData.get("deviceType");
		String deviceId = deviceData.get("deviceId");
		String time = deviceData.get("time");
		String key = deviceData.get("key");
		String value = deviceData.get("value");
		String description = deviceData.get("description");
		String deviceDataStream = null;
		try {
			//TODO read from configuration
			switch (description) {
				case DataStreamDefinitions.StreamTypeLabel.TEMPERATURE:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Temperature");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.TEMPERATURE_STREAM_DEFINITION);

					break;
				case DataStreamDefinitions.StreamTypeLabel.MOTION:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Motion (PIR)");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.MOTION_STREAM_DEFINITION);
					break;
				case DataStreamDefinitions.StreamTypeLabel.SONAR:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Sonar");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.SONAR_STREAM_DEFINITION);
					break;
				case DataStreamDefinitions.StreamTypeLabel.LIGHT:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Light");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.LIGHT_STREAM_DEFINITION);
					break;
				case DataStreamDefinitions.StreamTypeLabel.BULB:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Bulb Status");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.BULB_STREAM_DEFINITION);
					break;
				case DataStreamDefinitions.StreamTypeLabel.FAN:
					if (log.isDebugEnabled()) {
						log.debug("Stream definition set to Fan Status");
					}
					deviceDataStream = dataPublisher.defineStream(
							DataStreamDefinitions.FAN_STREAM_DEFINITION);
					break;
				default:
					break;
			}
		} catch (AgentException | MalformedStreamDefinitionException | StreamDefinitionException
				| DifferentStreamDefinitionAlreadyDefinedException e) {
			String error = "Error in defining fire-alarm specific streams for data publisher";
			log.error(error);
			throw new DeviceControllerException(error, e);
		}

		try {
			if (log.isDebugEnabled()) {
				log.debug("Publishing data");
			}
			dataPublisher.publish(deviceDataStream, System.currentTimeMillis(),
								  new Object[]{owner, deviceType, deviceId, Long.parseLong(
										  time)}, null,
								  new Object[]{value});

			if (log.isDebugEnabled()) {
				String logMsg = "event published to devicePinDataStream\n" + "\tOwner: " + owner +
						"\tDeviceType: " + deviceType + "\n" + "\tDeviceId: " + deviceId +
						"\tTime: " +
						time + "\n" + "\tDescription: " + description + "\n" + "\tKey: " + key +
						"\tValue: " + value + "\n";
				log.info(logMsg);
			}

		} catch (AgentException e) {
			String error = "Error while publishing device pin data";
			log.error(error);
			throw new DeviceControllerException(error, e);
		}
	}


}
