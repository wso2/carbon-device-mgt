/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "geo_services"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/geo-services"),
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
                        name = "View Analytics",
                        description = "",
                        key = "perm:geo-service:analytics-view",
                        permissions = {"/device-mgt/devices/owning-device/view-analytics"}
                ),
                @Scope(
                        name = "Manage Alerts",
                        description = "",
                        key = "perm:geo-service:alerts-manage",
                        permissions = {"/device-mgt/devices/owning-device/manage-alerts"}
                )
        }
)
@Path("/geo-services")
@Api(value = "Geo Service",
        description = "This carries all the resources related to the geo service functionalities.")
public interface GeoLocationBasedService {
    /**
     * Retrieve Analytics for the device type
     */
    @GET
    @Path("stats/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Location Details of a Device",
            notes = "Get the location details of a device during a define time period.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:analytics-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoDeviceStats(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("to") long to);

    /**
     * Get data to show device locations in a map
     */
    @GET
    @Path("stats/device-locations")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Devices in a Defined Geofence",
            notes = "Get the details of the devices that are within the defined geofence coordinates. The geofence you are defining is enclosed with four coordinates in the shape of a square or rectangle. This is done by defining two points of the geofence. The other two points are automatically created using the given points. You can define the zoom level or scale of the map too.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:analytics-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid parameters found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoDeviceLocations(
            @ApiParam(
                    name = "minLat",
                    value = "Define the minimum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85213577747345")
            @QueryParam("minLat") double minLat,
            @ApiParam(
                    name = "maxLat",
                    value = "Define the maximum latitude of the geofence.",
                    required = true,
                    defaultValue ="79.85266149044037")
            @QueryParam("maxLat") double maxLat,
            @ApiParam(
                    name = "minLong",
                    value = "Define the minimum longitude of the geofence.",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("minLong") double minLong,
            @ApiParam(
                    name = "maxLong",
                    value = "Define the maximum longitude of the geofence",
                    required = true,
                    defaultValue ="6.909673257977737")
            @QueryParam("maxLong") double maxLong,
            @ApiParam(
                    name = "zoom",
                    value = "Define the level to zoom or scale the map. You can define any value between 1 to 14.",
                    required = true,
                    defaultValue ="2")
            @QueryParam("zoom") int zoom);


    /**
     * Create Geo alerts
    */
    @POST
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "POST",
            value = "Retrieving a Specific Geo Alert Type from a Device",
            notes = "Retrieve a specific geo alert from a device, such as getting a speed alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response createGeoAlerts(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed,Exit, or Stationary.",
                    required = true)
            @PathParam("alertType") String alertType);


    /**
     * Create Geo alerts for geo-dashboard
     */
    @POST
    @Path("/alerts/{alertType}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "POST",
            value = "Create Geo alerts for the device",
            notes = "",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response createGeoAlertsForGeoDashboard(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Update Geo alerts
     */
    @PUT
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "PUT",
            value = "Updating the Geo Alerts of a Device",
            notes = "Update the a geo alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response updateGeoAlerts(
            @ApiParam(
                    name = "alert",
                    value = "The alert object",
                    required = true)
            @Valid Alert alert,
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Retrieve Geo alerts
     */
    @GET
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting a Geo Alert from a Device",
            notes = "Retrieve a specific geo alert from a device, such as getting a speed alert that was sent to a device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlerts(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android. or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType);

    /**
     * Retrieve Geo alerts history
     */
    @GET
    @Path("alerts/history/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Geo Service Alert History of a Device",
            notes = "Get the geo alert history of a device during the defined time period.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getGeoAlertsHistory(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in the Epoch or UNIX format.",
                    required = true)
            @QueryParam("to") long to);

    @DELETE
    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "DELETE",
            value = "Deleting a Geo Alert from a Device",
            notes = "Delete a specific geo alert from a device, such as deleting a speed alert that was sent to the device.",
            response = Response.class,
            tags = "Geo Service Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:geo-service:alerts-manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response removeGeoAlerts(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "deviceType",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "alertType",
                    value = "The alert type, such as Within, Speed, Exit, or Stationary",
                    required = true)
            @PathParam("alertType") String alertType,
            @ApiParam(
                    name = "queryName",
                    value = "When you define a geofence you define a fence name for it. That name needs to be defined" +
                            " here.",
                    required = true)
            @QueryParam("queryName") String queryName);
}

