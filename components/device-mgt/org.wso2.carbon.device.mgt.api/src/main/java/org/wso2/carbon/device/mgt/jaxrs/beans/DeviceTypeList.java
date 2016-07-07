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
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceTypeList {

    private int count;
    private String next;
    private String previous;
    private List<String> deviceTypes = new ArrayList<>();

    /**
     * Number of Devices Types returned.
     */
    @ApiModelProperty(value = "Number of resources returned.")
    @JsonProperty("count")
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    /**
     * Link to the next subset of resources qualified. \nEmpty if no more resources are to be returned.
     */
    @ApiModelProperty(value = "Link to the next subset of resources qualified. \n " +
            "Empty if no more resources are to be returned.")
    @JsonProperty("next")
    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    /**
     * Link to the previous subset of resources qualified. \nEmpty if current subset is the first subset returned.
     */
    @ApiModelProperty(value = "Link to the previous subset of resources qualified. \n" +
            "Empty if current subset is the first subset returned.")
    @JsonProperty("previous")
    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    @ApiModelProperty(value = "List of device types returned")
    @JsonProperty("devicesTypes")
    public List<String> getList() {
        return deviceTypes;
    }

    public void setList(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("  next: ").append(getNext()).append(",\n");
        sb.append("  previous: ").append(getPrevious()).append(",\n");
        sb.append("  deviceTypes: [").append(deviceTypes).append("\n");
        sb.append("]}\n");
        return sb.toString();
    }


}
