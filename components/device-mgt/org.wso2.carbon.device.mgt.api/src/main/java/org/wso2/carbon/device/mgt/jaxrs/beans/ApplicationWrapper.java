/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;


@ApiModel(value = "ApplicationWrapper", description = "Application details together with user, " +
        "role or devices the application is associated with.")
public class ApplicationWrapper {

    @ApiModelProperty(name = "userNameList", value = "List of user names.", required = true)
    private List<String> userNameList;
    @ApiModelProperty(name = "roleNameList", value = "List of role names.", required = true)
    private List<String> roleNameList;
    @ApiModelProperty(name = "deviceIdentifiers", value = "List of device identifiers.", required = true,
            dataType = "List[org.wso2.carbon.device.mgt.common.DeviceIdentifier]")
    private List<DeviceIdentifier> deviceIdentifiers;
    @ApiModelProperty(name = "application", value = "Details of the mobile application.", required = true)
    private MobileApp application;

    public MobileApp getApplication() {
        return application;
    }

    public void setApplication(MobileApp application) {
        this.application = application;
    }

    public List<String> getUserNameList() {
        return userNameList;
    }

    public void setUserNameList(List<String> userNameList) {
        this.userNameList = userNameList;
    }

    public List<String> getRoleNameList() {
        return roleNameList;
    }

    public void setRoleNameList(List<String> roleNameList) {
        this.roleNameList = roleNameList;
    }

    public List<DeviceIdentifier> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifiers(List<DeviceIdentifier> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

}
