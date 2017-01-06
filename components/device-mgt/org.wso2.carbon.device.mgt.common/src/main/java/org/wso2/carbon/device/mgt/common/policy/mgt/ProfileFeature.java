/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.common.policy.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "ProfileFeature", description = "This class carries all information related to profile "
        + "features")
public class ProfileFeature implements Serializable {

    private static final long serialVersionUID = 19981018L;

    @ApiModelProperty(
            name = "id",
            value = "Define the ID",
            required = true,
            example = "1")
    private int id;

    @ApiModelProperty(
            name = "featureCode",
            value = "Provide the code that defines the policy you wish to add",
            required = true,
            example = "CAMERA")
    private String featureCode;

    @ApiModelProperty(
            name = "profileId",
            value = "Define the ID of the profile",
            required = true,
            example = "1")
    private int profileId;

    @ApiModelProperty(
            name = "deviceType",
            value = "The ID used to define the type of the device platform",
            required = true,
            example = "android")
    private String deviceType;

    @ApiModelProperty(
            name = "content",
            value = "The list of parameters that define the policy",
            required = true,
            example = "{\\\"enabled\\\":false}")
    private Object content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}