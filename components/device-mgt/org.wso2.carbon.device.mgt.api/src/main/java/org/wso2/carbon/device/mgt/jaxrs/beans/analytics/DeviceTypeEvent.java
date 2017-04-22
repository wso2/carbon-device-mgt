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

/**
 * This hold stats data record
 */
public class DeviceTypeEvent {

    private EventAttributeList eventAttributes;
    private TransportType transport;

    @ApiModelProperty(value = "Attributes related to device type event")
    @JsonProperty("eventAttributes")
    public EventAttributeList getEventAttributeList() {
        return eventAttributes;
    }

    public void setEventAttributeList(
            EventAttributeList eventAttributes) {
        this.eventAttributes = eventAttributes;
    }

    @ApiModelProperty(value = "Transport to be used for device to server communication.")
    @JsonProperty("transport")
    public TransportType getTransportType() {
        return transport;
    }

    public void setTransportType(TransportType transport) {
        this.transport = transport;
    }
}

