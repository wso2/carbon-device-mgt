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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface NotificationManagementService {

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
            @ApiResponse(code = 200, message = "List of Notifications", response = Notification.class,
                    responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list")
    })
    @Permission(scope = "device-notification-view", permissions = {
            "/permission/admin/device-mgt/admin/notifications/view",
            "/permission/admin/device-mgt/user/notifications/view"})
    Response getNotifications(@QueryParam("offset") int offset, @QueryParam("limit") int limit);

    @GET
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
            @ApiResponse(code = 200, message = "List of Notifications", response = Notification.class,
                    responseContainer = "List"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the notification list")
    })
    @Permission(scope = "device-notification-view", permissions = {
            "/permission/admin/device-mgt/admin/notifications/view",
            "/permission/admin/device-mgt/user/notifications/view"})
    Response getNotificationsByStatus(@QueryParam("status") Notification.Status status,
                                      @QueryParam("offset") int offset, @QueryParam("limit") int limit);

    @PUT
    @Path("/{id}/status")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "PUT",
            value = "Updating the Device Notification Status",
            notes = "When a user has read the the device notification the device notification status must "
                    + "change from NEW to CHECKED. Update the device notification status using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Notification status updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status")
    })
    @Permission(scope = "device-notification-modify",
            permissions = {"/permission/admin/device-mgt/admin/notifications/modify"})
    Response updateNotificationStatus(@PathParam("id") int id, Notification.Status status);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Sending a Device Notification",
            notes = "Notify users on device operation failures and other information using this REST API")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "NNotification has added successfully"),
            @ApiResponse(code = 500, message = "Error occurred while updating notification status")
    })
    @Permission(scope = "device-notification-modify",
            permissions = {"/permission/admin/device-mgt/admin/notifications/modify"})
    Response addNotification(Notification notification);

}
