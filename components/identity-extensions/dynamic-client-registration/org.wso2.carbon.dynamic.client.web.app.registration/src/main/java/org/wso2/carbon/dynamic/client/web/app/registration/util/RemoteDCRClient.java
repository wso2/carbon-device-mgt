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

package org.wso2.carbon.dynamic.client.web.app.registration.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.dynamic.client.registration.DynamicClientRegistrationException;
import org.wso2.carbon.dynamic.client.registration.OAuthApplicationInfo;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.web.app.registration.internal.DynamicClientWebAppRegistrationDataHolder;
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

    private static final Log log = LogFactory.getLog(RemoteDCRClient.class);

    public static OAuthApplicationInfo createOAuthApplication(RegistrationProfile registrationProfile, String host)
            throws DynamicClientRegistrationException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking DCR service to create OAuth application for web app : " + registrationProfile.
                                                                                                      getClientName());
        }
        DefaultHttpClient httpClient = getHTTPSClient();
        String clientName = registrationProfile.getClientName();
        try {
            URI uri = new URIBuilder().setScheme(DynamicClientWebAppRegistrationConstants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    DynamicClientWebAppRegistrationConstants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT)
                                      .build();
            Gson gson = new Gson();
            StringEntity entity = new StringEntity(gson.toJson(registrationProfile),
                                                   DynamicClientWebAppRegistrationConstants.ContentTypes.CONTENT_TYPE_APPLICATION_JSON,
                                                   DynamicClientWebAppRegistrationConstants.CharSets.CHARSET_UTF8);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            HttpResponse response = httpClient.execute(httpPost);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity responseData = response.getEntity();
            String responseString = EntityUtils.toString(responseData, DynamicClientWebAppRegistrationConstants.
                    CharSets.CHARSET_UTF8);
            if (status != 201) {
                String msg = "Backend server error occurred while invoking DCR endpoint for " +
                        "registering service-provider upon web-app : '" + clientName + "'; Server returned response '" +
                        responseString + "' with HTTP status code '" + status + "'";
                throw new DynamicClientRegistrationException(msg);
            }
            return getOAuthApplicationInfo(gson.fromJson(responseString, JsonElement.class));
        } catch (URISyntaxException e) {
            throw new DynamicClientRegistrationException("Exception occurred while constructing the URI for invoking " +
                                                         "DCR endpoint for registering service-provider for web-app : "
                                                         + clientName, e);
        } catch (UnsupportedEncodingException e) {
            throw new DynamicClientRegistrationException(
                    "Exception occurred while constructing the payload for invoking " +
                    "DCR endpoint for registering service-provider for web-app : "
                    + clientName, e);
        } catch (IOException e) {
            throw new DynamicClientRegistrationException("Connection error occurred while invoking DCR endpoint for" +
                                                         " registering service-provider for web-app : " + clientName,
                                                         e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }

    public static boolean deleteOAuthApplication(String user, String appName, String clientid, String host)
            throws DynamicClientRegistrationException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking DCR service to remove OAuth application created for web app : " + appName);
        }
        DefaultHttpClient httpClient = getHTTPSClient();
        try {
             URI uri = new URIBuilder().setScheme(DynamicClientWebAppRegistrationConstants.RemoteServiceProperties.
                                                         DYNAMIC_CLIENT_SERVICE_PROTOCOL).setHost(host).setPath(
                    DynamicClientWebAppRegistrationConstants.RemoteServiceProperties.DYNAMIC_CLIENT_SERVICE_ENDPOINT)
                                      .setParameter("applicationName", appName)
                                      .setParameter("userId", user)
                                      .setParameter("consumerKey", clientid).build();
            HttpDelete httpDelete = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(httpDelete);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                return true;
            }
        } catch (IOException e) {
            throw new DynamicClientRegistrationException(
                    "Connection error occurred while constructing the payload for " +
                    "invoking DCR endpoint for unregistering the web-app : " + appName, e);
        } catch (URISyntaxException e) {
            throw new DynamicClientRegistrationException("Exception occurred while constructing the URI for invoking " +
                                                         "DCR endpoint for unregistering the web-app : " + appName, e);
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return false;
    }

    private static int getServerHTTPSPort() {
        // HTTPS port
        String mgtConsoleTransport = CarbonUtils.getManagementTransport();
        ConfigurationContextService configContextService =
                DynamicClientWebAppRegistrationDataHolder.getInstance().getConfigurationContextService();
        int port = CarbonUtils.getTransportPort(configContextService, mgtConsoleTransport);
        int httpsProxyPort =
                CarbonUtils.getTransportProxyPort(configContextService.getServerConfigContext(),
                                                  mgtConsoleTransport);
        if (httpsProxyPort > 0) {
            port = httpsProxyPort;
        }
        return port;
    }

    private static OAuthApplicationInfo getOAuthApplicationInfo(JsonElement jsonData) {
        JsonObject jsonObject = jsonData.getAsJsonObject();
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        JsonElement property = jsonObject.get("client_id");
        if (property != null) {
            oAuthApplicationInfo.setClientId(property.getAsString());
        }
        property = jsonObject.get("client_name");
        if (property != null) {
            oAuthApplicationInfo.setClientName(property.getAsString());
        }
        property = jsonObject.get("client_secret");
        if (property != null) {
            oAuthApplicationInfo.setClientSecret(property.getAsString());
        }
        return oAuthApplicationInfo;
    }

    private static DefaultHttpClient getHTTPSClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // Setup the HTTPS settings to accept any certificate.
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme(DynamicClientWebAppRegistrationConstants.RemoteServiceProperties.
                                             DYNAMIC_CLIENT_SERVICE_PROTOCOL, socketFactory, getServerHTTPSPort()));
        SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
        httpClient = new DefaultHttpClient(mgr, httpClient.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        return httpClient;
    }
}
