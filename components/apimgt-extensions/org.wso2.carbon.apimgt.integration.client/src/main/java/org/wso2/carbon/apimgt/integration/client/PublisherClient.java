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
package org.wso2.carbon.apimgt.integration.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.integration.client.configs.APIMConfigReader;
import org.wso2.carbon.apimgt.publisher.client.api.APIDocumentApi;
import org.wso2.carbon.apimgt.publisher.client.api.APIsApi;
import org.wso2.carbon.apimgt.publisher.client.api.ApplicationsApi;
import org.wso2.carbon.apimgt.publisher.client.api.EnvironmentsApi;
import org.wso2.carbon.apimgt.publisher.client.api.SubscriptionsApi;
import org.wso2.carbon.apimgt.publisher.client.api.TiersApi;
import org.wso2.carbon.apimgt.publisher.client.invoker.ApiClient;

/**
 * Publisher client generated using swagger.
 */
public class PublisherClient {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(PublisherClient.class);
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
        Feign.Builder builder = Feign.builder().requestInterceptor(requestInterceptor)
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder());

        ApiClient client = new ApiClient();
        client.setBasePath(APIMConfigReader.getInstance().getConfig().getPublisherEndpoint());
        client.setFeignBuilder(builder);

        api = client.buildClient(APIsApi.class);
        document = client.buildClient(APIDocumentApi.class);
        application = client.buildClient(ApplicationsApi.class);
        environments = client.buildClient(EnvironmentsApi.class);
        subscriptions = client.buildClient(SubscriptionsApi.class);
        tiers = client.buildClient(TiersApi.class);
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
