/*
 * Copyright (c) 2016, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Size;

/**
 * This class is used to wrap the events which receive from the agent application.
 */
@ApiModel(value = "EventBeanWrapper",
        description = "agent's event related Information.")
public class EventBeanWrapper {

    @ApiModelProperty(name = "payloadData", value = "Event payload payload.", required = true)
    Object[] payloadData;

    public Object[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(Object[] payloadData) {
        this.payloadData = payloadData;
    }

}
