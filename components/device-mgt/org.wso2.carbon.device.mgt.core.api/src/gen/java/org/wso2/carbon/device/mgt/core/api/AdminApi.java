/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.api;

import org.osgi.service.component.annotations.Component;

import org.wso2.carbon.device.mgt.core.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.api.factories.AdminApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


/**
 * This is the Admin micro services.
 */

@Component(
        name = "AdminApi",
        service = Microservice.class,
        immediate = true
)

@Path("/api/device-mgt/v1.[\\d]+/admin")
@Consumes({"application/json"})
@Produces({"application/json"})
@ApplicationPath("/admin")
@io.swagger.annotations.Api(description = "the admin API")
public class AdminApi implements Microservice {
    private final AdminApiService delegate = AdminApiServiceFactory.getAdminApi();


    @GET
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Getting the Supported Device Type with Meta Definition",
            notes = "Get the list of device types supported by WSO2 IoT.",
            response = DeviceType.class, responseContainer = "List",
            tags = {"Device Type Management Administrative Service"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "OK.   Successfully fetched the list of supported device types.",
                    response = DeviceType.class,
                    responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest" +
                            " version of the requested resource. ",
                    response = DeviceType.class,
                    responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(
                    code = 406,
                    message = "Not Acceptable. The requested media type is not supported",
                    response = DeviceType.class,
                    responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(
                    code = 500,
                    message = "Internal Server Error. " +
                            "Server error occurred while fetching the list of supported" +
                            " device types.",
                    response = DeviceType.class,
                    responseContainer = "List")})
    public Response adminDeviceTypesGet(@Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesGet(request);
    }

    @POST
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Add a device type to the server.",
            notes = "Add the details of a device type to the server which is provide meta data of" +
                    " the device.",
            response = void.class,
            tags = {"Device Type Management Administrative Service"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "OK.   Successfully added the device type.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the" +
                            " latest version of the requested resource.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 401,
                    message = "Unauthorized.  The unauthorized access to the requested resource. ",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 404,
                    message = "Not Found.  The specified device does not exist",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 406, message = "Not Acceptable.  The requested media type is not " +
                    "supported",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500,
                    message = "Internal Server Error. " +
                            "Server error occurred while fetching the device list.",
                    response = void.class)})
    public Response adminDeviceTypesPost(
            @ApiParam(
                    value = "The device type such as iOS, Android, Windows or fire-alarm.",
                    required = true) DeviceType type, @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesPost(type, request);
    }

    @PUT
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Update Device Type",
            notes = "Update the details of a device type in the server.",
            response = void.class,
            tags = {"Device Type Management Administrative Service"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "OK.   Successfully updated the device type.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has " +
                            "the latest version of the requested resource.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 401,
                    message = "Unauthorized.  The unauthorized access to the requested resource.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 404,
                    message = "Not Found.  The specified device does not exist",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 406,
                    message = "Not Acceptable.  The requested media type is not supported",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(
                    code = 500,
                    message = "Internal Server Error.   " +
                            "Server error occurred while fetching the device list.",
                    response = void.class)})
    public Response adminDeviceTypesPut(@ApiParam(
            value = "The device type such as ios, android, windows or fire-alarm.",
            required = true) DeviceType type, @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesPut(type, request);
    }
}
