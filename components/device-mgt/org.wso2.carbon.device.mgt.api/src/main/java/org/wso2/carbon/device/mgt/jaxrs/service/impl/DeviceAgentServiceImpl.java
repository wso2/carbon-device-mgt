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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
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
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

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
import java.util.Map;

@Path("/device/agent")
public class DeviceAgentServiceImpl implements DeviceAgentService {
    private static final Log log = LogFactory.getLog(DeviceAgentServiceImpl.class);
    private static final String POLICY_MONITOR = "POLICY_MONITOR";
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
            Device existingDevice = dms.getDevice(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
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
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
            String msg = "Error occurred while getting enrollment details of the device that carries the id '" +
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
    public Response publishEvents(@Valid Map<String, Object> payload, @PathParam("type") String type
            , @PathParam("deviceId") String deviceId) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            if (payload == null) {
                String msg = "invalid payload structure";
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            } else {
                boolean authorized = DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized
                        (new DeviceIdentifier(type, deviceId));
                if (!authorized) {
                    String msg = "Does not have permission to access the device.";
                    return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
                }
            }
            Object metaData[] = new Object[1];
            metaData[0] = deviceId;
            EventAttributeList eventAttributeList = DeviceMgtAPIUtils.getDynamicEventCache().get(type);
            if (eventAttributeList == null) {
                String streamName = DeviceMgtAPIUtils.getStreamDefinition(type, tenantDomain);
                eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
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
                    if (payload.size() != attributes.size()) {
                        String msg = "Payload does not match with the stream definition";
                        return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                    }
                    eventAttributeList = new EventAttributeList();
                    eventAttributeList.setList(attributes);
                    DeviceMgtAPIUtils.getDynamicEventCache().put(type, eventAttributeList);
                }
            }
            int i = 0;
            Object[] payloadData = new Object[eventAttributeList.getList().size()];
            for (Attribute attribute : eventAttributeList.getList()) {
                if (attribute.getType() == AttributeType.INT) {
                    payloadData[i] = ((Double) payload.get(attribute.getName())).intValue();
                } else if (attribute.getType() == AttributeType.LONG) {
                    payloadData[i] = ((Double) payload.get(attribute.getName())).longValue();
                } else {
                    payloadData[i] = payload.get(attribute.getName());
                }
                i++;
            }

            if (DeviceMgtAPIUtils.getEventPublisherService().publishEvent(DeviceMgtAPIUtils.getStreamDefinition(type
                    , PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())
                    , Constants.DEFAULT_STREAM_VERSION, metaData
                    , null, payloadData)) {
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
            log.error("Failed to retrieve event definitions for tenantDomain:" + tenantDomain, e);
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
        } finally {
            if (eventStreamAdminServiceStub != null) {
                try {
                    eventStreamAdminServiceStub.cleanup();
                } catch (AxisFault axisFault) {
                    log.warn("Failed to clean eventStreamAdminServiceStub");
                }
            }
        }
    }

    @POST
    @Path("/events/publish/data/{type}/{deviceId}")
    @Override
    public Response publishEvents(@Valid List<Object> payload, @PathParam("type") String type
            , @PathParam("deviceId") String deviceId) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            if (payload == null) {
                String msg = "Invalid payload structure";
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            } else {
                boolean authorized = DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized
                        (new DeviceIdentifier(type, deviceId));
                if (!authorized) {
                    String msg = "Does not have permission to access the device.";
                    return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
                }
            }
            Object metaData[] = new Object[1];
            metaData[0] = deviceId;
            EventAttributeList eventAttributeList = DeviceMgtAPIUtils.getDynamicEventCache().get(type);
            if (eventAttributeList == null) {
                String streamName = DeviceMgtAPIUtils.getStreamDefinition(type, tenantDomain);
                eventStreamAdminServiceStub = DeviceMgtAPIUtils.getEventStreamAdminServiceStub();
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
                    if (payload.size() != attributes.size()) {
                        String msg = "Payload does not match with the stream definition";
                        return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                    }
                    eventAttributeList = new EventAttributeList();
                    eventAttributeList.setList(attributes);
                    DeviceMgtAPIUtils.getDynamicEventCache().put(type, eventAttributeList);
                }
            }
            int i = 0;
            Object[] payloadData = new Object[eventAttributeList.getList().size()];
            for (Attribute attribute : eventAttributeList.getList()) {
                if (attribute.getType() == AttributeType.INT) {
                    payloadData[i] = ((Double) payload.get(i)).intValue();
                } else if (attribute.getType() == AttributeType.LONG) {
                    payloadData[i] = ((Double) payload.get(i)).longValue();
                } else {
                    payloadData[i] = payload.get(i);
                }
                i++;
            }

            if (DeviceMgtAPIUtils.getEventPublisherService().publishEvent(DeviceMgtAPIUtils.getStreamDefinition(type
                    , PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())
                    , Constants.DEFAULT_STREAM_VERSION, metaData
                    , null, payloadData)) {
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
            log.error("Failed to retrieve event definitions for tenantDomain:" + tenantDomain, e);
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
        } finally {
            if (eventStreamAdminServiceStub != null) {
                try {
                    eventStreamAdminServiceStub.cleanup();
                } catch (AxisFault axisFault) {
                    log.warn("Failed to clean eventStreamAdminServiceStub");
                }
            }
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }

    @GET
    @Path("/next-pending/operation/{type}/{id}")
    public Response getNextPendingOperation(@PathParam("type") String type, @PathParam("id") String deviceId) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device type is invalid";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            if (!DeviceMgtAPIUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            Operation operation = DeviceMgtAPIUtils.getDeviceManagementService().getNextPendingOperation(
                    deviceIdentifier);
            return Response.status(Response.Status.OK).entity(operation).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }

    @PUT
    @Path("/operations/{type}/{id}")
    public Response updateOperation(@PathParam("type") String type, @PathParam("id") String deviceId, @Valid Operation operation) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device type is invalid";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (operation == null) {
                String errorMessage = "Operation cannot empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, type);
            if (!DeviceMgtAPIUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + deviceId + "'";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            if (!Operation.Status.ERROR.equals(operation.getStatus()) && operation.getCode() != null &&
                    POLICY_MONITOR.equals(operation.getCode())) {
                if (log.isDebugEnabled()) {
                    log.info("Received compliance status from POLICY_MONITOR operation ID: " + operation.getId());
                }
                List<ComplianceFeature> features = getComplianceFeatures(operation.getPayLoad());
                DeviceMgtAPIUtils.getPolicyManagementService().checkCompliance(deviceIdentifier, features);

            } else {
                DeviceMgtAPIUtils.getDeviceManagementService().updateOperation(deviceIdentifier, operation);
            }
            return Response.status(Response.Status.OK).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } catch (PolicyComplianceException e) {
            String errorMessage = "Issue in retrieving deivce management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }

    @GET
    @Path("/status/operations/{type}/{id}")
    public Response getOperationsByDeviceAndStatus(@PathParam("type") String type, @PathParam("id") String deviceId,
                                                   @QueryParam("status") Operation.Status status) {
        if (status == null) {
            String errorMessage = "Status is empty";
            log.error(errorMessage);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Invalid Device Type";
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }

    private static List<ComplianceFeature> getComplianceFeatures(Object compliancePayload) throws
                                                                                           PolicyComplianceException {
        String compliancePayloadString = new Gson().toJson(compliancePayload);
        if (compliancePayload == null) {
            return null;
        }
        // Parsing json string to get compliance features.
        JsonElement jsonElement = new JsonParser().parse(compliancePayloadString);

        JsonArray jsonArray = jsonElement.getAsJsonArray();
        Gson gson = new Gson();
        ComplianceFeature complianceFeature;
        List<ComplianceFeature> complianceFeatures = new ArrayList<ComplianceFeature>(jsonArray.size());

        for (JsonElement element : jsonArray) {
            complianceFeature = gson.fromJson(element, ComplianceFeature.class);
            complianceFeatures.add(complianceFeature);
        }
        return complianceFeatures;
    }
}
