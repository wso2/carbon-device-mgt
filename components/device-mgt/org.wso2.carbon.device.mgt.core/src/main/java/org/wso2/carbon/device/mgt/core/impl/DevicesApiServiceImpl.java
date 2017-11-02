package org.wso2.carbon.device.mgt.core.impl;

import org.wso2.carbon.device.mgt.core.*;
import org.wso2.carbon.device.mgt.core.dto.*;


import java.util.List;
import org.wso2.carbon.device.mgt.core.NotFoundException;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class DevicesApiServiceImpl extends DevicesApiService {
    @Override
    public Response devicesGet(Integer limit
, String offset
, String user
, String since
, Boolean requireDeviceInfo
, String name
, String type
, String userPattern
, String role
, String ownership
, String status
, Integer groupId
  ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response devicesTypeIdGet(String type
, String id
  ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response devicesTypeIdStatusGet(String type
, String id
  ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response devicesUserDevicesGet(Integer limit
, String offset
, Boolean requireDeviceInfo
  ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
