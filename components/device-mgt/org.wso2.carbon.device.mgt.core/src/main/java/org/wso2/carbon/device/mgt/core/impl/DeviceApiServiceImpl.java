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

public class DeviceApiServiceImpl extends DeviceApiService {
    @Override
    public Response deviceGet(Device body
, Integer limit
  ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
