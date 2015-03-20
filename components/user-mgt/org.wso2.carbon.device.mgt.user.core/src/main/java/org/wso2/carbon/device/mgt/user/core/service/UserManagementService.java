/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
*/

package org.wso2.carbon.device.mgt.user.core.service;

import org.wso2.carbon.device.mgt.user.common.Role;
import org.wso2.carbon.device.mgt.user.common.User;
import org.wso2.carbon.device.mgt.user.common.UserManagementException;
import org.wso2.carbon.device.mgt.user.core.UserManager;
import org.wso2.carbon.device.mgt.user.core.internal.DeviceMgtUserDataHolder;

import java.util.List;

public class UserManagementService implements UserManager{

    @Override
    public List<User> getUsersForTenantAndRole(int tenantId, String roleName) throws UserManagementException {
        return DeviceMgtUserDataHolder.getInstance().getUserManager().getUsersForTenantAndRole(tenantId, roleName);
    }

    @Override
    public List<Role> getRolesForTenant(int tenantId) throws UserManagementException {
        return DeviceMgtUserDataHolder.getInstance().getUserManager().getRolesForTenant(tenantId);
    }

    @Override
    public List<User> getUsersForTenant(int tenantId) throws UserManagementException {
        return DeviceMgtUserDataHolder.getInstance().getUserManager().getUsersForTenant(tenantId);
    }

    @Override public User getUser(String username, int tenantId) throws UserManagementException {
        return DeviceMgtUserDataHolder.getInstance().getUserManager().getUser(username, tenantId);
    }

}
