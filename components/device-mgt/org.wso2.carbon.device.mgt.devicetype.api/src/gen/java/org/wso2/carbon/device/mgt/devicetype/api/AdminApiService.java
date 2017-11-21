package org.wso2.carbon.device.mgt.devicetype.api;

import org.wso2.carbon.device.mgt.devicetype.api.*;
import org.wso2.carbon.device.mgt.devicetype.api.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.devicetype.api.dto.ErrorResponse;
import java.util.List;

import java.util.List;
import org.wso2.carbon.device.mgt.devicetype.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class AdminApiService {
    public abstract Response adminDeviceTypesGet( Request request) throws NotFoundException;
    public abstract Response adminDeviceTypesNameGet(String name
  ,Request request) throws NotFoundException;
    public abstract Response adminDeviceTypesNamePut(DeviceType type
 ,String name
  ,Request request) throws NotFoundException;
    public abstract Response adminDeviceTypesPost(List<DeviceType> type
  ,Request request) throws NotFoundException;
    public abstract Response adminDeviceTypesPut(List<DeviceType> type
  ,Request request) throws NotFoundException;
}
