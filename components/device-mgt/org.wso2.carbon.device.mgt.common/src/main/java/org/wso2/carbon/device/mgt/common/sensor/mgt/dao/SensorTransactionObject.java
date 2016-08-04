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

import org.wso2.carbon.device.mgt.common.sensor.mgt.DeviceTypeSensor;

import java.io.Serializable;
import java.util.Map;

public class SensorTransactionObject implements Serializable{
    private static final long serialVersionUID = -3151279311229073204L;

    private String sensorIdentifier;
    private String deviceIdentifier;
    private String sensorTypeUniqueName;
    private Map<String, String> dynamicProperties;

    public SensorTransactionObject(){}

    public SensorTransactionObject(String sensorIdentifier, String deviceIdentifier){
        this.sensorIdentifier = sensorIdentifier;
        this.deviceIdentifier = deviceIdentifier;
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

    public String getSensorTypeUniqueName() {
        return sensorTypeUniqueName;
    }

    public void setSensorTypeUniqueName(String sensorTypeUniqueName) {
        this.sensorTypeUniqueName = sensorTypeUniqueName;
    }

    public Map<String, String> getDynamicProperties() {
        return dynamicProperties;
    }

    public void setDynamicProperties(Map<String, String> dynamicProperties) {
        this.dynamicProperties = dynamicProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorTransactionObject that = (SensorTransactionObject) o;

        if (sensorIdentifier != null ? !sensorIdentifier.equals(that.sensorIdentifier) :
                that.sensorIdentifier != null) {
            return false;
        }
        if (deviceIdentifier != null ? !deviceIdentifier.equals(that.deviceIdentifier) :
                that.deviceIdentifier != null) {
            return false;
        }
        if (!sensorTypeUniqueName.equals(that.sensorTypeUniqueName)) return false;
        return dynamicProperties != null ? dynamicProperties.equals(that.dynamicProperties) :
                that.dynamicProperties == null;

    }

    @Override
    public int hashCode() {
        int result = sensorIdentifier != null ? sensorIdentifier.hashCode() : 0;
        result = 31 * result + (deviceIdentifier != null ? deviceIdentifier.hashCode() : 0);
        result = 31 * result + sensorTypeUniqueName.hashCode();
        result = 31 * result + (dynamicProperties != null ? dynamicProperties.hashCode() : 0);
        return result;
    }

    public static class DAOConstants {
        public static final String SENSOR_IDENTIFIER = "SENSOR_IDENTIFIER";
        public static final String DEVICE_IDENTIFIER = "DEVICE_IDENTIFIER";
        public static final String SENSOR_TYPE_NAME = "SENSOR_TYPE_NAME";
        public static final String PROPERTY_KEY = "PROPERTY_KEY";
        public static final String PROPERTY_VALUE = "PROPERTY_VALUE";
    }
}
