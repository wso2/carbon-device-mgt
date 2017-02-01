/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.*;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.DashboardPaginationGadgetDataWrapper;
import org.wso2.carbon.device.mgt.jaxrs.service.api.Dashboard;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.device.mgt.analytics.dashboard.util.APIUtil.getAuthenticatedUser;

/**
 * This class consists of dashboard related REST APIs
 * to be consumed by individual client gadgets such as
 * [1] Overview of Devices,
 * [2] Potential Vulnerabilities,
 * [3] Non-compliant Devices by Features,
 * [4] Device Groupings and etc.
 */

@Consumes({"application/json"})
@Produces({"application/json"})

@SuppressWarnings("NonJaxWsWebServices")
public class DashboardImpl implements Dashboard {

    private static Log log = LogFactory.getLog(DashboardImpl.class);

    private static final String FLAG_TRUE = "true";
    private static final String FLAG_FALSE = "false";
    // Constants related to common error-response messages
    private static final String INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY = "Received an invalid value for " +
        "query parameter : " + POTENTIAL_VULNERABILITY + ", Should be either NON_COMPLIANT or UNMONITORED.";
    private static final String INVALID_QUERY_PARAM_VALUE_START_INDEX = "Received an invalid value for " +
        "query parameter : " + START_INDEX + ", Should not be lesser than 0.";
    private static final String INVALID_QUERY_PARAM_VALUE_RESULT_COUNT = "Received an invalid value for " +
        "query parameter : " + RESULT_COUNT + ", Should not be lesser than 5.";
    private static final String INVALID_QUERY_PARAM_VALUE_PAGINATION_ENABLED = "Received an invalid value for " +
        "query parameter : " + PAGINATION_ENABLED + ", Should be either true or false.";
    private static final String REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE = "Missing required query " +
        "parameter : " + NON_COMPLIANT_FEATURE_CODE;
    private static final String REQUIRED_QUERY_PARAM_VALUE_PAGINATION_ENABLED = "Missing required query " +
        "parameter : " + PAGINATION_ENABLED;
    private static final String ERROR_IN_RETRIEVING_REQUESTED_DATA = "ErrorResponse in retrieving requested data.";

    @GET
    @Path("device-count-overview")
    public Response getOverviewDeviceCounts() {
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();

        // getting total device count
        DeviceCountByGroup totalDeviceCount;
        try {
            String userName = getAuthenticatedUser();
            totalDeviceCount = gadgetDataService.getTotalDeviceCount(userName);
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve total device count.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        List<DeviceCountByGroup> totalDeviceCountInListEntry = new ArrayList<>();
        totalDeviceCountInListEntry.add(totalDeviceCount);

        dashboardGadgetDataWrapper1.setContext("Total-device-count");
        dashboardGadgetDataWrapper1.setGroupingAttribute(null);
        dashboardGadgetDataWrapper1.setData(totalDeviceCountInListEntry);

        // getting device counts by connectivity statuses
        List<DeviceCountByGroup> deviceCountsByConnectivityStatuses;
        try {
            String userName = getAuthenticatedUser();
            deviceCountsByConnectivityStatuses = gadgetDataService.getDeviceCountsByConnectivityStatuses(userName);
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve device counts by connectivity statuses.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();

        dashboardGadgetDataWrapper2.setContext("Device-counts-by-connectivity-statuses");
        dashboardGadgetDataWrapper2.setGroupingAttribute(CONNECTIVITY_STATUS);
        dashboardGadgetDataWrapper2.setData(deviceCountsByConnectivityStatuses);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper1);
        responsePayload.add(dashboardGadgetDataWrapper2);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("device-counts-by-potential-vulnerabilities")
    public Response getDeviceCountsByPotentialVulnerabilities() {
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        List<DeviceCountByGroup> deviceCountsByPotentialVulnerabilities;
        try {
            String userName = getAuthenticatedUser();
            deviceCountsByPotentialVulnerabilities = gadgetDataService.getDeviceCountsByPotentialVulnerabilities(userName);
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve device counts by potential vulnerabilities.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper.setContext("Device-counts-by-potential-vulnerabilities");
        dashboardGadgetDataWrapper.setGroupingAttribute(POTENTIAL_VULNERABILITY);
        dashboardGadgetDataWrapper.setData(deviceCountsByPotentialVulnerabilities);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("non-compliant-device-counts-by-features")
    public Response getNonCompliantDeviceCountsByFeatures(@QueryParam(START_INDEX) int startIndex,
                                                          @QueryParam(RESULT_COUNT) int resultCount) {

        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();
        DashboardPaginationGadgetDataWrapper
            dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();

        PaginationResult paginationResult;
        try {
            String userName = getAuthenticatedUser();
            paginationResult = gadgetDataService.
                getNonCompliantDeviceCountsByFeatures(startIndex, resultCount, userName);
        } catch (InvalidStartIndexValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a non-compliant set " +
                        "of device counts by features.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(INVALID_QUERY_PARAM_VALUE_START_INDEX).build();
        } catch (InvalidResultCountValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a non-compliant set " +
                        "of device counts by features.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(INVALID_QUERY_PARAM_VALUE_RESULT_COUNT).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a non-compliant set of device counts by features.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        dashboardPaginationGadgetDataWrapper.setContext("Non-compliant-device-counts-by-features");
        dashboardPaginationGadgetDataWrapper.setGroupingAttribute(NON_COMPLIANT_FEATURE_CODE);
        dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
        dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());

        List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardPaginationGadgetDataWrapper);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("device-counts-by-groups")
    public Response getDeviceCountsByGroups(@QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
                                            @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
                                            @QueryParam(PLATFORM) String platform,
                                            @QueryParam(OWNERSHIP) String ownership) {

        // getting gadget data service
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        // constructing filter set
        ExtendedFilterSet filterSet = new ExtendedFilterSet();
        filterSet.setConnectivityStatus(connectivityStatus);
        filterSet.setPotentialVulnerability(potentialVulnerability);
        filterSet.setPlatform(platform);
        filterSet.setOwnership(ownership);

        // creating device-Counts-by-platforms Data Wrapper
        List<DeviceCountByGroup> deviceCountsByPlatforms;
        try {
            String userName = getAuthenticatedUser();
            deviceCountsByPlatforms = gadgetDataService.getDeviceCountsByPlatforms(filterSet, userName);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a filtered set of device counts by platforms.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a filtered set of device counts by platforms.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper1.setContext("Device-counts-by-platforms");
        dashboardGadgetDataWrapper1.setGroupingAttribute(PLATFORM);
        dashboardGadgetDataWrapper1.setData(deviceCountsByPlatforms);

        // creating device-Counts-by-ownership-types Data Wrapper
        List<DeviceCountByGroup> deviceCountsByOwnerships;
        try {
            String userName = getAuthenticatedUser();
            deviceCountsByOwnerships = gadgetDataService.getDeviceCountsByOwnershipTypes(filterSet, userName);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a filtered set of device counts by ownerships.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a filtered set of device counts by ownerships.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper2.setContext("Device-counts-by-ownerships");
        dashboardGadgetDataWrapper2.setGroupingAttribute(OWNERSHIP);
        dashboardGadgetDataWrapper2.setData(deviceCountsByOwnerships);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper1);
        responsePayload.add(dashboardGadgetDataWrapper2);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("feature-non-compliant-device-counts-by-groups")
    public Response getFeatureNonCompliantDeviceCountsByGroups(@QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
                                                               @QueryParam(PLATFORM) String platform,
                                                               @QueryParam(OWNERSHIP) String ownership) {
        // getting gadget data service
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        // constructing filter set
        BasicFilterSet filterSet = new BasicFilterSet();
        filterSet.setPlatform(platform);
        filterSet.setOwnership(ownership);

        // creating feature-non-compliant-device-Counts-by-platforms Data Wrapper
        List<DeviceCountByGroup> featureNonCompliantDeviceCountsByPlatforms;
        try {
            String userName = getAuthenticatedUser();
            featureNonCompliantDeviceCountsByPlatforms = gadgetDataService.
                getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filterSet, userName);
        } catch (InvalidFeatureCodeValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a filtered set of feature " +
                        "non-compliant device counts by platforms.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a filtered set of feature non-compliant " +
                    "device counts by platforms.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper1 = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper1.setContext("Feature-non-compliant-device-counts-by-platforms");
        dashboardGadgetDataWrapper1.setGroupingAttribute(PLATFORM);
        dashboardGadgetDataWrapper1.setData(featureNonCompliantDeviceCountsByPlatforms);

        // creating feature-non-compliant-device-Counts-by-ownership-types Data Wrapper
        List<DeviceCountByGroup> featureNonCompliantDeviceCountsByOwnerships;
        try {
            String userName = getAuthenticatedUser();
            featureNonCompliantDeviceCountsByOwnerships = gadgetDataService.
                getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filterSet, userName);
        } catch (InvalidFeatureCodeValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a filtered set of feature " +
                        "non-compliant device counts by ownerships.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a filtered set of feature non-compliant " +
                    "device counts by ownerships.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper2 = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper2.setContext("Feature-non-compliant-device-counts-by-ownerships");
        dashboardGadgetDataWrapper2.setGroupingAttribute(OWNERSHIP);
        dashboardGadgetDataWrapper2.setData(featureNonCompliantDeviceCountsByOwnerships);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper1);
        responsePayload.add(dashboardGadgetDataWrapper2);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("filtered-device-count-over-total")
    public Response getFilteredDeviceCountOverTotal(@QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
                                                    @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
                                                    @QueryParam(PLATFORM) String platform,
                                                    @QueryParam(OWNERSHIP) String ownership) {

        // getting gadget data service
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        // constructing filter set
        ExtendedFilterSet filterSet = new ExtendedFilterSet();
        filterSet.setConnectivityStatus(connectivityStatus);
        filterSet.setPotentialVulnerability(potentialVulnerability);
        filterSet.setPlatform(platform);
        filterSet.setOwnership(ownership);

        // creating filteredDeviceCount Data Wrapper
        DeviceCountByGroup filteredDeviceCount;
        try {
            String userName = getAuthenticatedUser();
            filteredDeviceCount = gadgetDataService.getDeviceCount(filterSet, userName);
        } catch (InvalidPotentialVulnerabilityValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service " +
                    "function @ Dashboard API layer to retrieve a filtered device count over the total.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a filtered device count over the total.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        // creating TotalDeviceCount Data Wrapper
        DeviceCountByGroup totalDeviceCount;
        try {
            String userName = getAuthenticatedUser();
            totalDeviceCount = gadgetDataService.getTotalDeviceCount(userName);
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve the total device count over filtered.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        List<Object> filteredDeviceCountOverTotalDataWrapper = new ArrayList<>();
        filteredDeviceCountOverTotalDataWrapper.add(filteredDeviceCount);
        filteredDeviceCountOverTotalDataWrapper.add(totalDeviceCount);

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper.setContext("Filtered-device-count-over-total");
        dashboardGadgetDataWrapper.setGroupingAttribute(null);
        dashboardGadgetDataWrapper.setData(filteredDeviceCountOverTotalDataWrapper);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("feature-non-compliant-device-count-over-total")
    public Response getFeatureNonCompliantDeviceCountOverTotal(@QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
                                                               @QueryParam(PLATFORM) String platform,
                                                               @QueryParam(OWNERSHIP) String ownership) {

        // getting gadget data service
        GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

        // constructing filter set
        BasicFilterSet filterSet = new BasicFilterSet();
        filterSet.setPlatform(platform);
        filterSet.setOwnership(ownership);

        // creating featureNonCompliantDeviceCount Data Wrapper
        DeviceCountByGroup featureNonCompliantDeviceCount;
        try {
            String userName = getAuthenticatedUser();
            featureNonCompliantDeviceCount = gadgetDataService.
                getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filterSet, userName);
        } catch (InvalidFeatureCodeValueException e) {
            log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                "invalid (query) parameter value. This was while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a feature non-compliant device count over the total.", e);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE).build();
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve a feature non-compliant device count over the total.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        // creating TotalDeviceCount Data Wrapper
        DeviceCountByGroup totalDeviceCount;
        try {
            String userName = getAuthenticatedUser();
            totalDeviceCount = gadgetDataService.getTotalDeviceCount(userName);
        } catch (DataAccessLayerException e) {
            log.error("An internal error occurred while trying to execute relevant data service function " +
                "@ Dashboard API layer to retrieve the total device count over filtered feature non-compliant.", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
        }

        List<Object> featureNonCompliantDeviceCountOverTotalDataWrapper = new ArrayList<>();
        featureNonCompliantDeviceCountOverTotalDataWrapper.add(featureNonCompliantDeviceCount);
        featureNonCompliantDeviceCountOverTotalDataWrapper.add(totalDeviceCount);

        DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
        dashboardGadgetDataWrapper.setContext("Feature-non-compliant-device-count-over-total");
        dashboardGadgetDataWrapper.setGroupingAttribute(null);
        dashboardGadgetDataWrapper.setData(featureNonCompliantDeviceCountOverTotalDataWrapper);

        List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
        responsePayload.add(dashboardGadgetDataWrapper);

        return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
    }

    @GET
    @Path("devices-with-details")
    public Response getDevicesWithDetails(@QueryParam(CONNECTIVITY_STATUS) String connectivityStatus,
                                          @QueryParam(POTENTIAL_VULNERABILITY) String potentialVulnerability,
                                          @QueryParam(PLATFORM) String platform,
                                          @QueryParam(OWNERSHIP) String ownership,
                                          @QueryParam(PAGINATION_ENABLED) String paginationEnabled,
                                          @QueryParam(START_INDEX) int startIndex,
                                          @QueryParam(RESULT_COUNT) int resultCount) {

        if (paginationEnabled == null) {

            log.error("Bad request on retrieving a filtered set of devices with details @ " +
                "Dashboard API layer. " + REQUIRED_QUERY_PARAM_VALUE_PAGINATION_ENABLED);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(REQUIRED_QUERY_PARAM_VALUE_PAGINATION_ENABLED).build();

        } else if (FLAG_TRUE.equals(paginationEnabled)) {

            // getting gadget data service
            GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

            // constructing filter set
            ExtendedFilterSet filterSet = new ExtendedFilterSet();
            filterSet.setConnectivityStatus(connectivityStatus);
            filterSet.setPotentialVulnerability(potentialVulnerability);
            filterSet.setPlatform(platform);
            filterSet.setOwnership(ownership);

            PaginationResult paginationResult;
            try {
                String userName = getAuthenticatedUser();
                paginationResult = gadgetDataService.
                    getDevicesWithDetails(filterSet, startIndex, resultCount, userName);
            } catch (InvalidPotentialVulnerabilityValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY).build();
            } catch (InvalidStartIndexValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_START_INDEX).build();
            } catch (InvalidResultCountValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_RESULT_COUNT).build();
            } catch (DataAccessLayerException e) {
                log.error("An internal error occurred while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                    entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
            }

            DashboardPaginationGadgetDataWrapper
                dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();
            dashboardPaginationGadgetDataWrapper.setContext("Filtered-and-paginated-devices-with-details");
            dashboardPaginationGadgetDataWrapper.setGroupingAttribute(null);
            dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
            dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());

            List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList<>();
            responsePayload.add(dashboardPaginationGadgetDataWrapper);

            return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();

        } else if (FLAG_FALSE.equals(paginationEnabled)) {

            // getting gadget data service
            GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

            // constructing filter set
            ExtendedFilterSet filterSet = new ExtendedFilterSet();
            filterSet.setConnectivityStatus(connectivityStatus);
            filterSet.setPotentialVulnerability(potentialVulnerability);
            filterSet.setPlatform(platform);
            filterSet.setOwnership(ownership);

            List<DeviceWithDetails> devicesWithDetails;
            try {
                String userName = getAuthenticatedUser();
                devicesWithDetails = gadgetDataService.getDevicesWithDetails(filterSet, userName);
            } catch (InvalidPotentialVulnerabilityValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_POTENTIAL_VULNERABILITY).build();
            } catch (DataAccessLayerException e) {
                log.error("An internal error occurred while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a filtered set of devices with details.", e);
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                    entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
            }

            DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
            dashboardGadgetDataWrapper.setContext("Filtered-devices-with-details");
            dashboardGadgetDataWrapper.setGroupingAttribute(null);
            dashboardGadgetDataWrapper.setData(devicesWithDetails);

            List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
            responsePayload.add(dashboardGadgetDataWrapper);

            return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();

        } else {

            log.error("Bad request on retrieving a filtered set of devices with details @ " +
                "Dashboard API layer. " + INVALID_QUERY_PARAM_VALUE_PAGINATION_ENABLED);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(INVALID_QUERY_PARAM_VALUE_PAGINATION_ENABLED).build();

        }
    }

    @GET
    @Path("feature-non-compliant-devices-with-details")
    public Response getFeatureNonCompliantDevicesWithDetails(@QueryParam(NON_COMPLIANT_FEATURE_CODE) String nonCompliantFeatureCode,
                                                             @QueryParam(PLATFORM) String platform,
                                                             @QueryParam(OWNERSHIP) String ownership,
                                                             @QueryParam(PAGINATION_ENABLED) String paginationEnabled,
                                                             @QueryParam(START_INDEX) int startIndex,
                                                             @QueryParam(RESULT_COUNT) int resultCount) {
        if (paginationEnabled == null) {

            log.error("Bad request on retrieving a filtered set of feature non-compliant devices with " +
                "details @ Dashboard API layer. " + REQUIRED_QUERY_PARAM_VALUE_PAGINATION_ENABLED);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(REQUIRED_QUERY_PARAM_VALUE_PAGINATION_ENABLED).build();

        } else if (FLAG_TRUE.equals(paginationEnabled)) {

            // getting gadget data service
            GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

            // constructing filter set
            BasicFilterSet filterSet = new BasicFilterSet();
            filterSet.setPlatform(platform);
            filterSet.setOwnership(ownership);

            PaginationResult paginationResult;
            try {
                String userName = getAuthenticatedUser();
                paginationResult = gadgetDataService.
                    getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode,
                        filterSet, startIndex, resultCount, userName);
            } catch (InvalidFeatureCodeValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of " +
                            "feature non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE).build();
            } catch (InvalidStartIndexValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of " +
                            "feature non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_START_INDEX).build();
            } catch (InvalidResultCountValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of " +
                            "feature non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(INVALID_QUERY_PARAM_VALUE_RESULT_COUNT).build();
            } catch (DataAccessLayerException e) {
                log.error("An internal error occurred while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a filtered set of feature " +
                        "non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                    entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
            }

            DashboardPaginationGadgetDataWrapper
                dashboardPaginationGadgetDataWrapper = new DashboardPaginationGadgetDataWrapper();
            dashboardPaginationGadgetDataWrapper.
                setContext("Filtered-and-paginated-feature-non-compliant-devices-with-details");
            dashboardPaginationGadgetDataWrapper.setGroupingAttribute(null);
            dashboardPaginationGadgetDataWrapper.setData(paginationResult.getData());
            dashboardPaginationGadgetDataWrapper.setTotalRecordCount(paginationResult.getRecordsTotal());

            List<DashboardPaginationGadgetDataWrapper> responsePayload = new ArrayList<>();
            responsePayload.add(dashboardPaginationGadgetDataWrapper);

            return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();

        } else if (FLAG_FALSE.equals(paginationEnabled)) {

            // getting gadget data service
            GadgetDataService gadgetDataService = DeviceMgtAPIUtils.getGadgetDataService();

            // constructing filter set
            BasicFilterSet filterSet = new BasicFilterSet();
            filterSet.setPlatform(platform);
            filterSet.setOwnership(ownership);

            List<DeviceWithDetails> featureNonCompliantDevicesWithDetails;
            try {
                String userName = getAuthenticatedUser();
                featureNonCompliantDevicesWithDetails = gadgetDataService.
                    getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet, userName);
            } catch (InvalidFeatureCodeValueException e) {
                log.error("Bad request and error occurred @ Gadget Data Service layer due to " +
                    "invalid (query) parameter value. This was while trying to execute relevant data service " +
                        "function @ Dashboard API layer to retrieve a filtered set of " +
                            "feature non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_BAD_REQUEST).
                    entity(REQUIRED_QUERY_PARAM_VALUE_NON_COMPLIANT_FEATURE_CODE).build();
            } catch (DataAccessLayerException e) {
                log.error("An internal error occurred while trying to execute relevant data service function " +
                    "@ Dashboard API layer to retrieve a filtered set of feature " +
                        "non-compliant devices with details.", e);
                return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                    entity(ERROR_IN_RETRIEVING_REQUESTED_DATA).build();
            }

            DashboardGadgetDataWrapper dashboardGadgetDataWrapper = new DashboardGadgetDataWrapper();
            dashboardGadgetDataWrapper.setContext("Filtered-feature-non-compliant-devices-with-details");
            dashboardGadgetDataWrapper.setGroupingAttribute(null);
            dashboardGadgetDataWrapper.setData(featureNonCompliantDevicesWithDetails);

            List<DashboardGadgetDataWrapper> responsePayload = new ArrayList<>();
            responsePayload.add(dashboardGadgetDataWrapper);

            return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();

        } else {

            log.error("Bad request on retrieving a filtered set of feature non-compliant devices with " +
                "details @ Dashboard API layer. " + INVALID_QUERY_PARAM_VALUE_PAGINATION_ENABLED);
            return Response.status(HttpStatus.SC_BAD_REQUEST).
                entity(INVALID_QUERY_PARAM_VALUE_PAGINATION_ENABLED).build();

        }
    }

}
