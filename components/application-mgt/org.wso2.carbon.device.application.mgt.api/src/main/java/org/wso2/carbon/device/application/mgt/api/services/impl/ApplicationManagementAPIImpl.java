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
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.util.Arrays;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


@Produces({"application/json"})
@Consumes({"application/json"})
@Path("/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final int DEFAULT_LIMIT = 20;
    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Consumes("application/json")
    public Response getApplications(@QueryParam("offset") int offset, @QueryParam("limit") int limit,
                                    @QueryParam("query") String searchQuery) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (limit == 0) {
                limit = DEFAULT_LIMIT;
            }
            Filter filter = new Filter();
            filter.setOffset(offset);
            filter.setLimit(limit);
            filter.setSearchQuery(searchQuery);

            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response getApplication(@PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplication(uuid);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Application with UUID " + uuid + " not found").build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while getting application with the uuid " + uuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    public Response changeLifecycleState(@PathParam("uuid") String applicationUUID,
            @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        if (!Arrays.asList(Constants.LIFE_CYCLES).contains(state)) {
            log.warn("Provided lifecycle state " + state + " is not valid. Please select one from"
                    + Arrays.toString(Constants.LIFE_CYCLES));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Provided lifecycle state " + state + " is not valid. Please select one from "
                            + Arrays.toString(Constants.LIFE_CYCLES)).build();
        }
        try {
            applicationManager.changeLifecycle(applicationUUID, state);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while changing the lifecycle of application: " + applicationUUID;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK)
                .entity("Successfully changed the lifecycle state of the application: " + applicationUUID).build();
    }

    @GET
    @Path("/{uuid}/lifecycle")
    @Override
    public Response getNextLifeCycleStates(@PathParam("uuid") String applicationUUID) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (applicationManager.getApplication(applicationUUID) == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Application with the UUID '" + applicationUUID + "' is not found.");
                }
                return Response.status(Response.Status.NOT_FOUND).entity("Application with the UUID '" +
                        applicationUUID + "'  is not found.").build();
            }

            if (log.isDebugEnabled()) {
                log.debug("Application with UUID '" + applicationUUID + "' is found. Request received for getting "
                        + "next life-cycle states for the particular application.");
            }
            return Response.status(Response.Status.OK).entity(applicationManager.getLifeCycleStates(applicationUUID))
                    .build();
        } catch (ApplicationManagementException e) {
            log.error("Application Management Exception while trying to get next states for the applications with "
                    + "the application ID", e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes("application/json")
    public Response createApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.createApplication(application);
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    @PUT
    @Consumes("application/json")
    public Response editApplication(@Valid Application application) {

        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.editApplication(application);

        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }

    @DELETE
    @Path("/{appuuid}")
    public Response deleteApplication(@PathParam("appuuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteApplication(uuid);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        String responseMsg = "Successfully deleted the application: " + uuid;
        return Response.status(Response.Status.OK).entity(responseMsg).build();
    }
}
