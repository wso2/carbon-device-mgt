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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * DeviceNotification management REST-API implementation.
 * All end points support JSON, XMl with content negotiation.
 */
@Api(value = "DeviceNotification")
@SuppressWarnings("NonJaxWsWebServices")
@Path("/notifications")
@Produces({"application/json", "application/xml"})
@Consumes({ "application/json", "application/xml" })
public interface DeviceNotification {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting all Device Notification Details",
            notes = "Get the details of all notifications that were pushed to the device in WSO2 EMM using "
                    + "this REST API",
            response = Notification.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Notifications"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list")
            })
    Response getNotifications();

    @GET
    @Path("{status}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting the Device Notifications Filtered by the Status",
            notes = "Get the details of all the unread notifications or the details of all the read "
                    + "notifications using this REST API",
            response = Notification.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Notifications"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list")
            })
    Response getNotificationsByStatus(@ApiParam(name = "status", value = "Provide the notification status as"
                                            + " the value for {status}", required = true)
                                            @PathParam("status") Notification.Status status);

    @PUT
    @Path("{id}/{status}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "PUT",
            value = "Updating the Device Notification Status",
            notes = "When a user has read the the device notification the device notification status must "
                    + "change from NEW to CHECKED. Update the device notification status using this REST API",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Notification status updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status")
            })
    Response updateNotificationStatus( @ApiParam(name = "id", value = "Provide the ID of the notification"
                                            + " you wish you update", required = true) @PathParam("id") int id,
                                       @ApiParam(name = "status", value = "Provide the notification status as"
                                            + " the value", required = true) @PathParam("status")
                                            Notification.Status status);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Sending a Device Notification",
            notes = "Notify users on device operation failures and other information using this REST API",
            response = ResponsePayload.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "NNotification has added successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status")
            })
    Response addNotification(Notification notification);

}
