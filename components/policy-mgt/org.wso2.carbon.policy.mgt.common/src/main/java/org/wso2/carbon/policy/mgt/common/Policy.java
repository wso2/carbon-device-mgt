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

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * This class will be the used to create policy object with relevant information for evaluating.
 */
public class Policy {
    private int id;                         // Identifier of the policy.
    private int priorityId;                 // Priority of the policies. This will be used only for simple evaluation.
    private String policyName;              // Name of the policy.
    private List<Feature> featuresList;     // Features included in the policies.
    private boolean generic;                // If true, this should be applied to all related device.
    private List<String> roleList;          // Roles which this policy should be applied.
    private String ownershipType;           // Ownership type (COPE, BYOD, CPE)
    private List<String> DeviceList;        // Individual devices this policy should be applied
    private String deviceType;              // Device type to apply the policy.

    /*Dynamic policy attributes*/

    /*These are related to time based policies*/

    private int startTime;                  // Start time to apply the policy.
    private int endTime;                    // After this time policy will not be applied
    private Date startDate;                 // Start date to apply the policy
    private Date endDate;                   // After this date policy will not be applied.


    /*These are related to location based policies*/

    private String altitude;                // Altitude
    private String longitude;               // Longitude

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

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public List<Feature> getFeaturesList() {
        return featuresList;
    }

    public void setFeaturesList(List<Feature> featuresList) {
        this.featuresList = featuresList;
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

    public List<String> getDeviceList() {
        return DeviceList;
    }

    public void setDeviceList(List<String> deviceList) {
        DeviceList = deviceList;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
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

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
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
}