/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class HTTPNotificationStrategy implements NotificationStrategy {

    private static final Log log = LogFactory.getLog(HTTPNotificationStrategy.class);
    private final PushNotificationConfig config;
    private static final String URL_PROPERTY = "url";
    private static final String AUTHORIZATION_HEADER_PROPERTY = "authorization";
    private String endpoint;
    private static ExecutorService executorService;
    private HttpClient httpClient = null;
    private HostConfiguration hostConfiguration;
    private String authorizationHeaderValue;
    private String uri;

    public HTTPNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
        if (this.config == null) {
            throw new InvalidConfigurationException("Properties Cannot be found");
        }
        endpoint = config.getProperties().get(URL_PROPERTY);
        if (endpoint == null || endpoint.isEmpty()) {
            throw new InvalidConfigurationException("Property - 'url' cannot be found");
        }
        try {
            this.uri = endpoint;
            URL url = new URL(endpoint);
            hostConfiguration = new HostConfiguration();
            hostConfiguration.setHost(url.getHost(), url.getPort(), url.getProtocol());
            this.authorizationHeaderValue = config.getProperties().get(AUTHORIZATION_HEADER_PROPERTY);
            executorService = Executors.newFixedThreadPool(1);
            httpClient = new HttpClient();
        } catch (MalformedURLException e) {
            throw new InvalidConfigurationException("Property - 'url' is malformed.", e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        try {
            executorService.submit(new HTTPMessageExecutor(ctx, authorizationHeaderValue, uri, hostConfiguration
                    , httpClient));
        } catch (RejectedExecutionException e) {
            log.error("Failed to publish to external endpoint url: " + endpoint, e);
        }
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {
        executorService.shutdown();
    }

    @Override
    public PushNotificationConfig getConfig() {
        return config;
    }

}

