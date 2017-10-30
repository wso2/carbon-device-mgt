/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.handlers.invoker;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RESTInvoker {

    private static final Log log = LogFactory.getLog(RESTInvoker.class);

    private int maxTotalConnections = 100;
    private int maxTotalConnectionsPerRoute = 100;
    private int connectionTimeout = 120000;
    private int socketTimeout = 120000;

    private CloseableHttpClient client = null;
    private PoolingHttpClientConnectionManager connectionManager = null;

    public RESTInvoker() {
        configureHttpClient();
    }

    private void configureHttpClient() {

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(true)
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .build();

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxTotalConnectionsPerRoute);
        connectionManager.setMaxTotal(maxTotalConnections);
        client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        if(log.isDebugEnabled()){
            log.debug("REST client initialized with " +
                    "maxTotalConnection = " + maxTotalConnections +
                    "maxConnectionsPerRoute = " + maxTotalConnectionsPerRoute +
                    "connectionTimeout = " + connectionTimeout);
        }

    }

    public void closeHttpClient() {
        IOUtils.closeQuietly(client);
        IOUtils.closeQuietly(connectionManager);
    }

    /**
     * Invokes the http GET method
     *
     * @param uri        endpoint/service url
     * @param requestHeaders header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @return RESTResponse of the GET request (can be the response body or the response status code)
     * @throws Exception
     */
    public RESTResponse invokeGET(URI uri, Map<String, String> requestHeaders, String username, String password) throws IOException {

        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        Header[] headers;
        int httpStatus;
        String contentType;
        String output;
        try {
            httpGet = new HttpGet(uri);
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                Object keys[] = requestHeaders.keySet().toArray();
                for (Object header : keys) {
                    httpGet.setHeader(header.toString(), requestHeaders.get(header).toString());
                }
            }
            response = sendReceiveRequest(httpGet, username, password);
            output = IOUtils.toString(response.getEntity().getContent());
            headers = response.getAllHeaders();
            httpStatus = response.getStatusLine().getStatusCode();
            contentType = response.getEntity().getContentType().getValue();
            if (log.isDebugEnabled()) {
                log.debug("Invoked GET " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return new RESTResponse(contentType, output, headers, httpStatus);
    }


    public RESTResponse invokePOST(URI uri, Map<String, String> requestHeaders, String username,
                                   String password, String payload) throws IOException {

        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        Header[] headers;
        int httpStatus;
        String contentType;
        String output;
        try {
            httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(payload));
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                Object keys[] = requestHeaders.keySet().toArray();
                for (Object header : keys) {
                    httpPost.setHeader(header.toString(), requestHeaders.get(header).toString());
                }
            }
            response = sendReceiveRequest(httpPost, username, password);
            output = IOUtils.toString(response.getEntity().getContent());
            headers = response.getAllHeaders();
            httpStatus = response.getStatusLine().getStatusCode();
            contentType = response.getEntity().getContentType().getValue();
            if (log.isDebugEnabled()) {
                log.debug("Invoked POST " + uri.toString() +
                        " - Input payload: " + payload + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return new RESTResponse(contentType, output, headers, httpStatus);
    }

    /**
     * Invokes the http PUT method
     *
     * @param uri        endpoint/service url
     * @param requestHeaders header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @param payload    payload body passed
     * @return RESTResponse of the PUT request (can be the response body or the response status code)
     * @throws Exception
     */
    public RESTResponse invokePUT(URI uri, Map<String, String> requestHeaders, String username, String password,
                                  String payload) throws IOException {

        HttpPut httpPut = null;
        CloseableHttpResponse response = null;
        Header[] headers;
        int httpStatus;
        String contentType;
        String output;
        try {
            httpPut = new HttpPut(uri);
            httpPut.setEntity(new StringEntity(payload));
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                Object keys[] = requestHeaders.keySet().toArray();
                for (Object header : keys) {
                    httpPut.setHeader(header.toString(), requestHeaders.get(header).toString());
                }
            }
            response = sendReceiveRequest(httpPut, username, password);
            output = IOUtils.toString(response.getEntity().getContent());
            headers = response.getAllHeaders();
            httpStatus = response.getStatusLine().getStatusCode();
            contentType = response.getEntity().getContentType().getValue();
            if (log.isDebugEnabled()) {
                log.debug("Invoked PUT " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpPut != null) {
                httpPut.releaseConnection();
            }
        }
        return new RESTResponse(contentType, output, headers, httpStatus);
    }

    /**
     * Invokes the http DELETE method
     *
     * @param uri        endpoint/service url
     * @param requestHeaders header list
     * @param username   username for authentication
     * @param password   password for authentication
     * @return RESTResponse of the DELETE (can be the response status code or the response body)
     * @throws Exception
     */
    public RESTResponse invokeDELETE(URI uri, Map<String, String> requestHeaders, String username, String password) throws IOException {

        HttpDelete httpDelete = null;
        CloseableHttpResponse response = null;
        Header[] headers;
        int httpStatus;
        String contentType;
        String output;
        try {
            httpDelete = new HttpDelete(uri);
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                Object keys[] = requestHeaders.keySet().toArray();
                for (Object header : keys) {
                    httpDelete.setHeader(header.toString(), requestHeaders.get(header).toString());
                }
            }
            response = sendReceiveRequest(httpDelete, username, password);
            output = IOUtils.toString(response.getEntity().getContent());
            headers = response.getAllHeaders();
            httpStatus = response.getStatusLine().getStatusCode();
            contentType = response.getEntity().getContentType().getValue();
            if (log.isDebugEnabled()) {
                log.debug("Invoked DELETE " + uri.toString() + " - Response message: " + output);
            }
            EntityUtils.consume(response.getEntity());
        } finally {
            if (response != null) {
                IOUtils.closeQuietly(response);
            }
            if (httpDelete != null) {
                httpDelete.releaseConnection();
            }
        }
        return new RESTResponse(contentType, output, headers, httpStatus);
    }

    private CloseableHttpResponse sendReceiveRequest(HttpRequestBase requestBase, String username, String password)
            throws IOException {
        CloseableHttpResponse response;
        if (username != null && !username.equals("") && password != null) {
            String combinedCredentials = username + ":" + password;
            byte[] encodedCredentials = Base64.encodeBase64(combinedCredentials.getBytes(StandardCharsets.UTF_8));
            requestBase.addHeader("Authorization", "Basic " + new String(encodedCredentials));

            response = client.execute(requestBase);
        } else {
            response = client.execute(requestBase);
        }
        return response;
    }
}
