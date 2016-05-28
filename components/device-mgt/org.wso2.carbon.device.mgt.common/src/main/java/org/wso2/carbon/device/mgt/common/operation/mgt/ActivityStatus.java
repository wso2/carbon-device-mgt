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


package org.wso2.carbon.device.mgt.common.operation.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

@ApiModel(value = "ActivityStatus", description = "Status of an activity is described in this class.")
public class ActivityStatus {

    public enum Status {
        IN_PROGRESS, PENDING, COMPLETED, ERROR, REPEATED
    }
    @ApiModelProperty(
            name = "deviceIdentifier",
            value = "Device identifier of the device.",
            required = true)
    private DeviceIdentifier deviceIdentifier;
    @ApiModelProperty(
            name = "status",
            value = "Status of the activity performed.",
            required = true)
    private Status status;
    @ApiModelProperty(
            name = "responses",
            value = "Responses received from devices.",
            required = true)
    private List<OperationResponse> responses;
    @ApiModelProperty(
            name = "updatedTimestamp    ",
            value = "Last updated time of the activity.",
            required = true)
    private String updatedTimestamp;

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<OperationResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<OperationResponse> responses) {
        this.responses = responses;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }
}

