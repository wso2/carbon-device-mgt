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
import org.wso2.carbon.device.mgt.common.Device;
import java.util.List;

@ApiModel(value = "PolicyWrapper", description = "This class carries all information related to Policy "
        + "Wrappers")
public class PolicyWrapper {

    @ApiModelProperty(name = "id", value = "The policy ID", required = true)
    private int id;
    @ApiModelProperty(name = "profile", value = "Contains the details of the profile that is included in the"
            + " policy", required = true)
    private Profile profile;
    @ApiModelProperty(name = "policyName", value = "The name of the policy", required = true)
    private String policyName;
    @ApiModelProperty(name = "description", value = "Gives a description on the policy", required = true)
    private String description;
    @ApiModelProperty(name = "compliance", value = "Provides the non-compliance rules. WSO2 EMM provides the"
            + " following non-compliance rules:\n"
            + "Enforce - Forcefully enforce the policies on the devices\n"
            + "Warning - If the device does not adhere to the given policies a warning message will be sent\n"
            + "Monitor - If the device does not adhere to the given policies the server is notified of the "
            + "violation unknown to the user and the administrator can take the necessary actions with regard"
            + " to the reported", required = true)
    private String compliance;
    @ApiModelProperty(name = "roles", value = "The roles to whom the policy is applied on", required = true)
    private List<String> roles;
    @ApiModelProperty(name = "ownershipType", value = "The policy ownership type. It can be any of the "
            + "following values:\n"
            + "ANY - The policy will be applied on the BYOD and COPE device types\n"
            + "BYOD (Bring Your Own Device) - The policy will only be applied on the BYOD device type\n"
            + "COPE (Corporate-Owned, Personally-Enabled) - The policy will only be applied on the COPE "
            + "device type", required = true)
    private String ownershipType;
    @ApiModelProperty(name = "devices", value = "Lists out the devices the policy is enforced on",
            required = true)
    private List<Device> devices;
    @ApiModelProperty(name = "users", value = "Lists out the users on whose devices the policy is enforced",
            required = true)
    private List<String> users;
    @ApiModelProperty(name = "tenantId", value = "The ID of the tenant that created the policy",
            required = true)
    private int tenantId;
    @ApiModelProperty(name = "profileId", value = "The ID of each profile that is in the selected policy",
            required = true)
    private int profileId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getCompliance() {
        return compliance;
    }

    public void setCompliance(String compliance) {
        this.compliance = compliance;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

}
