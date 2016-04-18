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

package org.wso2.carbon.mdm.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.mdm.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.mdm.api.util.ResponsePayload;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Device related operations
 */
@SuppressWarnings("NonJaxWsWebServices")
public class Device {
    private static Log log = LogFactory.getLog(Device.class);

    /**
     * Get all devices. We have to use accept all the necessary query parameters sent by datatable.
     * Hence had to put lot of query params here.
     *
     * @return Device List
     */
    @GET
    public Response getAllDevices(@QueryParam("type") String type, @QueryParam("user") String user,
                                  @QueryParam("role") String role, @QueryParam("status") EnrolmentInfo.Status status,
                                  @QueryParam("start") int startIdx, @QueryParam("length") int length,
                                  @QueryParam("device-name") String deviceName,
                                  @QueryParam("ownership") EnrolmentInfo.OwnerShip ownership) {
        try {
            DeviceManagementProviderService service = DeviceMgtAPIUtils.getDeviceManagementService();
            //Length > 0 means this is a pagination request.
            if (length > 0) {
                PaginationRequest paginationRequest = new PaginationRequest(startIdx, length);
                paginationRequest.setDeviceName(deviceName);
                paginationRequest.setOwner(user);
                if (ownership != null) {
                    paginationRequest.setOwnership(ownership.toString());
                }
                if (status != null) {
                    paginationRequest.setStatus(status.toString());
                }
                paginationRequest.setDeviceType(type);
                return Response.status(Response.Status.OK).entity(service.getAllDevices(paginationRequest)).build();
            }

            List<org.wso2.carbon.device.mgt.common.Device> allDevices;
            if ((type != null) && !type.isEmpty()) {
                allDevices = service.getAllDevices(type);
            } else if ((user != null) && !user.isEmpty()) {
                allDevices = service.getDevicesOfUser(user);
            } else if ((role != null) && !role.isEmpty()) {
                allDevices = service.getAllDevicesOfRole(role);
            } else if (status != null) {
                allDevices = service.getDevicesByStatus(status);
            } else if (deviceName != null) {
                allDevices = service.getDevicesByName(deviceName);
            } else {
                allDevices = service.getAllDevices();
            }
            return Response.status(Response.Status.OK).entity(allDevices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device list.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Fetch device details for a given device type and device Id.
     *
     * @return Device wrapped inside Response
     */
    @GET
    @Path("view")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDevice(@QueryParam("type") String type,
                              @QueryParam("id") String id) {
        DeviceIdentifier deviceIdentifier = DeviceMgtAPIUtils.instantiateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        org.wso2.carbon.device.mgt.common.Device device;
        try {
            device = deviceManagementProviderService.getDevice(deviceIdentifier);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        ResponsePayload responsePayload = new ResponsePayload();
        if (device == null) {
            responsePayload.setStatusCode(HttpStatus.SC_NOT_FOUND);
            responsePayload.setMessageFromServer("Requested device by type: " +
                    type + " and id: " + id + " does not exist.");
            return Response.status(Response.Status.NOT_FOUND).entity(responsePayload).build();
        } else {
            responsePayload.setStatusCode(HttpStatus.SC_OK);
            responsePayload.setMessageFromServer("Sending Requested device by type: " + type + " and id: " + id + ".");
            responsePayload.setResponseContent(device);
            return Response.status(Response.Status.OK).entity(responsePayload).build();
        }
    }

    /**
     * Fetch device details of a given user.
     *
     * @param user         User Name
     * @return Device
     */
    @GET
    @Path("user/{user}")
    public Response getDevice(@PathParam("user") String user) {
        List<org.wso2.carbon.device.mgt.common.Device> devices;
        try {
            devices = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(user);
            if (devices == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the devices list of given user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Fetch device count of a given user.
     *
     * @param user User Name
     * @return Device
     */
    @GET
    @Path("user/{user}/count")
    public Response getDeviceCount(@PathParam("user") String user) {
        try {
            Integer count = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount(user);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the devices list of given user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Get current device count
     *
     * @return device count
     */
    @GET
    @Path("count")
    public Response getDeviceCount() {
        try {
            Integer count = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount();
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Get the list of devices that matches with the given name.
     *
     * @param deviceName   Device name
     * @param tenantDomain Callee tenant domain
     * @return list of devices.
     */
    @GET
    @Path("name/{name}/{tenantDomain}")
    public Response getDevicesByName(@PathParam("name") String deviceName,
                                     @PathParam("tenantDomain") String tenantDomain) {
        List<org.wso2.carbon.device.mgt.common.Device> devices;
        try {
            devices = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesByName(deviceName);
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the devices list of device name.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

	/**
	 * Get the list of available device types.
	 *
	 * @return list of device types.
	 */
	@GET
	@Path("types")
    public Response getDeviceTypes() {
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes();
            return Response.status(Response.Status.OK).entity(deviceTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}