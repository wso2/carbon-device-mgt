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
package org.wso2.carbon.device.mgt.jaxrs.beans.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.jaxrs.beans.BasePaginatedResult;

import java.util.ArrayList;
import java.util.List;

/**
 * This holds event attributes
 */
public class EventAttributeList {

    private List<Attribute> attributes = new ArrayList<>();

    @ApiModelProperty(value = "List of Event Attributes")
    @JsonProperty("attributes")
    public List<Attribute> getList() {
        return attributes;
    }

    public void setList(List<Attribute> attributes) {
        this.attributes = attributes;
    }

}

