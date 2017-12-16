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
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;

import javax.validation.constraints.Size;
import java.util.List;

@ApiModel(value = "PolicyWrapper", description = "This class carries all information related to Policy "
        + "Wrappers")
public class PolicyWrapper {

    @ApiModelProperty(
            name = "policyName",
            value = "The name of the policy",
            required = true)
    @Size(max = 45)
    private String policyName;

    @ApiModelProperty(
            name = "description",
            value = "Gives a description on the policy",
            required = true)
    @Size(max = 1000)
    private String description;

    @ApiModelProperty(
            name = "compliance",
            value = "Provides the non-compliance rules. WSO2 EMM provides the following non-compliance rules:\n"
            + "Enforce - Forcefully enforce the policies on the devices\n"
            + "Warning - If the device does not adhere to the given policies a warning message will be sent\n"
            + "Monitor - If the device does not adhere to the given policies the server is notified of the "
            + "violation unknown to the user and the administrator can take the necessary actions with regard"
            + " to the reported",
            required = true)
    @Size(max = 100)
    private String compliance;

    @ApiModelProperty(
            name = "ownershipType",
            value = "The policy ownership type. It can be any of the following values:\n"
            + "ANY - The policy will be applied on the BYOD and COPE device types\n"
            + "BYOD (Bring Your Own Device) - The policy will only be applied on the BYOD device type\n"
            + "COPE (Corporate-Owned, Personally-Enabled) - The policy will only be applied on the COPE "
            + "device type",
            required = true)
    @Size(max = 45)
    private String ownershipType;

    @ApiModelProperty(
            name = "active",
            value = "If the value is true it indicates that the policy is active. If the value is false it "
                    + "indicates that the policy is inactive",
            required = true)
    private boolean active;

    @ApiModelProperty(
            name = "profile",
            value = "Contains the details of the profile that is included in the policy",
            required = true)
    private Profile profile;

    @ApiModelProperty(
            name = "roles",
            value = "The roles to whom the policy is applied on",
            required = true)
    private List<String> roles;

    @ApiModelProperty(
            name = "deviceIdentifiers",
            value = "Lists out the devices the policy is enforced on",
            required = true)
    private List<DeviceIdentifier> deviceIdentifiers;

    @ApiModelProperty(
            name = "users",
            value = "Lists out the users on whose devices the policy is enforced",
            required = true)
    private List<String> users;

    @ApiModelProperty(name = "deviceGroups", value = "Lists out the groups on whose devices the policy is enforced",
            required = true)
    private List<DeviceGroupWrapper> deviceGroups;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<DeviceIdentifier> getDeviceIdentifiers() {
        return deviceIdentifiers;
    }

    public void setDeviceIdentifier(List<DeviceIdentifier> deviceIdentifiers) {
        this.deviceIdentifiers = deviceIdentifiers;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<DeviceGroupWrapper> getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(List<DeviceGroupWrapper> deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

}
