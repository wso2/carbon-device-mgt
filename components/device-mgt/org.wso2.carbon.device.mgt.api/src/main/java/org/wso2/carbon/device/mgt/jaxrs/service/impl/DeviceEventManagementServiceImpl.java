package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.stream.persistence.stub
        .EventStreamPersistenceAdminServiceEventStreamPersistenceAdminServiceExceptionException;
import org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.DeviceTypeEvent;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventRecords;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Attribute;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.AttributeType;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventAttributeList;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.TransportType;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceEventManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.event.publisher.stub.EventPublisherAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.receiver.stub.types.BasicInputAdapterPropertyDto;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is used for simple analytics purpose, to create streams and receiver dynamically and a common endpoint
 * to retrieve data.
 */
@Path("/device-types/events")
public class DeviceEventManagementServiceImpl implements DeviceEventManagementService {

    private static final Log log = LogFactory.getLog(DeviceEventManagementServiceImpl.class);

    private static final String DAS_PORT = "${iot.analytics.https.port}";
    private static final String DAS_HOST_NAME = "${iot.analytics.host}";
    private static final String DEFAULT_HTTP_PROTOCOL = "https";
    private static final String DAS_ADMIN_SERVICE_EP = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/EventReceiverAdminService" + "/";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer";
    private static final String KEY_STORE_TYPE = "JKS";
    private static final String TRUST_STORE_TYPE = "JKS";
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type
    private static final String SSLV3 = "SSLv3";
    private static final String DEFAULT_STREAM_VERSION = "1.0.0";
    private static final String DEFAULT_EVENT_STORE_NAME = "EVENT_STORE";
    private static final String DEFAULT_WEBSOCKET_PUBLISHER_ADAPTER_TYPE = "secured-websocket";

    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static char[] keyStorePassword;
    private static SSLContext sslContext;
    static {
        String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
        String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Password");
        String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
        String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Location");

        //Call to load the keystore.
        try {
            loadKeyStore(keyStoreLocation, keyStorePassword);
            //Call to load the TrustStore.
            loadTrustStore(trustStoreLocation, trustStorePassword);
            //Create the SSL context with the loaded TrustStore/keystore.
            initSSLConnection();
        } catch (KeyStoreException|IOException|CertificateException|NoSuchAlgorithmException
                | UnrecoverableKeyException | KeyManagementException e) {
            log.error("publishing dynamic event receiver is failed due to  " + e.getMessage(), e);
        }
    }

    /**
     * Deploy Event Stream, Receiver, Publisher and Store Configuration.
     */
    @POST
    @Path("/{type}")
    @Override
    public Response deployDeviceTypeEventDefinition(@PathParam("type") String deviceType, @Valid DeviceTypeEvent deviceTypeEvent) {
        TransportType transportType = deviceTypeEvent.getTransportType();
        EventAttributeList eventAttributes = deviceTypeEvent.getEventAttributeList();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        boolean superTenantMode = false;
        try {
            if (eventAttributes == null || eventAttributes.getList() == null || eventAttributes.getList().size() == 0 ||
                    deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String eventReceiverName = getReceiverName(deviceType, tenantDomain);
            String streamName = getStreamDefinition(deviceType, tenantDomain);
            String streamNameWithVersion = streamName + ":" + DEFAULT_STREAM_VERSION;
            publishStreamDefinitons(streamName, DEFAULT_STREAM_VERSION, deviceType, eventAttributes);
            publishEventReceivers(eventReceiverName, streamNameWithVersion, transportType, tenantDomain, deviceType);
            publishEventStore(streamName, DEFAULT_STREAM_VERSION, eventAttributes);
            publishWebsocketPublisherDefinition(streamNameWithVersion, deviceType);
            superTenantMode = true;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                publishStreamDefinitons(streamName, DEFAULT_STREAM_VERSION, deviceType, eventAttributes);
                publishEventReceivers(eventReceiverName, streamNameWithVersion, transportType, tenantDomain, deviceType);
            }
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("failed to create event definitions for tenantDomain:" + tenantDomain, e);
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
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (EventStreamPersistenceAdminServiceEventStreamPersistenceAdminServiceExceptionException e) {
            log.error("Failed to create event store for, tenantDomain: " + tenantDomain + " deviceType" + deviceType, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (superTenantMode) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @DELETE
    @Path("/{type}")
    @Override
    public Response deleteDeviceTypeEventDefinitions(@PathParam("type") String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String eventReceiverName = getReceiverName(deviceType, tenantDomain);
            String eventPublisherName = deviceType.trim().toLowerCase() + "_websocket_publisher";
            String streamName = getStreamDefinition(deviceType, tenantDomain);

            getEventStreamAdminServiceStub().removeEventStreamDefinition(streamName, DEFAULT_STREAM_VERSION);
            getEventReceiverAdminServiceStub().undeployActiveEventReceiverConfiguration(eventReceiverName);
            getEventPublisherAdminServiceStub().undeployActiveEventPublisherConfiguration(eventPublisherName);
            return Response.ok().build();
        } catch (AxisFault e) {
            log.error("failed to create event definitions for tenantDomain:" + tenantDomain, e);
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
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/{type}/{deviceId}")
    @Override
    public Response getData(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                            @QueryParam("to") long to,@PathParam("type")  String deviceType, @QueryParam("offset")
                                int offset, @QueryParam("limit") int limit) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND _timestamp : [" + fromDate + " TO " + toDate + "]";
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String sensorTableName = getTableName(getStreamDefinition(deviceType, tenantDomain));
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (!DeviceMgtAPIUtils.getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceId, deviceType))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField("_timestamp", SortType.ASC);
            sortByFields.add(sortByField);
            EventRecords eventRecords = getAllEventsForDevice(sensorTableName, query, sortByFields, offset, limit);
            return Response.status(Response.Status.OK.getStatusCode()).entity(eventRecords).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceManagementException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        }
    }

    @GET
    @Path("/{type}")
    @Override
    public Response getDeviceTypeEventDefinition(@PathParam("type") String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            if (deviceType == null ||
                    !DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(deviceType)) {
                String errorMessage = "Invalid device type";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            String streamName = getStreamDefinition(deviceType, tenantDomain);
            EventStreamDefinitionDto eventStreamDefinitionDto = getEventStreamAdminServiceStub().getStreamDefinitionDto(
                    streamName + ":" + DEFAULT_STREAM_VERSION);
            if (eventStreamDefinitionDto == null) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            EventStreamAttributeDto[] eventStreamAttributeDtos = eventStreamDefinitionDto.getPayloadData();
            EventAttributeList eventAttributeList = new EventAttributeList();
            List<Attribute> attributes = new ArrayList<>();
            for (EventStreamAttributeDto eventStreamAttributeDto : eventStreamAttributeDtos) {
                attributes.add(new Attribute(eventStreamAttributeDto.getAttributeName()
                        , AttributeType.valueOf(eventStreamAttributeDto.getAttributeType().toUpperCase())));
            }
            eventAttributeList.setList(attributes);
            return Response.ok().entity(eventAttributeList).build();
        } catch (AxisFault e) {
            log.error("failed to create event definitions for tenantDomain:" + tenantDomain, e);
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
        } catch (DeviceManagementException e) {
            log.error("Failed to access device management service, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    private void publishEventReceivers(String eventRecieverName, String streamNameWithVersion, TransportType transportType
            , String requestedTenantDomain, String deviceType)
            throws RemoteException, UserStoreException, JWTClientException {
        EventReceiverAdminServiceStub receiverAdminServiceStub = getEventReceiverAdminServiceStub();
        String adapterType = "oauth-mqtt";
        BasicInputAdapterPropertyDto basicInputAdapterPropertyDtos[];
        if (transportType == TransportType.MQTT) {
            basicInputAdapterPropertyDtos = new BasicInputAdapterPropertyDto[4];
            basicInputAdapterPropertyDtos[0] = getBasicInputAdapterPropertyDto("topic", requestedTenantDomain
                    + "/" + deviceType + "/+/events");
            basicInputAdapterPropertyDtos[1] = getBasicInputAdapterPropertyDto("contentValidator", "iot-mqtt");
            basicInputAdapterPropertyDtos[2] = getBasicInputAdapterPropertyDto("cleanSession", "true");
            basicInputAdapterPropertyDtos[3] = getBasicInputAdapterPropertyDto("clientId", generateUUID());
        } else {
            adapterType = "oauth-http";
            basicInputAdapterPropertyDtos = new BasicInputAdapterPropertyDto[1];
            basicInputAdapterPropertyDtos[0] = getBasicInputAdapterPropertyDto("contentValidator", "iot-mqtt");
        }
        receiverAdminServiceStub.deployJsonEventReceiverConfiguration(eventRecieverName, streamNameWithVersion
                , adapterType, null, basicInputAdapterPropertyDtos, false);
    }

    private void publishStreamDefinitons(String streamName, String version, String deviceType
            , EventAttributeList eventAttributes)
            throws RemoteException, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = getEventStreamAdminServiceStub();
        EventStreamDefinitionDto eventStreamDefinitionDto = new EventStreamDefinitionDto();
        eventStreamDefinitionDto.setName(streamName);
        eventStreamDefinitionDto.setVersion(version);
        EventStreamAttributeDto eventStreamAttributeDtos[] = new EventStreamAttributeDto[eventAttributes.getList().size()];
        int i = 0;
        for (Attribute attribute : eventAttributes.getList()) {
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(attribute.getName());
            eventStreamAttributeDto.setAttributeType(attribute.getType().toString());
            eventStreamAttributeDtos[i] = eventStreamAttributeDto;
            i++;
        }
        EventStreamAttributeDto metaData[] = new EventStreamAttributeDto[1];
        EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
        eventStreamAttributeDto.setAttributeName("deviceId");
        eventStreamAttributeDto.setAttributeType(AttributeType.STRING.toString());
        metaData[0] = eventStreamAttributeDto;
        eventStreamDefinitionDto.setMetaData(metaData);
        eventStreamDefinitionDto.setPayloadData(eventStreamAttributeDtos);
        String streamId = streamName + ":" + version;
        if (eventStreamAdminServiceStub.getStreamDefinitionAsString(streamId) != null) {
            eventStreamAdminServiceStub.editEventStreamDefinitionAsDto(eventStreamDefinitionDto, streamId);
        } else {
            eventStreamAdminServiceStub.addEventStreamDefinitionAsDto(eventStreamDefinitionDto);
        }

    }

    private void publishEventStore(String streamName, String version, EventAttributeList eventAttributes)
            throws RemoteException, UserStoreException, JWTClientException,
                   EventStreamPersistenceAdminServiceEventStreamPersistenceAdminServiceExceptionException {
        EventStreamPersistenceAdminServiceStub eventStreamAdminServiceStub = getEventStreamPersistenceAdminServiceStub();
        AnalyticsTable analyticsTable = new AnalyticsTable();
        analyticsTable.setRecordStoreName(DEFAULT_EVENT_STORE_NAME);
        analyticsTable.setStreamVersion(version);
        analyticsTable.setTableName(getTableName(streamName));
        AnalyticsTableRecord analyticsTableRecords[] = new AnalyticsTableRecord[eventAttributes.getList().size() + 1];
        int i = 0;
        for (Attribute attribute : eventAttributes.getList()) {
            AnalyticsTableRecord analyticsTableRecord = new AnalyticsTableRecord();
            analyticsTableRecord.setColumnName(attribute.getName());
            analyticsTableRecord.setColumnType(attribute.getType().toString().toUpperCase());
            analyticsTableRecord.setFacet(false);
            analyticsTableRecord.setIndexed(true);
            analyticsTableRecord.setPersist(true);
            analyticsTableRecord.setPrimaryKey(false);
            analyticsTableRecord.setScoreParam(false);
            analyticsTableRecords[i] = analyticsTableRecord;
            i++;
        }
        AnalyticsTableRecord analyticsTableRecord = new AnalyticsTableRecord();
        analyticsTableRecord.setColumnName("meta_deviceId");
        analyticsTableRecord.setColumnType(AttributeType.STRING.toString().toUpperCase());
        analyticsTableRecord.setFacet(false);
        analyticsTableRecord.setIndexed(true);
        analyticsTableRecord.setPersist(true);
        analyticsTableRecord.setPrimaryKey(false);
        analyticsTableRecord.setScoreParam(false);
        analyticsTableRecords[i] = analyticsTableRecord;
        analyticsTable.setAnalyticsTableRecords(analyticsTableRecords);
        eventStreamAdminServiceStub.addAnalyticsTable(analyticsTable);

    }

    private void publishWebsocketPublisherDefinition(String streamNameWithVersion, String deviceType)
            throws RemoteException, UserStoreException, JWTClientException {
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = getEventPublisherAdminServiceStub();
        String eventPublisherName = deviceType.trim().toLowerCase() + "_websocket_publisher";
        eventPublisherAdminServiceStub.startdeployJsonEventPublisherConfiguration(eventPublisherName
                , streamNameWithVersion, DEFAULT_WEBSOCKET_PUBLISHER_ADAPTER_TYPE, null, null,null, false, null);
    }

    private EventStreamAdminServiceStub getEventStreamAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventStreamAdminServiceStub eventStreamAdminServiceStub = new EventStreamAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP));
        Options streamOptions = eventStreamAdminServiceStub._getServiceClient().getOptions();
        if (streamOptions == null) {
            streamOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue =  AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"
        streamOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        streamOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        eventStreamAdminServiceStub._getServiceClient().setOptions(streamOptions);
        return eventStreamAdminServiceStub;
    }

    private EventReceiverAdminServiceStub getEventReceiverAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventReceiverAdminServiceStub receiverAdminServiceStub = new EventReceiverAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP));
        Options eventReciverOptions = receiverAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue =  AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        receiverAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return receiverAdminServiceStub;
    }

    private EventPublisherAdminServiceStub getEventPublisherAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventPublisherAdminServiceStub eventPublisherAdminServiceStub = new EventPublisherAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP));
        Options eventReciverOptions = eventPublisherAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue =  AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        eventPublisherAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return eventPublisherAdminServiceStub;
    }

    private EventStreamPersistenceAdminServiceStub getEventStreamPersistenceAdminServiceStub()
            throws AxisFault, UserStoreException, JWTClientException {
        EventStreamPersistenceAdminServiceStub eventStreamPersistenceAdminServiceStub
                = new EventStreamPersistenceAdminServiceStub(
                Utils.replaceSystemProperty(DAS_ADMIN_SERVICE_EP));
        Options eventReciverOptions = eventStreamPersistenceAdminServiceStub._getServiceClient().getOptions();
        if (eventReciverOptions == null) {
            eventReciverOptions = new Options();
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;
        JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

        String authValue =  AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                jwtClient.getJwtToken(username).getBytes()));

        List<Header> list = new ArrayList<>();
        Header httpHeader = new Header();
        httpHeader.setName(AUTHORIZATION_HEADER);
        httpHeader.setValue(authValue);
        list.add(httpHeader);//"https"

        eventReciverOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
        eventReciverOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                , new Protocol(DEFAULT_HTTP_PROTOCOL
                , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
        eventStreamPersistenceAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
        return eventStreamPersistenceAdminServiceStub;
    }

    /**
     * Loads the keystore.
     *
     * @param keyStorePath - the path of the keystore
     * @param ksPassword   - the keystore password
     */
    private static void loadKeyStore(String keyStorePath, String ksPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fis = null;
        try {
            keyStorePassword = ksPassword.toCharArray();
            keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            fis = new FileInputStream(keyStorePath);
            keyStore.load(fis, keyStorePassword);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Loads the trustore
     *
     * @param trustStorePath - the trustore path in the filesystem.
     * @param tsPassword     - the truststore password
     */
    private static void loadTrustStore(String trustStorePath, String tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        InputStream fis = null;
        try {
            trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
            fis = new FileInputStream(trustStorePath);
            trustStore.load(fis, tsPassword.toCharArray());
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Initializes the SSL Context
     */
    private static void initSSLConnection() throws NoSuchAlgorithmException, UnrecoverableKeyException,
                                            KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        sslContext = SSLContext.getInstance(SSLV3);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
    }

    private BasicInputAdapterPropertyDto getBasicInputAdapterPropertyDto(String key, String value) {
        BasicInputAdapterPropertyDto basicInputAdapterPropertyDto = new BasicInputAdapterPropertyDto();
        basicInputAdapterPropertyDto.setKey(key);
        basicInputAdapterPropertyDto.setValue(value);
        return basicInputAdapterPropertyDto;
    }

    private static String generateUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    private String getStreamDefinition(String deviceType, String tenantDomain) {
        return tenantDomain.toLowerCase() + "." + deviceType.toLowerCase();
    }

    private String getTableName(String streamName) {
        return streamName.toUpperCase().replace('.', '_');
    }

    private String getReceiverName(String deviceType, String tenantDomain) {
        return deviceType.trim().toLowerCase() + "-" + tenantDomain.toLowerCase() + "-receiver";
    }

    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        AnalyticsDataAPI analyticsDataAPI =
                (AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
        if (analyticsDataAPI == null) {
            String msg = "Analytics api service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return analyticsDataAPI;
    }

    protected static EventRecords getAllEventsForDevice(String tableName, String query, List<SortByField> sortByFields
            , int offset, int limit) throws AnalyticsException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
        int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
        if (eventCount == 0) {
            return null;
        }
        List<SearchResultEntry> resultEntries = analyticsDataAPI.search(tenantId, tableName, query, offset, limit,
                                                                        sortByFields);
        List<String> recordIds = getRecordIds(resultEntries);
        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
        EventRecords eventRecords = new EventRecords();
        eventRecords.setList(AnalyticsDataAPIUtil.listRecords(analyticsDataAPI, response));
        return eventRecords;
    }

    private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

}
