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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.NotFoundException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.UnexpectedServerErrorException;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
            @QueryParam("type") String type,
            @QueryParam("user") String user,
            @QueryParam("roleName") String roleName,
            @QueryParam("ownership") String ownership,
            @QueryParam("status") String status,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validateSelectionCriteria(type, user, roleName, ownership, status);

            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();

            if (type != null) {
                request.setDeviceType(type);
            }
            if (user != null) {
                request.setOwner(user);
            }
            if (ownership != null) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (status != null) {
                RequestValidationUtil.validateStatus(status);
                request.setStatus(status);
            }

            if (ifModifiedSince != null) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    throw new InputValidationException(
                            new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build());
                }
                request.setSince(sinceDate);
                result = dms.getAllDevices(request);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else if (since != null) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(since);
                } catch (ParseException e) {
                    throw new InputValidationException(
                            new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Invalid date " +
                                    "string is provided in 'since' filter").build());
                }
                request.setSince(sinceDate);
                result = dms.getAllDevices(request);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.OK).entity("No device is modified " +
                            "after the timestamp provided in 'since' filter").build();
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
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @GET
    @Path("/{type}/{id}")
    @Override
    public Response getDevice(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Device device;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            device = dms.getDevice(new DeviceIdentifier(id, type));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        if (device == null) {
            throw new NotFoundException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("Requested device of type '" +
                            type + "', which carries id '" + id + "' does not exist").build());
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @GET
    @Path("/{type}/{id}/features")
    @Override
    public Response getFeaturesOfDevice(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features;
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm = dms.getFeatureManager(type);
            if (fm == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No feature manager is " +
                                "registered with the given type '" + type + "'").build());
            }
            features = fm.getFeatures();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
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
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        if (devices == null || devices.size() == 0) {
            Response.status(Response.Status.OK).entity(deviceList);
        }

        deviceList.setList(devices);
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @GET
    @Path("/{type}/{id}/applications")
    @Override
    public Response getInstalledApplications(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        List<Application> applications;
        ApplicationManagementProviderService amc;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            amc = DeviceMgtAPIUtils.getAppManagementService();
            applications = amc.getApplicationListForDevice(new DeviceIdentifier(id, type));
            if (applications == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("It is likely that " +
                                "no applications is found upon the provided type and id").build());
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(applications).build();
    }

    @GET
    @Path("/{type}/{id}/operations")
    @Override
    public Response getDeviceOperations(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        List<? extends Operation> operations;
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            operations = dms.getOperations(new DeviceIdentifier(id, type));
            if (operations == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("It is likely that" +
                                " no operation is found upon the provided type and id").build());
            }
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(operations).build();
    }

    @GET
    @Path("/{type}/{id}/effective-policy")
    @Override
    public Response getEffectivePolicyOfDevice(@PathParam("type") String type,
                                               @PathParam("id") String id,
                                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);

            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            Policy policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));
            if (policy == null) {
                throw new NotFoundException(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage("No policy has " +
                                "been found for the '" + type + "' device, which carries the id '" + id + "'").build());
            }
            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @Override
    public Response getDeviceTypes() {
        List<DeviceType> deviceTypes;
        try {
            deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(new ErrorResponse.ErrorResponseBuilder().
                setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(deviceTypes).build();
    }

}
