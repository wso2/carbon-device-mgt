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

public class DeviceInfo implements Serializable {

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
        if (IMEI != null) {
            return IMEI;
        } else {
            return "";
        }
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getIMSI() {
        if (IMSI != null) {
            return IMSI;
        } else {
            return "";
        }
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getDeviceModel() {
        if (deviceModel != null) {
            return deviceModel;
        } else {
            return "";
        }
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getVendor() {
        if (vendor != null) {
            return vendor;
        } else {
            return "";
        }
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getOsVersion() {
        if (osVersion != null) {
            return osVersion;
        } else {
            return "";
        }
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Double getBatteryLevel() {
        if (batteryLevel != null) {
            return batteryLevel;
        } else {
            return 0.0;
        }
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Double getInternalTotalMemory() {
        if (internalTotalMemory != null) {
            return internalTotalMemory;
        } else {
            return 0.0;
        }
    }

    public void setInternalTotalMemory(Double internalTotalMemory) {
        this.internalTotalMemory = internalTotalMemory;
    }

    public Double getInternalAvailableMemory() {
        if (internalAvailableMemory != null) {
            return internalAvailableMemory;
        } else {
            return 0.0;
        }
    }

    public void setInternalAvailableMemory(Double internalAvailableMemory) {
        this.internalAvailableMemory = internalAvailableMemory;
    }

    public Double getExternalTotalMemory() {
        if (externalTotalMemory != null) {
            return externalTotalMemory;
        } else {
            return 0.0;
        }
    }

    public void setExternalTotalMemory(Double externalTotalMemory) {
        this.externalTotalMemory = externalTotalMemory;
    }

    public Double getExternalAvailableMemory() {
        if (externalAvailableMemory != null) {
            return externalAvailableMemory;
        } else {
            return 0.0;
        }
    }

    public void setExternalAvailableMemory(Double externalAvailableMemory) {
        this.externalAvailableMemory = externalAvailableMemory;
    }

    public String getOperator() {
        if (operator != null) {
            return operator;
        } else {
            return "";
        }
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getConnectionType() {
        if (connectionType != null) {
            return connectionType;
        } else {
            return "";
        }
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public Double getMobileSignalStrength() {
        if (mobileSignalStrength != null) {
            return mobileSignalStrength;
        } else {
            return 0.0;
        }
    }

    public void setMobileSignalStrength(Double mobileSignalStrength) {
        this.mobileSignalStrength = mobileSignalStrength;
    }

    public String getSsid() {
        if (ssid != null) {
            return ssid;
        } else {
            return "";
        }
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Double getCpuUsage() {
        if (cpuUsage != null) {
            return cpuUsage;
        } else {
            return 0.0;
        }
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getTotalRAMMemory() {
        if (totalRAMMemory != null) {
            return totalRAMMemory;
        } else {
            return 0.0;
        }
    }

    public void setTotalRAMMemory(Double totalRAMMemory) {
        this.totalRAMMemory = totalRAMMemory;
    }

    public Double getAvailableRAMMemory() {
        if (availableRAMMemory != null) {
            return availableRAMMemory;
        } else {
            return 0.0;
        }
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

    public void setDeviceDetailsMap(Map<String, String> deviceDetailsMap) {
        this.deviceDetailsMap = deviceDetailsMap;
    }

    public Map<String, String> getDeviceDetailsMap() {
        return deviceDetailsMap;
    }
}

