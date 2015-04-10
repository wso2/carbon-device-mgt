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


import org.wso2.carbon.device.mgt.core.dto.Device;

import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class will be the used to create policy object with relevant information for evaluating.
 */
public class Policy implements Comparable<Policy> {

    private int id;                         // Identifier of the policy.
    private int priorityId;                 // Priority of the policies. This will be used only for simple evaluation.
    private Profile profile;                  // Profile id
    private String policyName;              // Name of the policy.
    private boolean generic;                // If true, this should be applied to all related device.
    private List<String> roleList;          // Roles which this policy should be applied.
    private String ownershipType;           // Ownership type (COPE, BYOD, CPE)
    private List<Device> DeviceList;        // Individual devices this policy should be applied

    /*Dynamic policy attributes*/

    /*These are related to time based policies*/

    private int startTime;                  // Start time to apply the policy.
    private int endTime;                    // After this time policy will not be applied
    private Date startDate;                 // Start date to apply the policy
    private Date endDate;                   // After this date policy will not be applied.


    /*These are related to location based policies*/

    private String latitude;                // Latitude
    private String longitude;               // Longitude

    private int tenantId;

    /*This will be used to record attributes which will be used by customer extended PDPs and PIPs*/

    private Map<String, Object> attributes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(int priorityId) {
        this.priorityId = priorityId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public List<String> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<String> roleList) {
        this.roleList = roleList;
    }

    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    public List<Device> getDeviceList() {
        return DeviceList;
    }

    public void setDeviceList(List<Device> deviceList) {
        DeviceList = deviceList;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }


 /*   static final Comparator<Policy> PRIORITY_ORDER =
            new Comparator<Policy>() {
                public int compare(Policy p1, Policy p2) {
                    int dateCmp = new Integer(p2.getId()).compareTo(new Integer(p1.getId()));
                    if (dateCmp != 0)
                        return dateCmp;

                    return (p1.getId() < p2.getId() ? -1 :
                            (p1.getId() == p2.getId() ? 0 : 1));
                }
            };*/

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