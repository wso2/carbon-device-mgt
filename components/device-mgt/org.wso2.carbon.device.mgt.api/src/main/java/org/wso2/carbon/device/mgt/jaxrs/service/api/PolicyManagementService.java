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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.PolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PriorityUpdatedPolicyWrapper;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Policy related REST-API. This can be used to manipulated policies and associate them with devices, users, roles,
 * groups.
 */

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DevicePolicyManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/policies"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Adding a Policy",
                        description = "Adding a Policy",
                        key = "perm:policies:manage",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Getting Details of Policies",
                        description = "Getting Details of Policies",
                        key = "perm:policies:get-details",
                        permissions = {"/device-mgt/policies/view"}
                ),
                @Scope(
                        name = "Getting Details of a Policy",
                        description = "Getting Details of a Policy",
                        key = "perm:policies:get-policy-details",
                        permissions = {"/device-mgt/policies/view"}
                ),
                @Scope(
                        name = "Updating a Policy",
                        description = "Updating a Policy",
                        key = "perm:policies:update",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Removing Multiple Policies",
                        description = "Removing Multiple Policies",
                        key = "perm:policies:remove",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Activating Policies",
                        description = "Activating Policies",
                        key = "perm:policies:activate",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Deactivating Policies",
                        description = "Deactivating Policies",
                        key = "perm:policies:deactivate",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Applying Changes on Policies",
                        description = "Applying Changes on Policies",
                        key = "perm:policies:changes",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Updating the Policy Priorities",
                        description = "Updating the Policy Priorities",
                        key = "perm:policies:priorities",
                        permissions = {"/device-mgt/policies/manage"}
                ),
                @Scope(
                        name = "Fetching the Effective Policy",
                        description = "Fetching the Effective Policy",
                        key = "perm:policies:effective-policy",
                        permissions = {"/device-mgt/policies/view"}
                )
        }
)
@Api(value = "Device Policy Management", description = "This API includes the functionality around device policy management")
@Path("/policies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PolicyManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Policy",
            notes = "Add a policy using this REST API command. When adding a policy you will have the option of saving the policy or saving and publishing the policy." +
                    "Using this REST API you are able to save a created Policy and this policy will be in the inactive state.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:manage")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Successfully created the policy.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header",
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
                            message = "Not Found. \n The user that is currently logged in is not authorized to add policies.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while adding a new policy.",
                            response = ErrorResponse.class)
            })
    Response addPolicy(
            @ApiParam(
                    name = "policy",
                    value = "The properties required to add a new policy.",
                    required = true)
                    @Valid PolicyWrapper policy);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Policies",
            responseContainer = "List",
            notes = "Retrieve the details of all the policies in WSO2 EMM.",
            response = Policy.class,
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:get-details")
                })
            }
    )
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = ("Internal Server Error. \n Server error occurred while fetching the policies."),
                            response = ErrorResponse.class)
            })
    Response getPolicies(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many policy details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Policy",
            notes = "Retrieve the details of a policy that is in WSO2 EMM.",
            response = Policy.class,
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:get-policy-details")
                })
            }
    )
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A specified policy was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "policy.",
                            response = ErrorResponse.class)
            })
    Response getPolicy(
            @ApiParam(
                    name = "id",
                    value = "The policy identifier.",
                    required = true,
                    defaultValue = "")
            @PathParam("id")
                    int id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a Policy",
            notes = "Make changes to an existing policy by updating the policy using this resource.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:update")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the policy.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while updating the policy.",
                            response = ErrorResponse.class)
            })
    Response updatePolicy(
            @ApiParam(
                    name = "id",
                    value = "The policy ID.",
                    required = true,
                    defaultValue = "1")
            @PathParam("id")
                    int id,
            @ApiParam(
                    name = "policy",
                    value = "Update the required property details.",
                    required = true)
                    @Valid PolicyWrapper policy);

    @POST
    @Path("/remove-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Removing Multiple Policies",
            notes = "Delete one or more than one policy using this API.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:remove")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully removed the policy."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported.\n "
                                    + "supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred whilst bulk removing policies.",
                            response = ErrorResponse.class)
            })
    Response removePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "The list of policy IDs to be removed.",
                    required = true,
                    defaultValue = "[1]")
                    List<Integer> policyIds);

    @POST
    @Path("/activate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Activating Policies",
            notes = "Publish a policy using this API to bring a policy that is in the inactive state to the active state.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:activate")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Successfully activated the policy."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource/s does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Sever error whilst activating the policies.",
                            response = ErrorResponse.class)
            })
    Response activatePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "The list of the policy IDs to be activated",
                    required = true,
                    defaultValue = "[1]")
                    List<Integer> policyIds);

    @POST
    @Path("/deactivate-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Deactivating Policies",
            notes = "Unpublish a policy using this API to bring a policy that is in the active state to the inactive state.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:deactivate")
                })
            }
    )
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Successfully deactivated the policy."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "ErrorResponse in deactivating policies.",
                    response = ErrorResponse.class)
    })
    Response deactivatePolicies(
            @ApiParam(
                    name = "policyIds",
                    value = "The list of Policy IDs that needs to be deactivated.",
                    required = true,
                    defaultValue = "[1]")
                    List<Integer> policyIds);

    @PUT
    @Produces("application/json")
    @Path("apply-changes")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Applying Changes on Policies",
            notes = "Policies in the active state will be applied to new devices that register with WSO2 EMM based on" +
                    " the policy enforcement criteria . In a situation where you need to make changes to existing" +
                    " policies (removing, activating, deactivating and updating) or add new policies, the existing" +
                    " devices will not receive these changes immediately. Once all the required changes are made" +
                    " you need to apply the changes to push the policy changes to the existing devices.",
            tags = "Device Policy Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:changes")
                })
            }
    )
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Successfully updated the EMM server with the policy changes."),
            @ApiResponse(
                    code = 500,
                    message = "ErrorResponse in deactivating policies.",
                    response = ErrorResponse.class)
    })
    Response applyChanges();


    @PUT
    @Path("/priorities")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the Policy Priorities",
            notes = "Make changes to the existing policy priority order by updating the priority order using this API.",
            tags = "Device Policy Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:priorities")
            })
    }
    )
    @ApiResponses(
            value = {
            @ApiResponse(
                    code = 200,
                    message = "Successfully updated the policy priority order."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. Did not update the policy priority order.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Exception in updating the policy priorities.",
                    response = ErrorResponse.class)
    })
    Response updatePolicyPriorities(
            @ApiParam(
                    name = "priorityUpdatedPolicies",
                    value = "List of policies with priorities",
                    required = true)
                    List<PriorityUpdatedPolicyWrapper> priorityUpdatedPolicies);

    @GET
    @Path("/effective-policy/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Effective Policy",
            notes = "Retrieve the effective policy of a device using this API.",
            tags = "Device Policy Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:policies:effective-policy")
                    })
            }
    )
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A specified policy was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "policy.",
                            response = ErrorResponse.class)
            })
    Response getEffectivePolicy(
            @ApiParam(
                    name = "deviceType",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "deviceId",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("deviceId")
            @Size(max = 45)
                    String deviceId);
}
