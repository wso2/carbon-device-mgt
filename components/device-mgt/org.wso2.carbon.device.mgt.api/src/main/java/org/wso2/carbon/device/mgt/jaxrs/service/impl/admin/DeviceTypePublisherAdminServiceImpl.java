/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.java.security.SSLProtocolSocketFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.application.mgt.stub.upload.CarbonAppUploaderStub;
import org.wso2.carbon.application.mgt.stub.upload.types.carbon.UploadedFileItem;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceTypePublisherAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.event.receiver.stub.EventReceiverAdminServiceStub;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.activation.DataHandler;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

@Path("/admin/devicetype")
public class DeviceTypePublisherAdminServiceImpl implements DeviceTypePublisherAdminService {

    /**
     * required soap header for authorization
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * required soap header value for mutualSSL
     */
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer";

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



    private KeyStore keyStore;
    private KeyStore trustStore;
    private char[] keyStorePassword;
    private SSLContext sslContext;

    private String tenantDomain;

    private static final Log log = LogFactory.getLog(DeviceTypePublisherAdminServiceImpl.class);
    private static final String DEFAULT_RESOURCE_LOCATION = "/resources/devicetypes";
    private static final String CAR_FILE_LOCATION = CarbonUtils.getCarbonHome() + File.separator + "repository" +
    File.separator + "resources" + File.separator + "devicetypes";
    private static final String DAS_PORT = "${iot.analytics.https.port}";
    private static final String DAS_HOST_NAME = "${iot.analytics.host}";
    private static final String DEFAULT_HTTP_PROTOCOL = "https";
    private static final String IOT_MGT_PORT = "${iot.manager.https.port}";
    private static final String IOT_MGT_HOST_NAME = "${iot.manager.host}";
    private static final String DAS_URL = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/CarbonAppUploader/";
    private static final String DAS_EVENT_RECEIVER_EP = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/EventReceiverAdminService/";
    private static final String DAS_EVENT_STREAM_EP = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/EventStreamAdminService/";

    private static final String IOT_MGT_URL = DEFAULT_HTTP_PROTOCOL + "://" + IOT_MGT_HOST_NAME
            + ":" + IOT_MGT_PORT + "/services/CarbonAppUploader/";
    private static final String MEDIA_TYPE_XML = "application/xml";
    private static final String DEVICE_MANAGEMENT_TYPE = "device_management";
    private static final String TENANT_DOMAIN_PROPERTY = "\\$\\{tenant-domain\\}";


    @Override
    @POST
    @Path("/deploy/{type}")
    public Response doPublish(@PathParam("type") String type) {
        try {
            //Getting the tenant Domain
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantAdminUser = username + "@" + tenantDomain;

            String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
            String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Password");
            String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
            String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Location");

            //Call to load the keystore.
            loadKeyStore(keyStoreLocation, keyStorePassword);
            //Call to load the TrustStore.
            loadTrustStore(trustStoreLocation, trustStorePassword);
            //Create the SSL context with the loaded TrustStore/keystore.
            initSSLConnection();
            JWTClient jwtClient = DeviceMgtAPIUtils.getJWTClientManagerService().getJWTClient();

            String authValue =  AUTHORIZATION_HEADER_VALUE + " " + new String(Base64.encodeBase64(
                    jwtClient.getJwtToken(tenantAdminUser).getBytes()));

            List<Header> list = new ArrayList<>();
            Header httpHeader = new Header();
            httpHeader.setName(AUTHORIZATION_HEADER);
            httpHeader.setValue(authValue);
            list.add(httpHeader);//"https"

            List<String> streamFileList = getStreamsList(type);
            List<String> receiverFileList = getReceiversList(type);

            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                if (streamFileList != null) {
                    publishDynamicEventStream(type, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, streamFileList);
                }
                if (receiverFileList != null) {
                    publishDynamicEventReceivers(type, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, receiverFileList);
                }
            }
            if (deployAnalyticsCapp(type, list)){
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("\"Error, Artifact does not exist.\"").build();
            }
            if (receiverFileList != null) {
                publishDynamicEventReceivers(type, tenantDomain, receiverFileList);
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = DeviceMgtAPIUtils.getRegistryService().getConfigSystemRegistry(tenantId);
            if (!registry.resourceExists(DEFAULT_RESOURCE_LOCATION + type + ".exist")) {
                Resource resource = new ResourceImpl();
                resource.setContent("</exist>");
                resource.setMediaType(MEDIA_TYPE_XML);
                registry.put(DEFAULT_RESOURCE_LOCATION + type + ".exist", resource);
            }
            return Response.status(Response.Status.CREATED).entity("\"OK. \\n Successfully uploaded the artifacts.\"")
                    .build();
        } catch (AxisFault e) {
            log.error("failed to publish event definitions for tenantDomain:" + tenantDomain, e);
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
        } catch (CertificateException | UnrecoverableKeyException | KeyStoreException |
                KeyManagementException | IOException | NoSuchAlgorithmException e) {
            log.error("Failed to access keystore for, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (RegistryException e) {
            log.error("Failed to load tenant, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (ParseException e) {
            log.error("Invalid stream definition for device type" + type + " for tenant, tenantDomain: " + tenantDomain, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean deployAnalyticsCapp(@PathParam("type") String type, List<Header> list) throws IOException, RegistryException {
        CarbonAppUploaderStub carbonAppUploaderStub = null;
        try {
            File directory = new File(CAR_FILE_LOCATION + File.separator + type);
            if (directory.isDirectory() && directory.exists()) {
                UploadedFileItem[] uploadedFileItems = loadCappFromFileSystem(type);
                if (uploadedFileItems.length > 0) {
                    carbonAppUploaderStub = new CarbonAppUploaderStub(Utils.replaceSystemProperty(
                            IOT_MGT_URL));
                    Options appUploaderOptions = carbonAppUploaderStub._getServiceClient().getOptions();
                    if (appUploaderOptions == null) {
                        appUploaderOptions = new Options();
                    }
                    appUploaderOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
                    appUploaderOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                            , new Protocol(DEFAULT_HTTP_PROTOCOL, (ProtocolSocketFactory) new SSLProtocolSocketFactory
                            (sslContext), Integer.parseInt(Utils.replaceSystemProperty(IOT_MGT_PORT))));

                    carbonAppUploaderStub._getServiceClient().setOptions(appUploaderOptions);
                    carbonAppUploaderStub.uploadApp(uploadedFileItems);

                    if (!DEVICE_MANAGEMENT_TYPE.equals(type.toLowerCase())) {
                        carbonAppUploaderStub = new CarbonAppUploaderStub(Utils.replaceSystemProperty(DAS_URL));
                        appUploaderOptions = carbonAppUploaderStub._getServiceClient().getOptions();
                        if (appUploaderOptions == null) {
                            appUploaderOptions = new Options();
                        }
                        appUploaderOptions.setProperty(HTTPConstants.HTTP_HEADERS, list);
                        appUploaderOptions.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER
                                , new Protocol(DEFAULT_HTTP_PROTOCOL
                                , (ProtocolSocketFactory) new SSLProtocolSocketFactory(sslContext)
                                , Integer.parseInt(Utils.replaceSystemProperty(DAS_PORT))));

                        carbonAppUploaderStub._getServiceClient().setOptions(appUploaderOptions);
                        carbonAppUploaderStub.uploadApp(uploadedFileItems);
                    }
                }
            } else {
                return true;
            }
            return false;
        } finally {
            cleanup(carbonAppUploaderStub);
        }
    }

    @GET
    @Path("/deploy/{type}/status")
    @Override
    public Response getStatus(@PathParam("type") String deviceType) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry;
        try {
            registry = DeviceMgtAPIUtils.getRegistryService().getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(DEFAULT_RESOURCE_LOCATION + deviceType + ".exist")) {
                return Response.status(Response.Status.OK).entity("Exist").build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).entity("Does not Exist").build();
            }
        } catch (RegistryException e) {
            log.error("Registry failed to load." + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    "\"Error, Artifact status check has failed\"").build();
        }
    }


    private void publishDynamicEventReceivers(String deviceType, String tenantDomain, List<String> receiversList)
            throws IOException, UserStoreException, JWTClientException {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        EventReceiverAdminServiceStub receiverAdminServiceStub = null;
        try {
            receiverAdminServiceStub = new EventReceiverAdminServiceStub
                    (Utils.replaceSystemProperty(DAS_EVENT_RECEIVER_EP));
            Options eventReciverOptions = receiverAdminServiceStub._getServiceClient().getOptions();
            if (eventReciverOptions == null) {
                eventReciverOptions = new Options();
            }
            String username;
            if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
               username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getRealmConfiguration().getAdminUserName()+"@"+tenantDomain;
            }else {
                username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getRealmConfiguration().getAdminUserName();
            }


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
            for (String receiverContent:receiversList) {
                receiverAdminServiceStub.deployEventReceiverConfiguration(receiverContent);
            }
        } finally {
            cleanup(receiverAdminServiceStub);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void publishDynamicEventStream(String deviceType, String tenantDomain, List<String> streamList)
            throws IOException, UserStoreException, JWTClientException, ParseException {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        EventStreamAdminServiceStub eventStreamAdminServiceStub = null;
        try {
            eventStreamAdminServiceStub = new EventStreamAdminServiceStub
                    (Utils.replaceSystemProperty(DAS_EVENT_STREAM_EP));
            Options eventReciverOptions = eventStreamAdminServiceStub._getServiceClient().getOptions();
            if (eventReciverOptions == null) {
                eventReciverOptions = new Options();
            }
            String username;
            if(!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getRealmConfiguration().getAdminUserName()+"@"+tenantDomain;
            }else {
                username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getRealmConfiguration().getAdminUserName();
            }


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

            eventStreamAdminServiceStub._getServiceClient().setOptions(eventReciverOptions);
            for (String streamContent:streamList) {
                JSONParser jsonParser = new JSONParser();
                JSONObject steamJson = (JSONObject)jsonParser.parse(streamContent);
                String name = (String) steamJson.get("name");
                String version = (String) steamJson.get("version");
                String streamId = name +":"+version;
                if (eventStreamAdminServiceStub.getStreamDefinitionDto(streamId) == null) {
                    eventStreamAdminServiceStub.addEventStreamDefinitionAsString(streamContent);
                }
            }
        } finally {
            cleanup(eventStreamAdminServiceStub);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    private List<String> getReceiversList(String deviceType) throws IOException {
        File directory = new File(CAR_FILE_LOCATION + File.separator + deviceType+File.separator+"receiver");
        if (!directory.exists()) {
            return null;
        }
        File[] receiverFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
       List<String> receiverList = new ArrayList<>();
        for (File receiverFile:receiverFiles) {
            String receiverContentTemplate =new String(Files.readAllBytes(receiverFile.toPath()));
            final String receiverContent = receiverContentTemplate.replaceAll(TENANT_DOMAIN_PROPERTY, tenantDomain.toLowerCase());
            receiverList.add(receiverContent);
        }

        return receiverList;
    }

    private List<String> getStreamsList(String deviceType) throws IOException {
        File directory = new File(CAR_FILE_LOCATION + File.separator + deviceType+File.separator+"streams");
        if (!directory.exists()) {
            return null;
        }
        File[] receiverFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".json");
            }
        });
        List<String> streamList = new ArrayList<>();
        for (File StreamFile:receiverFiles) {
            String streamContent =new String(Files.readAllBytes(StreamFile.toPath()));
            streamList.add(streamContent);
        }
        return streamList;
    }

    private UploadedFileItem[] loadCappFromFileSystem(String deviceType) throws IOException {

        File directory = new File(CAR_FILE_LOCATION + File.separator + deviceType);
        File[] carFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".car");
            }
        });
        List<UploadedFileItem> uploadedFileItemLis = new ArrayList<>();
        if (carFiles != null) {

            for (File carFile : carFiles) {
                UploadedFileItem uploadedFileItem = new UploadedFileItem();
                DataHandler param = new DataHandler(carFile.toURI().toURL());
                uploadedFileItem.setDataHandler(param);
                uploadedFileItem.setFileName(carFile.getName());
                uploadedFileItem.setFileType("jar");
                uploadedFileItemLis.add(uploadedFileItem);
            }
        }
        UploadedFileItem[] fileItems = new UploadedFileItem[uploadedFileItemLis.size()];
        fileItems = uploadedFileItemLis.toArray(fileItems);
        return fileItems;
    }

    /**
     * Loads the keystore.
     *
     * @param keyStorePath - the path of the keystore
     * @param ksPassword   - the keystore password
     */
    private void loadKeyStore(String keyStorePath, String ksPassword)
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
    private void loadTrustStore(String trustStorePath, String tsPassword)
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
    private void initSSLConnection() throws NoSuchAlgorithmException, UnrecoverableKeyException,
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

    private void cleanup(Stub stub) {
        if (stub != null) {
            try {
                stub.cleanup();
            } catch (AxisFault axisFault) {
                //do nothing
            }
        }
    }

}

