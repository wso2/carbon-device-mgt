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
package org.wso2.carbon.device.mgt.common.group.mgt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Holds Device Group details and expose to external access
 */
@XmlRootElement
public class DeviceGroup implements Serializable {

    private String description;
    private String name;
    private Long dateOfCreation;
    private Long dateOfLastUpdate;
    private String owner;
    private List<GroupUser> users;
    private List<String> roles;

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public Long getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(Long dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

    @XmlElement
    public Long getDateOfLastUpdate() {
        return dateOfLastUpdate;
    }

    public void setDateOfLastUpdate(Long dateOfLastUpdate) {
        this.dateOfLastUpdate = dateOfLastUpdate;
    }

    @XmlElement
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @XmlElement
    public List<GroupUser> getUsers() {
        return users;
    }

    protected void setUsers(List<GroupUser> users) {
        this.users = users;
    }

    @XmlElement
    public List<String> getRoles() {
        return roles;
    }

    protected void setRoles(List<String> roles) {
        this.roles = roles;
    }

    protected DeviceGroup getGroup() {
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setDescription(getDescription());
        deviceGroup.setName(getName());
        deviceGroup.setDateOfCreation(getDateOfCreation());
        deviceGroup.setDateOfLastUpdate(getDateOfLastUpdate());
        deviceGroup.setOwner(getOwner());
        deviceGroup.setUsers(getUsers());
        deviceGroup.setRoles(getRoles());
        return deviceGroup;
    }

}
