/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationMetadataHolder;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationException;
import org.wso2.carbon.device.mgt.common.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationVisEdge;
import org.wso2.carbon.device.mgt.common.DeviceOrganizationVisNode;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceOrganizationService;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import javax.ws.rs.core.Response;
import java.util.List;

public class DeviceOrganizationServiceImpl implements DeviceOrganizationService {
    private static final Log log = LogFactory.getLog(DeviceAgentServiceImpl.class);

    @POST
    @Path("/devices")
    @Override
    public Response addDeviceOrganization(@Valid DeviceOrganizationMetadataHolder deviceOrganizationMetadataHolder) {
        if (deviceOrganizationMetadataHolder == null) {
            String errorMessage = "The payload of device metadata is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        String tempId = deviceOrganizationMetadataHolder.getDeviceId();
        String tempName = deviceOrganizationMetadataHolder.getDeviceName();
        String tempParent = deviceOrganizationMetadataHolder.getParent();
        int tempPingMins = deviceOrganizationMetadataHolder.getPingMins();
        int tempState = deviceOrganizationMetadataHolder.getState();
        int tempIsGateway = deviceOrganizationMetadataHolder.getIsGateway();
        boolean result;
        if (!"server".equals(tempId)) {
            try {
                result = dops.addDeviceOrganization(tempId, tempName, tempParent, tempPingMins, tempState, tempIsGateway);
                if (result) {
                    String msg = "Device added successfully";
                    return Response.status(Response.Status.OK).entity(msg).build();
                } else {
                    String msg = "Unable to add device";
                    if (log.isDebugEnabled()) {
                        log.debug(msg);
                    }
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }
            } catch (DeviceOrganizationException e) {
                String msg = "Error occurred while enrolling the device with ID:" + tempId + " to Device Organization ";
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            } catch (InvalidConfigurationException e) {
                log.error("failed to add operation", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            String msg = "Cannot manually add server";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @GET
    @Path("/devices/{deviceId}/state")
    @Override
    public Response getDeviceOrganizationStateById(@PathParam("deviceId") String deviceId) {
        if (deviceId.isEmpty()) {
            String errorMessage = "The parameter of the device organization ID is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        int result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getDeviceOrganizationStateById(deviceId);
            if (result == -1) {
                String msg = "Device with ID: " + deviceId + " not found.";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving state of device with ID:" + deviceId + " to Device " +
                    "Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            String msg = "failed to add operation";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/devices/{deviceId}/parent")
    @Override
    public Response getDeviceOrganizationParent(@PathParam("deviceId") String deviceId) {
        if (deviceId == null) {
            String errorMessage = "The parameter of the device organization ID is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        String result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getDeviceOrganizationParent(deviceId);
            if (result == null) {
                String msg = "Device with ID: " + deviceId + " not found.";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving state of device with ID:" + deviceId + " to Device " +
                    "Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/devices/{deviceId}/isgateway")
    @Override
    public Response getDeviceOrganizationIsGateway(@PathParam("deviceId") String deviceId) {
        if (deviceId == null) {
            String errorMessage = "The parameter of the device organization ID is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        int result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getDeviceOrganizationIsGateway(deviceId);
            if (result == -1) {
                String msg = "Device with ID: " + deviceId + " not found.";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving device with ID:" + deviceId + " is gateway in Device " +
                    "Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            String msg = "failed to add operation";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/devices")
    @Override
    public Response getDevicesInOrganization() {
        List<DeviceOrganizationMetadataHolder> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getDevicesInOrganization();
            if (result == null) {
                String msg = "No devices exist in organization";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving devices in organization";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            String msg =  "failed to add operation";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }


    @GET
    @Path("/devices/{parentId}/children")
    @Override
    public Response getChildrenByParentId(@PathParam("parentId") String parentId) {
        if (parentId == null) {
            String errorMessage = "The parameter of the parent ID is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        List<DeviceOrganizationMetadataHolder> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getChildrenByParentId(parentId);
            if (result == null) {
                String msg = "No children connected to device";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving children of device";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * This method is used by the visualization library to generate the nodes
     *
     * @return list of nodes as an array
     */
    @GET
    @Path("/visualization/nodes")
    @Override
    public Response generateNodes() {
        List<DeviceOrganizationVisNode> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        result = dops.generateNodes();
        if (result.isEmpty()) {
            String msg = "No Devices enrolled and visible";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }

    /**
     * This is used by the visualization library to generate edges
     *
     * @return list of edges as an array
     */
    @GET
    @Path("/visualization/edges")
    @Override
    public Response generateEdges() {
        List<DeviceOrganizationVisEdge> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        result = dops.generateEdges();
        if (result.isEmpty()) {
            String msg = "No Edge connections available and visible";
            if (log.isDebugEnabled()) {
                log.debug(msg);
            }
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @PUT
    @Path("/update/{deviceId}/{newParentId}")
    @Override
    public Response updateDeviceOrganizationParent(@PathParam("deviceId") String deviceId,
                                                   @PathParam("newParentId") String newParentId) {
        if (deviceId == null || newParentId == null) {
            String errorMessage = "One or more parameters are empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        String updatedParent;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            updatedParent = dops.updateDeviceOrganizationParent(deviceId, newParentId);
            if (updatedParent == null) {
                String msg = "Parent not updated";
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(updatedParent).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while updating parent of Device with ID: " + deviceId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            String msg = "failed to add operation";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
