/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceTypeList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceTypeManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/device-types")
public class DeviceTypeManagementServiceImpl implements DeviceTypeManagementService {

    private static Log log = LogFactory.getLog(DeviceTypeManagementServiceImpl.class);

    @GET
    @Override
    public Response getDeviceTypes(@HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<String> deviceTypes;
        try {
            deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes();

            DeviceTypeList deviceTypeList = new DeviceTypeList();
            deviceTypeList.setCount(deviceTypes.size());
            deviceTypeList.setList(deviceTypes);
            return Response.status(Response.Status.OK).entity(deviceTypeList).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

}
