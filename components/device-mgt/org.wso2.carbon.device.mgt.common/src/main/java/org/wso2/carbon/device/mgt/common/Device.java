/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

@ApiModel(value = "Device", description = "This class carries all information related to a managed device.")
public class Device implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "id", value = "ID of the device in the WSO2 EMM device information database.",
            required = true)
    private int id;
    @ApiModelProperty(name = "name", value = "The device name that can be set on the device by the device user.",
            required = true)
    private String name;
    @ApiModelProperty(name = "type", value = "The OS type of the device.", required = true)
    private String type;
    @ApiModelProperty(name = "description", value = "Additional information on the device.", required = true)
    private String description;
    @ApiModelProperty(name = "deviceIdentifier", value = "This is a 64-bit number (as a hex string) that is randomly" +
            " generated when the user first sets up the device and should" +
            " remain constant for the lifetime of the user's device." +
            " The value may change if a factory reset is performed on " +
            "the device.",
            required = true)
    private String deviceIdentifier;
    @ApiModelProperty(name = "enrolmentInfo", value = "This defines the device registration related information. " +
            "It is mandatory to define this information.", required = true)
    private EnrolmentInfo enrolmentInfo;
    @ApiModelProperty(name = "features", value = "List of features.", required = true)
    private List<Feature> features;
    private List<Device.Property> properties;

    public Device() {
    }

    public Device(String name, String type, String description, String deviceId, EnrolmentInfo enrolmentInfo,
                  List<Feature> features, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
        this.features = features;
        this.properties = properties;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public EnrolmentInfo getEnrolmentInfo() {
        return enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<Device.Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Device.Property> properties) {
        this.properties = properties;
    }

    public static class Property {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "device [" +
                "name=" + name + ";" +
                "type=" + type + ";" +
                "description=" + description + ";" +
                "identifier=" + deviceIdentifier + ";" +
//                "EnrolmentInfo[" +
//                "owner=" + enrolmentInfo.getOwner() + ";" +
//                "ownership=" + enrolmentInfo.getOwnership() + ";" +
//                "status=" + enrolmentInfo.getStatus() + ";" +
//                "]" +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Device))
            return false;

        Device device = (Device) o;

        return getDeviceIdentifier().equals(device.getDeviceIdentifier());

    }

    @Override
    public int hashCode() {
        return getDeviceIdentifier().hashCode();
    }

}
