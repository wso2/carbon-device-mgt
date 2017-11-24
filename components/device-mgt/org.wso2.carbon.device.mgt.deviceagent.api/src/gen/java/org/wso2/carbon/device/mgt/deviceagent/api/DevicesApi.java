package org.wso2.carbon.device.mgt.deviceagent.api;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.device.mgt.deviceagent.api.dto.Device;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.ErrorResponse;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Operation;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.OperationList;
import org.wso2.carbon.device.mgt.deviceagent.api.factories.DevicesApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.device.mgt.deviceagent.api.DevicesApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/device-mgt/v1.[\\d]+/devices")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/devices")
@io.swagger.annotations.Api(description = "the devices API")
public class DevicesApi implements Microservice  {
   private final DevicesApiService delegate = DevicesApiServiceFactory.getDevicesApi();

    
    @DELETE
    @Path("/enroll/{type}/{deviceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Unregistering a Device.", notes = "Update the device instance.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesEnrollTypeDeviceIdDelete(@ApiParam(value = "The device object such as ios, android, windows or fire-alarm." ,required=true) Device device
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesEnrollTypeDeviceIdDelete(device,deviceId,type,request);
    }
    
    @PUT
    @Path("/enroll/{type}/{deviceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update the device instance.", notes = "Update the device instance.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesEnrollTypeDeviceIdPut(@ApiParam(value = "The device object such as ios, android, windows or fire-alarm." ,required=true) Device device
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesEnrollTypeDeviceIdPut(device,deviceId,type,request);
    }
    
    @POST
    @Path("/enroll/{type}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new device instance.", notes = "Create a new device instance.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesEnrollTypePost(@ApiParam(value = "The device object such as ios, android, windows or fire-alarm." ,required=true) Device device
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesEnrollTypePost(device,type,request);
    }
    
    @POST
    @Path("/events/publish/data/{type}/{deviceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Publishing Events data only.", notes = "Publish events received by the device client to the WSO2 Data Analytics Server (DAS) using this API.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesEventsPublishDataTypeDeviceIdPost(@ApiParam(value = "The device object such as ios, android, windows or fire-alarm." ,required=true) Device device
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesEventsPublishDataTypeDeviceIdPost(device,type,deviceId,request);
    }
    
    @POST
    @Path("/events/publish/{type}/{deviceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Publishing Events.", notes = "Publish events received by the device client to the WSO2 Data Analytics Server (DAS) using this API.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesEventsPublishTypeDeviceIdPost(@ApiParam(value = "The device object such as ios, android, windows or fire-alarm." ,required=true) Device device
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesEventsPublishTypeDeviceIdPost(device,deviceId,type,request);
    }
    
    @GET
    @Path("/operations/{type}/{deviceId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get operations of a device.", notes = "Get operations of a device.", response = OperationList.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = OperationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = OperationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = OperationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = OperationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = OperationList.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = OperationList.class) })
    public Response devicesOperationsTypeDeviceIdGet(@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "status of the operation") @QueryParam("status") String status
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="5") @DefaultValue("5") @QueryParam("limit") Integer limit
,@ApiParam(value = "The starting pagination index for the complete list of qualified items.", defaultValue="0") @DefaultValue("0") @QueryParam("offset") String offset
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesOperationsTypeDeviceIdGet(type,deviceId,status,limit,offset,request);
    }
    
    @GET
    @Path("/operations/{type}/{deviceId}/{operationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get operation of a device.", notes = "Get opeartion of a device.", response = Operation.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = Operation.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = Operation.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = Operation.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = Operation.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = Operation.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = Operation.class) })
    public Response devicesOperationsTypeDeviceIdOperationIdGet(@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "The operation id",required=true) @PathParam("operationId") Integer operationId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesOperationsTypeDeviceIdOperationIdGet(type,deviceId,operationId,request);
    }
    
    @PUT
    @Path("/operations/{type}/{deviceId}/{operationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update opeartion of a device.", notes = "Update opeartion of a device.", response = void.class, tags={ "Device Agent Service", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.   Successfully created the device instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.  Specified device already exists.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.  The unauthorized access to the requested resource.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The specified device does not exist", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error.   Server error occurred while creating the device instance.", response = void.class) })
    public Response devicesOperationsTypeDeviceIdOperationIdPut(@ApiParam(value = "The operation object" ,required=true) Operation operation
,@ApiParam(value = "The device type such as ios, android, windows or fire-alarm.",required=true) @PathParam("type") String type
,@ApiParam(value = "The device id",required=true) @PathParam("deviceId") String deviceId
,@ApiParam(value = "The operation id",required=true) @PathParam("operationId") String operationId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesOperationsTypeDeviceIdOperationIdPut(operation,type,deviceId,operationId,request);
    }
}
