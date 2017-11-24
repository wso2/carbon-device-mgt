/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */


package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * This class carries all information related location of the device
 */
@ApiModel(value = "DeviceLocation", description = "This class carries all information related to the device location " +
        "details provided by a device.")
public class DeviceLocation implements Serializable {

    private static final long serialVersionUID = 1998101722L;

    @ApiModelProperty(name = "deviceId", value = "Device id", required = true)
    private int deviceId;
    @ApiModelProperty(name = "deviceType", value = "Device type such android, ios or windows.",
                      required = true)
    private String deviceType;

    @ApiModelProperty(name = "latitude", value = "Device GPS latitude.", required = true)
    private Double latitude;
    @ApiModelProperty(name = "longitude", value = "Device GPS longitude.", required = true)
    private Double longitude;
    @ApiModelProperty(name = "street1", value = "First line of the address.", required = true)
    private String street1;
    @ApiModelProperty(name = "street2", value = "Second part of the address.", required = true)
    private String street2;
    @ApiModelProperty(name = "city", value = "City of the device location.", required = true)
    private String city;
    @ApiModelProperty(name = "state", value = "State of the device address.", required = true)
    private String state;
    @ApiModelProperty(name = "zip", value = "Zip code of the device address.", required = true)
    private String zip;
    @ApiModelProperty(name = "country", value = "Country of the device address.", required = true)
    private String country;
    @ApiModelProperty(name = "updatedTime", value = "Update time of the device.", required = true)
    private Date updatedTime;

    public DeviceLocation(int deviceId, String deviceType, Double latitude, Double longitude, String street1,
                          String street2, String city, String state, String zip, String country, Date updatedTime) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.updatedTime = new Date(updatedTime.getTime());
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getStreet1() {
        return street1;
    }

    public String getStreet2() {
        return street2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getCountry() {
        return country;
    }

    public Date getUpdatedTime() {
        if (updatedTime == null) {
            updatedTime = new Date();
        }
        return new Date(updatedTime.getTime());
    }
}

