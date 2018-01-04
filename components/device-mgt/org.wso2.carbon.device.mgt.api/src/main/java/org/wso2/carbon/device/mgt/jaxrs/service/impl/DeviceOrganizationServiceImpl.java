package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceOrganizationService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class DeviceOrganizationServiceImpl implements DeviceOrganizationService{
    private static final Log log = LogFactory.getLog(DeviceAgentServiceImpl.class);

    @POST
    @Path("/add")
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
        try {
            result  = dops.addDeviceOrganization(tempId,tempName,tempParent,tempPingMins,tempState,tempIsGateway);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while enrolling the device with ID:" + tempId + " to Device Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{deviceId}/state")
    @Override
    public Response getDeviceOrganizationStateById(@PathParam("deviceId") String deviceId) {
        if (deviceId.length() <= 0) {
            String errorMessage = "The parameter of the device organization ID is empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        int result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            result = dops.getDeviceOrganizationStateById(deviceId);
            if (result == -1) {
                String msg = "Device with ID: " + deviceId + " not found.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving state of device with ID:" + deviceId + " to Device Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{deviceId}/parent")
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
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving state of device with ID:" + deviceId + " to Device Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{deviceId}/isgateway")
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
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving id device with ID:" + deviceId + " is gateway in Device Organization ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while retrieving devices in organization";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GET
    @Path("/{parentId}/children")
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
                log.error(msg);
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

    @GET
    @Path("/nodes")
    @Override
    public Response generateNodes() {
        List<DeviceOrganizationVisNode> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        result = dops.generateNodes();
        if (result.isEmpty()) {
            String msg = "No Devices enrolled and visible";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/edges")
    @Override
    public Response generateEdges() {
        List<DeviceOrganizationVisEdge> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        result = dops.generateEdges();
        if (result.isEmpty()) {
            String msg = "No Edge connections available and visible";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @GET
    @Path("/hierarchy")
    @Override
    public Response generateHierarchy() {
        return null;
    }

    @PUT
    @Path("/update/{deviceId}/{parentId}")
    @Override
    public Response updateDeviceOrganizationParent(@PathParam("deviceId") String deviceId,
                                                   @PathParam("parentId") String parentId) {
        if (deviceId == null || parentId == null) {
            String errorMessage = "One or more parameters are empty.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        String updatedParent;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        try {
            updatedParent = dops.updateDeviceOrganizationParent(deviceId,parentId);
            if (updatedParent == null) {
                String msg = "Parent not updated";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(updatedParent).build();
        } catch (DeviceOrganizationException e) {
            String msg = "Error occurred while updating parent of Device with ID: " + deviceId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
