/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.common.device.details;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfo implements Serializable{

    private static final long serialVersionUID = 1998101733L;

    private int deviceId;
    private String deviceType;
    private DeviceIdentifier deviceIdentifier;

    private String IMEI;
    private String IMSI;
    private String deviceModel;
    private String vendor;
    private String osVersion;
    private Double batteryLevel;
    private Double internalTotalMemory;
    private Double internalAvailableMemory;
    private Double externalTotalMemory;
    private Double externalAvailableMemory;
    private String operator;
    private String connectionType;
    private Double mobileSignalStrength;
    private String ssid;
    private Double cpuUsage;
    private Double totalRAMMemory;
    private Double availableRAMMemory;
    private boolean pluggedIn;

    private Map<String, String> deviceDetailsMap = new HashMap<>();

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Double getInternalTotalMemory() {
        return internalTotalMemory;
    }

    public void setInternalTotalMemory(Double internalTotalMemory) {
        this.internalTotalMemory = internalTotalMemory;
    }

    public Double getInternalAvailableMemory() {
        return internalAvailableMemory;
    }

    public void setInternalAvailableMemory(Double internalAvailableMemory) {
        this.internalAvailableMemory = internalAvailableMemory;
    }

    public Double getExternalTotalMemory() {
        return externalTotalMemory;
    }

    public void setExternalTotalMemory(Double externalTotalMemory) {
        this.externalTotalMemory = externalTotalMemory;
    }

    public Double getExternalAvailableMemory() {
        return externalAvailableMemory;
    }

    public void setExternalAvailableMemory(Double externalAvailableMemory) {
        this.externalAvailableMemory = externalAvailableMemory;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Double getMobileSignalStrength() {
        return mobileSignalStrength;
    }

    public void setMobileSignalStrength(Double mobileSignalStrength) {
        this.mobileSignalStrength = mobileSignalStrength;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getTotalRAMMemory() {
        return totalRAMMemory;
    }

    public void setTotalRAMMemory(Double totalRAMMemory) {
        this.totalRAMMemory = totalRAMMemory;
    }

    public Double getAvailableRAMMemory() {
        return availableRAMMemory;
    }

    public void setAvailableRAMMemory(Double availableRAMMemory) {
        this.availableRAMMemory = availableRAMMemory;
    }

    public boolean isPluggedIn() {
        return pluggedIn;
    }

    public void setPluggedIn(boolean pluggedIn) {
        this.pluggedIn = pluggedIn;
    }

    public void setDeviceDetailsMap(Map<String,String> deviceDetailsMap) {
        this.deviceDetailsMap = deviceDetailsMap;
    }

    public Map<String, String> getDeviceDetailsMap() {
        return deviceDetailsMap;
    }
}

