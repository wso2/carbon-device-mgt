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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

public class RESTInvoker {

    private static final Log log = LogFactory.getLog(RESTInvoker.class);

    private CloseableHttpClient client = null;

    public RESTInvoker() {
        configureHttpClient();
    }

    private void configureHttpClient() {
        int connectionTimeout = 120000;
        int socketTimeout = 120000;
        int maxTotalConnectionsPerRoute = 100;
        int maxTotalConnections = 100;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setExpectContinueEnabled(true)
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(maxTotalConnectionsPerRoute);
        connectionManager.setMaxTotal(maxTotalConnections);
        client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        if (log.isDebugEnabled()) {
            log.debug("REST client initialized with " +
                    "maxTotalConnection = " + maxTotalConnections +
                    "maxConnectionsPerRoute = " + maxTotalConnectionsPerRoute +
                    "connectionTimeout = " + connectionTimeout);
        }

    }

    public RESTResponse invokePOST(URI uri, Map<String, String> requestHeaders, String payload) throws IOException {

        HttpPost httpPost = null;
        CloseableHttpResponse response = null;
        int httpStatus;
        String output;
        try {
            httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(payload));
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                Set<String> keys = requestHeaders.keySet();
                for (String header : keys) {
                    httpPost.setHeader(header, requestHeaders.get(header));
                }
            }
            response = sendReceiveRequest(httpPost);
            output = IOUtils.toString(response.getEntity().getContent());
            httpStatus = response.getStatusLine().getStatusCode();
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
        return new RESTResponse(output, httpStatus);
    }

    private CloseableHttpResponse sendReceiveRequest(HttpRequestBase requestBase)
            throws IOException {
        return client.execute(requestBase);
    }
}
