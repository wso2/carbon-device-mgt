/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.api.util.iotdevice.dto;

import java.io.Serializable;
import java.util.Map;


public class IotDevice implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
	private String iotDeviceId;
    private String iotDeviceName;
    private Map<String, String> deviceProperties;

    public void setIotDeviceName(String iotDeviceName){
        this.iotDeviceName=iotDeviceName;

    }
    public String getIotDeviceName() {
        return iotDeviceName;
    }

    public String getIotDeviceId() {
        return iotDeviceId;
    }

    public void setIotDeviceId(String iotDeviceId) {
        this.iotDeviceId = iotDeviceId;
    }

    public Map<String, String> getDeviceProperties() {
        return deviceProperties;
    }

    public void setDeviceProperties(Map<String, String> deviceProperties) {
        this.deviceProperties = deviceProperties;
    }
}
