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

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.api.util.ResponsePayload;
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
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Policy.",
            notes = "Add a policy using this REST API command. When adding a policy you will have the option of " +
                    "saving the policy or saving and publishing the policy. Using the REST API command given below " +
                    "you are able to save a created Policy and this policy will be in the inactive state")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created the policy."),
                            @ApiResponse(code = 500, message = "Policy Management related error occurred when " +
                                                               "adding the policy") })
    Response addPolicy(@ApiParam(name = "policyWrapper", value = "Policy details related to the operation.",
                                  required = true) PolicyWrapper policyWrapper);

    @POST
    @Path("active-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding an Active Policy.",
            notes = "Add a policy that is in the active state using the REST API command. When adding a policy you " +
                    "will have the option of saving the policy or saving and publishing the policy. Using the REST " +
                    "API command given below you are able to save and publish a created policy and this policy will " +
                    "be in the active state.")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created the policy."),
                            @ApiResponse(code = 500, message = "Policy Management related error occurred when " +
                                                               "adding the policy") })
    Response addActivePolicy(@ApiParam(name = "policyWrapper", value = "Policy details related to the operation.",
                                       required = true) PolicyWrapper policyWrapper);

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Policies.",
            responseContainer = "List",
            notes = "Retrieve the details of all the policies that you have created in WSO2 EMM.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Fetched all policies."),
                            @ApiResponse(code = 500, message = "Policy Management related error occurred when " +
                                                               "fetching the policies.") })
    Response getAllPolicies();

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Policy.",
            notes = "Retrieve the details of a selected policy in WSO2 EMM.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Fetched policy details."),
                            @ApiResponse(code = 500, message = "Policy Management related error occurred when " +
                                                               "fetching the policies.") })
    Response getPolicy(@ApiParam(name = "id", value = "Policy ID value to identify a policy uniquely.",
                                 required = true) @PathParam("id") int policyId);

    @GET
    @Path("count")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Policy Count.",
            notes = "Get the number of policies that are created in WSO2 EMM.",
            response = Integer.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Fetched the policy count."),
                            @ApiResponse(code = 500, message = "Error while Fetching the policy count.") })
    Response getPolicyCount();

    @PUT
    @Path("{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a Policy.",
            notes = "If you wish to make changes to an existing policy, you can do so by updating the policy using " +
                    "this API")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Policy has been updated successfully."),
                            @ApiResponse(code = 500, message = "Policy Management related exception in policy " +
                                                               "update") })
    Response updatePolicy(@ApiParam(name = "policyWrapper", value = "Policy details related to the operation.",
                                    required = true) PolicyWrapper policyWrapper,
                          @ApiParam(name = "id", value = "Policy ID value to identify a policy uniquely.",
                                    required = true) @PathParam("id") int policyId);

    @PUT
    @Path("priorities")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the Policy Priority.",
            notes = "If you wish to make changes to the existing policy priority order, " +
                    "you can do so by updating the priority order using this API")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Policy Priorities successfully updated."),
                            @ApiResponse(code = 400, message = "Policy priorities did not update."),
                            @ApiResponse(code = 500, message = "Error in updating policy priorities.") })
    Response updatePolicyPriorities(@ApiParam(name = "priorityUpdatedPolicies",
                                              value = "List of policy update details..",
                                              required = true) List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies);

    @POST
    @Path("bulk-remove")
    @Consumes("application/json")
    @Produces("application/json")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Removing Multiple Policies.",
            notes = "In situations where you need to delete more than one policy you can do so using this API.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Policies have been successfully deleted."),
                            @ApiResponse(code = 400, message = "Policy does not exist."),
                            @ApiResponse(code = 500, message = "Error in deleting policies.") })
    Response bulkRemovePolicy(@ApiParam(name = "policyIds", value = "Policy ID list to be deleted.",
                                        required = true) List<Integer> policyIds);

    @PUT
    @Produces("application/json")
    @Path("activate")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Activating Policies.",
            notes = "Using the REST API command you are able to publish a policy in order to bring a policy that is " +
                    "in the inactive state to the active state.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Policies have been successfully activated."),
                            @ApiResponse(code = 500, message = "Error in activating policies.") })
    Response activatePolicy(@ApiParam(name = "policyIds", value = "Policy ID list to be activated.",
                                      required = true) List<Integer> policyIds);

    @PUT
    @Produces("application/json")
    @Path("inactivate")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Deactivating Policies.",
            notes = "Using the REST API command you are able to unpublish a policy in order to bring a policy that " +
                    "is in the active state to the inactive state.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Policies have been successfully deactivated."),
                            @ApiResponse(code = 500, message = "Error in deactivating policies.") })
    Response inactivatePolicy(@ApiParam(name = "policyIds", value = "Policy ID list to be deactivated.",
                                        required = true) List<Integer> policyIds) throws MDMAPIException;

    @PUT
    @Produces("application/json")
    @Path("apply-changes")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Applying Changes on Policies.",
            notes = "Policies in the active state will be applied to new device that register with WSO2 EMM based on" +
                    " the policy enforcement criteria . In a situation where you need to make changes to existing" +
                    " policies (removing, activating, deactivating and updating) or add new policies, the existing" +
                    " devices will not receive these changes immediately. Once all the required changes are made" +
                    " you need to apply the changes to push the policy changes to the existing devices.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Changes have been successfully updated."),
                            @ApiResponse(code = 500, message = "Error in updating policies.") })
    Response applyChanges();

    @GET
    @Path("start-task/{milliseconds}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Starting Policy Monitoring.",
            notes = "WSO2 EMM monitors the devices to identify any devices that have not complied to an enforced " +
                    "policy. The policy monitoring task begins at the point WSO2 EMM has a a published policy. " +
                    "It will monitor the device based on the policy monitoring frequency that you define in " +
                    "milliseconds.Using this REST API to start the policy monitoring task is optional as " +
                    "WSO2 EMM uses an OSGI call to start the monitoring task")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Policy monitoring service started successfully."),
                            @ApiResponse(code = 500, message = "Policy Management related exception when starting " +
                                                               "monitoring service.") })
    Response startTaskService(@ApiParam(name = "milliseconds", value = "Policy monitoring frequency in milliseconds.",
                                        required = true) @PathParam("milliseconds") int monitoringFrequency);

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
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Enforced Details of a Device.",
            notes = "When a device registers with WSO2 EMM a policy is enforced on the device. Initially the EMM " +
                    "filters the policies based on the Platform (device type), filters based on the device ownership" +
                    " type , filters based on the user role or name and finally the policy is enforced on the device.",
            response = org.wso2.carbon.policy.mgt.common.Policy.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Fetched current policy."),
                            @ApiResponse(code = 500, message = "Error occurred while getting the current policy.") })
    Response getDeviceActivePolicy(@ApiParam(name = "type", value = "Define the device type as the value for {type}." +
                                                                    " Example: ios, android, windows..",
                                             required = true) @PathParam("type") String type,
                                   @ApiParam(name = "id", value = "Define the device ID as the value for {id}.",
                                             required = true) @PathParam("id") String id);
}
