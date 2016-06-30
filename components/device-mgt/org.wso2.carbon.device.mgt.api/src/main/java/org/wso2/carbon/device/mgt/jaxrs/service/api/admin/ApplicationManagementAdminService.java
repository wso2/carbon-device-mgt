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
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "Application", version = "1.0.0", context = "/devicemgt_admin/applications", tags = {"devicemgt_admin"})

@Path("/admin/applications")
@Api(value = "Application Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ApplicationManagementAdminService {

    @POST
    @Path("/install-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Application installation API.(Internal API)",
            notes = "This is an internal API used for application installation on a device.",
            response = Activity.class,
            tags = "Application Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 202,
                    message = "OK. \n Install application operation will be delivered to the given devices",
                    response = Activity.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be processed does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while bulk issuing application installation operations upon " +
                            "a given set of devices.",
                    response = ErrorResponse.class)
    })
    Response installApplication(
            @ApiParam(
                    name = "applicationWrapper",
                    value = "Application details of the application to be installed.",
                    required = true) ApplicationWrapper applicationWrapper);

    @POST
    @Path("/uninstall-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Application un-installation API.(Internal API)",
            notes = "This is an internal API used for application un-installation on a device.",
            response = Activity.class,
            tags = "Application Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 202,
                    message = "OK. \n Uninstall application operation will be delivered to the provided devices",
                    response = Activity.class),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be processed does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while bulk issuing application un-installation operations upon " +
                            "a given set of devices.",
                    response = ErrorResponse.class)
    })
    Response uninstallApplication(
            @ApiParam(
                    name = "applicationWrapper",
                    value = "Application details of the application to be uninstalled.",
                    required = true) ApplicationWrapper applicationWrapper);

}
