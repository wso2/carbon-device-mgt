/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.publisher.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.publisher.api.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.LifecycleManagementAPI;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Lifecycle Management related jax-rs APIs.
 */
@Path("/lifecycles")
public class LifecycleManagementAPIImpl implements LifecycleManagementAPI {

    private static Log log = LogFactory.getLog(LifecycleManagementAPIImpl.class);

    @GET
    public Response getLifecycleStates() {
        return null;
//        LifecycleStateManager lifecycleStateManager = APIUtil.getLifecycleStateManager();
//        List<LifecycleState> lifecycleStates = new ArrayList<>();
//        try {
//            lifecycleStates = lifecycleStateManager.getLifecycleStates();
//        } catch (LifecycleManagementException e) {
//            String msg = "Error occurred while retrieving lifecycle states.";
//            log.error(msg, e);
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//        return Response.status(Response.Status.OK).entity(lifecycleStates).build();
    }

    @POST
    public Response addLifecycleState(LifecycleState state) {
        LifecycleStateManager lifecycleStateManager = APIUtil.getLifecycleStateManager();
        try {
            lifecycleStateManager.addLifecycleState(state);
        } catch (LifecycleManagementException e) {
            String msg = "Error occurred while adding lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity("Lifecycle state added successfully.").build();
    }

    @DELETE
    @Path("/{identifier}")
    public Response deleteLifecycleState(@PathParam("identifier") String identifier) {
        return null;
//        LifecycleStateManager lifecycleStateManager = APIUtil.getLifecycleStateManager();
//        try {
//            lifecycleStateManager.deleteLifecycleState(identifier);
//        } catch (LifecycleManagementException e) {
//            String msg = "Error occurred while deleting lifecycle state.";
//            log.error(msg, e);
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        }
//        return Response.status(Response.Status.OK).entity("Lifecycle state deleted successfully.").build();
    }

//    @PUT
//    @Consumes("application/json")
//    @Path("/{uuid}/lifecycle")
//    public Response changeLifecycleState(@PathParam("uuid") String applicationUUID, @QueryParam("state") String state) {
//        ApplicationManager applicationManager = APIUtil.getApplicationManager();
//
//        if (!Arrays.asList(Constants.LIFE_CYCLES).contains(state)) {
//            log.warn("Provided lifecycle state " + state + " is not valid. Please select one from"
//                    + Arrays.toString(Constants.LIFE_CYCLES));
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("Provided lifecycle state " + state + " is not valid. Please select one from "
//                            + Arrays.toString(Constants.LIFE_CYCLES)).build();
//        }
//        try {
//            applicationManager.changeLifecycle(applicationUUID, state);
//            return Response.status(Response.Status.OK)
//                    .entity("Successfully changed the lifecycle state of the application: " + applicationUUID).build();
//        } catch (org.wso2.carbon.device.application.mgt.core.exception.NotFoundException e) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (ApplicationManagementException e) {
//            String msg = "Error occurred while changing the lifecycle of application: " + applicationUUID;
//            log.error(msg, e);
//            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
//        }
//    }
//
//    @GET
//    @Path("/{uuid}/lifecycle")
//    @Override
//    public Response getNextLifeCycleStates(@PathParam("uuid") String applicationUUID) {
//        ApplicationManager applicationManager = APIUtil.getApplicationManager();
//        try {
//            if (applicationManager.getApplication(applicationUUID) == null) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Application with the UUID '" + applicationUUID + "' is not found.");
//                }
//                return Response.status(Response.Status.NOT_FOUND).entity("Application with the UUID '" +
//                        applicationUUID + "'  is not found.").build();
//            }
//
//            if (log.isDebugEnabled()) {
//                log.debug("Application with UUID '" + applicationUUID + "' is found. Request received for getting "
//                        + "next life-cycle states for the particular application.");
//            }
//            return Response.status(Response.Status.OK).entity(applicationManager.getLifeCycleStates(applicationUUID))
//                    .build();
//        } catch (NotFoundException e) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        } catch (ApplicationManagementException e) {
//            log.error("Application Management Exception while trying to get next states for the applications with "
//                    + "the application ID", e);
//            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
//        }
//    }

    //    ToDo

    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/lifecycle")
    public Response changeLifecycleState(@PathParam("uuid") String applicationUUID, @QueryParam("state") String state) {
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
            return Response.status(Response.Status.OK)
                    .entity("Successfully changed the lifecycle state of the application: " + applicationUUID).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while changing the lifecycle of application: " + applicationUUID;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("/{uuid}/lifecycle")
    @Override
    public Response getNextLifeCycleStates(@PathParam("uuid") String applicationUUID) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
//            if (applicationManager.getApplication(applicationUUID) == null) {
//                if (log.isDebugEnabled()) {
//                    log.debug("Application with the UUID '" + applicationUUID + "' is not found.");
//                }
//                return Response.status(Response.Status.NOT_FOUND).entity("Application with the UUID '" +
//                        applicationUUID + "'  is not found.").build();
//            }

            if (log.isDebugEnabled()) {
                log.debug("Application with UUID '" + applicationUUID + "' is found. Request received for getting "
                        + "next life-cycle states for the particular application.");
            }
            return Response.status(Response.Status.OK).entity(applicationManager.getLifeCycleStates(applicationUUID))
                    .build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Application Management Exception while trying to get next states for the applications with "
                    + "the application ID", e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
