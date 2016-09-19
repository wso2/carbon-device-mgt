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

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;

import java.util.List;

@ApiModel(value = "RoleInfo", description = "Role details including permission and the users in the roles are " +
        "wrapped here.")
public class RoleInfo {

    @ApiModelProperty(name = "roleName", value = "The name of the role.", required = true)
    private String roleName;
    @ApiModelProperty(name = "permissions", value = "Lists out all the permissions associated with roles.",
            required = true, dataType = "List[java.lang.String]")
    private String[] permissions;
    @ApiModelProperty(name = "users", value = "The list of users assigned to the selected role.",
            required = true, dataType = "List[java.lang.String]")
    private String[] users;
    @ApiModelProperty(name = "permissionList", value = "This contain the following, " +
            "\n resourcePath\tThe path related to the API.\n " +
            "displayName\tThe name of the permission that is shown " +
            "in the UI.\n" +
            "nodeList\tLists out the nested permissions.",
            required = true)
    private UIPermissionNode permissionList;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public UIPermissionNode getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(UIPermissionNode permissionList) {
        this.permissionList = permissionList;
    }

}
