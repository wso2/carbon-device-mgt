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

package org.wso2.carbon.device.mgt.common.sensor.mgt;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.SensorTransactionObject;

import java.util.List;

public interface SensorManager {

    /**
     * The following set of methods correspond to the Sensors that a specific DeviceType supports. They are ideally
     * used at the time of registering a new Device-Type to let the framework know what Sensors are attached to this
     * Device-Type. This information is later used during the time of instantiating a new Device of this Device-Type
     * to set Sensor properties of that specific device instance.
     */

    /**
     * @throws DeviceManagementException
     */
    void initDeviceTypeSensors() throws DeviceManagementException;

    List<DeviceTypeSensor> getDeviceTypeSensors() throws DeviceManagementException;

    /**
     * ---------------------------------------------------------------------------------------------------------------
     * The following methods corresponds to the individual Sensors that exist in specific instances of the Device-Type
     * itself. These methods are ideally used during the time of instantiating a new device of the given Device-Type.
     */


    boolean addSensor(SensorTransactionObject sensorTObject) throws DeviceManagementException;

    boolean addSensors(String deviceIdentifier, List<SensorTransactionObject> sensorTObjects)
            throws DeviceManagementException;

    boolean updateSensor(SensorTransactionObject sensorTObject) throws DeviceManagementException;

    boolean updateSensors(String deviceIdentifier, List<SensorTransactionObject> sensorTObjects)
            throws DeviceManagementException;

    SensorTransactionObject getSensor(String deviceIdentifier, String sensorIdentifier)
            throws DeviceManagementException;

    List<SensorTransactionObject> getSensors(String deviceIdentifier) throws DeviceManagementException;

    boolean removeSensor(String deviceIdentifier, String sensorIdentifier) throws DeviceManagementException;

    boolean removeSensors(String deviceIdentifier) throws DeviceManagementException;
}
