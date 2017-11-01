package org.wso2.carbon.device.mgt.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.Error;
import org.wso2.carbon.device.mgt.core.factories.DeviceApiServiceFactory;

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
    name = "org.wso2.carbon.device.mgt.core.DeviceApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/device-mgt/v1.[\\d]+/device")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/device")
@io.swagger.annotations.Api(description = "the device API")
public class DeviceApi implements Microservice  {
   private final DeviceApiService delegate = DeviceApiServiceFactory.getDeviceApi();

    
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Getting Details of Registered Devices.", notes = "Provides details of all the devices enrolled with WSO2 IoT Server. ", response = Device.class, tags={ "Core", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully fetched the list of devices ", response = Device.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Device.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = Device.class) })
    public Response deviceGet(@ApiParam(value = "Advanced level policy object that should to be added " ,required=true) Device body
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
 ,@Context Request request)
    throws NotFoundException {
        return delegate.deviceGet(body,limit,request);
    }
}
