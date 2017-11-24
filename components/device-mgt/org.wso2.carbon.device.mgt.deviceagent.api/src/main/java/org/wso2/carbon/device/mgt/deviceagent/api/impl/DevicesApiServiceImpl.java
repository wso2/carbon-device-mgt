package org.wso2.carbon.device.mgt.deviceagent.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.device.mgt.deviceagent.api.ApiResponseMessage;
import org.wso2.carbon.device.mgt.deviceagent.api.DevicesApiService;
import org.wso2.carbon.device.mgt.deviceagent.api.NotFoundException;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Device;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.Operation;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class DevicesApiServiceImpl extends DevicesApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevicesApiServiceImpl.class);

    @Override
    public Response devicesEnrollTypeDeviceIdDelete(Device device
            , String deviceId
            , String type
            , Request request) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
//        if (device == null || type == null || !type.equals(device.getType()) || deviceId == null ||
//                !deviceId.equals(device.getDeviceIdentifier())) {
//            String errorMessage = "The payload of the device enrollment is incorrect.";
//            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//        }
//        try {
//            DeviceAgentManager deviceAgentManager = ServiceComponent.getDeviceManagement().getDeviceAgentManager();
//            DeviceManager deviceManager = ServiceComponent.getDeviceManagement().getDeviceManager();
//            Optional<org.wso2.carbon.device.mgt.common.Device> existingDevice = deviceManager.getDevice(
//                    device.getDeviceIdentifier(), device.getType());
//            if (existingDevice.isPresent() &&
//                    existingDevice.get().getEnrolmentInfo().isPresent() &&
//                    existingDevice.get().getEnrolmentInfo().get().getStatus().equals(EnrolmentInfo.StatusEnum
// .ACTIVE)) {
//                String errorMessage = "An active enrolment exists";
//                return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//            }
//            EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
//            //TODO: Get the current logged in user
//            enrolmentInfo.setOwner("admin");
//            enrolmentInfo.setDateOfEnrolment(System.currentTimeMillis());
//            enrolmentInfo.setDateOfLastUpdate(System.currentTimeMillis());
//            boolean status = deviceAgentManager.enrollDevice(ModelMapper.map(device));
//            return Response.status(Response.Status.OK).entity(status).build();
//        } catch (DeviceManagementException e) {
//            String msg = "Error occurred while fetching the list of device types.";
//            LOGGER.error(msg, e);
//            return Response.status(e.getStatus()).entity(new ErrorResponse().message(msg).code(e.getStatus()))
// .build();
//        }
    }

    @Override
    public Response devicesEnrollTypeDeviceIdPut(Device device
            , String deviceId
            , String type
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
//        Optional<org.wso2.carbon.device.mgt.common.Device> existingDevice;
//        try {
//            existingDevice = ServiceComponent.getDeviceManagement().getDeviceManager().getDevice(deviceId, type);
//        } catch (DeviceManagementException e) {
//            String msg =
//                    "Error occurred while getting enrollment details of the " + type + " device that carries the id
// '" +
//                            deviceId + "'";
//            LOGGER.error(msg, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//        }
//        if (updateDevice == null) {
//            String errorMessage = "The payload of the device enrollment is incorrect.";
//            LOGGER.error(errorMessage);
//            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
//        }
//        if (existingDevice.isPresent()) {
//            String errorMessage = "The device to be modified doesn't exist.";
//            LOGGER.error(errorMessage);
//            return Response.status(Response.Status.NOT_FOUND).entity(errorMessage).build();
//        }
//        if (device.getEnrolmentInfo().getStatus() == EnrolmentInfo.Status.ACTIVE) {
//            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
//                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
//            boolean status;
//            try {
//                status = deviceAccessAuthorizationService.isUserAuthorized(new DeviceIdentifier(id, type));
//            } catch (DeviceAccessAuthorizationException e) {
//                String msg = "Error occurred while modifying enrollment of the Android device that carries the id '" +
//                        id + "'";
//                LOGGER.error(msg, e);
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//            }
//            if (!status) {
//                return Response.status(Response.Status.UNAUTHORIZED).build();
//            }
//        }
//        if (updateDevice.getEnrolmentInfo() != null) {
//            device.getEnrolmentInfo().setDateOfLastUpdate(System.currentTimeMillis());
//            device.setEnrolmentInfo(device.getEnrolmentInfo());
//        }
//        device.getEnrolmentInfo().setOwner(DeviceMgtAPIUtils.getAuthenticatedUser());
//        if (updateDevice.getDeviceInfo() != null) {
//            device.setDeviceInfo(updateDevice.getDeviceInfo());
//        }
//        device.setDeviceIdentifier(id);
//        if (updateDevice.getDescription() != null) {
//            device.setDescription(updateDevice.getDescription());
//        }
//        if (updateDevice.getName() != null) {
//            device.setName(updateDevice.getName());
//        }
//        if (updateDevice.getFeatures() != null) {
//            device.setFeatures(updateDevice.getFeatures());
//        }
//        if (updateDevice.getProperties() != null) {
//            device.setProperties(updateDevice.getProperties());
//        }
//        boolean result;
//        try {
//            device.setType(type);
//            result = DeviceMgtAPIUtils.getDeviceManagementService().modifyEnrollment(device);
//            if (result) {
//                return Response.status(Response.Status.ACCEPTED).build();
//            } else {
//                return Response.status(Response.Status.NOT_MODIFIED).build();
//            }
//        } catch (DeviceManagementException e) {
//            String msg = "Error occurred while modifying enrollment of the Android device that carries the id '" +
//                    id + "'";
//            LOGGER.error(msg, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//        }
    }

    @Override
    public Response devicesEnrollTypePost(Device device
            , String type
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response devicesEventsPublishDataTypeDeviceIdPost(Device device
            , String type
            , String deviceId
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response devicesEventsPublishTypeDeviceIdPost(Device device
            , String deviceId
            , String type
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response devicesOperationsTypeDeviceIdGet(String type
            , String deviceId
            , String status
            , Integer limit
            , String offset
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response devicesOperationsTypeDeviceIdOperationIdGet(String type
            , String deviceId
            , Integer operationId
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response devicesOperationsTypeDeviceIdOperationIdPut(Operation operation
            , String type
            , String deviceId
            , String operationId
            , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
