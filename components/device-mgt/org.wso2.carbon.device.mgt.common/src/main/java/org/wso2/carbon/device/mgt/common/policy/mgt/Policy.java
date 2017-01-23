/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common.policy.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class will be the used to create policy object with relevant information for evaluating.
 */
@XmlRootElement
@ApiModel(value = "Policy", description = "This class carries all information related to Policies")
public class Policy implements Comparable<Policy>, Serializable {

    private static final long serialVersionUID = 19981017L;

    @ApiModelProperty(
            name = "id",
            value = "The policy ID",
            required = true,
            example = "1")
    private int id;                         // Identifier of the policy.

    @ApiModelProperty(
            name = "priorityId",
            value = "The priority order of the policy. 1 indicates the highest priority",
            required = true,
            example = "1")
    private int priorityId;        // Priority of the policies. This will be used only for simple evaluation.

    @ApiModelProperty(
            name = "profile",
            value = "Contains the details of the profile that is included in the policy",
            required = true)
    private Profile profile;                  // Profile

    @ApiModelProperty(
            name = "policyName",
            value = "The name of the policy",
            required = true,
            example = "Block Camera")
    private String policyName;              // Name of the policy.

    @ApiModelProperty(
            name = "generic",
            value = "If true, this should be applied to all related device",
            required = true,
            example = "false")
    private boolean generic;                // If true, this should be applied to all related device.

    @ApiModelProperty(
            name = "roles",
            value = "The roles to whom the policy is applied on",
            required = true,
            example = "[ \n" + " \"ANY\"\n" + " ]")
    private List<String> roles;          // Roles which this policy should be applied.

    @ApiModelProperty(
            name = "ownershipType",
            value = "The policy ownership type. It can be any of the following values:\n"
                    + "ANY - The policy will be applied on the BYOD and COPE device types\n"
                    + "BYOD (Bring Your Own Device) - The policy will only be applied on the BYOD device type\n"
                    + "COPE (Corporate-Owned, Personally-Enabled) - The policy will only be applied on the COPE "
                    + "device type\n",
            required = true,
            example = "BYOD")
    private String ownershipType;           // Ownership type (COPE, BYOD, CPE)

    @ApiModelProperty(
            name = "devices",
            value = "Lists out the devices the policy is enforced on",
            required = true,
            example = "[]")
    private List<Device> devices;        // Individual devices this policy should be applied

    @ApiModelProperty(
            name = "users",
            value = "Lists out the users on whose devices the policy is enforced",
            required = true,
            example = "[]")
    private List<String> users;

    @ApiModelProperty(
            name = "active",
            value = "If the value is true it indicates that the policy is active. "
                    + "If the value is false it indicates that the policy is inactive",
            required = true,
            example = "false")
    private boolean active;

    @ApiModelProperty(
            name = "updated",
            value = "If you have made changes to the policy but have not applied"
                    + " these changes to the devices that are registered with EMM, then the value is defined as true."
                    + " But if you have already applied any changes made to the policy then the value is defined as"
                    + " false.",
            required = true,
            example = "false")
    private boolean updated;

    @ApiModelProperty(
            name = "description",
            value = "Gives a description on the policy",
            required = true,
            example = "This will block the camera functionality")
    private String description;

    @ApiModelProperty(
            name = "compliance",
            value = "Define the non-compliance rules. WSO2 EMM provides the following non-compliance rules:\n"
                    + "Enforce - Forcefully enforce the policies on the devices.\n"
                    + "Warning - If the device does not adhere to the given policies a warning message will "
                    + "be sent.\n"
                    + "Monitor - If the device does not adhere to the given policies the server is notified "
                    + "of the violation unknown to the user and the administrator can take the necessary "
                    + "actions with regard to the reported",
            required = true,
            example = "enforce")
    private String compliance; /* Compliance data*/

    /*Dynamic policy attributes*/

    /* This is related criteria based policy */

    @ApiModelProperty(
            name = "policyCriterias",
            value = "",
            required = true,
            example = "[]")
    private List<PolicyCriterion> policyCriterias;

    @ApiModelProperty(
            name = "tenantId",
            value = "",
            required = true,
            example = "-1234")
    private int tenantId;

    @ApiModelProperty(
            name = "profileId",
            value = "",
            required = true,
            example = "1")
    private int profileId;

    /*This will be used to record attributes which will be used by customer extended PDPs and PIPs*/

    private Map<String, Object> attributes;

    /*This will keep the list of groups to which the policy will be applied. */
    @ApiModelProperty(
            name = "deviceGroups",
            value = "This will keep the list of groups to which the policy will be applied",
            required = true,
            example = "[]")
    private List<DeviceGroupWrapper> deviceGroups;


    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public int getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(int priorityId) {
        this.priorityId = priorityId;
    }

    @XmlElement
    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    @XmlElement
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    @XmlElement
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @XmlElement
    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    @XmlElement
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @XmlElement
    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    @XmlElement
    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    @XmlElement
    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    @XmlElement
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlElement
    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public List<PolicyCriterion> getPolicyCriterias() {
        return policyCriterias;
    }

    public void setPolicyCriterias(List<PolicyCriterion> policyCriterias) {
        this.policyCriterias = policyCriterias;
    }

    @XmlElement
    public String getCompliance() {
        return compliance;
    }

    public void setCompliance(String compliance) {
        this.compliance = compliance;
    }

    @XmlElement
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @XmlElement
    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }


    @XmlElement
    public List<DeviceGroupWrapper> getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(List<DeviceGroupWrapper> deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

    @Override
    public int compareTo(Policy o) {
        if (this.priorityId == o.priorityId)
            return 0;
        else if ((this.priorityId) > o.priorityId)
            return 1;
        else
            return -1;
    }
}