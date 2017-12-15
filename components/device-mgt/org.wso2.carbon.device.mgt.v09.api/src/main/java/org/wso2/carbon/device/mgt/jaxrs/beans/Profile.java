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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.List;

@XmlRootElement
@ApiModel(value = "Profile", description = "This class carries all information related to policy profiles")
public class Profile {

    @ApiModelProperty(name = "profileId",
            value = "The ID of each profile that is in the selected policy",
            required = true,
            example = "1")
    private int profileId;
    @ApiModelProperty(name = "profileName",
            value = "The name of the profile",
            required = true,
            example = "Block Camera")
    private String profileName;
    @ApiModelProperty(name = "tenantId",
            value = "The ID of the tenant that added the policy",
            required = true,
            example = "-1234")
    private int tenantId;
    @ApiModelProperty(name = "deviceType",
            value = "Contains the device type details the policy was created for",
            required = true,
            example = "android")
    private String deviceType;
    @ApiModelProperty(name = "createdDate",
            value = "The date the policy was created",
            required = true,
            example = "Thu, 6 Oct 2016 14:39:32 +0530")
    private Timestamp createdDate;
    @ApiModelProperty(name = "updatedDate",
            value = "The date the changes made to the policy was published to the devices registered with the EMM",
            required = true,
            example = "Thu, 6 Oct 2016 14:39:32 +0530")
    private Timestamp updatedDate;
    @ApiModelProperty(name = "profileFeaturesList",
            value = "Contains the features specific to each profile in the policy",
            required = true)
    private List<ProfileFeature> profileFeaturesList;     // Features included in the policies.

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    @XmlElement
    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

/*    public List<FeatureImpl> getFeaturesList() {
        return featuresList;
    }

    public void setFeaturesList(List<FeatureImpl> featuresList) {
        this.featuresList = featuresList;
    }*/
    @XmlElement
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @XmlElement
    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    @XmlElement
    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @XmlElement
    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    @XmlElement
    public List<ProfileFeature> getProfileFeaturesList() {
        return profileFeaturesList;
    }

    public void setProfileFeaturesList(List<ProfileFeature> profileFeaturesList) {
        this.profileFeaturesList = profileFeaturesList;
    }

}
