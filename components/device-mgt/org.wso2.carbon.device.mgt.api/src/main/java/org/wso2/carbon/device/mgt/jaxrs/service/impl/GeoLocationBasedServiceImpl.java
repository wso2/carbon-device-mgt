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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.common.geo.service.Event;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFence;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.jaxrs.service.api.GeoLocationBasedService;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    @Path("stats/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getGeoDeviceStats(@PathParam("deviceId") String deviceId,
                                      @PathParam("deviceType") String deviceType,
                                      @QueryParam("from") long from, @QueryParam("to") long to) {
        //First, check whether the Geo Location service has been enabled in the cdmf-config.xml file
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                .getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            if(!deviceManagementConfig.getGeoLocationConfiguration().getPublishLocationOperationResponse()){
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
            }
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

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername()
            );

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.createGeoAlert(alert, identifier, alertType);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
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

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername()
            );

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.updateGeoAlert(alert, identifier, alertType);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while creating the geo alert for " + deviceType + " with id: " + deviceId;
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
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

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername()
            );

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();
            geoService.removeGeoAlert(alertType, identifier, queryName);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while removing the geo alert for " + deviceType + " with id: " + deviceId;
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

            // this is the user who initiates the request
            String authorizedUser = MultitenantUtils.getTenantAwareUsername(
                    CarbonContext.getThreadLocalCarbonContext().getUsername()
            );

            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(deviceId);
            identifier.setType(deviceType);

            GeoLocationProviderService geoService = DeviceMgtAPIUtils.getGeoService();

            if (GeoServices.ALERT_TYPE_WITHIN.equals(alertType)) {
                List<GeoFence> alerts = geoService.getWithinAlerts(identifier);
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_EXIT.equals(alertType)) {
                List<GeoFence> alerts = geoService.getExitAlerts(identifier);
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_SPEED.equals(alertType)) {
                String result = geoService.getSpeedAlerts(identifier);
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_PROXIMITY.equals(alertType)) {
                String result = geoService.getProximityAlerts(identifier);
                return Response.ok().entity(result).build();
            } else if (GeoServices.ALERT_TYPE_STATIONARY.equals(alertType)) {
                List<GeoFence> alerts = geoService.getStationaryAlerts(identifier);
                return Response.ok().entity(alerts).build();
            } else if (GeoServices.ALERT_TYPE_TRAFFIC.equals(alertType)) {
                List<GeoFence> alerts = geoService.getTrafficAlerts(identifier);
                return Response.ok().entity(alerts).build();
            }
            return null;
        } catch (DeviceAccessAuthorizationException | GeoLocationBasedServiceException e) {
            String error = "Error occurred while getting the geo alerts for " + deviceType + " with id: " + deviceId;
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
}
