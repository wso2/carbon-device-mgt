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

package org.wso2.carbon.mdm.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.mdm.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.mdm.api.util.ResponsePayload;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * DeviceNotification management REST-API implementation.
 * All end points support JSON, XMl with content negotiation.
 */
@SuppressWarnings("NonJaxWsWebServices")
@Produces({"application/json", "application/xml"})
@Consumes({ "application/json", "application/xml" })
public class DeviceNotification {

	private static Log log = LogFactory.getLog(Configuration.class);

	@GET
    public Response getNotifications() {
        String msg;
        try {
            List<Notification> notifications = DeviceMgtAPIUtils.getNotificationManagementService().getAllNotifications();
            return Response.status(Response.Status.OK).entity(notifications).build();
        } catch (NotificationManagementException e) {
            msg = "Error occurred while retrieving the notification list.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@GET
	@Path("{status}")
    public Response getNotificationsByStatus(@PathParam("status") Notification.Status status) {
        String msg;
        try {
            List<Notification> notifications = DeviceMgtAPIUtils.getNotificationManagementService().getNotificationsByStatus(status);
            return Response.status(Response.Status.OK).entity(notifications).build();
        } catch (NotificationManagementException e) {
            msg = "Error occurred while retrieving the notification list.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@PUT
	@Path("{id}/{status}")
    public Response updateNotificationStatus(@PathParam("id") int id,
                                             @PathParam("status") Notification.Status status) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getNotificationManagementService().updateNotificationStatus(id, status);
            responseMsg.setMessageFromServer("Notification status updated successfully.");
			responseMsg.setStatusCode(HttpStatus.SC_ACCEPTED);
            return Response.status(Response.Status.ACCEPTED).entity(responseMsg).build();
        } catch (NotificationManagementException e) {
            String msg = "Error occurred while updating notification status.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	@POST
    public Response addNotification(Notification notification) {
        ResponsePayload responseMsg = new ResponsePayload();
        try {
            DeviceMgtAPIUtils.getNotificationManagementService().addNotification(notification);
            responseMsg.setMessageFromServer("Notification has added successfully.");
			responseMsg.setStatusCode(HttpStatus.SC_CREATED);
            return Response.status(Response.Status.CREATED).entity(responseMsg).build();
        } catch (NotificationManagementException e) {
            String msg = "Error occurred while updating notification status.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
