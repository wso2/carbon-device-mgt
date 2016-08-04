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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class definition for the Sensor Objects that are units attached to a specific Device-type. Each Sensor object is
 * of a specific SensorType. Also the stream-definition and the properties of the SensorType are inherited upon
 * initialization of a Sensor object. Additional properties and stream-definition related data can be added to the
 * Sensor instance.
 */
public class Sensor implements Serializable {
    private String sensorIdentifier;
    private String deviceIdentifier;
    private DeviceTypeSensor deviceTypeSensor;
    private Map<String, String> dynamicProperties;

    public Sensor() {

    }

    public Sensor(String sensorIdentifier, String deviceIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
        this.deviceIdentifier = deviceIdentifier;
    }

    /**
     * Default constructor for the Sensor Object. Requires a DeviceTypeSensor Object to be passed to indicate of
     * which DeviceTypeSensor's instance is this Sensor attached to the Device.
     *
     * @param sensorIdentifier
     * @param deviceIdentifier
     * @param deviceTypeSensor The DeviceTypeSensor of the Sensor in the Device to be created.
     */
    public Sensor(String sensorIdentifier, String deviceIdentifier, DeviceTypeSensor deviceTypeSensor) {
        this.sensorIdentifier = sensorIdentifier;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceTypeSensor = deviceTypeSensor;
    }

    public Sensor(String sensorIdentifier, String deviceIdentifier, DeviceTypeSensor deviceTypeSensor,
                  Map<String, String> dynamicProperties) {
        this.sensorIdentifier = sensorIdentifier;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceTypeSensor = deviceTypeSensor;
        this.dynamicProperties = dynamicProperties;
    }

    public String getSensorIdentifier() {
        return sensorIdentifier;
    }

    public void setSensorIdentifier(String sensorIdentifier) {
        this.sensorIdentifier = sensorIdentifier;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public DeviceTypeSensor getDeviceTypeSensor() {
        return deviceTypeSensor;
    }

    public void setDeviceTypeSensor(DeviceTypeSensor deviceTypeSensor) {
        this.deviceTypeSensor = deviceTypeSensor;
    }

    public Map<String, String> getDynamicProperties() {
        return dynamicProperties;
    }

    public void setDynamicProperties(Map<String, String> dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    public Map<String, String> getStaticProperties() {
        return this.deviceTypeSensor.getStaticProperties();
    }

    public void setStaticProperties(Map<String, String> staticProperties) {
        this.deviceTypeSensor.setStaticProperties(staticProperties);
    }

    /**
     * @return
     */
    public Map<String, Object> getStaticAndDynamicProperties() {
        Map<String, Object> aggregatedProperties = new HashMap<>();
        aggregatedProperties.putAll(this.deviceTypeSensor.getStaticProperties());
        aggregatedProperties.putAll(this.dynamicProperties);
        return aggregatedProperties;
    }

    /**
     * Adds a new property relevant to the specific Sensor object.
     *
     * @param dynamicProperty The property key to be added.
     * @param value           The value of the above newly added property.
     */
    public void addDynamicProperty(String dynamicProperty, String value) {
        this.dynamicProperties.put(dynamicProperty, value);
    }


}
