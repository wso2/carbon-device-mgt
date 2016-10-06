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

@ApiModel(value = "OperationResponse", description = "This class carries all information related to operation"
        + " responses")
public class OperationResponse {

    @ApiModelProperty(
            name = "response",
            value = "Operation response returned from the device",
            required = true,
            example = "SUCCESSFUL")
    @JsonProperty("response")
    private String response;

    @ApiModelProperty(
            name = "receivedTimeStamp",
            value = "Time that the operation response received",
            required = true,
            example = "Thu Oct 06 11:18:47 IST 2016")
    @JsonProperty("receivedTimeStamp")
    private String receivedTimeStamp;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getReceivedTimeStamp() {
        return receivedTimeStamp;
    }

    public void setReceivedTimeStamp(String receivedTimeStamp) {
        this.receivedTimeStamp = receivedTimeStamp;
    }
}

