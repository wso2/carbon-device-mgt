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
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/devices")
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
            @HeaderParam("If-Modified-Since") Date timestamp,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            PaginationRequest request = new PaginationRequest(offset, limit);

            PaginationResult result = dms.getAllDevices(request);
            if (result == null || result.getData().size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No device is currently enrolled " +
                        "with the server").build();
            }
            return Response.status(Response.Status.OK).entity(result.getData()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response getDevicesInfo(
            List<DeviceIdentifier> deviceIds,
            @HeaderParam("If-Modified-Since") Date timestamp) {
        DeviceInformationManager informationManager;
        List<DeviceInfo> deviceInfo;
        try {
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceInfo = informationManager.getDevicesInfo(deviceIds);
            if (deviceInfo == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No device information is available for the " +
                        "device list submitted").build();
            }
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceInfo).build();
    }

    @GET
    @Override
    public Response getDevice(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Device device;
        try {
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            device = dms.getDevice(new DeviceIdentifier(id, type));
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        if (device == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Requested device of type '" + type +
                    "', which carries id '" + id + "' does not exist").build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @GET
    @Path("/{type}/{id}/location")
    @Override
    public Response getDeviceLocation(
            @PathParam("type") String type,
            @PathParam("id") String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceInformationManager informationManager;
        DeviceLocation deviceLocation;
        try {
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceLocation = informationManager.getDeviceLocation(new DeviceIdentifier(id, type));
            if (deviceLocation == null || deviceLocation.getLatitude() == null ||
                    deviceLocation.getLongitude() == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Location details are not available for the " +
                        "given device id '" + id + "'").build();
            }
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the last updated location of the '" + type + "' device, " +
                    "which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocation).build();
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
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            features = dms.getFeatureManager(type).getFeatures();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        if (features == null || features.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No feature is currently associated " +
                    "with the '" + type + "' device, which carries the id '" + id + "'").build();
        }
        return Response.status(Response.Status.OK).entity(features).build();
    }

    @POST
    @Path("/search-devices")
    @Override
    public Response searchDevices(SearchContext searchContext, @QueryParam("offset") int offset, int limit) {
        SearchManagerService searchManagerService;
        List<DeviceWrapper> devices;
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.search(searchContext);
        } catch (SearchMgtException e) {
            String msg = "Error occurred while searching for devices that matches the provided selection criteria";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        if (devices == null || devices.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No device can be retrieved upon the provided " +
                    "selection criteria").build();
        }
        return Response.status(Response.Status.OK).entity(devices).build();
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
            amc = DeviceMgtAPIUtils.getAppManagementService();
            applications = amc.getApplicationListForDevice(new DeviceIdentifier(id, type));
            if (applications == null || applications.size() == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("No installed applications found on the " +
                        "device searched").build();
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
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
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            operations = dms.getOperations(new DeviceIdentifier(id, type));
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(operations).build();
    }

    @Override
    public Response getEffectivePolicyOfDevice(@QueryParam("type") String type,
                                               @QueryParam("id") String id,
                                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            Policy policy = policyManagementService.getAppliedPolicyToDevice(new DeviceIdentifier(id, type));
            if (policy == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No policy has been found for the '" +
                        type + "' device, which carries the id '" + id + "'").build();
            }
            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
