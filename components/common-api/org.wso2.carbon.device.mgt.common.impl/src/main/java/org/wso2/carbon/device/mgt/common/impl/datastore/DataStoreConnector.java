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

package org.wso2.carbon.device.mgt.common.impl.datastore;

import org.wso2.carbon.device.mgt.common.impl.config.server.datasource.DataStore;
import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;

import java.util.HashMap;

/**
 * The Interface DataStoreConnector.
 *
 */
public interface DataStoreConnector {

	/**
	 * Initializes the data store.
	 * This method loads the initial configurations relevant to the
	 * Data-Store implementation where the sensor data will pushed into
	 *
	 * @return A status message according to the outcome of the
	 *         method execution.
	 */
	void initDataStore(DataStore config) throws DeviceControllerException;

	/**
	 * Pushes the device/sensor data received from the devices into the
	 * implemented data-store
	 *
	 * @param deviceData
	 *            A Hash Map which contains the parameters relevant to the
	 *            device and the actual device-data to be pushed into
	 *            the datastore
	 * @return A status message according to the outcome of the
	 *         method execution.
	 */
	void publishDeviceData(HashMap<String, String> deviceData)
			throws DeviceControllerException;
}
