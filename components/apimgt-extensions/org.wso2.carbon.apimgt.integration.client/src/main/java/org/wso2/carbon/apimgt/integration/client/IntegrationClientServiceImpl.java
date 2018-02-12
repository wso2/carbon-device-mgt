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

import feign.RequestInterceptor;
import org.wso2.carbon.apimgt.integration.client.publisher.PublisherClient;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.apimgt.integration.client.store.StoreClient;

public class IntegrationClientServiceImpl implements IntegrationClientService {

    private static IntegrationClientServiceImpl instance;
    private StoreClient storeClient;
    private PublisherClient publisherClient;
    private OAuthRequestInterceptor oAuthRequestInterceptor;

    private IntegrationClientServiceImpl() {
        oAuthRequestInterceptor = new OAuthRequestInterceptor();
        storeClient = new StoreClient(oAuthRequestInterceptor);
        publisherClient = new PublisherClient(oAuthRequestInterceptor);
    }

    public static IntegrationClientServiceImpl getInstance() {
        if (instance == null) {
            synchronized (IntegrationClientService.class) {
                if (instance == null) {
                    instance = new IntegrationClientServiceImpl();
                }
            }
        }
        return instance;
    }

    public void resetUserInfo(String userName, String tenantDomain) {
        oAuthRequestInterceptor.removeToken(userName, tenantDomain);
    }

    @Override
    public StoreClient getStoreClient() {
        return storeClient;
    }

    @Override
    public PublisherClient getPublisherClient() {
        return publisherClient;
    }
}
