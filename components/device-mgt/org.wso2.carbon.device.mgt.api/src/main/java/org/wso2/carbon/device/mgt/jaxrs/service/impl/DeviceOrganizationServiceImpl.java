package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceOrganizationProviderServiceImpl;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceOrganizationService;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
        if (deviceId.equals(null) && deviceId.length() <= 0) {
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
    public Response getChildrenIdsByParentId(String parentId) {
        return null;
    }

    @GET
    @Path("/nodes")
    @Override
    public Response generateNodes() {
        List<DeviceOrganizationVisNode> result;
        DeviceOrganizationProviderService dops = new DeviceOrganizationProviderServiceImpl();
        result = dops.generateNodes();
        if (result.isEmpty()) {
            String msg = "Node generation unsuccessful";
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
            String msg = "Edge generation unsuccessful";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(result).build();
    }
}
