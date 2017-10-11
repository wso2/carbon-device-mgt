/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.webapp.publisher;

import feign.FeignException;
import org.wso2.carbon.apimgt.integration.generated.client.publisher.model.*;
import org.wso2.carbon.apimgt.integration.client.publisher.PublisherClient;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.util.*;

/**
 * This class represents the concrete implementation of the APIPublisherService that corresponds to providing all
 * API publishing related operations.
 */
public class APIPublisherServiceImpl implements APIPublisherService {
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final String API_PUBLISH_ENVIRONMENT = "Production and Sandbox";
    private static final String CONTENT_TYPE = "application/json";
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    private static final String CREATED_STATUS = "CREATED";
    private static final String PUBLISH_ACTION = "Publish";

    @Override
    public void publishAPI(APIConfig apiConfig) throws APIManagerPublisherException {
        String tenantDomain = MultitenantUtils.getTenantDomain(apiConfig.getOwner());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(apiConfig.getOwner());
        try {
            PublisherClient publisherClient = APIPublisherDataHolder.getInstance().getIntegrationClientService()
                    .getPublisherClient();
            API api = getAPI(apiConfig);
            APIList apiList = publisherClient.getApi().apisGet(100, 0, "name:" + api.getName(), CONTENT_TYPE, null);

            if (!isExist(api, apiList)) {
                api = publisherClient.getApi().apisPost(api, CONTENT_TYPE);
                if (CREATED_STATUS.equals(api.getStatus())) {
                    publisherClient.getApi().apisChangeLifecyclePost(PUBLISH_ACTION, api.getId(), null, null, null);
                }
            } else {
                if (WebappPublisherConfig.getInstance().isEnabledUpdateApi()) {
                    for (APIInfo apiInfo : apiList.getList()) {
                        if (api.getName().equals(apiInfo.getName()) && api.getVersion().equals(apiInfo.getVersion())) {
                            api = publisherClient.getApi().apisApiIdPut(apiInfo.getId(), api, CONTENT_TYPE, null, null);
                            if (CREATED_STATUS.equals(api.getStatus())) {
                                publisherClient.getApi().apisChangeLifecyclePost(PUBLISH_ACTION, api.getId(), null, null,
                                                                                 null);
                            }
                        }

                    }
                }
            }
        } catch (FeignException e) {
            throw new APIManagerPublisherException(e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private boolean isExist(API api, APIList apiList) {
        if (apiList == null || apiList.getList() == null || apiList.getList().size() == 0) {
            return false;
        }
        for (APIInfo existingApi : apiList.getList()) {
            if (existingApi.getName().equals(api.getName()) && existingApi.getVersion().equals(api.getVersion())) {
                return true;
            }
        }
        return false;
    }

    private API getAPI(APIConfig config) {

        API api = new API();
        api.setName(config.getName());
        api.setDescription("");

        String context = config.getContext();
        context = context.startsWith("/") ? context : ("/" + context);
        api.setContext(context);
        api.setVersion(config.getVersion());
        api.setProvider(config.getOwner());
        api.setApiDefinition(APIPublisherUtil.getSwaggerDefinition(config));
        api.setWsdlUri(null);
        api.setStatus(PUBLISHED_STATUS);
        api.setResponseCaching("DISABLED");
        api.setDestinationStatsEnabled("false");
        api.isDefaultVersion(true);
        List<String> transport = new ArrayList<>();
        transport.add("https");
        transport.add("http");
        api.transport(transport);
        api.setTags(Arrays.asList(config.getTags()));
        api.addTiersItem(UNLIMITED_TIER);
        api.setGatewayEnvironments(API_PUBLISH_ENVIRONMENT);
        if (config.isSharedWithAllTenants()) {
            api.setSubscriptionAvailability(API.SubscriptionAvailabilityEnum.all_tenants);
            api.setVisibility(API.VisibilityEnum.PUBLIC);
        } else {
            api.setSubscriptionAvailability(API.SubscriptionAvailabilityEnum.current_tenant);
            api.setVisibility(API.VisibilityEnum.PRIVATE);
        }
        String endpointConfig = "{\"production_endpoints\":{\"url\":\"" + config.getEndpoint() +
                "\",\"config\":null},\"implementation_status\":\"managed\",\"endpoint_type\":\"http\"}";


        api.setEndpointConfig(endpointConfig);
        APICorsConfiguration apiCorsConfiguration = new APICorsConfiguration();
        List<String> accessControlAllowOrigins = new ArrayList<>();
        accessControlAllowOrigins.add("*");
        apiCorsConfiguration.setAccessControlAllowOrigins(accessControlAllowOrigins);

        List<String> accessControlAllowHeaders = new ArrayList<>();
        accessControlAllowHeaders.add("authorization");
        accessControlAllowHeaders.add("Access-Control-Allow-Origin");
        accessControlAllowHeaders.add("Content-Type");
        accessControlAllowHeaders.add("SOAPAction");
        apiCorsConfiguration.setAccessControlAllowHeaders(accessControlAllowHeaders);

        List<String> accessControlAllowMethods = new ArrayList<>();
        accessControlAllowMethods.add("GET");
        accessControlAllowMethods.add("PUT");
        accessControlAllowMethods.add("DELETE");
        accessControlAllowMethods.add("POST");
        accessControlAllowMethods.add("PATCH");
        accessControlAllowMethods.add("OPTIONS");
        apiCorsConfiguration.setAccessControlAllowMethods(accessControlAllowMethods);
        apiCorsConfiguration.setAccessControlAllowCredentials(false);
        apiCorsConfiguration.corsConfigurationEnabled(false);
        api.setCorsConfiguration(apiCorsConfiguration);
        return api;
    }
}
