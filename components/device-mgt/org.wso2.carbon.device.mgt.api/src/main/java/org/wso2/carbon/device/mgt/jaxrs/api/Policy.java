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
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PriorityUpdatedPolicyWrapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Api(value = "Policy")
public interface Policy {

    @POST
    @Path("inactive-policy")
    Response addPolicy(PolicyWrapper policyWrapper);

    @POST
    @Path("active-policy")
    Response addActivePolicy(PolicyWrapper policyWrapper);

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    Response getAllPolicies();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{id}")
    Response getPolicy(@PathParam("id") int policyId);

    @GET
    @Path("count")
    Response getPolicyCount();

    @PUT
    @Path("{id}")
    Response updatePolicy(PolicyWrapper policyWrapper, @PathParam("id") int policyId);

    @PUT
    @Path("priorities")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Response updatePolicyPriorities(List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies);

    @POST
    @Path("bulk-remove")
    @Consumes("application/json")
    @Produces("application/json")
    Response bulkRemovePolicy(List<Integer> policyIds);

    @PUT
    @Produces("application/json")
    @Path("activate")
    Response activatePolicy(List<Integer> policyIds);

    @PUT
    @Produces("application/json")
    @Path("inactivate")
    Response inactivatePolicy(List<Integer> policyIds) throws MDMAPIException;

    @PUT
    @Produces("application/json")
    @Path("apply-changes")
    Response applyChanges();

    @GET
    @Path("start-task/{milliseconds}")
    Response startTaskService(@PathParam("milliseconds") int monitoringFrequency);

    @GET
    @Path("update-task/{milliseconds}")
    Response updateTaskService(@PathParam("milliseconds") int monitoringFrequency);

    @GET
    @Path("stop-task")
    Response stopTaskService();

    @GET
    @Path("{type}/{id}")
    Response getComplianceDataOfDevice(@PathParam("type") String type, @PathParam("id") String id);

    @GET
    @Path("{type}/{id}/active-policy")
    Response getDeviceActivePolicy(@PathParam("type") String type, @PathParam("id") String id);
}
