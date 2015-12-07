/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.impl.sensormgt;

import org.wso2.carbon.device.mgt.common.impl.exception.DeviceControllerException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store latest sensor value readings against a device id in an in-memory map.
 */
public class SensorDataManager {

    private static final SensorDataManager instance = new SensorDataManager();
    private Map<String, DeviceRecord> deviceMap = new HashMap<>();

    private SensorDataManager() {
    }

    public static SensorDataManager getInstance() {
        return instance;
    }

    /**
     * Store sensor record in a map.
     * @param deviceId
     * @param sensorName
     * @param sensorValue
     * @param time
     * @return if success returns true
     */
    public boolean setSensorRecord(String deviceId, String sensorName, String sensorValue, long time){

        DeviceRecord deviceRecord = deviceMap.get(deviceId);
        if(deviceRecord==null){
            deviceRecord = new DeviceRecord(sensorName, sensorValue, time);
        }else{
            deviceRecord.addDeviceRecord(sensorName, sensorValue, time);
        }
        deviceMap.put(deviceId, deviceRecord);
        return true;
    }

    /**
     * Returns last updated sensor records list for a device
     * @param deviceId
     * @return list of sensor records
     */
    public SensorRecord[] getSensorRecords(String deviceId) throws DeviceControllerException{
        DeviceRecord deviceRecord = deviceMap.get(deviceId);
        if(deviceRecord != null){
            Collection<SensorRecord> list = deviceRecord.getSensorDataList().values();
            return list.toArray(new SensorRecord[list.size()]);
        }
        throw new DeviceControllerException("Error: No records found for the device ID: " + deviceId);
    }

    /**
     * Returns last updated sensor record for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return sensor record
     */
    public SensorRecord getSensorRecord(String deviceId, String sensorName) throws
            DeviceControllerException{
        DeviceRecord deviceRecord = deviceMap.get(deviceId);
        if(deviceRecord != null){
            SensorRecord sensorRecord = deviceRecord.getSensorDataList().get(sensorName);
            if(sensorRecord != null){
                return sensorRecord;
            }
            throw new DeviceControllerException("Error: No records found for the Device ID: " + deviceId + " Sensor Name: "+sensorName);
        }
        throw new DeviceControllerException("Error: No records found for the device ID: " + deviceId);
    }

    /**
     * Returns last updated sensor value for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return sensor reading
     */
    public String getSensorRecordValue(String deviceId, String sensorName) throws DeviceControllerException{
        DeviceRecord deviceRecord = deviceMap.get(deviceId);
        if(deviceRecord != null){
            SensorRecord sensorRecord = deviceRecord.getSensorDataList().get(sensorName);
            if(sensorRecord != null){
                return sensorRecord.getSensorValue();
            }
            throw new DeviceControllerException("Error: No records found for the Device ID: " + deviceId + " Sensor Name: "+sensorName);
        }
        throw new DeviceControllerException("Error: No records found for the device ID: " + deviceId);
    }

    /**
     * Returns last updated sensor value reading time for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return time in millis
     */
    public long getSensorRecordTime(String deviceId, String sensorName) throws DeviceControllerException{
        DeviceRecord deviceRecord = deviceMap.get(deviceId);
        if(deviceRecord != null){
            SensorRecord sensorRecord = deviceRecord.getSensorDataList().get(sensorName);
            if(sensorRecord != null){
                return sensorRecord.getTime();
            }
            throw new DeviceControllerException("Error: No records found for the Device ID: " + deviceId + " Sensor Name: "+sensorName);
        }
        throw new DeviceControllerException("Error: No records found for the device ID: " + deviceId);
    }

}
