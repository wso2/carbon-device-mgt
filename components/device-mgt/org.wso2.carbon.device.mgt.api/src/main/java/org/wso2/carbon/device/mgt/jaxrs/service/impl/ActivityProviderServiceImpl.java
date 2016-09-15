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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ActivityList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ActivityInfoProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Path("/activities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivityProviderServiceImpl implements ActivityInfoProviderService {

    private static final Log log = LogFactory.getLog(ActivityProviderServiceImpl.class);

    @GET
    @Override
    @Path("/{id}")
    public Response getActivity(@PathParam("id")
                                @Size(max = 45) String id,
                                @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Activity activity;
        DeviceManagementProviderService dmService;
        try {
            RequestValidationUtil.validateActivityId(id);

            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activity = dmService.getOperationByActivityId(id);
            if (activity == null) {
                return Response.status(404).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No activity can be " +
                                "found upon the provided activity id '" + id + "'").build()).build();
            }
            return Response.status(Response.Status.OK).entity(activity).build();
        } catch (OperationManagementException e) {
            String msg = "ErrorResponse occurred while fetching the activity for the supplied id.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    public Response getActivities(@QueryParam("since") String since, @QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit,
                                  @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        long ifModifiedSinceTimestamp;
        long sinceTimestamp;
        long timestamp = 0;
        boolean isIfModifiedSinceSet = false;
        boolean isSinceSet = false;
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifSinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                ifSinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                return Response.status(400).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Invalid date string is provided in 'If-Modified-Since' header").build()).build();
            }
            ifModifiedSinceTimestamp = ifSinceDate.getTime();
            isIfModifiedSinceSet = true;
            timestamp = ifModifiedSinceTimestamp / 1000;
        } else if (since != null && !since.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                sinceDate = format.parse(since);
            } catch (ParseException e) {
                return Response.status(400).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Invalid date string is provided in 'since' filter").build()).build();
            }
            sinceTimestamp = sinceDate.getTime();
            isSinceSet = true;
            timestamp = sinceTimestamp / 1000;
        }

        List<Activity> activities;
        ActivityList activityList = new ActivityList();
        DeviceManagementProviderService dmService;
        try {
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activities = dmService.getActivitiesUpdatedAfter(timestamp, limit, offset);
            activityList.setList(activities);
            int count = dmService.getActivityCountUpdatedAfter(timestamp);
            activityList.setCount(count);
            if (activities == null || activities.size() == 0) {
                if (isIfModifiedSinceSet) {
                    return Response.notModified().build();
                }
            }
            return Response.ok().entity(activityList).build();
        } catch (OperationManagementException e) {
            String msg
                    = "ErrorResponse occurred while fetching the activities updated after given time stamp.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

}
