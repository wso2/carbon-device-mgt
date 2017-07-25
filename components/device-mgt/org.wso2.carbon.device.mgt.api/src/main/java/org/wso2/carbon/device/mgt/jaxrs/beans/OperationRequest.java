/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import java.util.List;

@ApiModel(value = "OperationRequest", description = "Operation details together with deviceIdentifier")
public class OperationRequest {

    @ApiModelProperty(name = "deviceIdentifiers", value = "list of devices that needs to be verified against the user", required = true)
    List<String> deviceIdentifiers;
    @ApiModelProperty(name = "operation", value = "operation data", required = false)
    Operation operation;

    public List<String> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<String> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
