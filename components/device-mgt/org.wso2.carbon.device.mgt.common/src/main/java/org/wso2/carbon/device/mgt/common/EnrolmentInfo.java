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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "EnrolmentInfo", description = "This class carries all information related to a devices enrollment" +
                                                 " status.")
public class EnrolmentInfo implements Serializable {

    private static final long serialVersionUID = 1998101712L;

    public enum Status {
        CREATED, ACTIVE, INACTIVE, UNREACHABLE, UNCLAIMED, SUSPENDED, BLOCKED, REMOVED, DISENROLLMENT_REQUESTED
    }

    public enum OwnerShip {
        BYOD, COPE
    }

    @ApiModelProperty(name = "id", value = "ID of the device in the WSO2 EMM device information database.",
                      required = true)
    private int id;
    @ApiModelProperty(name = "dateOfEnrolment", value = "Date of the device enrollment. This value is not necessary.", required = false )
    private Long dateOfEnrolment;
    @ApiModelProperty(name = "dateOfLastUpdate", value = "Date of the device's last update. This value is not necessary.", required = false )
    private Long dateOfLastUpdate;
    @ApiModelProperty(name = "ownership", value = "Defines the ownership details. The ownership type can be any of the" +
                                                  " following values.\n" +
                                                  "BYOD - Bring your own device (BYOD).\n" +
                                                  "COPE - Corporate owned personally enabled (COPE).", required = true )
    private OwnerShip ownership;
    @ApiModelProperty(name = "status", value = "Current status of the device, such as whether the device " +
                                               "is active, removed etc.", required = true )
    private Status status;
    @ApiModelProperty(name = "owner", value = "The device owner's name.", required = true )
    private String owner;

    public EnrolmentInfo() {
    }

    public EnrolmentInfo(String owner, OwnerShip ownership, Status status) {
        this.owner = owner;
        this.ownership = ownership;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EnrolmentInfo) {
            EnrolmentInfo tempInfo = (EnrolmentInfo) obj;
            if (this.owner != null && this.ownership != null) {
                if (this.owner.equalsIgnoreCase(tempInfo.getOwner()) && this.ownership.equals(tempInfo.getOwnership())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return owner.hashCode() ^ ownership.hashCode();
    }

}
