package org.wso2.carbon.device.mgt.deviceagent.api;

import org.wso2.carbon.device.mgt.deviceagent.api.*;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.device.mgt.deviceagent.api.dto.Device;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.ErrorResponse;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Operation;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.OperationList;

import java.util.List;
import org.wso2.carbon.device.mgt.deviceagent.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class DevicesApiService {
    public abstract Response devicesEnrollTypeDeviceIdDelete(Device device
 ,String deviceId
 ,String type
  ,Request request) throws NotFoundException;
    public abstract Response devicesEnrollTypeDeviceIdPut(Device device
 ,String deviceId
 ,String type
  ,Request request) throws NotFoundException;
    public abstract Response devicesEnrollTypePost(Device device
 ,String type
  ,Request request) throws NotFoundException;
    public abstract Response devicesEventsPublishDataTypeDeviceIdPost(Device device
 ,String type
 ,String deviceId
  ,Request request) throws NotFoundException;
    public abstract Response devicesEventsPublishTypeDeviceIdPost(Device device
 ,String deviceId
 ,String type
  ,Request request) throws NotFoundException;
    public abstract Response devicesOperationsTypeDeviceIdGet(String type
 ,String deviceId
 ,String status
 ,Integer limit
 ,String offset
  ,Request request) throws NotFoundException;
    public abstract Response devicesOperationsTypeDeviceIdOperationIdGet(String type
 ,String deviceId
 ,Integer operationId
  ,Request request) throws NotFoundException;
    public abstract Response devicesOperationsTypeDeviceIdOperationIdPut(Operation operation
 ,String type
 ,String deviceId
 ,String operationId
  ,Request request) throws NotFoundException;
}
