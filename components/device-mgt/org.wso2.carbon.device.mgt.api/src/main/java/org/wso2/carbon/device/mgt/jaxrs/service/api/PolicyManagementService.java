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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PriorityUpdatedPolicyWrapper;
import org.wso2.carbon.policy.mgt.common.Policy;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Policy related REST-API. This can be used to manipulated policies and associate them with devices, users, roles,
 * groups.
 */
@Api(value = "Device Policy Management", description = "This API carries all the necessary functionalities " +
        "around device policy management")
@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PolicyManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a new policy.",
            notes = "This particular resource can be used to add a new policy, which will be created in in-active state.",
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Policy has successfully been created",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added policy."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 401,
                            message = "Not Found. \n Current logged in user is not authorized to add policies.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while adding a new policy.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify",
            permissions = {"/permission/admin/device-mgt/admin/policies/add"}
    )
    Response addPolicy(
            @ApiParam(
                    name = "policy",
                    value = "Policy details related to the operation.",
                    required = true)
                    @Valid PolicyWrapper policy);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get details of policies.",
            responseContainer = "List",
            notes = "Retrieve the details of all the policies that have been created in EMM.",
            response = Policy.class,
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched policies.",
                            response = Policy.class,
                            responseContainer = "List",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = ("Internal Server Error. \n Server error occurred while fetching " +
                                    "policies."),
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-view",
            permissions = {"/permission/admin/device-mgt/admin/policies/list"}
    )
    Response getPolicies(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get details of a policy.",
            notes = "Retrieve the details of a given policy that has been created in EMM.",
            response = Policy.class,
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the policy.",
                            response = Policy.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No policy is found with the given id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "policy.",
                            response = ErrorResponse.class)
            })
    @Permission(
            scope = "policy-view",
            permissions = {"/permission/admin/device-mgt/admin/policies/list"}
    )
    Response getPolicy(
            @ApiParam(
                    name = "id",
                    value = "Policy identifier",
                    required = true)
            @PathParam("id")
                    int id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update a policy.",
            notes = "If you wish to make changes to an existing policy, that can be done by updating the policy using " +
                    "this resource.",
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Policy has been updated successfully",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the updated device."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource to be deleted does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while updating the policy.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify",
            permissions = {"/permission/admin/device-mgt/admin/policies/update"}
    )
    Response updatePolicy(
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id")
                    int id,
            @ApiParam(
                    name = "policy",
                    value = "Policy details related to the operation.",
                    required = true)
                    @Valid PolicyWrapper policy);

    @POST
    @Path("/remove-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Remove multiple policies.",
            notes = "In situations where you need to delete more than one policy you can do so using this API.",
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Policies have successfully been removed"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource to be deleted does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not "
                                    + "supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while bulk removing policies.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify",
            permissions = {"/permission/admin/device-mgt/admin/policies/remove"}
    )
    Response removePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "Policy id list to be deleted.",
                    required = true)
                    List<Integer> policyIds);

    @POST
    @Path("/activate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Activating policies.",
            notes = "Using the REST API command you are able to publish a policy in order to bring a policy that is " +
                    "in the inactive state to the active state.",
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Policies have been successfully activated."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "ErrorResponse in activating policies.",
                            response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify", permissions = {
            "/permission/admin/device-mgt/admin/policies/update",
            "/permission/admin/device-mgt/admin/policies/add"}
    )
    Response activatePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "Policy ID list to be activated.",
                    required = true)
                    List<Integer> policyIds);

    @POST
    @Path("/deactivate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Deactivating policies.",
            notes = "Using the REST API command you are able to unpublish a policy in order to bring a "
                    + "policy that is in the active state to the inactive state.",
            tags = "Device Policy Management")
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Policies have been successfully deactivated."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "ErrorResponse in deactivating policies.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify",
            permissions = {
            "/permission/admin/device-mgt/admin/policies/update",
            "/permission/admin/device-mgt/admin/policies/add"}
    )
    Response deactivatePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "Policy ID list to be deactivated.",
                    required = true)
                    List<Integer> policyIds);

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
                    " you need to apply the changes to push the policy changes to the existing devices.",
            tags = "Device Policy Management"
    )
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Changes have been successfully updated."),
            @ApiResponse(
                    code = 500,
                    message = "ErrorResponse in deactivating policies.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "policy-modify",
            permissions = {"/permission/admin/device-mgt/admin/policies/update"}
    )
    Response applyChanges();


    @PUT
    @Path("/priorities")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Prioritizing policies.",
            notes = "If you wish to make changes to the existing policy priority order, you can do so by "
                    + "updating the priority order using this end-point",
            tags = "Device Policy Management"
    )
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Policy Priorities successfully updated."),
            @ApiResponse(
                    code = 400,
                    message = "Policy priorities did not update. Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Exception in updating policy priorities.",
                    response = ErrorResponse.class)
            }
    )
    @Permission(
            scope = "",
            permissions = {})
    Response updatePolicyPriorities(
            @ApiParam(
                    name = "priorityUpdatedPolicies",
                    value = "List of policies with priorities",
                    required = true)
                    List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies);


}
