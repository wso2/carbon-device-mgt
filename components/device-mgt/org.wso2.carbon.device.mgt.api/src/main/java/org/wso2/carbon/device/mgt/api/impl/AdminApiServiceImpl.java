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

package org.wso2.carbon.device.mgt.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.device.mgt.api.AdminApiService;
import org.wso2.carbon.device.mgt.api.NotFoundException;
import org.wso2.carbon.device.mgt.api.dto.DeviceType;
import org.wso2.carbon.device.mgt.api.dto.ErrorResponse;
import org.wso2.carbon.device.mgt.api.internal.ServiceComponent;
import org.wso2.carbon.device.mgt.api.mapper.ModelMapper;
import org.wso2.carbon.device.mgt.common.exception.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.manager.DeviceTypeManager;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class AdminApiServiceImpl extends AdminApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminApiService.class);

    @Override
    public Response adminDeviceTypesDeviceTypeGet(String deviceType
            , Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();
            org.wso2.carbon.device.mgt.common.DeviceType savedDeviceType = deviceTypeManager.getDeviceType(deviceType);
            return Response.status(Response.Status.OK).entity(ModelMapper.map(savedDeviceType)).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            LOGGER.error(msg, e);
            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg).code(e.getStatus())).build();
        }
    }

    @Override
    public Response adminDeviceTypesDeviceTypePut(DeviceType type
            , String name
            , Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();
            org.wso2.carbon.device.mgt.common.DeviceType updatedDeviceType = deviceTypeManager.updateDeviceType(
                    ModelMapper.map(type), name
            );
            return Response.status(Response.Status.OK).entity(ModelMapper.map(updatedDeviceType)).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            LOGGER.error(msg, e);
            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg).code(e.getStatus())).build();
        }
    }

    @Override
    public Response adminDeviceTypesGet(Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();
            List<DeviceType> deviceTypesList = new ArrayList<>();
            deviceTypeManager.getDeviceTypes().forEach(deviceType -> deviceTypesList.add(ModelMapper.map(deviceType)));
            return Response.status(Response.Status.OK).entity(deviceTypesList).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            LOGGER.error(msg, e);
            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg).code(e.getStatus())).build();
        }
    }

    @Override
    public Response adminDeviceTypesPost(List<DeviceType> type
            , Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();
            List<org.wso2.carbon.device.mgt.common.DeviceType> pendingTypes = new ArrayList<>();
            List<DeviceType> addedTypes = new ArrayList<>();
            type.forEach(deviceType -> pendingTypes.add(ModelMapper.map(deviceType)));
            deviceTypeManager.addDeviceTypes(pendingTypes).forEach(
                    deviceType -> addedTypes.add(ModelMapper.map(deviceType))
            );
            return Response.status(Response.Status.OK).entity(addedTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while creating the device types.";
            LOGGER.error(msg, e);
            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg)).build();
        }
    }

    @Override
    public Response adminDeviceTypesPut(List<DeviceType> type
            , Request request) throws NotFoundException {
        try {
            DeviceTypeManager deviceTypeManager = ServiceComponent.getDeviceManagement().getDeviceTypeManager();

            List<org.wso2.carbon.device.mgt.common.DeviceType> pendingTypes = new ArrayList<>();
            List<DeviceType> updatedTypes = new ArrayList<>();
            type.forEach(deviceType -> pendingTypes.add(ModelMapper.map(deviceType)));
            deviceTypeManager.updateDeviceTypes(pendingTypes).forEach(
                    deviceType -> updatedTypes.add(ModelMapper.map(deviceType))
            );
            return Response.status(Response.Status.OK).entity(updatedTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating the device types.";
            LOGGER.error(msg, e);
            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg)).build();
        }
    }
}
