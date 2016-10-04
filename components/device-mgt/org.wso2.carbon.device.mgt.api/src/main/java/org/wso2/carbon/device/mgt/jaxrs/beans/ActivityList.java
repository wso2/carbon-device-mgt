/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "List of activities", description = "This contains a set of activities that matches a given " +
        "criteria as a collection")
public class ActivityList extends BasePaginatedResult {

    private List<Activity> activities;

    @ApiModelProperty(value = "List of devices returned")
    @JsonProperty("activities")
    public List<Activity> getList() {
        return activities;
    }

    public void setList(List<Activity> activities) {
        this.activities = activities;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("  devices: [").append(activities).append("\n");
        sb.append("]}\n");
        return sb.toString();
    }


}
