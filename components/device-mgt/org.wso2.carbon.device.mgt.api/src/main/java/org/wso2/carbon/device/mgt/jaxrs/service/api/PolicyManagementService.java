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
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Policy related REST-API. This can be used to manipulated policies and associate them with devices, users, roles,
 * groups.
 */
@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PolicyManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a policy.",
            notes = "Add a policy using this REST API command. Using the REST API command given below " +
                    "you are able to save a created Policy and this policy will be in the inactive state.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created the policy."),
            @ApiResponse(code = 401, message = "Current user is not authorized to add policies."),
            @ApiResponse(code = 500, message = "Policy Management related error occurred when adding the policy.")})
    @Permission(scope = "policy-modify", permissions = {"/permission/admin/device-mgt/admin/policies/add"})
    Response addPolicy(@ApiParam(name = "policy", value = "Policy details related to the operation.",
            required = true) PolicyWrapper policy);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting details of policies.",
            responseContainer = "List",
            notes = "Retrieve the details of all the policies that you have created in EMM.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Fetched all policies.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "No policies found."),
            @ApiResponse(code = 500, message = "Policy Management related error occurred when " +
                    "fetching the policies.")
    })
    @Permission(scope = "policy-view", permissions = {"/permission/admin/device-mgt/admin/policies/list"})
    Response getPolicies(
            @ApiParam(name = "offset", value = "Starting pagination index.",required = true)
            @QueryParam("offset") int offset,
            @ApiParam(name = "limit", value = "How many policy details are required from the starting pagination " +
                    "index.", required = true)
            @QueryParam("limit") int limit);

    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting details of a policy.",
            notes = "Retrieve the details of a given policy in EMM.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Fetched policy details.",
                    response = org.wso2.carbon.policy.mgt.common.Policy.class),
            @ApiResponse(code = 404, message = "Policy not found."),
            @ApiResponse(code = 500, message = "Policy management related error occurred when " +
                    "fetching the policy.")
    })
    @Permission(scope = "policy-view", permissions = {"/permission/admin/device-mgt/admin/policies/list"})
    Response getPolicy(
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") int id);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a policy.",
            notes = "If you wish to make changes to an existing policy, you can do so by updating the policy using " +
                    "this API.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Policy has been updated successfully."),
            @ApiResponse(code = 500, message = "Policy management related exception in policy update.")
    })
    @Permission(scope = "policy-modify", permissions = {"/permission/admin/device-mgt/admin/policies/update"})
    Response updatePolicy(
            @ApiParam(name = "id", value = "The device identifier of the device.", required = true)
            @PathParam("id") int id,
            @ApiParam(name = "policy", value = "Policy details related to the operation.",
            required = true) PolicyWrapper policy);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Removing multiple policies.",
            notes = "In situations where you need to delete more than one policy you can do so using this API.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Policies have been successfully deleted."),
            @ApiResponse(code = 404, message = "Policy does not exist."),
            @ApiResponse(code = 500, message = "Error in deleting policies.")
    })
    @Permission(scope = "policy-modify", permissions = {"/permission/admin/device-mgt/admin/policies/remove"})
    Response removePolicies(@ApiParam(name = "policyIds", value = "Policy ID list to be deleted.",
            required = true) List<Integer> policyIds);

    @POST
    @Path("/activate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Activating policies.",
            notes = "Using the REST API command you are able to publish a policy in order to bring a policy that is " +
                    "in the inactive state to the active state.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Policies have been successfully activated."),
            @ApiResponse(code = 500, message = "Error in activating policies.")
    })
    @Permission(scope = "policy-modify", permissions = {
            "/permission/admin/device-mgt/admin/policies/update",
            "/permission/admin/device-mgt/admin/policies/add"})
    Response activatePolicies(
            @ApiParam(name = "policyIds", value = "Policy ID list to be activated.",
            required = true) List<Integer> policyIds);

    @POST
    @Path("/deactivate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Deactivating policies.",
            notes = "Using the REST API command you are able to unpublish a policy in order to bring a policy that " +
                    "is in the active state to the inactive state.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Policies have been successfully deactivated."),
            @ApiResponse(code = 500, message = "Error in deactivating policies.")
    })
    @Permission(scope = "policy-modify", permissions = {
            "/permission/admin/device-mgt/admin/policies/update",
            "/permission/admin/device-mgt/admin/policies/add"})
    Response deactivatePolicies(
            @ApiParam(name = "policyIds", value = "Policy ID list to be deactivated.",
            required = true) List<Integer> policyIds);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Enforced Details of a Device.",
            notes = "When a device registers with WSO2 EMM a policy is enforced on the device. Initially the EMM " +
                    "filters the policies based on the Platform (device type), filters based on the device ownership" +
                    " type , filters based on the user role or name and finally the policy is enforced on the device.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Fetched current policy."),
            @ApiResponse(code = 404, message = "No policy found."),
            @ApiResponse(code = 500, message = "Error occurred while getting the current policy.")
    })
    Response getEffectivePolicyOfDevice(
            @ApiParam(name = "device-type", value = "The device type, such as ios, android or windows.", required = true)
            @QueryParam("device-type") String type,
            @ApiParam(name = "device-id", value = "The device identifier of the device.", required = true)
            @QueryParam("device-id") String deviceId);

}
