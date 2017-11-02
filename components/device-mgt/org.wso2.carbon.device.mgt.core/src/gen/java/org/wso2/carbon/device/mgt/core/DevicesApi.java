package org.wso2.carbon.device.mgt.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.device.mgt.core.dto.DeviceList;
import org.wso2.carbon.device.mgt.core.dto.ErrorResponse;
import org.wso2.carbon.device.mgt.core.factories.DevicesApiServiceFactory;

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
    name = "org.wso2.carbon.device.mgt.core.DevicesApi",
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

    
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Getting Details of Registered Devices.", notes = "Provides details of all the devices enrolled with WSO2 IoT Server. ", response = DeviceList.class, tags={ "Device Management", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully fetched the list of devices ", response = DeviceList.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = DeviceList.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request The incoming request has more than one selection criteria defined via the  query parameters. ", response = DeviceList.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found The search criteria did not match any device registered with the server. ", response = DeviceList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = DeviceList.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error. Server error occurred while fetching the device list. ", response = DeviceList.class) })
    public Response devicesGet(@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="5") @DefaultValue("5") @QueryParam("limit") Integer limit
,@ApiParam(value = "The starting pagination index for the complete list of qualified items.", defaultValue="0") @DefaultValue("0") @QueryParam("offset") String offset
,@ApiParam(value = "The starting pagination index for the complete list of qualified items.") @QueryParam("user") String user
,@ApiParam(value = "Checks if the requested variant was created since the specified date-time. Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. Example: Mon, 05 Jan 2014 15:10:00 +0200 ") @QueryParam("since") String since
,@ApiParam(value = "A flag indicating whether to include device-info (location, application list etc) to the device object.") @QueryParam("requireDeviceInfo") Boolean requireDeviceInfo
,@ApiParam(value = "Name of the device, such as shamu, bullhead or Nexus.") @QueryParam("name") String name
,@ApiParam(value = "The device type, such as ios, android or windows.") @QueryParam("type") String type
,@ApiParam(value = "The pattern of username of the device owner.") @QueryParam("userPattern") String userPattern
,@ApiParam(value = "A role of device owners. Ex - store-admin.") @QueryParam("role") String role
,@ApiParam(value = "Provide the ownership status of the device. The following values can be assigned:- BYOD: Bring Your Own Device | COPE: Corporate-Owned, Personally-Enabled. ", allowableValues="BYOD, COPE") @QueryParam("ownership") String ownership
,@ApiParam(value = "Provide the device status details, such as active or inactive.") @QueryParam("status") String status
,@ApiParam(value = "Id of the group which the device belongs to.") @QueryParam("groupId") Integer groupId
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesGet(limit,offset,user,since,requireDeviceInfo,name,type,userPattern,role,ownership,status,groupId,request);
    }
    
    @GET
    @Path("/{type}/{id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Getting details of a device by Id and type.", notes = "Get the details of an enrolled device by specifying the device type and device identifier. ", response = Boolean.class, tags={ "Device Management", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully fetched the details of the device. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request Invalid request or validation error. The device ID or the type may be wrong. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found A device carrying the specified device ID and device type is not found. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error. Server error occurred while fetching the device list. ", response = Boolean.class) })
    public Response devicesTypeIdGet(@ApiParam(value = "The device type, such as ios, android or windows.",required=true) @PathParam("type") String type
,@ApiParam(value = "The unique device ID.",required=true) @PathParam("id") String id
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesTypeIdGet(type,id,request);
    }
    
    @GET
    @Path("/{type}/{id}/status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Checking if a provided device is enrolled with the server or not.", notes = "Device can be enrolled or unenrolled from the server and the device status is returned to identify if the device is currently enrolled or not. ", response = Boolean.class, tags={ "Device Management", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully fetched device enrollment status. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request Invalid request or validation error. The device ID or the type may be wrong. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found A device carrying the specified device ID and device type is not found. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error. Server error occurred while fetching the device list. ", response = Boolean.class) })
    public Response devicesTypeIdStatusGet(@ApiParam(value = "The device type, such as ios, android or windows.",required=true) @PathParam("type") String type
,@ApiParam(value = "The unique device ID.",required=true) @PathParam("id") String id
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesTypeIdStatusGet(type,id,request);
    }
    
    @GET
    @Path("/user-devices")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Getting details of registered devices, owned by an authenticated user", notes = "Provides details of devices enrolled by authenticated user. ", response = Boolean.class, tags={ "Device Management", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully fetched the list of devices. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request Invalid request or validation error. The device ID or the type may be wrong. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found A device carrying the specified device ID and device type is not found. ", response = Boolean.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error. Server error occurred while fetching the device list. ", response = Boolean.class) })
    public Response devicesUserDevicesGet(@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="5") @DefaultValue("5") @QueryParam("limit") Integer limit
,@ApiParam(value = "The starting pagination index for the complete list of qualified items.", defaultValue="0") @DefaultValue("0") @QueryParam("offset") String offset
,@ApiParam(value = "A flag indicating whether to include device-info (location, application list etc) to the device object.") @QueryParam("requireDeviceInfo") Boolean requireDeviceInfo
 ,@Context Request request)
    throws NotFoundException {
        return delegate.devicesUserDevicesGet(limit,offset,requireDeviceInfo,request);
    }
}
