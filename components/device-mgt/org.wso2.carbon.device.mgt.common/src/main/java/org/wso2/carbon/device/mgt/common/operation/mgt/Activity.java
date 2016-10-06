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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "Activity", description = "Each activity instance has a unique identifier that can be " +
        "used to identify an operation instance.")
public class Activity {

    public enum Type {
        CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY
    }

    @ApiModelProperty(
            name = "activityId",
            value = "The unique activity identifier",
            required = true)
    @JsonProperty("activityId")
    private String activityId;

    @ApiModelProperty(
            name = "code",
            value = "The activity code",
            required = true)
    @JsonProperty("code")
    private String code;

    @ApiModelProperty(
            name = "type",
            value = "The type of the activity, such as CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY.",
            required = true,
            allowableValues = "CONFIG, MESSAGE, INFO, COMMAND, PROFILE, POLICY")
    @JsonProperty("type")
    private Type type;

    @ApiModelProperty(
            name = "createdTimeStamp",
            value = "The recorded timestamp of when the activity took place.",
            required = true)
    @JsonProperty("createdTimestamp")
    private String createdTimeStamp;

    @ApiModelProperty(
            name = "activityStatuses",
            value = "The collection of statuses for a given activity.",
            required = true)
    @JsonProperty("activityStatuses")
    private List<ActivityStatus> activityStatus;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedTimeStamp(String createdTimeStamp) {
        this.createdTimeStamp = createdTimeStamp;
    }

    public List<ActivityStatus> getActivityStatus() {
        return activityStatus;
    }

    public void setActivityStatus(List<ActivityStatus> activityStatus) {
        this.activityStatus = activityStatus;
    }
}

