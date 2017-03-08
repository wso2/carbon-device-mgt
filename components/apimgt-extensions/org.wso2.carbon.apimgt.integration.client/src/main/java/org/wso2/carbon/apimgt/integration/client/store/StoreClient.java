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
package org.wso2.carbon.apimgt.integration.client.store;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.integration.client.configs.APIMConfigReader;
import org.wso2.carbon.apimgt.integration.generated.client.store.api.*;
import org.wso2.carbon.core.util.Utils;

/**
 * API Store client, created using swagger gen.
 */
public class StoreClient {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(StoreClient.class);
    private APICollectionApi apis = null;
    private APIIndividualApi individualApi = null;
    private ApplicationCollectionApi applications = null;
    private ApplicationIndividualApi individualApplication = null;
    private SubscriptionCollectionApi subscriptions = null;
    private SubscriptionIndividualApi individualSubscription = null;
    private SubscriptionMultitpleApi subscriptionMultitpleApi = null;
    private ThrottlingTierIndividualApi individualTier = null;
    private TagCollectionApi tags = null;
    private ThrottlingTierCollectionApi tiers = null;


    public StoreClient(RequestInterceptor requestInterceptor) {

        Feign.Builder builder = Feign.builder().client(
                org.wso2.carbon.apimgt.integration.client.util.Utils.getSSLClient()).logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(requestInterceptor).encoder(new GsonEncoder()).decoder(new GsonDecoder());
        String basePath = Utils.replaceSystemProperty(APIMConfigReader.getInstance().getConfig().getStoreEndpoint());

        apis = builder.target(APICollectionApi.class, basePath);
        individualApi = builder.target(APIIndividualApi.class, basePath);
        applications = builder.target(ApplicationCollectionApi.class, basePath);
        individualApplication = builder.target(ApplicationIndividualApi.class, basePath);
        subscriptions = builder.target(SubscriptionCollectionApi.class, basePath);
        individualSubscription = builder.target(SubscriptionIndividualApi.class, basePath);
        subscriptionMultitpleApi = builder.target(SubscriptionMultitpleApi.class, basePath);
        tags = builder.target(TagCollectionApi.class, basePath);
        tiers = builder.target(ThrottlingTierCollectionApi.class, basePath);
        individualTier = builder.target(ThrottlingTierIndividualApi.class, basePath);

    }

    public APICollectionApi getApis() {
        return apis;
    }

    public APIIndividualApi getIndividualApi() {
        return individualApi;
    }

    public ApplicationCollectionApi getApplications() {
        return applications;
    }

    public ApplicationIndividualApi getIndividualApplication() {
        return individualApplication;
    }

    public SubscriptionCollectionApi getSubscriptions() {
        return subscriptions;
    }

    public SubscriptionIndividualApi getIndividualSubscription() {
        return individualSubscription;
    }

    public ThrottlingTierIndividualApi getIndividualTier() {
        return individualTier;
    }

    public TagCollectionApi getTags() {
        return tags;
    }

    public ThrottlingTierCollectionApi getTiers() {
        return tiers;
    }

    public SubscriptionMultitpleApi getSubscriptionMultitpleApi() {
        return subscriptionMultitpleApi;
    }
}
