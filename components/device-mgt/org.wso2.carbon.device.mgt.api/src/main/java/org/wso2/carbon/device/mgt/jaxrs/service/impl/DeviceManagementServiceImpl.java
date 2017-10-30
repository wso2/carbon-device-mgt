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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.DataAccessLayerException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidFeatureCodeValueException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidPotentialVulnerabilityValueException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidResultCountValueException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidStartIndexValueException;
import org.wso2.carbon.device.mgt.analytics.dashboard.util.APIUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardPaginationGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceCompliance;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private static final Log log = LogFactory.getLog(DeviceManagementServiceImpl.class);

    @GET
    @Override
    public Response getDevices(
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("user") String user,
            @QueryParam("role") String role,
            @QueryParam("ownership") String ownership,
            @QueryParam("status") String status,
            @QueryParam("groupId") int groupId,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("model") String model) {
        try {
            if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(role)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Request contains both name and role " +
                                "parameters. Only one is allowed " +
                                "at once.").build()).build();
            }
//            RequestValidationUtil.validateSelectionCriteria(type, user, roleName, ownership, status);
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
            if (deviceAccessAuthorizationService == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Device access authorization service is " +
                                "failed").build()).build();
            }
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();
            if(model == null || model.isEmpty())
            	model="all";

            if (name != null && !name.isEmpty()) {
                request.setDeviceName(name);
            }
            if (type != null && !type.isEmpty()) {
                request.setDeviceType(type);
            }
            if (user != null && !user.isEmpty()) {
                request.setOwner(user);
            }
            if (ownership != null && !ownership.isEmpty()) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (status != null && !status.isEmpty()) {
                RequestValidationUtil.validateStatus(status);
                request.setStatus(status);
            }
            if (groupId != 0) {
                request.setGroupId(groupId);
            }
            if (role != null && !role.isEmpty()) {
                request.setOwnerRole(role);
            }
            if(model != null && !model.isEmpty()) {
            	request.setModel(model);
            }

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(CarbonContext.getThreadLocalCarbonContext().getUsername());

            // check whether the user is device-mgt admin
            if (deviceAccessAuthorizationService.isDeviceAdminUser()) {
                if (user != null && !user.isEmpty()) {
                    request.setOwner(MultitenantUtils.getTenantAwareUsername(user));
                }
            } else {
                if (user != null && !user.isEmpty()) {
                    user = MultitenantUtils.getTenantAwareUsername(user);
                    if (user.equals(authorizedUser)) {
                        request.setOwner(user);
                    } else {
                        String msg = "User '" + authorizedUser + "' is not authorized to retrieve devices of '" + user
                                + "' user";
                        log.error(msg);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(
                                new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(msg).build()).build();
                    }
                } else {
                    request.setOwner(authorizedUser);
                }
            }

            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
                request.setSince(sinceDate);
                result = dms.getAllDevices(request);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else if (since != null && !since.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(since);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'since' filter").build()).build();
                }
                request.setSince(sinceDate);
                result = dms.getAllDevices(request);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    devices.setList(new ArrayList<Device>());
                    devices.setCount(0);
                    return Response.status(Response.Status.OK).entity(devices).build();
                }
            } else {
                result = dms.getAllDevices(request);
                int resultCount = result.getRecordsTotal();
                if (resultCount == 0) {
                    Response.status(Response.Status.OK).entity(devices).build();
                }
            }

            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking device access authorization";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Path("/user-devices")
    public Response getDeviceByUser(@QueryParam("offset") int offset,
                                    @QueryParam("limit") int limit) {

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        PaginationResult result;
        DeviceList devices = new DeviceList();

        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        request.setOwner(currentUser);

        try {
            result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(request);
            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Override
    @Path("/type/{device-type}/id/{device-id}")
    public Response deleteDevice(@PathParam("device-type") String deviceType, @PathParam("device-id") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            boolean response = deviceManagementProviderService.disenrollDevice(deviceIdentifier);
            return Response.status(Response.Status.OK).entity(response).build();

        } catch (DeviceManagementException e) {
            String msg = "Error encountered while deleting device of type : " + deviceType + " and " +
                    "ID : " + deviceId;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()
            ).build();
        }
    }

    @POST
    @Override
    @Path("/type/{device-type}/id/{device-id}/rename")
    public Response renameDevice(Device device, @PathParam("device-type") String deviceType,
                                 @PathParam("device-id") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Device persistedDevice = deviceManagementProviderService.getDevice(new DeviceIdentifier
                    (deviceId, deviceType));
            persistedDevice.setName(device.getName());
            boolean response = deviceManagementProviderService.modifyEnrollment(persistedDevice);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (DeviceManagementException e) {
            log.error("Error encountered while updating device of type : " + deviceType + " and " +
                    "ID : " + deviceId);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Error while updating " +
                            "device of type " + deviceType + " and ID : " + deviceId).build()).build();
        }
    }

    @GET
    @Path("/{type}/{id}")
    @Override
    public Response getDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Device device = null;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            // check whether the user is authorized
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" + id;
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(msg).build()).build();
            }

            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
                device = dms.getDevice(new DeviceIdentifier(id, type), sinceDate);
                if (device == null) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else {
                device = dms.getDevice(new DeviceIdentifier(id, type));
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking the device authorization.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Requested device of type '" +
                            type + "', which carries id '" + id + "' does not exist").build()).build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @GET
    @Path("/{type}/{id}/location")
    @Override
    public Response getDeviceLocation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
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
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocation).build();

    }

    @GET
    @Path("/{type}/{id}/features")
    @Override
    public Response getFeaturesOfDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features;
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm = dms.getFeatureManager(type);
            if (fm == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No feature manager is " +
                                "registered with the given type '" + type + "'").build()).build();
            }
            features = fm.getFeatures();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(features).build();
    }

    @POST
    @Path("/search-devices")
    @Override
    public Response searchDevices(@QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit, SearchContext searchContext) {
        SearchManagerService searchManagerService;
        List<Device> devices;
        DeviceList deviceList = new DeviceList();
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.search(searchContext);
        } catch (SearchMgtException e) {
            String msg = "Error occurred while searching for devices that matches the provided selection criteria";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        deviceList.setList(devices);
        deviceList.setCount(devices.size());
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @GET
    @Path("/{type}/{id}/applications")
    @Override
    public Response getInstalledApplications(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        List<Application> applications;
        //ApplicationList appList;
        ApplicationManagementProviderService amc;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            amc = DeviceMgtAPIUtils.getAppManagementService();
            applications = amc.getApplicationListForDevice(new DeviceIdentifier(id, type));

            //TODO: return app list
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(applications).build();
    }

    @GET
    @Path("/{type}/{id}/operations")
    @Override
    public Response getDeviceOperations(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("owner") String owner) {
        OperationList operationsList = new OperationList();
        RequestValidationUtil.validateOwnerParameter(owner);
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        request.setOwner(owner);
        PaginationResult result;
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            result = dms.getOperations(new DeviceIdentifier(id, type), request);

            operationsList.setList((List<? extends Operation>) result.getData());
            operationsList.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(operationsList).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/{type}/{id}/effective-policy")
    @Override
    public Response getEffectivePolicyOfDevice(@PathParam("type") @Size(max = 45) String type,
                                               @PathParam("id") @Size(max = 45) String id,
                                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            Policy policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));

            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("{type}/{id}/compliance-data")
    public Response getComplianceDataOfDevice(@PathParam("type") @Size(max = 45) String type,
                                              @PathParam("id") @Size(max = 45) String id) {

        RequestValidationUtil.validateDeviceIdentifier(type, id);
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        Policy policy;
        NonComplianceData complianceData = null;
        DeviceCompliance deviceCompliance = new DeviceCompliance();

        try {
            policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        if (policy == null) {
            deviceCompliance.setDeviceID(id);
            deviceCompliance.setComplianceData(null);
            return Response.status(Response.Status.OK).entity(deviceCompliance).build();
        } else {
            try {
                policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
                complianceData = policyManagementService.getDeviceCompliance(
                        new DeviceIdentifier(id, type));
                deviceCompliance.setDeviceID(id);
                deviceCompliance.setComplianceData(complianceData);
                return Response.status(Response.Status.OK).entity(deviceCompliance).build();
            } catch (PolicyComplianceException e) {
                String error = "Error occurred while getting the compliance data.";
                log.error(error, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(error).build()).build();
            }
        }
    }

    /**
     * Change device status.
     *
     * @param type Device type
     * @param id Device id
     * @param newsStatus Device new status
     * @return {@link Response} object
     */
    @PUT
    @Path("/{type}/{id}/changestatus")
    public Response changeDeviceStatus(@PathParam("type") @Size(max = 45) String type,
                                       @PathParam("id") @Size(max = 45) String id,
                                       @QueryParam("newStatus") EnrolmentInfo.Status newsStatus) {
        RequestValidationUtil.validateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            boolean response = deviceManagementProviderService.changeDeviceStatus(deviceIdentifier, newsStatus);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while changing device status of type : " + type + " and " +
                    "device id : " + id;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
@Override
	 @GET
	 @Path("/dashboard/device-count-overview")
	public Response getOverviewDeviceCounts() {
		 GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
		    
		    DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();
		    DeviceCountByGroup totalDeviceCount;
		    try
		    {
		      totalDeviceCount = gadgetDataService.getTotalDeviceCount(APIUtil.getAuthenticatedUser());
		    }
		    catch (DataAccessLayerException e)
		    {
		      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve total device count.", e);
		      
		      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
		    }
		    List<DeviceCountByGroup> totalDeviceCountInListEntry = new ArrayList();
		    totalDeviceCountInListEntry.add(totalDeviceCount);
		    
		    dashboardGadgetDataWrapper1.setContext("Total-device-count");
		    dashboardGadgetDataWrapper1.setGroupingAttribute(null);
		    dashboardGadgetDataWrapper1.setData(totalDeviceCountInListEntry);
		    List<DeviceCountByGroup> deviceCountsByConnectivityStatuses;
		    try
		    {
		      deviceCountsByConnectivityStatuses = gadgetDataService.getDeviceCountsByConnectivityStatuses(APIUtil.getAuthenticatedUser());
		    }
		    catch (DataAccessLayerException e)
		    {
		      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve device counts by connectivity statuses.", e);
		      
		      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
		    }
		    DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();
		    
		    dashboardGadgetDataWrapper2.setContext("Device-counts-by-connectivity-statuses");
		    dashboardGadgetDataWrapper2.setGroupingAttribute("connectivity-status");
		    dashboardGadgetDataWrapper2.setData(deviceCountsByConnectivityStatuses);
		    
		    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
		    responsePayload.add(dashboardGadgetDataWrapper1);
		    responsePayload.add(dashboardGadgetDataWrapper2);
		    
		    return Response.status(200).entity(responsePayload).build();
	}
	 @GET
	  @Path("/dashboard/device-counts-by-potential-vulnerabilities")
	  public Response getDeviceCountsByPotentialVulnerabilities()
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    List<DeviceCountByGroup> deviceCountsByPotentialVulnerabilities;
	    try
	    {
	      deviceCountsByPotentialVulnerabilities = gadgetDataService.getDeviceCountsByPotentialVulnerabilities(APIUtil.getAuthenticatedUser());
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve device counts by potential vulnerabilities.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper.setContext("Device-counts-by-potential-vulnerabilities");
	    dashboardGadgetDataWrapper.setGroupingAttribute("potential-vulnerability");
	    dashboardGadgetDataWrapper.setData(deviceCountsByPotentialVulnerabilities);
	    
	    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardGadgetDataWrapper);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/non-compliant-device-counts-by-features")
	  public Response getNonCompliantDeviceCountsByFeatures(@QueryParam("start") int startIndex, @QueryParam("length") int resultCount)
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    
	    DashboardPaginationGadgetDataWrapper dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();
	    PaginationResult paginationResult;
	    try
	    {
	      paginationResult = gadgetDataService.getNonCompliantDeviceCountsByFeatures(startIndex, resultCount,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidStartIndexValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a non-compliant set of device counts by features.", e);
	      
	      return Response.status(400).entity("Received an invalid value for query parameter : start, Should not be lesser than 0.").build();
	    }
	    catch (InvalidResultCountValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a non-compliant set of device counts by features.", e);
	      
	      return Response.status(400).entity("Received an invalid value for query parameter : length, Should not be lesser than 5.").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a non-compliant set of device counts by features.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    dashboardPaginationGadgetDataWrapper.setContext("Non-compliant-device-counts-by-features");
	    dashboardPaginationGadgetDataWrapper.setGroupingAttribute("non-compliant-feature-code");
	    dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
	    dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());
	    
	    List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardPaginationGadgetDataWrapper);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/device-counts-by-groups")
	  public Response getDeviceCountsByGroups(@QueryParam("connectivity-status") String connectivityStatus, @QueryParam("potential-vulnerability") String potentialVulnerability, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership)
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    
	    ExtendedFilterSet filterSet = new ExtendedFilterSet();
	    filterSet.setConnectivityStatus(connectivityStatus);
	    filterSet.setPotentialVulnerability(potentialVulnerability);
	    filterSet.setPlatform(platform);
	    filterSet.setOwnership(ownership);
	    List<DeviceCountByGroup> deviceCountsByPlatforms;
	    try
	    {
	      deviceCountsByPlatforms = gadgetDataService.getDeviceCountsByPlatforms(filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidPotentialVulnerabilityValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of device counts by platforms.", e);
	      
	      return Response.status(400).entity("Received an invalid value for query parameter : potential-vulnerability, Should be either NON_COMPLIANT or UNMONITORED.").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of device counts by platforms.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper1.setContext("Device-counts-by-platforms");
	    dashboardGadgetDataWrapper1.setGroupingAttribute("platform");
	    dashboardGadgetDataWrapper1.setData(deviceCountsByPlatforms);
	    List<DeviceCountByGroup> deviceCountsByOwnerships;
	    try
	    {
	      deviceCountsByOwnerships = gadgetDataService.getDeviceCountsByOwnershipTypes(filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidPotentialVulnerabilityValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of device counts by ownerships.", e);
	      
	      return Response.status(400).entity("Received an invalid value for query parameter : potential-vulnerability, Should be either NON_COMPLIANT or UNMONITORED.").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of device counts by ownerships.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper2.setContext("Device-counts-by-ownerships");
	    dashboardGadgetDataWrapper2.setGroupingAttribute("ownership");
	    dashboardGadgetDataWrapper2.setData(deviceCountsByOwnerships);
	    
	    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardGadgetDataWrapper1);
	    responsePayload.add(dashboardGadgetDataWrapper2);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/feature-non-compliant-device-counts-by-groups")
	  public Response getFeatureNonCompliantDeviceCountsByGroups(@QueryParam("non-compliant-feature-code") String nonCompliantFeatureCode, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership)
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    
	    BasicFilterSet filterSet = new BasicFilterSet();
	    filterSet.setPlatform(platform);
	    filterSet.setOwnership(ownership);
	    List<DeviceCountByGroup> featureNonCompliantDeviceCountsByPlatforms;
	    try
	    {
	      featureNonCompliantDeviceCountsByPlatforms = gadgetDataService.getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidFeatureCodeValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant device counts by platforms.", e);
	      
	      return Response.status(400).entity("Missing required query parameter : non-compliant-feature-code").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant device counts by platforms.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper1.setContext("Feature-non-compliant-device-counts-by-platforms");
	    dashboardGadgetDataWrapper1.setGroupingAttribute("platform");
	    dashboardGadgetDataWrapper1.setData(featureNonCompliantDeviceCountsByPlatforms);
	    List<DeviceCountByGroup> featureNonCompliantDeviceCountsByOwnerships;
	    try
	    {
	      featureNonCompliantDeviceCountsByOwnerships = gadgetDataService.getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidFeatureCodeValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant device counts by ownerships.", e);
	      
	      return Response.status(400).entity("Missing required query parameter : non-compliant-feature-code").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant device counts by ownerships.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper2.setContext("Feature-non-compliant-device-counts-by-ownerships");
	    dashboardGadgetDataWrapper2.setGroupingAttribute("ownership");
	    dashboardGadgetDataWrapper2.setData(featureNonCompliantDeviceCountsByOwnerships);
	    
	    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardGadgetDataWrapper1);
	    responsePayload.add(dashboardGadgetDataWrapper2);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/filtered-device-count-over-total")
	  public Response getFilteredDeviceCountOverTotal(@QueryParam("connectivity-status") String connectivityStatus, @QueryParam("potential-vulnerability") String potentialVulnerability, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership)
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    
	    ExtendedFilterSet filterSet = new ExtendedFilterSet();
	    filterSet.setConnectivityStatus(connectivityStatus);
	    filterSet.setPotentialVulnerability(potentialVulnerability);
	    filterSet.setPlatform(platform);
	    filterSet.setOwnership(ownership);
	    DeviceCountByGroup filteredDeviceCount;
	    try
	    {
	      filteredDeviceCount = gadgetDataService.getDeviceCount(filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidPotentialVulnerabilityValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered device count over the total.", e);
	      
	      return Response.status(400).entity("Received an invalid value for query parameter : potential-vulnerability, Should be either NON_COMPLIANT or UNMONITORED.").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered device count over the total.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DeviceCountByGroup totalDeviceCount;
	    try
	    {
	      totalDeviceCount = gadgetDataService.getTotalDeviceCount(APIUtil.getAuthenticatedUser());
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve the total device count over filtered.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    List<Object> filteredDeviceCountOverTotalDataWrapper = new ArrayList();
	    filteredDeviceCountOverTotalDataWrapper.add(filteredDeviceCount);
	    filteredDeviceCountOverTotalDataWrapper.add(totalDeviceCount);
	    
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper.setContext("Filtered-device-count-over-total");
	    dashboardGadgetDataWrapper.setGroupingAttribute(null);
	    dashboardGadgetDataWrapper.setData(filteredDeviceCountOverTotalDataWrapper);
	    
	    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardGadgetDataWrapper);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/feature-non-compliant-device-count-over-total")
	  public Response getFeatureNonCompliantDeviceCountOverTotal(@QueryParam("non-compliant-feature-code") String nonCompliantFeatureCode, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership)
	  {
	    GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	    
	    BasicFilterSet filterSet = new BasicFilterSet();
	    filterSet.setPlatform(platform);
	    filterSet.setOwnership(ownership);
	    DeviceCountByGroup featureNonCompliantDeviceCount;
	    try
	    {
	      featureNonCompliantDeviceCount = gadgetDataService.getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filterSet,APIUtil.getAuthenticatedUser());
	    }
	    catch (InvalidFeatureCodeValueException e)
	    {
	      log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a feature non-compliant device count over the total.", e);
	      
	      return Response.status(400).entity("Missing required query parameter : non-compliant-feature-code").build();
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a feature non-compliant device count over the total.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    DeviceCountByGroup totalDeviceCount;
	    try
	    {
	      totalDeviceCount = gadgetDataService.getTotalDeviceCount(APIUtil.getAuthenticatedUser());
	    }
	    catch (DataAccessLayerException e)
	    {
	      log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve the total device count over filtered feature non-compliant.", e);
	      
	      return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	    }
	    List<Object> featureNonCompliantDeviceCountOverTotalDataWrapper = new ArrayList();
	    featureNonCompliantDeviceCountOverTotalDataWrapper.add(featureNonCompliantDeviceCount);
	    featureNonCompliantDeviceCountOverTotalDataWrapper.add(totalDeviceCount);
	    
	    DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
	    dashboardGadgetDataWrapper.setContext("Feature-non-compliant-device-count-over-total");
	    dashboardGadgetDataWrapper.setGroupingAttribute(null);
	    dashboardGadgetDataWrapper.setData(featureNonCompliantDeviceCountOverTotalDataWrapper);
	    
	    List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	    responsePayload.add(dashboardGadgetDataWrapper);
	    
	    return Response.status(200).entity(responsePayload).build();
	  }
	  
	  @GET
	  @Path("/dashboard/devices-with-details")
	  public Response getDevicesWithDetails(@QueryParam("connectivity-status") String connectivityStatus, @QueryParam("potential-vulnerability") String potentialVulnerability, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership, @QueryParam("pagination-enabled") String paginationEnabled, @QueryParam("start") int startIndex, @QueryParam("length") int resultCount)
	  {
	    if (paginationEnabled == null)
	    {
	      log.error("Bad request on retrieving a filtered set of devices with details @ Dashboard API layer. Missing required query parameter : pagination-enabled");
	      
	      return Response.status(400).entity("Missing required query parameter : pagination-enabled").build();
	    }
	    if ("true".equals(paginationEnabled))
	    {
	      GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	      
	      ExtendedFilterSet filterSet = new ExtendedFilterSet();
	      filterSet.setConnectivityStatus(connectivityStatus);
	      filterSet.setPotentialVulnerability(potentialVulnerability);
	      filterSet.setPlatform(platform);
	      filterSet.setOwnership(ownership);
	      PaginationResult paginationResult;
	      try
	      {
	        paginationResult = gadgetDataService.getDevicesWithDetails(filterSet, startIndex, resultCount,APIUtil.getAuthenticatedUser());
	      }
	      catch (InvalidPotentialVulnerabilityValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : potential-vulnerability, Should be either NON_COMPLIANT or UNMONITORED.").build();
	      }
	      catch (InvalidStartIndexValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : start, Should not be lesser than 0.").build();
	      }
	      catch (InvalidResultCountValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : length, Should not be lesser than 5.").build();
	      }
	      catch (DataAccessLayerException e)
	      {
	        log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	      }
	      DashboardPaginationGadgetDataWrapper dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();
	      dashboardPaginationGadgetDataWrapper.setContext("Filtered-and-paginated-devices-with-details");
	      dashboardPaginationGadgetDataWrapper.setGroupingAttribute(null);
	      dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
	      dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());
	      
	      List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList();
	      responsePayload.add(dashboardPaginationGadgetDataWrapper);
	      
	      return Response.status(200).entity(responsePayload).build();
	    }
	    if ("false".equals(paginationEnabled))
	    {
	      GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	      
	      ExtendedFilterSet filterSet = new ExtendedFilterSet();
	      filterSet.setConnectivityStatus(connectivityStatus);
	      filterSet.setPotentialVulnerability(potentialVulnerability);
	      filterSet.setPlatform(platform);
	      filterSet.setOwnership(ownership);
	      List<DeviceWithDetails> devicesWithDetails;
	      try
	      {
	        devicesWithDetails = gadgetDataService.getDevicesWithDetails(filterSet,APIUtil.getAuthenticatedUser());
	      }
	      catch (InvalidPotentialVulnerabilityValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : potential-vulnerability, Should be either NON_COMPLIANT or UNMONITORED.").build();
	      }
	      catch (DataAccessLayerException e)
	      {
	        log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
	        
	        return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	      }
	      DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
	      dashboardGadgetDataWrapper.setContext("Filtered-devices-with-details");
	      dashboardGadgetDataWrapper.setGroupingAttribute(null);
	      dashboardGadgetDataWrapper.setData(devicesWithDetails);
	      
	      List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	      responsePayload.add(dashboardGadgetDataWrapper);
	      
	      return Response.status(200).entity(responsePayload).build();
	    }
	    log.error("Bad request on retrieving a filtered set of devices with details @ Dashboard API layer. Received an invalid value for query parameter : pagination-enabled, Should be either true or false.");
	    
	    return Response.status(400).entity("Received an invalid value for query parameter : pagination-enabled, Should be either true or false.").build();
	  }
	  
	  @GET
	  @Path("/dashboard/feature-non-compliant-devices-with-details")
	  public Response getFeatureNonCompliantDevicesWithDetails(@QueryParam("non-compliant-feature-code") String nonCompliantFeatureCode, @QueryParam("platform") String platform, @QueryParam("ownership") String ownership, @QueryParam("pagination-enabled") String paginationEnabled, @QueryParam("start") int startIndex, @QueryParam("length") int resultCount)
	  {
	    if (paginationEnabled == null)
	    {
	      log.error("Bad request on retrieving a filtered set of feature non-compliant devices with details @ Dashboard API layer. Missing required query parameter : pagination-enabled");
	      
	      return Response.status(400).entity("Missing required query parameter : pagination-enabled").build();
	    }
	    if ("true".equals(paginationEnabled))
	    {
	      GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	      
	      BasicFilterSet filterSet = new BasicFilterSet();
	      filterSet.setPlatform(platform);
	      filterSet.setOwnership(ownership);
	      PaginationResult paginationResult;
	      try
	      {
	        paginationResult = gadgetDataService.getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet, startIndex, resultCount,APIUtil.getAuthenticatedUser());
	      }
	      catch (InvalidFeatureCodeValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(400).entity("Missing required query parameter : non-compliant-feature-code").build();
	      }
	      catch (InvalidStartIndexValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : start, Should not be lesser than 0.").build();
	      }
	      catch (InvalidResultCountValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(400).entity("Received an invalid value for query parameter : length, Should not be lesser than 5.").build();
	      }
	      catch (DataAccessLayerException e)
	      {
	        log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	      }
	      DashboardPaginationGadgetDataWrapper dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();
	      dashboardPaginationGadgetDataWrapper.setContext("Filtered-and-paginated-feature-non-compliant-devices-with-details");
	      
	      dashboardPaginationGadgetDataWrapper.setGroupingAttribute(null);
	      dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
	      dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());
	      
	      List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList();
	      responsePayload.add(dashboardPaginationGadgetDataWrapper);
	      
	      return Response.status(200).entity(responsePayload).build();
	    }
	    if ("false".equals(paginationEnabled))
	    {
	      GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
	      
	      BasicFilterSet filterSet = new BasicFilterSet();
	      filterSet.setPlatform(platform);
	      filterSet.setOwnership(ownership);
	      List<DeviceWithDetails> featureNonCompliantDevicesWithDetails;
	      try
	      {
	        featureNonCompliantDevicesWithDetails = gadgetDataService.getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet,APIUtil.getAuthenticatedUser());
	      }
	      catch (InvalidFeatureCodeValueException e)
	      {
	        log.error("Bad request and error occurred @ Gadget Data Service layer due to invalid (query) parameter value. This was while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(400).entity("Missing required query parameter : non-compliant-feature-code").build();
	      }
	      catch (DataAccessLayerException e)
	      {
	        log.error("An internal error occurred while trying to execute relevant data service function @ Dashboard API layer to retrieve a filtered set of feature non-compliant devices with details.", e);
	        
	        return Response.status(500).entity("ErrorResponse in retrieving requested data.").build();
	      }
	      DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
	      dashboardGadgetDataWrapper.setContext("Filtered-feature-non-compliant-devices-with-details");
	      dashboardGadgetDataWrapper.setGroupingAttribute(null);
	      dashboardGadgetDataWrapper.setData(featureNonCompliantDevicesWithDetails);
	      
	      List<DashboardGadgetDataWrapper> responsePayload = new ArrayList();
	      responsePayload.add(dashboardGadgetDataWrapper);
	      
	      return Response.status(200).entity(responsePayload).build();
	    }
	    log.error("Bad request on retrieving a filtered set of feature non-compliant devices with details @ Dashboard API layer. Received an invalid value for query parameter : pagination-enabled, Should be either true or false.");
	    
	    return Response.status(400).entity("Received an invalid value for query parameter : pagination-enabled, Should be either true or false.").build();
	  }
	  @POST
	    @Path("/find-devices")
	@Override
	public Response findDevices( SearchContext searchContext) {
		// TODO Auto-generated method stub
		 SearchManagerService searchManagerService;
	        List<Device> devices=null;
	        DeviceList deviceList = new DeviceList();
	        searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
			try {
				devices = searchManagerService.getPolicyDevice(searchContext.getRoles(),searchContext.getUsers(),searchContext.getGroups());
			} catch (SearchMgtException e) {
				// TODO Auto-generated catch block
				 String msg = "Error occurred while searching for devices that matches the provided selection criteria";
		            log.error(msg, e);
		            return Response.serverError().entity(
		                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
			}
	        deviceList.setList(devices);
	        deviceList.setCount(devices.size());
	        return Response.status(Response.Status.OK).entity(deviceList).build();
		
	} 
}
