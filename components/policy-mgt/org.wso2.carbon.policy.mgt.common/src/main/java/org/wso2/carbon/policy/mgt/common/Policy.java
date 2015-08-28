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
package org.wso2.carbon.policy.mgt.common;

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
public class Policy implements Comparable<Policy>, Serializable {

    private static final long serialVersionUID = 19981017L;

    private int id;                         // Identifier of the policy.
    private int priorityId;                 // Priority of the policies. This will be used only for simple evaluation.
    private Profile profile;                  // Profile
    private String policyName;              // Name of the policy.
    private boolean generic;                // If true, this should be applied to all related device.
    private List<String> roles;          // Roles which this policy should be applied.
    private String ownershipType;           // Ownership type (COPE, BYOD, CPE)
    private List<Device> devices;        // Individual devices this policy should be applied
    private List<String> users;
    private boolean active;
    private boolean updated;


    /* Compliance data*/
    private String Compliance;

    /*Dynamic policy attributes*/

    /* This is related criteria based policy */

    private List<PolicyCriterion> policyCriterias;

    private int tenantId;
    private int profileId;

    /*This will be used to record attributes which will be used by customer extended PDPs and PIPs*/

    private Map<String, Object> attributes;

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
    public List<PolicyCriterion> getPolicyCriterias() {
        return policyCriterias;
    }

    public void setPolicyCriterias(List<PolicyCriterion> policyCriterias) {
        this.policyCriterias = policyCriterias;
    }

    @XmlElement
    public String getCompliance() {
        return Compliance;
    }

    public void setCompliance(String compliance) {
        Compliance = compliance;
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