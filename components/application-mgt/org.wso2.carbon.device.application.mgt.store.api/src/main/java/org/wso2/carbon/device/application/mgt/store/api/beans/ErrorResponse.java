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
package org.wso2.carbon.device.application.mgt.publisher.api.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents a response that need to be send back to the client, when the request cannot be completed
 * successfully.
 */
@ApiModel(description = "Error Response")
public class ErrorResponse {

    private Integer code = null;
    private String message = null;
    private String description = null;
    private String moreInfo = null;
    private List<ErrorListItem> errorItems = new ArrayList<>();

    @JsonProperty(value = "code")
    @ApiModelProperty(required = true, value = "")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @JsonProperty(value = "message")
    @ApiModelProperty(required = true, value = "ErrorResponse message.")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty(value = "description")
    @ApiModelProperty(value = "A detail description about the error message.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty(value = "moreInfo")
    @ApiModelProperty(value = "Preferably an url with more details about the error.")
    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public void addErrorListItem(ErrorListItem item) {
        this.errorItems.add(item);
    }

    /**
     * If there are more than one error list them out. \nFor example, list out validation errors by each field.
     */
    @JsonProperty(value = "errorItems")
    @ApiModelProperty(value = "If there are more than one error list them out. \n" +
            "For example, list out validation errors by each field.")
    public List<ErrorListItem> getErrorItems() {
        return errorItems;
    }

    public void setErrorItems(List<ErrorListItem> error) {
        this.errorItems = error;
    }
}
