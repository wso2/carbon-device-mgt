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

package org.wso2.carbon.device.mgt.group.core.internal;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupUser;

import java.util.List;

public class DeviceGroupBroker extends DeviceGroup {

    public DeviceGroupBroker(DeviceGroup deviceGroup) {
        this.setId(deviceGroup.getId());
        this.setId(deviceGroup.getId());
        this.setDescription(deviceGroup.getDescription());
        this.setName(deviceGroup.getName());
        this.setDateOfCreation(deviceGroup.getDateOfCreation());
        this.setDateOfLastUpdate(deviceGroup.getDateOfLastUpdate());
        this.setOwner(deviceGroup.getOwner());
        this.setUsers(deviceGroup.getUsers());
        this.setDevices(deviceGroup.getDevices());
        this.setRoles(deviceGroup.getRoles());
    }

    @Override public void setId(int id) {
        super.setId(id);
    }

    @Override public void setUsers(List<GroupUser> users) {
        super.setUsers(users);
    }

    @Override public void setDevices(List<Device> devices) {
        super.setDevices(devices);
    }

    @Override public void setRoles(List<String> roles) {
        super.setRoles(roles);
    }

    @Override public DeviceGroup getGroup() {
        return super.getGroup();
    }
}
