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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.sensor.mgt.dao;

import java.util.List;

/**
 * This is an interface intended for use by the device-plugin writer
 */
public interface DeviceSensorDAO {
    boolean addSensor(SensorTransactionObject sensorTransactionObject) throws DeviceSensorDAOException;

    boolean updateSensor(SensorTransactionObject sensorTransactionObject) throws DeviceSensorDAOException;

    SensorTransactionObject getSensor(String deviceIdentifier, String sensorIdentifier) throws DeviceSensorDAOException;

    SensorTransactionObject getSensor(String sensorIdentifier) throws DeviceSensorDAOException;

    List<SensorTransactionObject> getSensors(String deviceIdentifier) throws DeviceSensorDAOException;

    boolean removeSensor(String deviceIdentifier, String sensorIdentifier) throws DeviceSensorDAOException;

    boolean removeSensor(String sensorIdentifier) throws DeviceSensorDAOException;

    boolean removeSensors(String deviceIdentifier) throws DeviceSensorDAOException;
}
