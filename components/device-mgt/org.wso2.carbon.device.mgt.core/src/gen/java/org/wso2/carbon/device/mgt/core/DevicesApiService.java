package org.wso2.carbon.device.mgt.core;

import org.wso2.carbon.device.mgt.core.*;
import org.wso2.carbon.device.mgt.core.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.device.mgt.core.dto.DeviceList;
import org.wso2.carbon.device.mgt.core.dto.ErrorResponse;

import java.util.List;
import org.wso2.carbon.device.mgt.core.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class DevicesApiService {
    public abstract Response devicesGet(Integer limit
 ,String offset
 ,String user
 ,String since
 ,Boolean requireDeviceInfo
 ,String name
 ,String type
 ,String userPattern
 ,String role
 ,String ownership
 ,String status
 ,Integer groupId
  ,Request request) throws NotFoundException;
    public abstract Response devicesTypeIdGet(String type
 ,String id
  ,Request request) throws NotFoundException;
    public abstract Response devicesTypeIdStatusGet(String type
 ,String id
  ,Request request) throws NotFoundException;
    public abstract Response devicesUserDevicesGet(Integer limit
 ,String offset
 ,Boolean requireDeviceInfo
  ,Request request) throws NotFoundException;
}
