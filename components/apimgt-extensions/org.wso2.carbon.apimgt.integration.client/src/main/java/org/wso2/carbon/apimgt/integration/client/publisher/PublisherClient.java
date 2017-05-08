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
package org.wso2.carbon.apimgt.integration.client.publisher;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.integration.client.configs.APIMConfigReader;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.api.*;
import org.wso2.carbon.core.util.Utils;

/**
 * Publisher client generated using swagger.
 */
public class PublisherClient {

    private static final Log log = LogFactory.getLog(PublisherClient.class);
    private APIsApi api = null;
    private APIDocumentApi document = null;
    private ApplicationsApi application = null;
    private EnvironmentsApi environments = null;
    private SubscriptionsApi subscriptions = null;
    private TiersApi tiers = null;


    /**
     * PublisherClient constructor - Initialize a PublisherClient instance
     *
     */
    public PublisherClient(RequestInterceptor requestInterceptor) {
        Feign.Builder builder = Feign.builder().client(
                org.wso2.carbon.apimgt.integration.client.util.Utils.getSSLClient()).logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor).encoder(new GsonEncoder()).decoder(new GsonDecoder());
        String basePath = Utils.replaceSystemProperty(APIMConfigReader.getInstance().getConfig().getPublisherEndpoint());

        api = builder.target(APIsApi.class, basePath);
        document = builder.target(APIDocumentApi.class, basePath);
        application = builder.target(ApplicationsApi.class, basePath);
        environments = builder.target(EnvironmentsApi.class, basePath);
        subscriptions = builder.target(SubscriptionsApi.class, basePath);
        tiers = builder.target(TiersApi.class, basePath);
    }

    public APIsApi getApi() {
        return api;
    }

    public APIDocumentApi getDocument() {
        return document;
    }

    public ApplicationsApi getApplication() {
        return application;
    }

    public EnvironmentsApi getEnvironments() {
        return environments;
    }

    public SubscriptionsApi getSubscriptions() {
        return subscriptions;
    }

    public TiersApi getTiers() {
        return tiers;
    }
}
