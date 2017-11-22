package org.wso2.carbon.device.mgt.devicetype.api;

import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceType;
import org.wso2.msf4j.Request;

import java.util.List;
import javax.ws.rs.core.Response;

public abstract class AdminApiService {
    public abstract Response adminDeviceTypesGet(Request request) throws NotFoundException;

    public abstract Response adminDeviceTypesNameGet(String name
            , Request request) throws NotFoundException;

    public abstract Response adminDeviceTypesNamePut(DeviceType type
            , String name
            , Request request) throws NotFoundException;

    public abstract Response adminDeviceTypesPost(List<DeviceType> type
            , Request request) throws NotFoundException;

    public abstract Response adminDeviceTypesPut(List<DeviceType> type
            , Request request) throws NotFoundException;
}
