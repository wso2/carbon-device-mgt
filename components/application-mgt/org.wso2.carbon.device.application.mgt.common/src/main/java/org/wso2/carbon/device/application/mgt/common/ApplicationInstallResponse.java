/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;

import java.util.List;

public class ApplicationInstallResponse {
    @ApiModelProperty(
            name = "successfulDevices",
            value = "List of successful devices",
            dataType = "List[org.wso2.carbon.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceIdentifier> successfulDevices;

    @ApiModelProperty(
            name = "failedDevices",
            value = "List of failed devices",
            dataType = "List[org.wso2.carbon.device.mgt.common.DeviceIdentifier]"
    )
    private List<DeviceIdentifier> failedDevices;

    @ApiModelProperty(
            name = "activity",
            value = "Activity corresponding to the operation"
    )
    private Activity activity;

    public List<DeviceIdentifier> getSuccessfulDevices() {
        return successfulDevices;
    }

    public void setSuccessfulDevices(List<DeviceIdentifier> successfulDevices) {
        this.successfulDevices = successfulDevices;
    }

    public List<DeviceIdentifier> getFailedDevices() {
        return failedDevices;
    }

    public void setFailedDevices(List<DeviceIdentifier> failedDevices) {
        this.failedDevices = failedDevices;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
