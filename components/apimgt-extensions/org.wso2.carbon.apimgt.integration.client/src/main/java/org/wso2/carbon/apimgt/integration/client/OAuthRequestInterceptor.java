/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.integration.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.wso2.carbon.apimgt.integration.client.configs.APIMConfigReader;
import org.wso2.carbon.apimgt.integration.client.exception.APIMClientOAuthException;
import org.wso2.carbon.apimgt.integration.client.internal.APIIntegrationClientDataHolder;
import org.wso2.carbon.apimgt.integration.client.model.ClientProfile;
import org.wso2.carbon.apimgt.integration.client.model.DCRClient;
import org.wso2.carbon.apimgt.integration.client.model.OAuthApplication;
import org.wso2.carbon.apimgt.integration.client.util.PropertyUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {

    private static final String APPLICATION_NAME = "api_integration_client";
    private static final String GRANT_TYPES = "password refresh_token urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String REQUIRED_SCOPE =
            "apim:api_create apim:api_view apim:api_publish apim:subscribe apim:tier_view apim:tier_manage " +
                    "apim:subscription_view apim:subscription_block";
    private static final long DEFAULT_REFRESH_TIME_OFFSET_IN_MILLIS = 100000;
    private DCRClient dcrClient;
    private static OAuthApplication oAuthApplication;
    private static Map<String, AccessTokenInfo> tenantTokenMap = new HashMap<>();

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        String username = APIMConfigReader.getInstance().getConfig().getUsername();
        String password = APIMConfigReader.getInstance().getConfig().getPassword();
        dcrClient = Feign.builder().requestInterceptor(
                new BasicAuthRequestInterceptor(username, password))
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(DCRClient.class, PropertyUtils.replaceProperties(
                        APIMConfigReader.getInstance().getConfig().getDcrEndpoint()));
    }

    @Override
    public void apply(RequestTemplate template) {
        if (oAuthApplication == null) {
            //had to do on demand initialization due to start up error.
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setClientName(APPLICATION_NAME);
            clientProfile.setCallbackUrl("");
            clientProfile.setGrantType(GRANT_TYPES);
            clientProfile.setOwner(APIMConfigReader.getInstance().getConfig().getUsername());
            clientProfile.setSaasApp(true);
            oAuthApplication = dcrClient.register(clientProfile);
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        AccessTokenInfo tenantBasedAccessTokenInfo = tenantTokenMap.get(tenantDomain);

        if ((tenantBasedAccessTokenInfo == null ||
                ((System.currentTimeMillis() + DEFAULT_REFRESH_TIME_OFFSET_IN_MILLIS) >
                        tenantBasedAccessTokenInfo.getExpiresIn()))) {
            try {
                JWTClient jwtClient = APIIntegrationClientDataHolder.getInstance().getJwtClientManagerService()
                        .getJWTClient();
                String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getRealmConfiguration().getAdminUserName() + "@" + tenantDomain;

                tenantBasedAccessTokenInfo = jwtClient.getAccessToken(oAuthApplication.getClientId(),
                                                                      oAuthApplication.getClientSecret(), username,
                                                                      REQUIRED_SCOPE);
                tenantBasedAccessTokenInfo.setExpiresIn(
                        System.currentTimeMillis() + (tenantBasedAccessTokenInfo.getExpiresIn() * 1000));
                tenantTokenMap.put(tenantDomain, tenantBasedAccessTokenInfo);
            } catch (JWTClientException e) {
                throw new APIMClientOAuthException("failed to retrieve oauth token using jwt", e);
            } catch (UserStoreException e) {
                throw new APIMClientOAuthException("failed to retrieve tenant admin username", e);
            }

        }
        if (tenantBasedAccessTokenInfo.getAccessToken() != null) {
            String headerValue = "Bearer " + tenantBasedAccessTokenInfo.getAccessToken();
            template.header("Authorization", headerValue);
        }
    }

}
