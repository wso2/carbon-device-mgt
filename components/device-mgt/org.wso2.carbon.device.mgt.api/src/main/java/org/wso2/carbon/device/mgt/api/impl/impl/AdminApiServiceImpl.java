/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.api.impl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.device.mgt.api.AdminApiService;
import org.wso2.carbon.device.mgt.api.ApiResponseMessage;
import org.wso2.carbon.device.mgt.api.NotFoundException;
import org.wso2.carbon.device.mgt.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.api.dto.ErrorResponse;
import org.wso2.carbon.device.mgt.api.internal.ServiceComponent;
import org.wso2.carbon.device.mgt.api.mapper.ModelMapper;
import org.wso2.carbon.device.mgt.common.exception.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.manager.DeviceTypeManager;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * This class is used for admin apis.
 */
public class AdminApiServiceImpl extends AdminApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminApiServiceImpl.class);

    @Override
    public Response adminDeviceTypesGet(Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();
            List<DeviceType> deviceTypesList = new ArrayList<>();
            List<org.wso2.carbon.device.mgt.common.DeviceType> deviceTypes = deviceTypeManager.getDeviceTypes();
            deviceTypes.forEach(deviceType -> deviceTypesList.add(ModelMapper.map(deviceType)));
            return Response.status(Response.Status.OK).entity(deviceTypesList).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            LOGGER.error(msg, e);
            return Response.serverError().entity(new ErrorResponse().message(msg)).build();
        }
    }

    @Override
    public Response adminDeviceTypesPost(DeviceType type
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response adminDeviceTypesPut(DeviceType type
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
