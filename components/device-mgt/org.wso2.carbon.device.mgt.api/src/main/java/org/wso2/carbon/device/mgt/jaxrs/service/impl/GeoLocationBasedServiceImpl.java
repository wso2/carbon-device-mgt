/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.geo.service.*;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.core.geo.GeoCluster;
import org.wso2.carbon.device.mgt.core.geo.geoHash.GeoCoordinate;
import org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy.GeoHashLengthStrategy;
import org.wso2.carbon.device.mgt.core.geo.geoHash.geoHashStrategy.ZoomGeoHashLengthStrategy;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GeoLocationBasedService;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The api for
 */
public class GeoLocationBasedServiceImpl implements GeoLocationBasedService {

    private static Log log = LogFactory.getLog(GeoLocationBasedServiceImpl.class);

    public static final String EMAIL_DOMAIN_SEPARATOR = "@";

    public static final String EMAIL_DOMAIN_SEPARATOR_REPLACEMENT = "-AT-";
    public static final String SECONDERY_USER_STORE_SEPERATOR = ":";
    public static final String SECONDERY_USER_STORE_DEFAULT_SEPERATOR = "/";

    @Path("stats/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoDeviceStats(@PathParam("deviceId") String deviceId,
                                      @PathParam("deviceType") String deviceType,
                                      @QueryParam("from") long from, @QueryParam("to") long to) {
        try {
            if (!DeviceManagerUtil.isPublishLocationResponseEnabled()) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
                        .entity("Unable to retrive Geo Device stats. Geo Data publishing does not enabled.").build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(e.getMessage()).build();
        }
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_FUSEDSPATIALEVENT";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "id:" + deviceId + " AND type:" + deviceType;
        if (from != 0 || to != 0) {
            query += " AND timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
            sortByFields.add(sortByField);

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername() + EMAIL_DOMAIN_SEPARATOR +
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
                int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
                AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
                List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
                        0,
                        100,
                        sortByFields);
                List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
                        searchResults);
                return Response.ok().entity(events).build();
            } catch (AnalyticsException | UserStoreException e) {
                log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
                throw DeviceMgtUtil.buildBadRequestException(
                        Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
            }
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("stats/device-locations")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoDeviceLocations(
            @QueryParam("deviceType") String deviceType,
            @QueryParam("minLat") double minLat,
            @QueryParam("maxLat") double maxLat,
            @QueryParam("minLong") double minLong,
            @QueryParam("maxLong") double maxLong,
            @QueryParam("zoom") int zoom) {

        GeoHashLengthStrategy geoHashLengthStrategy = new ZoomGeoHashLengthStrategy();
        GeoCoordinate southWest = new GeoCoordinate(minLat, minLong);
        GeoCoordinate northEast = new GeoCoordinate(maxLat, maxLong);
        int geohashLength = geoHashLengthStrategy.getGeohashLength(southWest, northEast, zoom);
        DeviceManagementProviderService deviceManagementService = DeviceMgtAPIUtils.getDeviceManagementService();
        List<GeoCluster> geoClusters;
        try {
            geoClusters = deviceManagementService.findGeoClusters(deviceType, southWest, northEast, geohashLength);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving geo clusters ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(geoClusters).build();

    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGeoAlerts(Alert alert, @PathParam("deviceId") String deviceId,
                                    @PathParam("deviceType") String deviceType,
                                    @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.createGeoAlert(alert, identifier, alertType, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }


    @Path("alerts/{alertType}")
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response createGeoAlertsForGeoClusters(Alert alert, @PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.createGeoAlert(alert, alertType);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating " + alertType + " alert";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }


    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGeoAlerts(Alert alert, @PathParam("deviceId") String deviceId,
                                    @PathParam("deviceType") String deviceType,
                                    @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.updateGeoAlert(alert, identifier, alertType, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateGeoAlertsForGeoClusters(Alert alert, @PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.updateGeoAlert(alert, alertType);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while updating the geo alert for geo clusters";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (AlertAlreadyExistException e) {
            String error = "A geo alert with this name already exists.";
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public Response removeGeoAlerts(@PathParam("deviceId") String deviceId,
                                    @PathParam("deviceType") String deviceType,
                                    @PathParam("alertType") String alertType,
                                    @QueryParam("queryName") String queryName) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.removeGeoAlert(alertType, identifier, queryName, device.getEnrolmentInfo().getOwner());
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while removing the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @DELETE
    @Consumes("application/json")
    @Produces("application/json")
    public Response removeGeoAlertsForGeoClusters(@PathParam("alertType") String alertType, @QueryParam("queryName") String queryName) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.removeGeoAlert(alertType, queryName);
            return Response.ok().build();
        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while removing the geo alert for geo clusters";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Path("alerts/{alertType}/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlerts(@PathParam("deviceId") String deviceId,
                                 @PathParam("deviceType") String deviceType,
                                 @PathParam("alertType") String alertType) {
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(identifier, false);
            if (device == null || device.getEnrolmentInfo() == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Device not found: " + identifier.toString());
                }
                return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
            }

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();

            if (GeoServices.ALERT_TYPE_WITHIN.equals(alertType)) {
                List<GeoFence> alerts = geoService.getWithinAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_EXIT.equals(alertType)) {
                List<GeoFence> alerts = geoService.getExitAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_SPEED.equals(alertType)) {
                String result = geoService.getSpeedAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_PROXIMITY.equals(alertType)) {
                String result = geoService.getProximityAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_STATIONARY.equals(alertType)) {
                List<GeoFence> alerts = geoService.getStationaryAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_TRAFFIC.equals(alertType)) {
                List<GeoFence> alerts = geoService.getTrafficAlerts(identifier, device.getEnrolmentInfo().getOwner());
                return Response.ok().entity(alerts).build();
            }
            return null;
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while getting the geo alerts for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (DeviceManagementException e) {
            String error = "Error occurred while retrieving the device enrollment info of " +
                    deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
    }

    @Path("alerts/{alertType}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsForGeoClusters(@PathParam("alertType") String alertType) {
        try {
            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            List<GeoFence> alerts = null;
            String result = null;

            switch (alertType) {
                case GeoServices.ALERT_TYPE_WITHIN:
                    alerts = geoService.getWithinAlerts();
                    break;
                case GeoServices.ALERT_TYPE_EXIT:
                    alerts = geoService.getExitAlerts();
                    break;
                case GeoServices.ALERT_TYPE_STATIONARY:
                    alerts = geoService.getStationaryAlerts();
                    break;
                case GeoServices.ALERT_TYPE_TRAFFIC:
                    alerts = geoService.getTrafficAlerts();
                    break;
                case GeoServices.ALERT_TYPE_SPEED:
                    result = geoService.getSpeedAlerts();
                    return Response.ok().entity(result).build();
                case GeoServices.ALERT_TYPE_PROXIMITY:
                    result = geoService.getProximityAlerts();
                    return Response.ok().entity(result).build();
                default:
                    throw new GeoLocationBasedServiceException("Invalid Alert Type");
            }
            return Response.ok().entity(alerts).build();

        } catch (GeoLocationBasedServiceException e) {
            String error = "Error occurred while getting the geo alerts for " + alertType + " alert";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Path("alerts/history/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsHistory(@PathParam("deviceId") String deviceId,
                                        @PathParam("deviceType") String deviceType,
                                        @QueryParam("from") long from, @QueryParam("to") long to) {
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_ALERTNOTIFICATIONS";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "id:" + deviceId + " AND type:" + deviceType;
        if (from != 0 || to != 0) {
            query += " AND timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
        try {
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType),
                    DeviceGroupConstants.Permissions.DEFAULT_STATS_MONITOR_PERMISSIONS)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
            sortByFields.add(sortByField);

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            try {
                String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
                int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
                AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
                List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
                        0,
                        100,
                        sortByFields);
                List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
                        searchResults);
                return Response.ok().entity(events).build();
            } catch (AnalyticsException | UserStoreException e) {
                log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
                throw DeviceMgtUtil.buildBadRequestException(
                        Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
            }
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("alerts/history")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoAlertsHistoryForGeoClusters(@QueryParam("from") long from, @QueryParam("to") long to) {
        String tableName = "IOT_PER_DEVICE_STREAM_GEO_ALERTNOTIFICATIONS";
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "";
        if (from != 0 || to != 0) {
            query = "timeStamp : [" + fromDate + " TO " + toDate + "]";
        }
        try {
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("timeStamp", SortType.ASC);
            sortByFields.add(sortByField);

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
            int tenantId = DeviceMgtAPIUtils.getRealmService().getTenantManager().getTenantId(tenantDomain);
            AnalyticsDataAPI analyticsDataAPI = DeviceMgtAPIUtils.getAnalyticsDataAPI();
            List<SearchResultEntry> searchResults = analyticsDataAPI.search(tenantId, tableName, query,
                    0,
                    100,
                    sortByFields);
            List<Event> events = getEventBeans(analyticsDataAPI, tenantId, tableName, new ArrayList<String>(),
                    searchResults);
            return Response.ok().entity(events).build();

        } catch (AnalyticsException | UserStoreException e) {
            log.error("Failed to perform search on table: " + tableName + " : " + e.getMessage(), e);
            throw DeviceMgtUtil.buildBadRequestException(
                    Constants.ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT);
        }
    }

    private List<Event> getEventBeans(AnalyticsDataAPI analyticsDataAPI, int tenantId, String tableName,
                                      List<String> columns,
                                      List<SearchResultEntry> searchResults) throws AnalyticsException {
        List<String> ids = getIds(searchResults);
        List<String> requiredColumns = (columns == null || columns.isEmpty()) ? null : columns;
        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, requiredColumns, ids);
        List<Record> records = AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, response);
        Map<String, Event> eventBeanMap = getEventBeanKeyedWithIds(records);
        return getSortedEventBeans(eventBeanMap, searchResults);
    }

    private List<Event> getSortedEventBeans(Map<String, Event> eventBeanMap,
                                            List<SearchResultEntry> searchResults) {
        List<Event> sortedRecords = new ArrayList<>();
        for (SearchResultEntry entry : searchResults) {
            sortedRecords.add(eventBeanMap.get(entry.getId()));
        }
        return sortedRecords;
    }

    private Map<String, Event> getEventBeanKeyedWithIds(List<Record> records) {
        Map<String, Event> eventBeanMap = new HashMap<>();
        for (Record record : records) {
            Event event = getEventBean(record);
            eventBeanMap.put(event.getId(), event);
        }
        return eventBeanMap;
    }

    private List<String> getIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        if (searchResults != null) {
            for (SearchResultEntry resultEntry : searchResults) {
                ids.add(resultEntry.getId());
            }
        }
        return ids;
    }

    private static Event getEventBean(Record record) {
        Event eventBean = new Event();
        eventBean.setId(record.getId());
        eventBean.setTableName(record.getTableName());
        eventBean.setTimestamp(record.getTimestamp());
        eventBean.setValues(record.getValues());
        return eventBean;
    }



    /**
     * When an input is having '@',replace it with '-AT-' [This is required to
     * persist WebApp data in registry,as registry paths don't allow '@' sign.]
     *
     * @param input
     *            inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomain(String input) {
        if (input != null && input.contains(EMAIL_DOMAIN_SEPARATOR)) {
            input =
                    input.replace(EMAIL_DOMAIN_SEPARATOR,
                            EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
        }
        return input;
    }

    /**
     * When an input is having '-AT-',replace it with @ [This is required to
     * persist WebApp data between registry and database]
     *
     * @param input
     *            inputString
     * @return String modifiedString
     */
    public static String replaceEmailDomainBack(String input) {
        if (input != null){
            if (input.contains(EMAIL_DOMAIN_SEPARATOR_REPLACEMENT)) {
                input =
                        input.replace(EMAIL_DOMAIN_SEPARATOR_REPLACEMENT,
                                EMAIL_DOMAIN_SEPARATOR);
            }else if (input.contains(SECONDERY_USER_STORE_SEPERATOR)){
                input =
                        input.replace(SECONDERY_USER_STORE_SEPERATOR,
                                SECONDERY_USER_STORE_DEFAULT_SEPERATOR);
            }
        }
        return input;
    }
}
