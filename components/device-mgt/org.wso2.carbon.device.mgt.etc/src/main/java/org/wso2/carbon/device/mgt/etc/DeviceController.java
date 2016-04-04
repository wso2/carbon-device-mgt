/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.etc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.etc.config.server.datasource.ControlQueue;
import org.wso2.carbon.device.mgt.etc.config.server.datasource.DeviceCloudConfig;
import org.wso2.carbon.device.mgt.etc.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.etc.datastore.impl.ThriftDataStoreConnector;
import org.wso2.carbon.device.mgt.etc.util.ResourceFileLoader;
import org.wso2.carbon.device.mgt.etc.config.server.DeviceCloudConfigManager;
import org.wso2.carbon.device.mgt.etc.config.server.datasource.DataStore;
import org.wso2.carbon.device.mgt.etc.controlqueue.ControlQueueConnector;
import org.wso2.carbon.device.mgt.etc.datastore.DataStoreConnector;
import org.wso2.carbon.device.mgt.etc.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.etc.exception.UnauthorizedException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class DeviceController {

	private static final Log log = LogFactory.getLog(DeviceController.class);

	private static HashMap<String, DataStoreConnector> dataStoresMap = new HashMap<>();
	private static HashMap<String, ControlQueueConnector> controlQueueMap = new HashMap<>();

	public static void init() {
		DeviceCloudConfig config = DeviceCloudConfigManager.getInstance().getDeviceCloudMgtConfig();

		if (config != null) {
			initSecurity(config);
			loadDataStores(config);
			loadControlQueues(config);
		}

	}


	private static void loadDataStores(DeviceCloudConfig config) {
		List<DataStore> dataStores = config.getDataStores().getDataStore();
		if (dataStores == null) {
			log.error("Error occurred when trying to read data stores configurations");
			return;
		}

		for (DataStore dataStore : dataStores) {
			try {
				String handlerClass = dataStore.getPublisherClass();

				Class<?> dataStoreClass = Class.forName(handlerClass);
				if (DataStoreConnector.class.isAssignableFrom(dataStoreClass)) {

					DataStoreConnector dataStoreConnector =
							(DataStoreConnector) dataStoreClass.newInstance();
					String dataStoreName = dataStore.getName();
					if (dataStore.isEnabled()) {
						dataStoresMap.put(dataStoreName, dataStoreConnector);
						dataStoreConnector.initDataStore(dataStore);
					}
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
				log.error("Error occurred when trying to initiate data store", ex);
			} catch (DeviceControllerException ex) {
				log.error(ex.getMessage());
			}
		}
	}

	private static void loadControlQueues(DeviceCloudConfig config) {
		List<ControlQueue> controlQueues = config.getControlQueues().getControlQueue();
		if (controlQueues == null) {
			log.error("Error occurred when trying to read control queue configurations");
			return;
		}

		for (ControlQueue controlQueue : controlQueues) {
			try {
				String handlerClass = controlQueue.getControlClass();

				Class<?> controlQueueClass = Class.forName(handlerClass);
				if (ControlQueueConnector.class.isAssignableFrom(controlQueueClass)) {

					ControlQueueConnector controlQueueConnector =
							(ControlQueueConnector) controlQueueClass.newInstance();
					String controlQueueName = controlQueue.getName();
					if (controlQueue.isEnabled()) {
						controlQueueMap.put(controlQueueName, controlQueueConnector);
						controlQueueConnector.initControlQueue();
					}
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
				log.error("Error occurred when trying to initiate control queue" +
								  controlQueue.getName());
			} catch (DeviceControllerException ex) {
				log.error(ex.getMessage());
			}
		}
	}

	private static void initSecurity(DeviceCloudConfig config) {
		String trustStoreFile = null;
		String trustStorePassword = null;
		File certificateFile = null;

		trustStoreFile = config.getSecurity().getClientTrustStore();
		trustStorePassword = config.getSecurity().getPassword();
		String certificatePath =
				CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
						"resources" + File.separator + "security" + File.separator;

		certificateFile = new ResourceFileLoader(certificatePath + trustStoreFile).getFile();
		if (certificateFile.exists()) {
			trustStoreFile = certificateFile.getAbsolutePath();
			log.info("Trust Store Path : " + trustStoreFile);

			System.setProperty("javax.net.ssl.trustStore", trustStoreFile);
			System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
		} else {
			log.error("Trust Store not found in path : " + certificateFile.getAbsolutePath());
		}
	}

	public boolean publishMqttControl(String owner, String deviceType, String deviceId, String key,
									  String value) throws DeviceControllerException {
		HashMap<String, String> deviceControlsMap = new HashMap<String, String>();

		deviceControlsMap.put("owner", owner);
		deviceControlsMap.put("deviceType", deviceType);
		deviceControlsMap.put("deviceId", deviceId);
		deviceControlsMap.put("key", key);
		deviceControlsMap.put("value", value);


		ControlQueueConnector mqttControlQueue = controlQueueMap.get(MqttConfig.getMqttQueueConfigName());
		if (mqttControlQueue == null) {
			log.info("MQTT Queue has not been listed in 'device-mgt-config.xml'");
			return false;
		}

		mqttControlQueue.enqueueControls(deviceControlsMap);
		return true;
	}

	private boolean pushData(HashMap<String, String> deviceDataMap, String publisherType)
			throws DeviceControllerException {

		DataStoreConnector dataStoreConnector = dataStoresMap.get(publisherType);
		if (dataStoreConnector == null) {
			log.error(publisherType + " is not enabled");
			return false;
		}

		dataStoreConnector.publishDeviceData(deviceDataMap);
		return true;
	}

	public boolean pushBamData(String owner, String deviceType, String deviceId, Long time,
							   String key,
							   String value, String description) throws UnauthorizedException {

		HashMap<String, String> deviceDataMap = new HashMap<String, String>();

		deviceDataMap.put("owner", owner);
		deviceDataMap.put("deviceType", deviceType);
		deviceDataMap.put("deviceId", deviceId);
		deviceDataMap.put("time", "" + time);
		deviceDataMap.put("key", key);
		deviceDataMap.put("value", value);
		deviceDataMap.put("description", description);

		try {
			return pushData(deviceDataMap, ThriftDataStoreConnector.DataStoreConstants.BAM);
		} catch (DeviceControllerException e) {
			throw new UnauthorizedException(e);
		}
	}

	public boolean pushCepData(String owner, String deviceType, String deviceId, Long time,
							   String key,
							   String value, String description)
			throws UnauthorizedException {
		HashMap<String, String> deviceDataMap = new HashMap<String, String>();

		deviceDataMap.put("owner", owner);
		deviceDataMap.put("deviceType", deviceType);
		deviceDataMap.put("deviceId", deviceId);
		deviceDataMap.put("time", "" + time);
		deviceDataMap.put("key", key);
		deviceDataMap.put("value", value);
		deviceDataMap.put("description", description);

		try {
			return pushData(deviceDataMap, ThriftDataStoreConnector.DataStoreConstants.CEP);
		} catch (DeviceControllerException e) {
			throw new UnauthorizedException(e);
		}

	}

}
