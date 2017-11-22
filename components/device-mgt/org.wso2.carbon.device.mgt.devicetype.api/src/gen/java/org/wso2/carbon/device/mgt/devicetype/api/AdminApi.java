package org.wso2.carbon.device.mgt.devicetype.api;


import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.devicetype.api.factories.AdminApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.util.List;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
        name = "org.wso2.carbon.device.mgt.devicetype.api.AdminApi",
        service = Microservice.class,
        immediate = true
)
@Path("/api/device-mgt/v1.[\\d]+/admin")
@Consumes({"application/json"})
@Produces({"application/json"})
@ApplicationPath("/admin")
@io.swagger.annotations.Api(description = "the admin API")
public class AdminApi implements Microservice {
    private final AdminApiService delegate = AdminApiServiceFactory.getAdminApi();


    @GET
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Getting the Supported Device Type with Meta Definition",
                                         notes = "Get the list of device types supported by WSO2 IoT.",
                                         response = DeviceType.class, responseContainer = "List",
                                         tags = {"Device Type Management Administrative Service",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                                                message = "OK.   Successfully fetched the list of supported device " +
                                                        "types.",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified.   Empty body because the client already has " +
                                                        "the latest version of the requested resource. ",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable.  The requested media type is not supported",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Internal Server Error.   Server error occurred while " +
                                                        "fetching the list of supported device types.",
                                                response = DeviceType.class, responseContainer = "List")})
    public Response adminDeviceTypesGet(@Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesGet(request);
    }

    @GET
    @Path("/device-types/{name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Getting a Device Type with Meta Definition",
                                         notes = "Get a device type information.", response = DeviceType.class,
                                         responseContainer = "List",
                                         tags = {"Device Type Management Administrative Service",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully fetched the device type.",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified.   Empty body because the client already has " +
                                                        "the latest version of the requested resource. ",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable.  The requested media type is not supported",
                                                response = DeviceType.class, responseContainer = "List"),

            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Internal Server Error.   Server error occurred while " +
                                                        "fetching the list of supported device types.",
                                                response = DeviceType.class, responseContainer = "List")})
    public Response adminDeviceTypesNameGet(
            @ApiParam(value = "The device type name such as ios, android, windows or fire-alarm.", required = true)
            @PathParam("name") String name
            , @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesNameGet(name, request);
    }

    @PUT
    @Path("/device-types/{name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update a Device Type",
                                         notes = "Update the details of a list of device types in the server.",
                                         response = void.class,
                                         tags = {"Device Type Management Administrative Service",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully updated the device type.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified. Empty body because the client already has " +
                                                        "the latest version of the requested resource.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 401,
                                                message = "Unauthorized.  The unauthorized access to the requested " +
                                                        "resource.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  The specified device does not exist",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable.  The requested media type is not supported",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Internal Server Error.   Server error occurred while " +
                                                        "fetching the device list.",
                                                response = void.class)})
    public Response adminDeviceTypesNamePut(
            @ApiParam(value = "The device type such as ios, android, windows or fire-alarm.", required = true)
                    DeviceType type
            , @ApiParam(value = "The device type name such as ios, android, windows or fire-alarm.", required = true)
            @PathParam("name") String name
            , @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesNamePut(type, name, request);
    }

    @POST
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add Multiple Device Types",
                                         notes = "Add the details of a device type to the server which is provide " +
                                                 "meta data of the device.",
                                         response = void.class,
                                         tags = {"Device Type Management Administrative Service",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully added the device type.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified. Empty body because the client already has " +
                                                        "the latest version of the requested resource.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 401,
                                                message = "Unauthorized.  The unauthorized access to the requested " +
                                                        "resource. ",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  The specified device does not exist",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable.  The requested media type is not supported",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Internal Server Error.   Server error occurred while fetching the device list.",
                                                response = void.class)})
    public Response adminDeviceTypesPost(
            @ApiParam(value = "The device type such as iOS, Android, Windows or fire-alarm.", required = true)
                    List<DeviceType> type
            , @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesPost(type, request);
    }

    @PUT
    @Path("/device-types")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update Multiple Device Types",
                                         notes = "Update the details of a device type in the server.",
                                         response = void.class,
                                         tags = {"Device Type Management Administrative Service",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully updated the device type.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified. Empty body because the client already has the latest version of the requested resource.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 401,
                                                message = "Unauthorized.  The unauthorized access to the requested resource.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  The specified device does not exist",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable.  The requested media type is not supported",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500,
                                                message = "Internal Server Error.   Server error occurred while fetching the device list.",
                                                response = void.class)})
    public Response adminDeviceTypesPut(
            @ApiParam(value = "The device type such as ios, android, windows or fire-alarm.", required = true)
                    List<DeviceType> type
            , @Context Request request)
            throws NotFoundException {
        return delegate.adminDeviceTypesPut(type, request);
    }
}
