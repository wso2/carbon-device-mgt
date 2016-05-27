/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.api;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Activity related REST-API implementation.
 */
@API(name = "Activities", version = "1.0.0", context = "/devicemgt_admin/activities", tags = {"devicemgt_admin"})
@Path("/activities")
@Api(value = "ActivityInfo", description = "Activity related information manipulation. For example operation details " +
        "and responses from devices.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ActivityInfoProviderService {

    @GET
    @Path("/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Retrieving the operation details.",
            notes = "This will return the operation details including the responses from the devices.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Activity details provided successfully."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the activity for the supplied id.")
    })
    @Permission(scope = "operation-view", permissions = {"/permission/admin/device-mgt/admin/devices/view"})
    Response getActivity(
            @ApiParam(name = "id", value = "Activity id of the operation/activity to be retrieved.",
                    required = true)
            @PathParam("id") String id);

}
