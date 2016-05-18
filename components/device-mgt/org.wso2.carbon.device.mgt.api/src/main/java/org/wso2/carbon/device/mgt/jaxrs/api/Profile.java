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
import org.wso2.carbon.apimgt.annotations.api.*;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * These end points provide profile related operations.
 */
@API(name = "Profile", version = "1.0.0", context = "/profiles", tags = {"devicemgt_admin"})

// Below Api is for swagger annotations
@Api(value = "Profile")
@Path("/profiles")
@SuppressWarnings("NonJaxWsWebServices")
public interface Profile {

    @POST
    @Permission(scope = "profile", permissions = {"/permission/admin/device-mgt/admin/policies/add"})
    Response addProfile(org.wso2.carbon.policy.mgt.common.Profile profile);

    @POST
    @Path("{id}")
    @Permission(scope = "profile", permissions = {"/permission/admin/device-mgt/admin/policies/update"})
    Response updateProfile(org.wso2.carbon.policy.mgt.common.Profile profile,
                           @PathParam("id") String profileId);

    @DELETE
    @Path("{id}")
    @Permission(scope = "profile", permissions = {"/permission/admin/device-mgt/admin/policies/remove"})
    Response deleteProfile(@PathParam("id") int profileId);
}
