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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.EventBeanWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Attribute;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AttributeType;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventAttributeList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceAgentService;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@Path("/device/agent")
public class DeviceAgentServiceImpl implements DeviceAgentService {
    private static final Log log = LogFactory.getLog(DeviceAgentServiceImpl.class);

    @POST
    @Path("/enroll")
    @Override
    public Response enrollDevice(@Valid Device device) {
        if (device == null) {
            String errorMessage = "The payload of the device enrollment is incorrect.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        try {
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            if (device.getType() == null || device.getDeviceIdentifier() == null) {
                String errorMessage = "The payload of the device enrollment is incorrect.";
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            Device existingDevice = dms.getDevice(new DeviceIdentifier(device.getType(), device.getType()));
            if (existingDevice != null && existingDevice.getEnrolmentInfo() != null && existingDevice
                    .getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                String errorMessage = "An active enrolment exists";
                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
            }
            device.getEnrolmentInfo().setOwner(DeviceMgtAPIUtils.getAuthenticatedUser());
            device.getEnrolmentInfo().setDateOfEnrolment(System.currentTimeMillis());
            device.getEnrolmentInfo().setDateOfLastUpdate(System.currentTimeMillis());
            boolean status = dms.enrollDevice(device);
            return Response.status(Response.Status.OK).entity(status).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling the device, which carries the id '" +
                    device.getDeviceIdentifier() + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/enroll/{type}/{id}")
    @Override
    public Response disEnrollDevice(@PathParam("type") String type, @PathParam("id") String id) {
        boolean result;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
        try {
            result = DeviceMgtAPIUtils.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (result) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).entity(type + " device that carries id '" + id +
                                                                          "' has not been dis-enrolled").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling the device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/enroll/{type}/{id}")
    @Override
    public Response updateDevice(@PathParam("type") String type, @PathParam("id") String id, @Valid Device updateDevice) {
        Device device;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(id);
        deviceIdentifier.setType(type);
        try {
            device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting enrollment details of the Android device that carries the id '" +
                    id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }

        if (updateDevice == null) {
            String errorMessage = "The payload of the device enrollment is incorrect.";
            log.error(errorMessage);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        if (device == null) {
            String errorMessage = "The device to be modified doesn't exist.";
            log.error(errorMessage);
            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
        }
        if (device.getEnrolmentInfo().getStatus() == EnrolmentInfo.Status.ACTIVE ) {
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
            boolean status;
            try {
                status = deviceAccessAuthorizationService.isUserAuthorized(new DeviceIdentifier(id, type));
            } catch (DeviceAccessAuthorizationException e) {
                String msg = "Error occurred while modifying enrollment of the Android device that carries the id '" +
                        id + "'";
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            if (!status) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        if(updateDevice.getEnrolmentInfo() != null) {
            device.getEnrolmentInfo().setDateOfLastUpdate(System.currentTimeMillis());
            device.setEnrolmentInfo(device.getEnrolmentInfo());
        }
        device.getEnrolmentInfo().setOwner(DeviceMgtAPIUtils.getAuthenticatedUser());
        if(updateDevice.getDeviceInfo() != null) {
            device.setDeviceInfo(updateDevice.getDeviceInfo());
        }
        device.setDeviceIdentifier(id);
        if(updateDevice.getDescription() != null) {
            device.setDescription(updateDevice.getDescription());
        }
        if(updateDevice.getName() != null) {
            device.setName(updateDevice.getName());
        }
        if(updateDevice.getFeatures() != null) {
            device.setFeatures(updateDevice.getFeatures());
        }
        if(updateDevice.getProperties() != null) {
            device.setProperties(updateDevice.getProperties());
        }
        boolean result;
        try {
            device.setType(type);
            result = DeviceMgtAPIUtils.getDeviceManagementService().modifyEnrollment(device);
            if (result) {
                return Response.status(Response.Status.ACCEPTED).build();
            } else {
                return Response.status(Response.Status.NOT_MODIFIED).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while modifying enrollment of the Android device that carries the id '" +
                    id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/events/publish/{type}/{deviceId}")
    @Override
    public Response publishEvents(@Valid EventBeanWrapper eventBeanWrapper, @PathParam("type") String type
            , @PathParam("deviceId") String deviceId) {


        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (eventBeanWrapper == null) {
                String msg = "invalid payload structure";
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            } else {
                boolean authorized = DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized
                        (new DeviceIdentifier(type, deviceId));
                if (!authorized) {
                    String msg = "does not have permission to access the device.";
                    return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
                }
            }
            Object metaData[] = new Object[1];
            metaData[0] = deviceId;
            EventAttributeList eventAttributeList = DeviceMgtAPIUtils.getDynamicEventCache().get(type);
            if (eventAttributeList == null) {
                String streamName = DeviceMgtAPIUtils.getStreamDefinition(type, tenantDomain);
                EventStreamAdminServiceStub eventStreamAdminServiceStub =
                        DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
                EventStreamDefinitionDto eventStreamDefinitionDto = eventStreamAdminServiceStub.getStreamDefinitionDto(
                        streamName + ":" + Constants.DEFAULT_STREAM_VERSION);
                if (eventStreamDefinitionDto == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                } else {
                    EventStreamAttributeDto[] eventStreamAttributeDtos = eventStreamDefinitionDto.getPayloadData();
                    List<Attribute> attributes = new ArrayList<>();
                    for (EventStreamAttributeDto eventStreamAttributeDto : eventStreamAttributeDtos) {
                        attributes.add(new Attribute(eventStreamAttributeDto.getAttributeName()
                                , AttributeType.valueOf(eventStreamAttributeDto.getAttributeType().toUpperCase())));

                    }
                    if (eventBeanWrapper.getPayloadData().length != attributes.size()) {
                        String msg = "payload does not match the stream definition";
                        return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                    }
                    eventAttributeList = new EventAttributeList();
                    eventAttributeList.setList(attributes);
                    DeviceMgtAPIUtils.getDynamicEventCache().put(type, eventAttributeList);
                }
            }
            Object[] payload = eventBeanWrapper.getPayloadData();
            int i = 0;
            for (Attribute attribute : eventAttributeList.getList()) {
                if (attribute.getType() == AttributeType.INT) {
                    payload[i] = ((Double) payload[i]).intValue();
                } else if (attribute.getType() == AttributeType.LONG) {
                    payload[i] = ((Double) payload[i]).longValue();
                }
                i++;
            }
            eventBeanWrapper.setPayloadData(payload);

            if (DeviceMgtAPIUtils.getEventPublisherService().publishEvent(DeviceMgtAPIUtils.getStreamDefinition(type
                    , PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())
                    , Constants.DEFAULT_STREAM_VERSION, metaData
                    , null, eventBeanWrapper.getPayloadData())) {
                return Response.status(Response.Status.OK).build();
            } else {
                String msg = "Error occurred while publishing the event.";
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (DataPublisherConfigurationException e) {
            String msg = "Error occurred while publishing the event.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred when checking for authorization";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (AxisFault e) {
            log.error("failed to retrieve event definitions for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RemoteException e) {
            log.error("Failed to connect with the remote services:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (JWTClientException e) {
            log.error("Failed to generate jwt token for tenantDomain:" + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserStoreException e) {
            log.error("Failed to connect with the user store, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        }
    }

    @GET
    @Path("/pending/operations/{type}/{id}")
    public Response getPendingOperations(@PathParam("type") String type, @PathParam("id") String deviceId) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device identifier list is empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            if (!DeviceMgtAPIUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
            }
            List<? extends Operation> operations = DeviceMgtAPIUtils.getDeviceManagementService().getPendingOperations(
                    deviceIdentifier);
            OperationList operationsList = new OperationList();
            operationsList.setList(operations);
            operationsList.setCount(operations.size());
            return Response.status(Response.Status.OK).entity(operationsList).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @GET
    @Path("/last-pending/operation/{type}/{id}")
    public Response getNextPendingOperation(@PathParam("type") String type, @PathParam("id") String deviceId) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device identifier list is empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            if (!DeviceMgtAPIUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
            }
            Operation operation = DeviceMgtAPIUtils.getDeviceManagementService().getNextPendingOperation(
                    deviceIdentifier);
            return Response.status(Response.Status.OK).entity(operation).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @PUT
    @Path("/operations/{type}/{id}")
    public Response updateOperation(@PathParam("type") String type, @PathParam("id") String deviceId, @Valid Operation operation) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device identifier list is empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (operation == null) {
                String errorMessage = "Device identifier list is empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            if (!DeviceMgtAPIUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
            }
            DeviceMgtAPIUtils.getDeviceManagementService().updateOperation
                    (deviceIdentifier, operation);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @GET
    @Path("/status/operations/{type}/{id}")
    public Response getOperationsByDeviceAndStatus(@PathParam("type") String type, @PathParam("id") String deviceId,
                                                   @QueryParam("status")Operation.Status status) {
        if (status == null) {
            String errorMessage = "status is empty";
            log.error(errorMessage);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device identifier list is empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            List<? extends Operation> operations = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getOperationsByDeviceAndStatus(new DeviceIdentifier(deviceId, type), status);
            OperationList operationsList = new OperationList();
            operationsList.setList(operations);
            operationsList.setCount(operations.size());
            return Response.status(Response.Status.OK).entity(operationsList).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }
}
