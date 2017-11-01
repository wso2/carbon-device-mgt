package org.wso2.carbon.device.mgt.core;

import org.wso2.carbon.device.mgt.core.*;
import org.wso2.carbon.device.mgt.core.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.Error;

import java.util.List;
import org.wso2.carbon.device.mgt.core.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class DeviceApiService {
    public abstract Response deviceGet(Device body
 ,Integer limit
  ,Request request) throws NotFoundException;
}
