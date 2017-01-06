package org.wso2.carbon.device.mgt.jaxrs.service.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardPaginationGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Device Analytics Dashboard related REST-APIs. This can be used to obtain device related analytics.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceAnalyticsDashboard"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/dashboard"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device Analytics Dashboard related APIs.")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Device Count Overview",
                        description = "Device Count Overview",
                        key = "perm:dashboard:count-overview",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Device Counts by Potential Vulnerabilities",
                        description = "Device Counts by Potential Vulnerabilities",
                        key = "perm:dashboard:vulnerabilities",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get the number of devices that have not complied to a policy",
                        description = "Get the number of devices that have not complied to a policy",
                        key = "perm:dashboard:non-compliant",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get the number of devices for a given device type, such as connectivity status, "
                                + "potential vulnerability, platform, and ownership",
                        description = "Get the number of devices for a given device type, such as connectivity status, "
                                + "potential vulnerability, platform, and ownership",
                        key = "perm:dashboard:by-groups",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get the number of devices that have not complied to a given policy based on a particular",
                        description = "Get the number of devices that have not complied to a given policy based on a particular",
                        key = "perm:dashboard:device-counts",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get the number of devices that have not complied to a given policy based on a particular"
                                + " device type.",
                        description = "Get the number of devices that have not complied to a given policy based on a " +
                                "particular device type.",
                        key = "perm:dashboard:filtered-count",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get the number of devices that have not complied to a given policy over the total"
                                + " number of devices registered with WSO2 EMM.\n",
                        description = "Get the number of devices that have not complied to a given policy over the total"
                                + " number of devices registered with WSO2 EMM.\n",
                        key = "perm:dashboard:non-compliant-count",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get device details of devices based on a particular device type.",
                        description = "Get device details of devices based on a particular device type.",
                        key = "perm:dashboard:details",
                        permissions = {"/device-mgt/dashboard/view"}
                ),
                @Scope(
                        name = "Get device details of non-compliant devices which do not comply to a given policy.",
                        description = "Get device details of non-compliant devices which do not comply to a given policy.",
                        key = "perm:dashboard:feature-non-compliant",
                        permissions = {"/device-mgt/dashboard/view"}
                )
        }
)
@Path("/dashboard")
@Api(value = "Device Analytics Dashboard",
        description = "Device Analytics Dashboard related information APIs are described here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface Dashboard {

    String CONNECTIVITY_STATUS = "connectivity-status";
    String POTENTIAL_VULNERABILITY = "potential-vulnerability";
    String NON_COMPLIANT_FEATURE_CODE = "non-compliant-feature-code";
    String PLATFORM = "platform";
    String OWNERSHIP = "ownership";
    // Constants related to pagination
    String PAGINATION_ENABLED = "pagination-enabled";
    String START_INDEX = "start";
    String RESULT_COUNT = "length";

    @GET
    @Path("device-count-overview")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the details of registered devices in WSO2 IoT.",
            notes = "Get the details of active, inactive, removed and total number of registered devices in"
                    + " WSO2 IoT.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:count-overview")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getOverviewDeviceCounts();

    @GET
    @Path("device-counts-by-potential-vulnerabilities")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of unmonitored and non-compliant devices in WSO2 IoT.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:vulnerabilities")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching activity data.",
                    response = ErrorResponse.class)
    })
    Response getDeviceCountsByPotentialVulnerabilities();

    @GET
    @Path("non-compliant-device-counts-by-features")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of devices that have not complied to a policy that was enforced on a "
                    + "device.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:non-compliant")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardPaginationGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getNonCompliantDeviceCountsByFeatures(
            @ApiParam(
                    name = "start",
                    value = "Provide the starting pagination index. Example 10",
                    required = true)
            @QueryParam(START_INDEX) int startIndex,
            @ApiParam(
                    name = "length",
                    value = "Provide how many policy details you require from the starting pagination index."
                            + " For example if you require the non-compliant policy details from the 10th "
                            + "pagination index to the 15th, you must define 10 as the value for start and "
                            + "5 as the value for length.",
                    required = true)
            @QueryParam(RESULT_COUNT) int resultCount);

    @GET
    @Path("device-counts-by-groups")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of devices for a given device type, such as connectivity status, "
                    + "potential vulnerability, platform, and ownership.\n",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:by-groups")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DeviceCountByGroup.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getDeviceCountsByGroups(
            @ApiParam(
                    name = "connectivity-status",
                    value = "Provide the connectivity status of the device. The following values can be assigned:\n"
                            + "active: The devices that are registered with WSO2 IoT and are actively "
                            + "communicating with the server.\n"
                            + "inactive: The devices that are registered with WSO2 IoT but unable to "
                            + "actively communicate with the server.\n"
                            + "removed: The devices that have unregistered from WSO2 IoT",
                    required = true)
            @QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
            @ApiParam(
                    name = "potential-vulnerability",
                    value = "Provide details of the potential vulnerabilities of the device. The following "
                            + "values can be assigned:\n"
                            + "non-compliant: Devices that have not complied to the policies enforced on the "
                            + "device by WSO2 IoT.\n"
                            + "unmonitored: Devices that have no policy assigned to them.",
                    required = true)
            @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. The following values can "
                            + "be assigned:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = true)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = true)
            @QueryParam(OWNERSHIP) String ownership);

    @GET
    @Path("feature-non-compliant-device-counts-by-groups")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of devices that have not complied to a given policy based on a particular"
                    + " device type.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:device-counts")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DeviceCountByGroup.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getFeatureNonCompliantDeviceCountsByGroups(
            @ApiParam(
                    name = "non-compliant-feature-code",
                    value = "As the value for this parameter, the policy feature code or ID can be used. Some"
                            + " examples for feature codes are:  PASSCODE_POLICY,CAMERA and WIFI.",
                    required = true)
            @QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. The following values can "
                            + "be assigned:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = false)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = false)
            @QueryParam(OWNERSHIP) String ownership);

    @GET
    @Path("filtered-device-count-over-total")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of devices that are registered with WSO2 IoT filtered by one of the "
                    + "following attributes:\n"
                    + "Connectivity status of the device, such as active, inactive or removed.\n"
                    + "The device ownership type, such as BYOD or COPE.\n" + "The device platform.\n"
                    + "The potential vulnerabilities faced by the devices.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:filtered-count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getFilteredDeviceCountOverTotal(
            @ApiParam(
                    name = "connectivity-status",
                    value = "Provide the connectivity status of the device. You can assign any of the values "
                            + "given below:\n"
                            + "Total: All the devices that have registered with WSO2 IoT.\n"
                            + "active: The devices that are registered with WSO2 IoT and are actively "
                            + "communicating with the server.\n"
                            + "inactive: The devices that are registered with WSO2 IoT but unable to actively"
                            + " communicate with the server.\n"
                            + "removed: The devices that have unregistered from WSO2 IoT.",
                    required = true)
            @QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
            @ApiParam(
                    name = "potential-vulnerability",
                    value = "Provide details of the potential vulnerabilities of the device. You can assign"
                            + " any of the values given below:\n"
                            + "non-compliant: Devices that have not complied to the policies enforced on the "
                            + "device by WSO2 IoT.\n"
                            + "unmonitored: Devices that have no policy assigned to them.",
                    required = true)
            @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. You can assign any of the "
                            + "values given below:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = true)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. You can assign any of the values "
                            + "given below:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = true)
            @QueryParam(OWNERSHIP) String ownership);

    @GET
    @Path("feature-non-compliant-device-count-over-total")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get the number of devices that have not complied to a given policy over the total"
                    + " number of devices registered with WSO2 IoT.\n",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:non-compliant-count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getFeatureNonCompliantDeviceCountOverTotal(
            @ApiParam(
                    name = "non-compliant-feature-code",
                    value = "Provide the feature code or ID of the policy. Some examples for feature codes "
                            + "are: WIFI, PASSCODE_POLICY, CAMERA and ENCRYPT_STORAGE.",
                    required = true)
            @QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. You can assign the values "
                            + "given below:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = true)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. You can assign the values given below:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = true)
            @QueryParam(OWNERSHIP) String ownership);

    @GET
    @Path("devices-with-details")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get device details of devices based on a particular device type.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardPaginationGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getDevicesWithDetails(
            @ApiParam(
                    name = "connectivity-status",
                    value = "Provide the connectivity status of the device. This can be one of the following:\n"
                            + "Total: All the devices that have registered with WSO2 IoT.\n"
                            + "active: The devices that are registered with WSO2 IoT and are actively "
                            + "communicating with the server.\n"
                            + "inactive: The devices that are registered with WSO2 IoT but unable to actively"
                            + " communicate with the server.\n"
                            + "removed: The devices that have unregistered from WSO2 IoT.",
                    required = true)
            @QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
            @ApiParam(
                    name = "potential-vulnerability",
                    value = "Provide details of the potential vulnerabilities of the device. This can be:\n"
                            + "non-compliant: Devices that have not complied to the policies enforced on "
                            + "the device by WSO2 IoT.\n"
                            + "unmonitored: Devices that have no policy assigned to them. ",
                    required = true)
            @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. This can be one of the following:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = true)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. This can be one of the following:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = true)
            @QueryParam(OWNERSHIP) String ownership,
            @ApiParam(
                    name = "pagination-enabled",
                    value = "To enable/disable pagination set the value as true or false",
                    required = true)
            @QueryParam(PAGINATION_ENABLED) String paginationEnabled,
            @ApiParam(
                    name = "start",
                    value = "Provide the starting pagination index.",
                    required = true)
            @QueryParam(START_INDEX) int startIndex,
            @ApiParam(
                    name = "length",
                    value = "Provide how many policy details you require from the starting pagination index.",
                    required = true)
            @QueryParam(RESULT_COUNT) int resultCount);

    @GET
    @Path("feature-non-compliant-devices-with-details")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get device details of non-compliant devices which do not comply to a given policy.",
            tags = "Dashboard",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:dashboard:feature-non-compliant")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = DashboardPaginationGadgetDataWrapper.class,
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
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n ErrorResponse in retrieving requested data.",
                    response = ErrorResponse.class)
    })
    Response getFeatureNonCompliantDevicesWithDetails(
            @ApiParam(
                    name = "non-compliant-feature-code",
                    value = "Provide the code specific to the feature "
                            + "(examples for feature codes are: WIFI,PASSCODE_POLICY, CAMERA and ENCRYPT_STORAGE.)",
                    required = true)
            @QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
            @ApiParam(
                    name = "platform",
                    value = "Provide the platform that the device is running on. This can be one of the following:\n"
                            + "iOS\n" + "Android\n" + "Windows",
                    required = true)
            @QueryParam(PLATFORM) String platform,
            @ApiParam(
                    name = "ownership",
                    value = "Provide the ownership status of the device. This can be one of the following:\n"
                            + "BYOD: Bring Your Own Device\n" + "COPE: Corporate-Owned, Personally-Enabled",
                    required = true)
            @QueryParam(OWNERSHIP) String ownership,
            @ApiParam(
                    name = "pagination-enabled",
                    value = "To enable/disable pagination set the value as true or false",
                    required = true)
            @QueryParam(PAGINATION_ENABLED) String paginationEnabled,
            @ApiParam(
                    name = "start",
                    value = "Provide the starting pagination index.",
                    required = true)
            @QueryParam(START_INDEX) int startIndex,
            @ApiParam(
                    name = "length",
                    value = "Provide how many policy details you require from the starting pagination index.",
                    required = true)
            @QueryParam(RESULT_COUNT) int resultCount);
}
