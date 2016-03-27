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

package org.wso2.carbon.device.mgt.core.group.mgt;

import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;

import java.util.List;

/**
 * This class is used to expose protected methods to the core. Use with internal access only.
 */
public class DeviceGroupBuilder extends DeviceGroup {

    /**
     * Set device group to be decorated with the builder
     *
     * @param deviceGroup to decorate
     */
    public DeviceGroupBuilder(DeviceGroup deviceGroup) {
        this.setDescription(deviceGroup.getDescription());
        this.setName(deviceGroup.getName());
        this.setDateOfCreation(deviceGroup.getDateOfCreation());
        this.setDateOfLastUpdate(deviceGroup.getDateOfLastUpdate());
        this.setOwner(deviceGroup.getOwner());
        this.setUsers(deviceGroup.getUsers());
        this.setRoles(deviceGroup.getRoles());
    }

    @Override
    public void setUsers(List<GroupUser> users) {
        super.setUsers(users);
    }

    @Override
    public void setRoles(List<String> roles) {
        super.setRoles(roles);
    }

    @Override
    public DeviceGroup getGroup() {
        return super.getGroup();
    }

}
