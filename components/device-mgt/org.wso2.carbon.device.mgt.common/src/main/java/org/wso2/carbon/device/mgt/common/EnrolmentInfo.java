/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.common;

public class EnrolmentInfo {

    public enum Status {
        CREATED, ACTIVE, INACTIVE, UNREACHABLE, UNCLAIMED, SUSPENDED, BLOCKED, REMOVED
    }

    public enum OwnerShip {
        BYOD, COPE
    }

    private Device device;
    private Long dateOfEnrolment;
    private Long dateOfLastUpdate;
    private OwnerShip ownership;
    private Status status;
    private String owner;

    public EnrolmentInfo() {}

    public EnrolmentInfo(Device device, String owner, OwnerShip ownership, Status status) {
        this.device = device;
        this.owner = owner;
        this.ownership = ownership;
        this.status = status;
    }

    public Long getDateOfEnrolment() {
        return dateOfEnrolment;
    }

    public void setDateOfEnrolment(Long dateOfEnrolment) {
        this.dateOfEnrolment = dateOfEnrolment;
    }

    public Long getDateOfLastUpdate() {
        return dateOfLastUpdate;
    }

    public void setDateOfLastUpdate(Long dateOfLastUpdate) {
        this.dateOfLastUpdate = dateOfLastUpdate;
    }

    public OwnerShip getOwnership() {
        return ownership;
    }

    public void setOwnership(OwnerShip ownership) {
        this.ownership = ownership;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
