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
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ActivityInfoProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.*;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.NotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

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
    public Response getActivity(
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Activity activity;
        DeviceManagementProviderService dmService;
        try {
            RequestValidationUtil.validateActivityId(id);

            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activity = dmService.getOperationByActivityId(id);
            if (activity == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No activity can be " +
                                "found upon the provided activity id '" + id + "'").build());
            }
        } catch (OperationManagementException e) {
            String msg = "ErrorResponse occurred while fetching the activity for the supplied id.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(activity).build();
    }

    @GET
    @Override
    public Response getActivities(
            @QueryParam("since") String since,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        long ifModifiedSinceTimestamp = 0;
        long sinceTimestamp =0;
        boolean isIfModifiedSinceSet = false;
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                sinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                        .setMessage("Invalid date string is provided in 'If-Modified-Since' header").build());
            }
            ifModifiedSinceTimestamp = sinceDate.getTime();
        }
        if (since != null && !since.isEmpty()){
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try{
                sinceDate = format.parse(since);
            }catch (ParseException e){
                throw new InputValidationException(new ErrorResponse.ErrorResponseBuilder().setCode(400l)
                        .setMessage("Invalid date string is provided in 'since' filter").build());
            }
            sinceTimestamp = sinceDate.getTime();
        }
        if (ifModifiedSinceTimestamp >= sinceTimestamp) {
            sinceTimestamp = ifModifiedSinceTimestamp;
            isIfModifiedSinceSet = true;
        }
        List<Activity> activities;
        DeviceManagementProviderService dmService;
        try {
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            activities = dmService.getActivitiesUpdatedAfter(sinceTimestamp);
            if (activities == null || activities.size() == 0) {
                if (isIfModifiedSinceSet) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No activities " +
                            "after the time provided in 'If-Modified-Since' header").build();
                }
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No activities " +
                                "found.").build());
            }
        } catch (OperationManagementException e) {
            String msg = "ErrorResponse occurred while fetching the activities updated after given time stamp.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(activities).build();
    }
}
