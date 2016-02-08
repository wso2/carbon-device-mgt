/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.dynamic.client.web.proxy.util;

import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.Response;

/**
 * Holds the utility methods used by DCR proxy app.
 */
public class DCRProxyUtils {

    public static ConfigurationContextService getConfigurationContextService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return  (ConfigurationContextService) ctx.getOSGiService(ConfigurationContextService.class, null);
    }

    public static DefaultHttpClient getHttpsClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // Setup the HTTPS settings to accept any certificate.
        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
        registry.register(new Scheme(Constants.RemoteServiceProperties.
                                             DYNAMIC_CLIENT_SERVICE_PROTOCOL, socketFactory, DCRProxyUtils.getServerHTTPSPort()));
        SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
        httpClient = new DefaultHttpClient(mgr, httpClient.getParams());

        // Set verifier
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        return  httpClient;
    }

    public static Response.Status getResponseStatus(int statusCode) {
        switch (statusCode) {
            case 200 :
                return Response.Status.OK;
            case 201 :
                return Response.Status.CREATED;
            case 400 :
                return Response.Status.BAD_REQUEST;
            case 415 :
                return Response.Status.UNSUPPORTED_MEDIA_TYPE;
            case 500 :
                return Response.Status.INTERNAL_SERVER_ERROR;
        }
        return Response.Status.ACCEPTED;
    }

    public static String getKeyManagerHost()
            throws IllegalArgumentException {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration.
                                                       getAuthenticatorConfig(
                                                               Constants.ConfigurationProperties.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getParameters() != null) {
            return getHostName(authenticatorConfig.getParameters().get(Constants.ConfigurationProperties.
                                                                               AUTHENTICATOR_CONFIG_HOST_URL));

        }else{
            throw new IllegalArgumentException("Configuration parameters need to be defined in Authenticators.xml.");
        }
    }

    private static String getHostName(String host) {
        if (host != null && !host.isEmpty()) {
            if (host.contains("https://")) {
                return host.replace("https://","");
            }
        } else {
            throw new IllegalArgumentException("Remote Host parameter must defined in Authenticators.xml.");
        }
        return null;
    }


    public static int getServerHTTPSPort() {
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
