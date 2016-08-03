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

public class DeviceTypeSensorTransactionObject implements Serializable {
    private static final long serialVersionUID = -3151279311229073204L;

    private int deviceTypeId;
    private int deviceTypeSensorId;
    private DeviceTypeSensor deviceTypeSensor;

    public DeviceTypeSensorTransactionObject() {
    }

    public DeviceTypeSensorTransactionObject(DeviceTypeSensor deviceTypeSensor) {
        this.deviceTypeSensor = deviceTypeSensor;
    }

    public DeviceTypeSensor getDeviceTypeSensor() {
        return deviceTypeSensor;
    }

    public void setDeviceTypeSensor(DeviceTypeSensor deviceTypeSensor) {
        this.deviceTypeSensor = deviceTypeSensor;
    }

    public int getDeviceTypeSensorId() {
        return deviceTypeSensorId;
    }

    public void setDeviceTypeSensorId(int deviceTypeSensorId) {
        this.deviceTypeSensorId = deviceTypeSensorId;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceTypeSensorTransactionObject that = (DeviceTypeSensorTransactionObject) o;
        return deviceTypeId == that.deviceTypeId && deviceTypeSensorId == that.deviceTypeSensorId &&
                deviceTypeSensor.equals(that.deviceTypeSensor);
    }

    @Override
    public int hashCode() {
        int result = deviceTypeId;
        result = 31 * result + deviceTypeSensorId;
        result = 31 * result + deviceTypeSensor.hashCode();
        return result;
    }

    public static class DAOConstants {
        public static final String SENSOR_ID = "SENSOR_ID";
        public static final String SENSOR_NAME = "SENSOR_NAME";
        public static final String DEVICE_TYPE_ID = "DEVICE_TYPE_ID";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String SENSOR_TYPE = "SENSOR_TYPE";
        public static final String STATIC_PROPERTIES = "STATIC_PROPERTIES";
        public static final String STREAM_DEFINITION = "STREAM_DEFINITION";
    }
}
