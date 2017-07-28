/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.geo.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices;
import org.wso2.carbon.device.mgt.common.geo.service.Alert;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFence;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices.DAS_PORT;
import static org.wso2.carbon.device.mgt.common.DeviceManagementConstants.GeoServices.DEFAULT_HTTP_PROTOCOL;

/**
 * This class will read events, set alerts, read alerts related to geo-fencing and it will
 * use Registry as the persistence storage.
 */
public class GeoLocationProviderServiceImpl implements GeoLocationProviderService {

    private static Log log = LogFactory.getLog(GeoLocationProviderServiceImpl.class);

    /**
     * required soap header for authorization
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    /**
     * required soap header value for mutualSSL
     */
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer";
    /**
     * Default keystore type of the client
     */
    private static final String KEY_STORE_TYPE = "JKS";
    /**
     * Default truststore type of the client
     */
    private static final String TRUST_STORE_TYPE = "JKS";
    /**
     * Default keymanager type of the client
     */
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type

    private static final String SSLV3 = "SSLv3";

    @Override
    public List<GeoFence> getWithinAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {

        Registry registry = getGovernanceRegistry();
        String registryPath = GeoServices.REGISTRY_PATH_FOR_ALERTS +
                GeoServices.ALERT_TYPE_WITHIN + "/" + identifier.getId() + "/";
        Resource resource;
        try {
            resource = registry.get(registryPath);
        } catch (RegistryException e) {
            log.error("Error while reading the registry path: " + registryPath);
            return null;
        }

        try {
            List<GeoFence> fences = new ArrayList<>();
            if (resource != null) {
                Object contentObj = resource.getContent();
                if (contentObj instanceof String[]) {
                    String[] content = (String[]) contentObj;
                    for (String res : content) {
                        Resource childRes = registry.get(res);
                        Properties props = childRes.getProperties();

                        GeoFence geoFence = new GeoFence();

                        InputStream inputStream = childRes.getContentStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(inputStream, writer, "UTF-8");
                        geoFence.setGeoJson(writer.toString());

                        List queryNameObj = (List) props.get(GeoServices.QUERY_NAME);
                        geoFence.setQueryName(queryNameObj != null ? queryNameObj.get(0).toString() : null);
                        List areaNameObj = (List) props.get(GeoServices.AREA_NAME);
                        geoFence.setAreaName(areaNameObj != null ? areaNameObj.get(0).toString() : null);
                        geoFence.setCreatedTime(childRes.getCreatedTime().getTime());
                        fences.add(geoFence);
                    }
                }
            }
            return fences;
        } catch (RegistryException | IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while getting the geo alerts for " + identifier.getType() + " with id: " +
                            identifier.getId(), e);
        }
    }

    @Override
    public List<GeoFence> getExitAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {

        Registry registry = getGovernanceRegistry();
        String registryPath = GeoServices.REGISTRY_PATH_FOR_ALERTS +
                GeoServices.ALERT_TYPE_EXIT + "/" + identifier.getId() + "/";
        Resource resource;
        try {
            resource = registry.get(registryPath);
        } catch (RegistryException e) {
            log.error("Error while reading the registry path: " + registryPath);
            return null;
        }

        try {
            List<GeoFence> fences = new ArrayList<>();
            if (resource != null) {
                Object contentObj = resource.getContent();
                if (contentObj instanceof String[]) {
                    String[] content = (String[]) contentObj;
                    for (String res : content) {
                        Resource childRes = registry.get(res);
                        Properties props = childRes.getProperties();

                        GeoFence geoFence = new GeoFence();

                        InputStream inputStream = childRes.getContentStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(inputStream, writer, "UTF-8");
                        geoFence.setGeoJson(writer.toString());

                        List queryNameObj = (List) props.get(GeoServices.QUERY_NAME);
                        geoFence.setQueryName(queryNameObj != null ? queryNameObj.get(0).toString() : null);
                        List areaNameObj = (List) props.get(GeoServices.AREA_NAME);
                        geoFence.setAreaName(areaNameObj != null ? areaNameObj.get(0).toString() : null);
                        geoFence.setCreatedTime(childRes.getCreatedTime().getTime());
                        fences.add(geoFence);
                    }
                }
            }
            return fences;
        } catch (RegistryException | IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while getting the geo alerts for " + identifier.getType() + " with id: " +
                            identifier.getId(), e);
        }
    }

    @Override
    public boolean createGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType)
            throws GeoLocationBasedServiceException {
        return saveGeoAlert(alert, identifier, alertType, false);
    }

    @Override
    public boolean updateGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType)
            throws GeoLocationBasedServiceException {
        return saveGeoAlert(alert, identifier, alertType, true);
    }

    public boolean saveGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType, boolean isUpdate)
            throws GeoLocationBasedServiceException {

        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Gson gson = new Gson();
        Map<String, String> parseMap = gson.fromJson(alert.getParseData(), type);

        Map<String, String> options = new HashMap<>();
        Object content = null;

        if (GeoServices.ALERT_TYPE_WITHIN.equals(alertType)) {
            options.put(GeoServices.QUERY_NAME, alert.getQueryName());
            options.put(GeoServices.AREA_NAME, alert.getCustomName());
            content = parseMap.get(GeoServices.GEO_FENCE_GEO_JSON);

        } else if (GeoServices.ALERT_TYPE_EXIT.equals(alertType)) {
            options.put(GeoServices.QUERY_NAME, alert.getQueryName());
            options.put(GeoServices.AREA_NAME, alert.getCustomName());
            content = parseMap.get(GeoServices.GEO_FENCE_GEO_JSON);

        } else if (GeoServices.ALERT_TYPE_SPEED.equals(alertType)) {
            content = parseMap.get(GeoServices.SPEED_ALERT_VALUE);

        } else if (GeoServices.ALERT_TYPE_PROXIMITY.equals(alertType)) {
            options.put(GeoServices.PROXIMITY_DISTANCE, alert.getProximityDistance());
            options.put(GeoServices.PROXIMITY_TIME, alert.getProximityTime());
            content = alert.getParseData();

        } else if (GeoServices.ALERT_TYPE_STATIONARY.equals(alertType)) {
            options.put(GeoServices.QUERY_NAME, alert.getQueryName());
            options.put(GeoServices.AREA_NAME, alert.getCustomName());
            options.put(GeoServices.STATIONARY_TIME, alert.getStationeryTime());
            options.put(GeoServices.FLUCTUATION_RADIUS, alert.getFluctuationRadius());
            content = alert.getParseData();

        } else if (GeoServices.ALERT_TYPE_TRAFFIC.equals(alertType)) {
            content = parseMap.get(GeoServices.GEO_FENCE_GEO_JSON);
        } else {
            throw new GeoLocationBasedServiceException(
                    "Unrecognized execution plan type: " + alertType + " while creating geo alert");
        }

        //persist alert in registry
        updateRegistry(getRegistryPath(alertType, identifier, alert.getQueryName()), identifier, content,
                       options);

        //deploy alert into event processor
        EventProcessorAdminServiceStub eventprocessorStub = null;
        String action = (isUpdate ? "updating" : "creating");
        try {
            eventprocessorStub = getEventProcessorAdminServiceStub();
            String parsedTemplate = parseTemplate(alertType, parseMap);
            String validationResponse = eventprocessorStub.validateExecutionPlan(parsedTemplate);
            if (validationResponse.equals("success")) {
                if (isUpdate) {
                    String executionPlanName = getExecutionPlanName(alertType, alert.getQueryName(),
                                                                    identifier.getId());
                    eventprocessorStub.editActiveExecutionPlan(parsedTemplate, executionPlanName);
                } else {
                    eventprocessorStub.deployExecutionPlan(parsedTemplate);
                }
            } else {
                if (validationResponse.startsWith(
                        "'within' is neither a function extension nor an aggregated attribute extension"
                )) {
                    log.error("GPL Siddhi Geo Extension is not configured. Please execute maven script " +
                                      "`siddhi-geo-extention-deployer.xml` in $IOT_HOME/analytics/scripts");
                } else {
                    log.error("Execution plan validation failed: " + validationResponse);
                }
                throw new GeoLocationBasedServiceException(
                        "Error occurred while " + action + " geo " + alertType + " alert for " +
                                identifier.getType() + " with id: " + identifier.getId());
            }
            return true;
        } catch (AxisFault axisFault) {
            throw new GeoLocationBasedServiceException(
                    "Event processor admin service initialization failed while " + action + " geo alert '" +
                            alertType + "' for " + identifier.getType() + " " +
                            "device with id: " + identifier.getId(), axisFault
            );
        } catch (IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Event processor admin service failed while " + action + " geo alert '" +
                            alertType + "' for " + identifier.getType() + " " +
                            "device with id: " + identifier.getId(), e);
        } catch (JWTClientException e) {
            throw new GeoLocationBasedServiceException(
                    "JWT token creation failed while " + action + " geo alert '" + alertType + "' for " +
                            identifier.getType() + " device with id:" + identifier.getId(), e);
        } finally {
            cleanup(eventprocessorStub);
        }
    }

    private String getRegistryPath(String alertType, DeviceIdentifier identifier, String queryName)
            throws GeoLocationBasedServiceException {
        String path = "";
        if (GeoServices.ALERT_TYPE_WITHIN.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_WITHIN +
                    "/" + identifier.getId() + "/" + queryName;
        } else if (GeoServices.ALERT_TYPE_EXIT.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_EXIT +
                    "/" + identifier.getId() + "/" + queryName;
        } else if (GeoServices.ALERT_TYPE_SPEED.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_SPEED +
                    "/" + identifier.getId();
        } else if (GeoServices.ALERT_TYPE_PROXIMITY.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_PROXIMITY +
                    "/" + identifier.getId() + "/" + queryName;
        } else if (GeoServices.ALERT_TYPE_STATIONARY.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_STATIONARY +
                    "/" + identifier.getId() + "/" + queryName;
        } else if (GeoServices.ALERT_TYPE_TRAFFIC.equals(alertType)) {
            path = GeoServices.REGISTRY_PATH_FOR_ALERTS + GeoServices.ALERT_TYPE_TRAFFIC +
                    "/" + identifier.getId() + "/" + queryName;
        } else {
            throw new GeoLocationBasedServiceException(
                    "Unrecognized execution plan type: " + alertType);
        }
        return path;
    }

    private String getExecutionPlanName(String alertType, String queryName, String deviceId) {
        if ("Traffic".equals(alertType)) {
            return "Geo-ExecutionPlan-Traffic_" + queryName + "_alert";
        } else {
            return "Geo-ExecutionPlan-" + alertType + "_" + queryName + "---_" + deviceId + "_alert";
        }
    }

    @Override
    public boolean removeGeoAlert(String alertType, DeviceIdentifier identifier, String queryName)
            throws GeoLocationBasedServiceException {
        removeFromRegistry(alertType, identifier, queryName);
        String executionPlanName = getExecutionPlanName(alertType, queryName, identifier.getId());
        EventProcessorAdminServiceStub eventprocessorStub = null;
        try {
            eventprocessorStub = getEventProcessorAdminServiceStub();
            eventprocessorStub.undeployActiveExecutionPlan(executionPlanName);
            return true;
        } catch (IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Event processor admin service stub invocation failed while removing geo alert '" +
                            alertType +
                            "': " + executionPlanName + " for " +
                            identifier.getType() + " device with id:" + identifier.getId(), e
            );
        } catch (JWTClientException e) {
            throw new GeoLocationBasedServiceException(
                    "JWT token creation failed while removing geo alert '" + alertType + "': " +
                            executionPlanName + " for " +
                            identifier.getType() + " device with id:" + identifier.getId(), e
            );
        } finally {
            cleanup(eventprocessorStub);
        }
    }

    private void removeFromRegistry(String alertType, DeviceIdentifier identifier, String queryName)
            throws GeoLocationBasedServiceException {
        String path = "unknown";
        try {
            path = getRegistryPath(alertType, identifier, queryName);
            getGovernanceRegistry().delete(path);
        } catch (RegistryException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while removing " + alertType + " alert for " + identifier.getType() +
                            " device with id:" + identifier.getId() + " from the path: " + path);
        }
    }

    private EventProcessorAdminServiceStub getEventProcessorAdminServiceStub() throws JWTClientException {
        //send alert to event-processing
        String eventProcessorAdminServiceWSUrl = Utils.replaceSystemProperty(GeoServices.DAS_URL) +
                "/services/EventProcessorAdminService";

        //Getting the tenant Domain
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String tenantAdminUser = username + "@" + tenantDomain;

        try {
            //Create the SSL context with the loaded TrustStore/keystore.
            SSLContext sslContext = initSSLConnection(tenantAdminUser);
            JWTClient jwtClient = getJWTClientManagerService().getJWTClient();

            String authValue = AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                    jwtClient.getJwtToken(tenantAdminUser).getBytes()));

            EventProcessorAdminServiceStub eventprocessorStub = new EventProcessorAdminServiceStub(
                    eventProcessorAdminServiceWSUrl);

            Options eventProcessorOption = eventprocessorStub._getServiceClient().getOptions();
            if (eventProcessorOption == null) {
                eventProcessorOption = new Options();
            }

            List<Header> list = new ArrayList<>();
            Header httpHeader = new Header();
            httpHeader.setName(AUTHORIZATION_HEADER);
            httpHeader.setValue(authValue);
            list.add(httpHeader);//"https"

            eventProcessorOption.setProperty(HTTPConstants.HTTP_HEADERS, list);
            eventProcessorOption.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                    , new Protocol(DEFAULT_HTTP_PROTOCOL
                            , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                            , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));
            eventprocessorStub._getServiceClient().setOptions(eventProcessorOption);

            return eventprocessorStub;
        } catch (CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException |
                KeyManagementException | IOException e) {
            throw new JWTClientException("JWT token creation failed for the Event Processor Stub", e);
        }
    }

    @Override
    public String getSpeedAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {
        try {
            Registry registry = getGovernanceRegistry();
            Resource resource = registry.get(GeoServices.REGISTRY_PATH_FOR_ALERTS +
                                                     GeoServices.ALERT_TYPE_SPEED + "/" + identifier.getId());
            if (resource == null) {
                return "{'content': false}";
            }
            InputStream inputStream = resource.getContentStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, "UTF-8");
            return "{'speedLimit':" + writer.toString() + "}";
        } catch (RegistryException | IOException e) {
            return "{'content': false}";
        }
    }

    @Override
    public String getProximityAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {
        try {
            Registry registry = getGovernanceRegistry();
            Resource resource = registry.get(GeoServices.REGISTRY_PATH_FOR_ALERTS +
                                                     GeoServices.ALERT_TYPE_PROXIMITY
                                                     + "/" + identifier.getId());
            if (resource != null) {
                Properties props = resource.getProperties();

                List proxDisObj = (List) props.get(GeoServices.PROXIMITY_DISTANCE);
                List proxTimeObj = (List) props.get(GeoServices.PROXIMITY_TIME);

                return String.format("{proximityDistance:\"%s\", proximityTime:\"%s\"}",
                                     proxDisObj != null ? proxDisObj.get(0).toString() : "",
                                     proxTimeObj != null ? proxTimeObj.get(0).toString() : "");
            } else {
                return "{'content': false}";
            }
        } catch (RegistryException e) {
            return "{'content': false}";
        }
    }

    @Override
    public List<GeoFence> getStationaryAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {

        Registry registry = getGovernanceRegistry();
        String registryPath = GeoServices.REGISTRY_PATH_FOR_ALERTS +
                GeoServices.ALERT_TYPE_STATIONARY + "/" + identifier.getId() + "/";
        Resource resource;
        try {
            resource = registry.get(registryPath);
        } catch (RegistryException e) {
            log.error("Error while reading the registry path: " + registryPath);
            return null;
        }

        try {
            List<GeoFence> fences = new ArrayList<>();
            if (resource != null) {
                Object contentObj = resource.getContent();

                if (contentObj instanceof String[]) {
                    String[] content = (String[]) contentObj;
                    for (String res : content) {
                        Resource childRes = registry.get(res);
                        Properties props = childRes.getProperties();
                        GeoFence geoFence = new GeoFence();

                        InputStream inputStream = childRes.getContentStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(inputStream, writer, "UTF-8");
                        geoFence.setGeoJson(writer.toString());

                        List queryNameObj = (List) props.get(GeoServices.QUERY_NAME);
                        geoFence.setQueryName(queryNameObj != null ? queryNameObj.get(0).toString() : null);
                        List areaNameObj = (List) props.get(GeoServices.AREA_NAME);
                        geoFence.setAreaName(areaNameObj != null ? areaNameObj.get(0).toString() : null);
                        List sTimeObj = (List) props.get(GeoServices.STATIONARY_TIME);
                        geoFence.setStationaryTime(sTimeObj != null ? sTimeObj.get(0).toString() : null);
                        List fluctRadiusObj = (List) props.get(GeoServices.FLUCTUATION_RADIUS);
                        geoFence.setFluctuationRadius(fluctRadiusObj != null ? fluctRadiusObj.get(0).toString() : null);
                        geoFence.setCreatedTime(childRes.getCreatedTime().getTime());
                        fences.add(geoFence);
                    }
                }
            }
            return fences;
        } catch (RegistryException | IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while getting the geo alerts for " + identifier.getType() + " with id: " +
                            identifier.getId(), e);
        }
    }

    @Override
    public List<GeoFence> getTrafficAlerts(DeviceIdentifier identifier) throws GeoLocationBasedServiceException {
        Registry registry = getGovernanceRegistry();
        String registryPath = GeoServices.REGISTRY_PATH_FOR_ALERTS +
                GeoServices.ALERT_TYPE_STATIONARY + "/" + identifier.getId() + "/";
        Resource resource;
        try {
            resource = registry.get(registryPath);
        } catch (RegistryException e) {
            log.error("Error while reading the registry path: " + registryPath);
            return null;
        }

        try {
            List<GeoFence> fences = new ArrayList<>();
            if (resource != null) {
                Object contentObj = resource.getContent();
                if (contentObj instanceof String[]) {
                    String[] content = (String[]) contentObj;
                    for (String res : content) {
                        Resource childRes = registry.get(res);
                        Properties props = childRes.getProperties();

                        GeoFence geoFence = new GeoFence();

                        InputStream inputStream = childRes.getContentStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(inputStream, writer, "UTF-8");
                        geoFence.setGeoJson(writer.toString());

                        List queryNameObj = (List) props.get(GeoServices.QUERY_NAME);
                        geoFence.setQueryName(queryNameObj != null ? queryNameObj.get(0).toString() : null);
                        List sNameObj = (List) props.get(GeoServices.STATIONARY_NAME);
                        geoFence.setAreaName(sNameObj != null ? sNameObj.get(0).toString() : null);
                        geoFence.setCreatedTime(childRes.getCreatedTime().getTime());
                        fences.add(geoFence);
                    }
                }
            }
            return fences;
        } catch (RegistryException | IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while getting the geo alerts for " + identifier.getType() + " with id: " +
                            identifier.getId(), e);
        }
    }

    private Registry getGovernanceRegistry() throws GeoLocationBasedServiceException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return DeviceManagementDataHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(
                            tenantId);
        } catch (RegistryException e) {
            throw new GeoLocationBasedServiceException(
                    "Error in retrieving governance registry instance: " +
                            e.getMessage(), e);
        }
    }

    private String parseTemplate(String alertType, Map<String, String> parseMap) throws
                                                                                 GeoLocationBasedServiceException {
        String templatePath = "alerts/Geo-ExecutionPlan-" + alertType + "_alert.siddhiql";
        InputStream resource = getClass().getClassLoader().getResourceAsStream(templatePath);
        if (resource == null) {
            throw new GeoLocationBasedServiceException("Could not find template in path : " + templatePath);
        }
        try {
            //Read template
            String template = IOUtils.toString(resource, StandardCharsets.UTF_8.toString());
            //Replace variables
            for (Map.Entry<String, String> parseEntry : parseMap.entrySet()) {
                String find = "\\$" + parseEntry.getKey();
                template = template.replaceAll(find, parseEntry.getValue());
            }
            return template;
        } catch (IOException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while populating the template for the Within Alert", e);
        }
    }

    private void updateRegistry(String path, DeviceIdentifier identifier, Object content, Map<String, String> options)
            throws GeoLocationBasedServiceException {
        try {

            Registry registry = getGovernanceRegistry();
            Resource newResource = registry.newResource();
            newResource.setContent(content);
            newResource.setMediaType("application/json");
            for (Map.Entry<String, String> option : options.entrySet()) {
                newResource.addProperty(option.getKey(), option.getValue());
            }
            registry.put(path, newResource);
        } catch (RegistryException e) {
            throw new GeoLocationBasedServiceException(
                    "Error occurred while setting the Within Alert for " + identifier.getType() + " with id: " +
                            identifier.getId(), e);
        }
    }

    /**
     * Loads the keystore.
     *
     * @param keyStorePath     - the path of the keystore
     * @param keyStorePassword - the keystore password
     */
    private KeyStore loadKeyStore(String keyStorePath, char[] keyStorePassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fis = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
            fis = new FileInputStream(keyStorePath);
            keyStore.load(fis, keyStorePassword);
            return keyStore;
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
    private KeyStore loadTrustStore(String trustStorePath, char[] tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        InputStream fis = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
            fis = new FileInputStream(trustStorePath);
            trustStore.load(fis, tsPassword);
            return trustStore;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Initializes the SSL Context
     */
    private SSLContext initSSLConnection(String tenantAdminUser)
            throws NoSuchAlgorithmException, UnrecoverableKeyException,
                   KeyStoreException, KeyManagementException, IOException, CertificateException {
        String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
        String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Password");
        String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
        String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                "Security.TrustStore.Location");

        //Call to load the keystore.
        KeyStore keyStore = loadKeyStore(keyStoreLocation, keyStorePassword.toCharArray());
        //Call to load the TrustStore.
        KeyStore trustStore = loadTrustStore(trustStoreLocation, trustStorePassword.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication

        SSLContext sslContext = SSLContext.getInstance(SSLV3);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
        return sslContext;
    }

    private void cleanup(Stub stub) {
        if (stub != null) {
            try {
                stub.cleanup();
            } catch (AxisFault axisFault) {
                //do nothing
            }
        }
    }

    public static JWTClientManagerService getJWTClientManagerService() {
        JWTClientManagerService jwtClientManagerService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        jwtClientManagerService = (JWTClientManagerService) ctx.getOSGiService(JWTClientManagerService.class, null);
        if (jwtClientManagerService == null) {
            String msg = "jwtClientManagerServicehas not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return jwtClientManagerService;
    }
}
