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
package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "DeviceManagementAdmin", version = "1.0.0", context = "/devicemgt_admin/applications",
        tags = {"devicemgt_admin"})
@Path("/devices")
@Api(value = "DeviceManagementAdmin", description = "Device management admin related operations are exposed through " +
        "this API.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementAdminService {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get devices by the name.",
            notes = "Get devices the name of device and tenant.",
            response = org.wso2.carbon.device.mgt.common.Device.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched device details.",
                    response = org.wso2.carbon.device.mgt.common.Device.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No matching device found in the provided tenant."),
            @ApiResponse(code = 500, message = "Error while fetching device information.")
    })
    Response getDevicesByName(
            @ApiParam(name = "name", value = "Name of the device.",required = true)
            @QueryParam("name") String name,
            @ApiParam(name = "tenant-domain", value = "Name of the tenant.",required = true)
            @QueryParam("tenant-domain") String tenantDomain);

}
