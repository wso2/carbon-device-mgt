/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.jaxrs.api.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.jaxrs.api.DeviceInformation;
import org.wso2.carbon.device.mgt.jaxrs.api.util.DeviceMgtAPIUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

@SuppressWarnings("NonJaxWsWebServices")
public class DeviceInformationImpl implements DeviceInformation {

    private static Log log = LogFactory.getLog(DeviceInformationImpl.class);

    @GET
    @Path("{type}/{id}")
    public Response getDeviceInfo(@PathParam("type") String type, @PathParam("id") String id) {
        DeviceInformationManager informationManager;
        DeviceInfo deviceInfo;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceInfo = informationManager.getDeviceInfo(deviceIdentifier);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceInfo).build();
    }


    @POST
    @Path("list")
    public Response getDevicesInfo(List<DeviceIdentifier> deviceIdentifiers) {
        DeviceInformationManager informationManager;
        List<DeviceInfo> deviceInfos;
        try {
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceInfos = informationManager.getDevicesInfo(deviceIdentifiers);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceInfos).build();
    }


    @GET
    @Path("location/{type}/{id}")
    public Response getDeviceLocation(@PathParam("type") String type, @PathParam("id") String id) {
        DeviceInformationManager informationManager;
        DeviceLocation deviceLocation;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceLocation = informationManager.getDeviceLocation(deviceIdentifier);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device location.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocation).build();
    }

    @Override
    public Response getDeviceLocations(@ApiParam(name = "deviceIdentifiers", value = "List of device identifiers",
            required = true) List<DeviceIdentifier> deviceIdentifiers) {
        DeviceInformationManager informationManager;
        List<DeviceLocation> deviceLocations;
        try {
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceLocations = informationManager.getDeviceLocations(deviceIdentifiers);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device location.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocations).build();
    }
}

