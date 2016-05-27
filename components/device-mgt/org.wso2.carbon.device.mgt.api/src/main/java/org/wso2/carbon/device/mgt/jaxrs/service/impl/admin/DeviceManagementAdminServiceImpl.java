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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementAdminServiceImpl implements DeviceManagementAdminService {

    private static final Log log = LogFactory.getLog(DeviceManagementAdminServiceImpl.class);

    @Override
    @GET
    public Response getDevicesByName(@QueryParam("name") String name, @QueryParam("tenant-domain") String tenantDomain) {
        List<Device> devices;
        try {
            devices = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesByName(name);
            if (devices == null || devices.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No device, which carries the name '" +
                        name + "', is currently enrolled in the system").build();
            }
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the devices that carry the name '" + name + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
