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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApiModel(value = "DeviceInfo", description = "This class carries all information related to the device information " +
                                              "provided by a device.")
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 1998101733L;

//    @ApiModelProperty(name = "deviceId", value = "Device Id.", required = false)
//    private int deviceId;
//
//    @ApiModelProperty(name = "deviceType", value = "Type of the device.", required = true)
//    private String deviceType;
//
//    @ApiModelProperty(name = "deviceId", value = "Device identifier.", required = true)
//    private DeviceIdentifier deviceIdentifier;

    @ApiModelProperty(name = "IMEI", value = "IMEI number of the device.", required = true)
    private String IMEI;

    @ApiModelProperty(name = "IMSI", value = "IMSI number of the device.", required = true)
    private String IMSI;

    @ApiModelProperty(name = "deviceModel", value = "Model of the device.", required = true)
    private String deviceModel;

    @ApiModelProperty(name = "vendor", value = "Vendor of the device.", required = true)
    private String vendor;

    @ApiModelProperty(name = "osVersion", value = "Operating system version.", required = true)
    private String osVersion;

    @ApiModelProperty(name = "osBuildDate", value = "Operating system build date.", required = true)
    private String osBuildDate;

    @ApiModelProperty(name = "batteryLevel", value = "Battery level of the device.", required = true)
    private Double batteryLevel;

    @ApiModelProperty(name = "internalTotalMemory", value = "Total internal memory of the device.", required = true)
    private Double internalTotalMemory;

    @ApiModelProperty(name = "internalAvailableMemory", value = "Total available memory of the device.",
                      required = true)
    private Double internalAvailableMemory;

    @ApiModelProperty(name = "externalTotalMemory", value = "Total external memory of the device.", required = true)
    private Double externalTotalMemory;

    @ApiModelProperty(name = "externalAvailableMemory", value = "Total external memory avilable of the device.",
                      required = true)
    private Double externalAvailableMemory;

    @ApiModelProperty(name = "operator", value = "Mobile operator of the device.", required = true)
    private String operator;

    @ApiModelProperty(name = "connectionType", value = "How the device is connected to the network.", required = true)
    private String connectionType;

    @ApiModelProperty(name = "mobileSignalStrength", value = "Current mobile signal strength.", required = true)
    private Double mobileSignalStrength;

    @ApiModelProperty(name = "ssid", value = "ssid of the connected WiFi.", required = true)
    private String ssid;

    @ApiModelProperty(name = "cpuUsage", value = "Current total cpu usage.", required = true)
    private Double cpuUsage;

    @ApiModelProperty(name = "totalRAMMemory", value = "Total Ram memory size.", required = true)
    private Double totalRAMMemory;

    @ApiModelProperty(name = "availableRAMMemory", value = "Available total memory of RAM.", required = true)
    private Double availableRAMMemory;

    @ApiModelProperty(name = "pluggedIn", value = "Whether the device is plugged into power or not.",
                      required = true)
    private boolean pluggedIn;

    @ApiModelProperty(name = "updatedTime", value = "Device updated time.", required = true)
    private Date updatedTime;

    @ApiModelProperty(name = "location", value = "Last updated location of the device", required = false)
    private DeviceLocation location;

    @ApiModelProperty(name = "deviceDetailsMap", value = ".", required = true)
    private Map<String, String> deviceDetailsMap = new HashMap<>();

//    public int getDeviceId() {
//        return deviceId;
//    }
//
//    public void setDeviceId(int deviceId) {
//        this.deviceId = deviceId;
//    }
//
//    public String getDeviceType() {
//        return deviceType;
//    }
//
//    public void setDeviceType(String deviceType) {
//        this.deviceType = deviceType;
//    }
//
//    public DeviceIdentifier getDeviceIdentifier() {
//        return deviceIdentifier;
//    }
//
//    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
//        this.deviceIdentifier = deviceIdentifier;
//    }


    public DeviceLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceLocation location) {
        this.location = location;
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


    public String getOsBuildDate() {
        if (osBuildDate != null) {
            return osBuildDate;
        } else {
            return "";
        }
    }

    public void setOsBuildDate(String osBuildDate) {
        this.osBuildDate = osBuildDate;
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

    public Date getUpdatedTime() {
        if(updatedTime == null){
            updatedTime = new Date();
        }
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setDeviceDetailsMap(Map<String, String> deviceDetailsMap) {
        this.deviceDetailsMap = deviceDetailsMap;
    }

    public Map<String, String> getDeviceDetailsMap() {
        return deviceDetailsMap;
    }
}

