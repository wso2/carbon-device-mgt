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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.api.context.DeviceOperationContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 */
@API(name = "Operation", version = "1.0.0", context = "/operations", tags = {"devicemgt_admin"})

// Below Api is for swagger annotations
@Path("/operations")
@Api(value = "Operation", description = "Operation management related operations can be found here.")
public interface Operation {

    /* @deprecated */
    @GET
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getAllOperations();

    @GET
    @Path("paginate/{type}/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Paginated Details for Operations on a Device.",
            notes = "You will carry out many operations on a device. In a situation where you wish to view the all" +
                    " the operations carried out on a device it is not feasible to show all the details on one page" +
                    " therefore the details are paginated." +
                    " Example: You carry out 21 operations via a given device. When you wish to see the operations " +
                    "carried out, the details of the 21 operations will be broken down into 3 pages with 10 operation" +
                    " details per page.",
            response = org.wso2.carbon.device.mgt.common.operation.mgt.Operation.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of Operations on a device."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the operations for the " +
                    "device.")})
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getDeviceOperations(@ApiParam(name = "type", value = "Define the device type as the value for {type}. " +
            "Example: ios, android or windows.",
            required = true) @PathParam("type") String type,
                                 @ApiParam(name = "id", value = "Define the device ID",
                                         required = true) @PathParam("id") String id,
                                 @ApiParam(name = "start", value = "Provide the starting pagination index. Example 10",
                                         required = true) @QueryParam("start") int startIdx,
                                 @ApiParam(name = "length", value = "Provide how many device details you require from" +
                                         " the starting pagination index. For example if " +
                                         "you require the device details from the 10th " +
                                         "pagination index to the 15th, " +
                                         "you must define 10 as the value for start and 5 " +
                                         "as the value for length.",
                                         required = true) @QueryParam("length") int length,
                                 @QueryParam("search") String search);

    @GET
    @Path("{type}/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Device Operation Details.",
            responseContainer = "List",
            notes = "Get the details of operations carried out on a selected device.",
            response = org.wso2.carbon.device.mgt.common.operation.mgt.Operation.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of Operations on a device."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the operations for the " +
                    "device.")})
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getAllDeviceOperations(@ApiParam(name = "type", value = "Define the device type as the value for {type}. " +
            "Example: ios, android or windows.",
            required = true) @PathParam("type") String type,
                                    @ApiParam(name = "id", value = "Define the device ID",
                                            required = true) @PathParam("id") String id);

    /* @deprecated */
    @POST
    @Permission(scope = "operation-modify", permissions = {
            "/permission/admin/device-mgt/admin/devices/add"})
    Response addOperation(DeviceOperationContext operationContext);

    @GET
    @Path("{type}/{id}/apps")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Installed Application Details of a Device.",
            responseContainer = "List",
            notes = "Get the list of applications that a device has subscribed.",
            response = Application.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "List of installed application details of a device.", response = Application.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the apps of the device" +
                    ".")})
    @Permission(scope = "operation-view", permissions = {
            "/permission/admin/device-mgt/admin/devices/view",
            "/permission/admin/device-mgt/user/devices/view"})
    Response getInstalledApps(@ApiParam(name = "type", value = "Define the device type as the value for {type}. " +
            "Example: ios, android or windows.",
            required = true) @PathParam("type") String type,
                              @ApiParam(name = "id", value = "Define the device ID",
                                      required = true) @PathParam("id") String id);

    @POST
    @Path("installApp/{tenantDomain}")
    @Permission(scope = "operation-install",
            permissions = {"/permission/admin/device-mgt/admin/operations/applications/install-applications"})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Installing an Application on a Device.",
            notes = "Install a selected application on a device.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Operation was successfully added to the queue."),
            @ApiResponse(code = 500, message = "Error occurred while saving the operation.")})
    Response installApplication(@ApiParam(name = "applicationWrapper", value = "Details about the application and the" +
            " users and roles it should be " +
            "installed on.",
            required = true) ApplicationWrapper applicationWrapper,
                                @ApiParam(name = "tenantDomain", value = "Provide the tenant domain as the value for " +
                                        "{tenantDomain}. The default tenant domain " +
                                        "of WSO2 EMM is carbon.super.",
                                        required = true) @PathParam("tenantDomain") String tenantDomain);

    @POST
    @Path("uninstallApp/{tenantDomain}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Uninstalling an Application from a Device.",
            notes = "Uninstall a selected application from a device.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Operation was successfully added to the queue."),
            @ApiResponse(code = 500, message = "Error occurred while saving the operation.")})
    @Permission(scope = "operation-uninstall",
            permissions = {"/permission/admin/device-mgt/admin/operations/applications/uninstall-applications"})
    Response uninstallApplication(@ApiParam(name = "applicationWrapper", value = "Details about the application and" +
            " the users and roles it should be " +
            "uninstalled.",
            required = true) ApplicationWrapper applicationWrapper,
                                  @ApiParam(name = "tenantDomain", value = "Provide the tenant domain as the value for " +
                                          "{tenantDomain}. The default tenant domain " +
                                          "of WSO2 EMM is carbon.super.",
                                          required = true) @PathParam("tenantDomain") String tenantDomain);


    @GET
    @Path("activity/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Retrieving the operation details.",
            notes = "This will return the operation details including the responses from the devices")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Activity details provided successfully.."),
            @ApiResponse(code = 500, message = "Error occurred while fetching the activity for the supplied id.")})
    @Permission(scope = "operation-view", permissions = {"/permission/admin/device-mgt/admin/devices/view"})
    Response getActivity(@ApiParam(name = "id", value = "Provide activity id {id} as ACTIVITY_(number)",
            required = true) @PathParam("id") String id)
            throws MDMAPIException;
}
