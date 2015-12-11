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

package org.wso2.carbon.dynamic.client.web.proxy.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class holds the necessary logic to create and delete service-providers by invoking the
 * dynamic-client-registration endpoint.
 */
public class RemoteDCRClient {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CHARSET_UTF_8 = "UTF-8";

    public static CloseableHttpResponse createOAuthApplication(RegistrationProfile registrationProfile)
            throws DynamicClientRegistrationException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String clientName = registrationProfile.getClientName();
        String host = DCRProxyUtils.getKeyManagerHost();
        try {
            // Setup the HTTPS settings to accept any certificate.
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme(Constants.RemoteServiceProperties.
                                                 DYNAMIC_CLIENT_SERVICE_PROTOCOL, socketFactory, getServerHTTPSPort()));
            SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
            httpClient = new DefaultHttpClient(mgr, httpClient.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            URI uri = new URIBuilder().setScheme(Constants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    Constants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT).build();
            Gson gson = new Gson();
            StringEntity entity = new StringEntity(gson.toJson(registrationProfile), CONTENT_TYPE_APPLICATION_JSON,
                                                   CHARSET_UTF_8);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            return httpClient.execute(httpPost);
        } catch (URISyntaxException e) {
            throw new DynamicClientRegistrationException("Exception occurred while constructing the URI for invoking " +
                                                         "DCR endpoint for registering service-provider for web-app : "
                                                         + clientName, e);
        } catch (UnsupportedEncodingException e) {
            throw new DynamicClientRegistrationException("Exception occurred while constructing the payload for invoking " +
                                                         "DCR endpoint for registering service-provider for web-app : "
                                                         + clientName, e);
        } catch (IOException e) {
            throw new DynamicClientRegistrationException("Connection error occurred while invoking DCR endpoint for" +
                                                         " registering service-provider for web-app : " + clientName, e);
        }
    }

    public static CloseableHttpResponse deleteOAuthApplication(String user, String appName, String clientid)
            throws DynamicClientRegistrationException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String host = DCRProxyUtils.getKeyManagerHost();
        try {
            // Setup the HTTPS settings to accept any certificate.
            HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

            SchemeRegistry registry = new SchemeRegistry();
            SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
            socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
            registry.register(new Scheme(Constants.RemoteServiceProperties.
                                                 DYNAMIC_CLIENT_SERVICE_PROTOCOL, socketFactory, getServerHTTPSPort()));
            SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
            httpClient = new DefaultHttpClient(mgr, httpClient.getParams());

            // Set verifier
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

            URI uri = new URIBuilder().setScheme(Constants.RemoteServiceProperties.
                            DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    Constants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT)
                                                           .setParameter("applicationName", appName)
                                                           .setParameter("userId", user)
                                                           .setParameter("consumerKey", clientid).build();
            HttpDelete httpDelete = new HttpDelete(uri);
            return httpClient.execute(httpDelete);
        } catch (IOException e) {
            throw new DynamicClientRegistrationException("Connection error occurred while constructing the payload for " +
                                                "invoking DCR endpoint for unregistering the web-app : " + appName, e);
        } catch (URISyntaxException e) {
            throw new DynamicClientRegistrationException("Exception occurred while constructing the URI for invoking " +
                                                         "DCR endpoint for unregistering the web-app : " + appName, e);
        }
    }

    private static int getServerHTTPSPort() {
        // HTTPS port
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService = DCRProxyUtils.getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                                                  mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return  port;
    }
}