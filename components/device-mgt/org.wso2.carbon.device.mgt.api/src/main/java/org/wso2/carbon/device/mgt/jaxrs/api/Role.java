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

package org.wso2.carbon.device.mgt.jaxrs.api;

import io.swagger.annotations.Api;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleWrapper;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Api(value = "Role")
public interface Role {

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    Response getRoles();

    @GET
    @Path("{userStore}")
    @Produces({MediaType.APPLICATION_JSON})
    Response getRoles(@PathParam("userStore") String userStore);

    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    Response getMatchingRoles(@QueryParam("filter") String filter);

    @GET
    @Path("permissions")
    @Produces({MediaType.APPLICATION_JSON})
    Response getPermissions(@QueryParam("rolename") String roleName);

    @GET
    @Path("role")
    @Produces({MediaType.APPLICATION_JSON})
    Response getRole(@QueryParam("rolename") String roleName);

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    Response addRole(RoleWrapper roleWrapper);

    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    Response updateRole(@QueryParam("rolename") String roleName, RoleWrapper roleWrapper);

    @DELETE
    @Produces({MediaType.APPLICATION_JSON})
    Response deleteRole(@QueryParam("rolename") String roleName);

    @PUT
    @Path("users")
    @Produces({MediaType.APPLICATION_JSON})
    Response updateUsers(@QueryParam("rolename") String roleName, List<String> userList);

    @GET
    @Path("count")
    Response getRoleCount();
}
