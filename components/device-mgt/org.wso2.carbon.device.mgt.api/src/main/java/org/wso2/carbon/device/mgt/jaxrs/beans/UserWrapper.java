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

@ApiModel(value = "UserWrapper", description = "User details and the roles of the user.")
public class UserWrapper {

    private String username;
    /*
        Base64 encoded password
     */

    @ApiModelProperty(name = "password", value = "Base64 encoded password.", required = true )
    private String password;
    @ApiModelProperty(name = "firstname", value = "The first name of the user.", required = true )
    private String firstname;
    @ApiModelProperty(name = "lastname", value = "The last name of the user.", required = true )
    private String lastname;
    @ApiModelProperty(name = "emailAddress", value = "The email address of the user.", required = true )
    private String emailAddress;
    @ApiModelProperty(name = "roles", value = "List of roles.", required = true )
    private String[] roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /*
        Giving a clone of the array since arrays are mutable
     */
    public String[] getRoles() {
        String[] copiedRoles = roles;
        if (roles != null){
            copiedRoles = roles.clone();
        }
        return copiedRoles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}