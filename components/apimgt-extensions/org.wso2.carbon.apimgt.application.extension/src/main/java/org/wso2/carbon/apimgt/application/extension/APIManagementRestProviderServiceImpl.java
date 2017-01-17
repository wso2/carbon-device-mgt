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

package org.wso2.carbon.apimgt.application.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import org.wso2.carbon.apimgt.application.extension.util.APIManagerUtil;
import org.wso2.carbon.apimgt.integration.client.StoreClient;
import org.wso2.carbon.apimgt.store.client.model.APIInfo;
import org.wso2.carbon.apimgt.store.client.model.APIList;
import org.wso2.carbon.apimgt.store.client.model.Application;
import org.wso2.carbon.apimgt.store.client.model.ApplicationInfo;
import org.wso2.carbon.apimgt.store.client.model.ApplicationKey;
import org.wso2.carbon.apimgt.store.client.model.ApplicationKeyGenerateRequest;
import org.wso2.carbon.apimgt.store.client.model.ApplicationList;
import org.wso2.carbon.apimgt.store.client.model.Subscription;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementRestProviderServiceImpl implements APIManagementProviderService {

    private static final Log log = LogFactory.getLog(APIManagementRestProviderServiceImpl.class);
    private static final String CONTENT_TYPE = "application/json";
    private static final int MAX_API_PER_TAG = 200;

    @Override
    public void removeAPIApplication(String applicationName, String username) throws APIManagerException {

        StoreClient storeClient = APIApplicationManagerExtensionDataHolder.getInstance().getIntegrationClientService()
                .getStoreClient();
        ApplicationList applicationList = storeClient.getApplications()
                .applicationsGet("", applicationName, 1, 0, CONTENT_TYPE, null);
        if (applicationList.getList() != null && applicationList.getList().size() > 0) {
            ApplicationInfo applicationInfo = applicationList.getList().get(0);
            storeClient.getIndividualApplication().applicationsApplicationIdDelete(applicationInfo.getApplicationId(),
                                                                                   null, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
                                                                String keyType, String username,
                                                                boolean isAllowedAllDomains, String validityTime)
            throws APIManagerException {
        StoreClient storeClient = APIApplicationManagerExtensionDataHolder.getInstance().getIntegrationClientService()
                .getStoreClient();
        ApplicationList applicationList = storeClient.getApplications()
                .applicationsGet("", applicationName, 1, 0, CONTENT_TYPE, null);
        Application application;
        if (applicationList == null || applicationList.getList() == null || applicationList.getList().size() == 0) {
            //create application;
            application = new Application();
            application.setName(applicationName);
            application.setSubscriber(username);
            application.setDescription("");
            application.setThrottlingTier(ApiApplicationConstants.DEFAULT_TIER);
            application.setGroupId("");
            application = storeClient.getIndividualApplication().applicationsPost(application, CONTENT_TYPE);
        } else {
            ApplicationInfo applicationInfo = applicationList.getList().get(0);
            application = storeClient.getIndividualApplication()
                    .applicationsApplicationIdGet(applicationInfo.getApplicationId(), CONTENT_TYPE, null, null);
        }
        if (application == null) {
            throw new APIManagerException (
                    "Api application creation failed for " + applicationName + " to the user " + username);
        }
        // subscribe to apis.
        if (tags != null && tags.length > 0) {
            for (String tag: tags) {
                APIList apiList = storeClient.getApis().apisGet(MAX_API_PER_TAG, 0, null, "tag:" + tag, CONTENT_TYPE, null);
                if (apiList.getList() != null && apiList.getList().size() == 0) {
                    apiList = storeClient.getApis().apisGet(MAX_API_PER_TAG, 0
                            , MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, "tag:" + tag, CONTENT_TYPE, null);
                }

                if (apiList.getList() != null && apiList.getList().size() > 0) {
                    for (APIInfo apiInfo :apiList.getList()) {
                        Subscription subscription = new Subscription();
                        subscription.setApiIdentifier(apiInfo.getId());
                        subscription.setApplicationId(application.getApplicationId());
                        subscription.tier(ApiApplicationConstants.DEFAULT_TIER);
                        storeClient.getIndividualSubscription().subscriptionsPost(subscription, CONTENT_TYPE);
                    }

                }
            }
        }
        //end of subscription

        List<ApplicationKey> applicationKeys = application.getKeys();
        if (applicationKeys != null) {
            for (ApplicationKey applicationKey : applicationKeys) {
                if (keyType.equals(applicationKey.getKeyType())) {
                    ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                    apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
                    apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
                    return apiApplicationKey;
                }
            }
        }

        ApplicationKeyGenerateRequest applicationKeyGenerateRequest = new ApplicationKeyGenerateRequest();
        List<String> allowedDomains = new ArrayList<>();
        if (isAllowedAllDomains) {
            allowedDomains.add(ApiApplicationConstants.ALLOWED_DOMAINS);
        } else {
            allowedDomains.add(APIManagerUtil.getTenantDomain());
        }
        applicationKeyGenerateRequest.setAccessAllowDomains(allowedDomains);
        applicationKeyGenerateRequest.setCallbackUrl("");
        applicationKeyGenerateRequest.setKeyType(ApplicationKeyGenerateRequest.KeyTypeEnum.PRODUCTION);
        applicationKeyGenerateRequest.setValidityTime(validityTime);

        ApplicationKey applicationKey = storeClient.getIndividualApplication().applicationsGenerateKeysPost(
                application.getApplicationId(), applicationKeyGenerateRequest, CONTENT_TYPE, null, null);
        ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
        apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
        apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
        return apiApplicationKey;
    }

}
