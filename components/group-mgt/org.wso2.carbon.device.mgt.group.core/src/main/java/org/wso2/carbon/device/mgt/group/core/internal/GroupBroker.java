package org.wso2.carbon.device.mgt.group.core.internal;

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.group.common.Group;
import org.wso2.carbon.device.mgt.group.common.GroupUser;

import java.util.List;

public class GroupBroker extends Group {

    public GroupBroker(Group group) {
        this.setId(group.getId());
        this.setId(group.getId());
        this.setDescription(group.getDescription());
        this.setName(group.getName());
        this.setDateOfCreation(group.getDateOfCreation());
        this.setDateOfLastUpdate(group.getDateOfLastUpdate());
        this.setOwner(group.getOwner());
        this.setUsers(group.getUsers());
        this.setDevices(group.getDevices());
        this.setRoles(group.getRoles());
        this.setTenantId(group.getTenantId());
    }

    @Override
    public void setId(int id) {
        super.setId(id);
    }

    @Override
    public void setTenantId(int tenantId) {
        super.setTenantId(tenantId);
    }

    @Override
    public void setUsers(List<GroupUser> users) {
        super.setUsers(users);
    }

    @Override
    public void setDevices(List<Device> devices) {
        super.setDevices(devices);
    }

    @Override
    public void setRoles(List<String> roles) {
        super.setRoles(roles);
    }

    @Override
    public Group getGroup() {
        return super.getGroup();
    }
}
