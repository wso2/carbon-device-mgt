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
import feign.RequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.integration.client.configs.APIMConfigReader;
import org.wso2.carbon.apimgt.integration.client.store.api.*;
import org.wso2.carbon.core.util.Utils;

/**
 * API Store client, created using swagger gen.
 */
public class StoreClient {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(StoreClient.class);
    private ApisAPIApi apis = null;
    private APIindividualApi individualApi = null;
    private ApplicationCollectionApi applications = null;
    private ApplicationindividualApi individualApplication = null;
    private SubscriptionCollectionApi subscriptions = null;
    private SubscriptionindividualApi individualSubscription = null;
    private TierindividualApi individualTier = null;
    private TagCollectionApi tags = null;
    private TierCollectionApi tiers = null;


    public StoreClient(RequestInterceptor requestInterceptor) {

        Feign.Builder builder = Feign.builder().requestInterceptor(requestInterceptor)
                .encoder(new GsonEncoder()).decoder(new GsonDecoder());
        String basePath = Utils.replaceSystemProperty(APIMConfigReader.getInstance().getConfig().getStoreEndpoint());

        apis = builder.target(ApisAPIApi.class, basePath);
        individualApi = builder.target(APIindividualApi.class, basePath);
        applications = builder.target(ApplicationCollectionApi.class, basePath);
        individualApplication = builder.target(ApplicationindividualApi.class, basePath);
        subscriptions = builder.target(SubscriptionCollectionApi.class, basePath);
        individualSubscription = builder.target(SubscriptionindividualApi.class, basePath);
        tags = builder.target(TagCollectionApi.class, basePath);
        tiers = builder.target(TierCollectionApi.class, basePath);
        individualTier = builder.target(TierindividualApi.class, basePath);

    }

    public ApisAPIApi getApis() {
        return apis;
    }

    public APIindividualApi getIndividualApi() {
        return individualApi;
    }

    public ApplicationCollectionApi getApplications() {
        return applications;
    }

    public ApplicationindividualApi getIndividualApplication() {
        return individualApplication;
    }

    public SubscriptionCollectionApi getSubscriptions() {
        return subscriptions;
    }

    public SubscriptionindividualApi getIndividualSubscription() {
        return individualSubscription;
    }

    public TierindividualApi getIndividualTier() {
        return individualTier;
    }

    public TagCollectionApi getTags() {
        return tags;
    }

    public TierCollectionApi getTiers() {
        return tiers;
    }
}
