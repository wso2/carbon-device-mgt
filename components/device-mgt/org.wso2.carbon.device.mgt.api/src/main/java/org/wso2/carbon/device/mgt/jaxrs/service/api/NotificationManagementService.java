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
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Notifications related REST-API.
 */
@API(name = "Device Notification", version = "1.0.0", context = "/devicemgt_admin/notifications",
        tags = {"devicemgt_admin"})
@Api(value = "DeviceNotification", description = "Device notification related operations can be found here.")
@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface NotificationManagementService {

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting all device notification details.",
            notes = "Get the details of all notifications that were pushed to the device in WSO2 EMM using "
                    + "this REST API",
            response = Notification.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of notifications",
                    response = Notification.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No notification is available to be retrieved."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list.")
    })
    @Permission(scope = "device-notification-view", permissions = {
            "/permission/admin/device-mgt/admin/notifications/view",
            "/permission/admin/device-mgt/user/notifications/view"
    })
    Response getNotifications(
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How notification device details are required from the starting " +
                    "pagination index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the device notifications filtered by the status.",
            notes = "Get the details of all the unread notifications or the details of all the read "
                    + "notifications using this REST API.",
            response = Notification.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully fetched the list of notifications.",
                    response = Notification.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No notification, which carries the given status " +
                    "is available to be retrieved."),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list.")
    })
    @Permission(scope = "device-notification-view", permissions = {
            "/permission/admin/device-mgt/admin/notifications/view",
            "/permission/admin/device-mgt/user/notifications/view"
    })
    Response getNotificationsByStatus(
            @ApiParam(name = "status", value = "Status of the notification.",required = true)
                    Notification.Status status,
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many notification details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @PUT
    @Path("/{id}/status")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the device notification status",
            notes = "When a user has read the the device notifications, the device notification status must "
                    + "change from NEW to CHECKED. Update the device notification status using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Notification status updated successfully."),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status.")
    })
    @Permission(scope = "device-notification-modify",
            permissions = {"/permission/admin/device-mgt/admin/notifications/modify"})
    Response updateNotificationStatus(
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") int id,
            @ApiParam(name = "status", value = "Status of the notification.",required = true)
                    Notification.Status status);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending a device notification.",
            notes = "Notify users on device operation failures and other information using this REST API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Notification has added successfully."),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status.")
    })
    @Permission(scope = "device-notification-modify",
            permissions = {"/permission/admin/device-mgt/admin/notifications/modify"})
    Response addNotification(@ApiParam(name = "notification", value = "Notification details to be added.",required =
            true) Notification notification);

}
