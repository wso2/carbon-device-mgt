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
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.LifecycleManagementAPI;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/lifecycles")
public class LifecycleManagementAPIImpl implements LifecycleManagementAPI {

    private static Log log = LogFactory.getLog(LifecycleManagementAPIImpl.class);

    @GET
    public Response getLifecycleStates() {
        LifecycleStateManager lifecycleStateManager = APIUtil.getLifecycleStateManager();
        List<LifecycleState> lifecycleStates = new ArrayList<>();
        try {
            lifecycleStates = lifecycleStateManager.getLifecycleStates();
        } catch (LifecycleManagementException e) {
            String msg = "Error occurred while retrieving lifecycle states.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity(lifecycleStates).build();
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
        LifecycleStateManager lifecycleStateManager = APIUtil.getLifecycleStateManager();
        try {
            lifecycleStateManager.deleteLifecycleState(identifier);
        } catch (LifecycleManagementException e) {
            String msg = "Error occurred while deleting lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.status(Response.Status.OK).entity("Lifecycle state deleted successfully.").build();
    }
}
