/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.notification.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO of Notification object which is used to communicate Operation notifications to MDM core.
 */

@ApiModel(value = "Notification", description = "This is used to communicate Operation notifications to MDM.")
public class Notification {

    public enum Status {
        NEW, CHECKED
    }

    public enum Type {
        ALERT
    }

    @JsonProperty(value = "id", required = false)
    @ApiModelProperty(name = "id", value = "Defines the notification ID.", required = false)
    private int id;

    @JsonProperty(value = "description", required = false)
    @ApiModelProperty(name = "description", value = "Provides the message you want to send to the user.",
            required = true)
    private String description;

    @JsonProperty(value = "operationId", required = true)
    @ApiModelProperty(name = "operationId", value = "Provides the operationID.", required = true)
    private int operationId;

    @JsonProperty(value = "status", required = true)
    @ApiModelProperty(name = "status", value = "Provides the status of the message." +
            "The following values can be assigned for the status.\n" +
            "NEW: The message is in the unread state.\n" +
            "CHECKED: The message is in the read state.", required = true)
    private Status status;

    @JsonProperty(value = "deviceIdentifier", required = false)
    @ApiModelProperty(name = "deviceIdentifier", value = "Defines the device ID related to the notification.",
            required = false)
    private String deviceIdentifier;

    @JsonProperty(value = "deviceName", required = false)
    @ApiModelProperty(name = "deviceName", value = "Defines the device Name related to the notification.",
                      required = false)
    private String deviceName;

    @JsonProperty(value = "devieType", required = false)
    @ApiModelProperty(name = "devieType", value = "Defines the device type related to the notification.",
            required = false)
    private String deviceType;

    public Status getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }

    public int getNotificationId() {
        return id;
    }

    public void setNotificationId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String devieType) {
        this.deviceType = devieType;
    }

    @Override
    public String toString() {
        return "notification {" +
                "  id='" + id + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", operationId='" + operationId + '\'' +
                ", deviceIdentifier='" + deviceIdentifier + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }

}
