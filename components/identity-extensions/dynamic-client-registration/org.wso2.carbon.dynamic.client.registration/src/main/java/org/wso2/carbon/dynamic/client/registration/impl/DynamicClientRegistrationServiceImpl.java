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

package org.wso2.carbon.dynamic.client.registration.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.dynamic.client.registration.*;
import org.wso2.carbon.dynamic.client.registration.internal.DynamicClientRegistrationDataHolder;
import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;
import org.wso2.carbon.dynamic.client.registration.util.DCRConstants;
import org.wso2.carbon.dynamic.client.registration.util.DynamicClientRegistrationUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.*;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.sso.saml.admin.SAMLSSOConfigAdmin;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOServiceProviderDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of DynamicClientRegistrationService.
 */
public class DynamicClientRegistrationServiceImpl implements DynamicClientRegistrationService {

    private static final String TOKEN_SCOPE = "tokenScope";
    private static final String MDM = "mdm";
    private static final String SAML_SSO = "samlsso";
    private static final String BASIC_AUTHENTICATOR = "BasicAuthenticator";
    private static final String BASIC = "basic";
    private static final String LOCAL = "local";
    private static final Log log = LogFactory.getLog(DynamicClientRegistrationService.class);
    private static final String AUTH_TYPE_OAUTH_2 = "oauth2";
    private static final String OAUTH_CONSUMER_SECRET = "oauthConsumerSecret";
    private static final int STEP_ORDER = 1;
    private static final String OAUTH_VERSION = "OAuth-2.0";

    private static final String APPLICATION_TYPE_WEBAPP = "webapp";
    private static final String APPLICATION_TYPE_DEVICE = "device";

    @Override
    public OAuthApplicationInfo registerOAuthApplication(
            RegistrationProfile profile) throws DynamicClientRegistrationException {
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();

        String applicationName = profile.getClientName();

        if (log.isDebugEnabled()) {
            log.debug("Trying to register OAuth application: '" + applicationName + "'");
        }

        String tokenScope = profile.getTokenScope();
        String tokenScopes[] = new String[1];
        tokenScopes[0] = tokenScope;

        oAuthApplicationInfo.addParameter(TOKEN_SCOPE, Arrays.toString(tokenScopes));
        OAuthApplicationInfo info;
        try {
            info = this.createOAuthApplication(profile);
        } catch (DynamicClientRegistrationException | IdentityException e) {
            throw new DynamicClientRegistrationException("Can not create OAuth application  : " + applicationName, e);
        }

        if (info == null || info.getJsonString() == null) {
            throw new DynamicClientRegistrationException(
                    "OAuth app does not contain required data: '" + applicationName + "'");
        }

        oAuthApplicationInfo.setClientName(info.getClientName());
        oAuthApplicationInfo.setClientId(info.getClientId());
        oAuthApplicationInfo.setCallBackURL(info.getCallBackURL());
        oAuthApplicationInfo.setClientSecret(info.getClientSecret());

        try {
            JSONObject jsonObject = new JSONObject(info.getJsonString());
            if (jsonObject.has(DCRConstants.ClientMetadata.OAUTH_REDIRECT_URIS)) {
                oAuthApplicationInfo.addParameter(DCRConstants.ClientMetadata.OAUTH_REDIRECT_URIS,
                        jsonObject
                                .get(DCRConstants.ClientMetadata.OAUTH_REDIRECT_URIS));
            }

            if (jsonObject.has(DCRConstants.ClientMetadata.OAUTH_CLIENT_GRANT)) {
                oAuthApplicationInfo.addParameter(DCRConstants.ClientMetadata.OAUTH_CLIENT_GRANT, jsonObject
                        .get(DCRConstants.ClientMetadata.OAUTH_CLIENT_GRANT));
            }
        } catch (JSONException e) {
            throw new DynamicClientRegistrationException(
                    "Can not retrieve information of the created OAuth application", e);
        }
        return oAuthApplicationInfo;
    }

    private OAuthApplicationInfo createOAuthApplication(
            RegistrationProfile profile) throws DynamicClientRegistrationException, IdentityException {

        //Subscriber's name should be passed as a parameter, since it's under the subscriber
        //the OAuth App is created.
        String userId = profile.getOwner();
        String applicationName = profile.getClientName();
        String grantType = profile.getGrantType();
        String callbackUrl = profile.getCallbackUrl();
        boolean isSaaSApp = profile.isSaasApp();

        if (userId == null || userId.isEmpty()) {
            return null;
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String baseUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String userName = MultitenantUtils.getTenantAwareUsername(userId);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

        // Acting as the provided user. When creating Service Provider/OAuth App,
        // username is fetched from CarbonContext
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);

        try {
            // Append the username before Application name to make application name unique across two users.
            applicationName = replaceInvalidChars(userName) + "_" + applicationName;

            // Create the Service Provider
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName(applicationName);
            User user = new User();
            user.setUserName(userName);
            user.setTenantDomain(tenantDomain);
            serviceProvider.setOwner(user);

            serviceProvider.setDescription("Service Provider for application " + applicationName);
            ApplicationManagementService appMgtService = DynamicClientRegistrationDataHolder.getInstance().
                    getApplicationManagementService();
            if (appMgtService == null) {
                throw new IllegalStateException("Error occurred while retrieving Application Management" + "Service");
            }

            ServiceProvider existingServiceProvider = appMgtService.getServiceProvider(applicationName, tenantDomain);
            if (existingServiceProvider == null) {
                appMgtService.createApplication(serviceProvider, tenantDomain, userName);
            }

            ServiceProvider createdServiceProvider = appMgtService.getServiceProvider(applicationName, tenantDomain);
            if (createdServiceProvider == null) {
                throw new DynamicClientRegistrationException("Couldn't create Service Provider Application " +
                        applicationName);
            }
            //Set SaaS app option
            createdServiceProvider.setSaasApp(isSaaSApp);
            // Then Create OAuthApp
            OAuthAdminService oAuthAdminService = new OAuthAdminService();

            OAuthConsumerAppDTO oAuthConsumerApp = new OAuthConsumerAppDTO();
            oAuthConsumerApp.setApplicationName(applicationName);
            oAuthConsumerApp.setCallbackUrl(callbackUrl);
            oAuthConsumerApp.setGrantTypes(grantType);
            oAuthConsumerApp.setOAuthVersion(OAUTH_VERSION);
            if (log.isDebugEnabled()) {
                log.debug("Creating OAuth App " + applicationName);
            }

            if ((existingServiceProvider == null) || (existingServiceProvider.getInboundAuthenticationConfig().
                    getInboundAuthenticationRequestConfigs().length == 0)) {
                oAuthAdminService.registerOAuthApplicationData(oAuthConsumerApp);
            }

            if (log.isDebugEnabled()) {
                log.debug("Created OAuth App " + applicationName);
            }

            OAuthConsumerAppDTO createdApp =
                    oAuthAdminService.getOAuthApplicationDataByAppName(oAuthConsumerApp.getApplicationName());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved Details for OAuth App " + createdApp.getApplicationName());
            }
            // Set the OAuthApp in InboundAuthenticationConfig
            InboundAuthenticationConfig inboundAuthenticationConfig =
                    new InboundAuthenticationConfig();
            List<InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigs = new ArrayList<>();

            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = new
                    InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthKey(createdApp.getOauthConsumerKey());
            inboundAuthenticationRequestConfig.setInboundAuthType(AUTH_TYPE_OAUTH_2);
            String oauthConsumerSecret = createdApp.getOauthConsumerSecret();
            if (oauthConsumerSecret != null && !oauthConsumerSecret.isEmpty()) {
                Property property = new Property();
                property.setName(OAUTH_CONSUMER_SECRET);
                property.setValue(oauthConsumerSecret);
                Property[] properties = {property};
                inboundAuthenticationRequestConfig.setProperties(properties);
            }

            if (APPLICATION_TYPE_WEBAPP.equals(profile.getApplicationType())) {
                SAMLSSOServiceProviderDTO samlssoServiceProviderDTO = new SAMLSSOServiceProviderDTO();
                samlssoServiceProviderDTO.setIssuer(applicationName);

                SAMLSSOConfigAdmin configAdmin = new SAMLSSOConfigAdmin(getConfigSystemRegistry());
                configAdmin.addRelyingPartyServiceProvider(samlssoServiceProviderDTO);

                InboundAuthenticationRequestConfig samlAuthenticationRequest = new InboundAuthenticationRequestConfig();
                samlAuthenticationRequest.setInboundAuthKey(applicationName);
                samlAuthenticationRequest.setInboundAuthType(SAML_SSO);
                inboundAuthenticationRequestConfigs.add(samlAuthenticationRequest);
            }

            LocalAuthenticatorConfig localAuth = new LocalAuthenticatorConfig();
            localAuth.setName(BASIC_AUTHENTICATOR);
            localAuth.setDisplayName(BASIC);
            localAuth.setEnabled(true);

            AuthenticationStep authStep = new AuthenticationStep();
            authStep.setStepOrder(STEP_ORDER);
            authStep.setSubjectStep(true);
            authStep.setAttributeStep(true);

            authStep.setLocalAuthenticatorConfigs(new LocalAuthenticatorConfig[]{localAuth});

            LocalAndOutboundAuthenticationConfig localOutboundAuthConfig = new LocalAndOutboundAuthenticationConfig();
            localOutboundAuthConfig.setAuthenticationType(LOCAL);
            localOutboundAuthConfig.setAuthenticationSteps(new AuthenticationStep[]{authStep});
            createdServiceProvider.setLocalAndOutBoundAuthenticationConfig(localOutboundAuthConfig);

            inboundAuthenticationRequestConfigs.add(inboundAuthenticationRequestConfig);
            inboundAuthenticationConfig
                    .setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfigs.toArray(
                            new InboundAuthenticationRequestConfig[inboundAuthenticationRequestConfigs.size()]));
            createdServiceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

            // Update the Service Provider app to add OAuthApp as an Inbound Authentication Config
            appMgtService.updateApplication(createdServiceProvider, tenantDomain, userName);

            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            oAuthApplicationInfo.setClientId(createdApp.getOauthConsumerKey());
            oAuthApplicationInfo.setCallBackURL(createdApp.getCallbackUrl());
            oAuthApplicationInfo.setClientSecret(oauthConsumerSecret);
            oAuthApplicationInfo.setClientName(createdApp.getApplicationName());

            oAuthApplicationInfo.addParameter(
                    DCRConstants.ClientMetadata.OAUTH_REDIRECT_URIS, createdApp.getCallbackUrl());
            oAuthApplicationInfo.addParameter(
                    DCRConstants.ClientMetadata.OAUTH_CLIENT_GRANT, createdApp.getGrantTypes());

            return oAuthApplicationInfo;
        } catch (IdentityApplicationManagementException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while creating ServiceProvider for app " + applicationName, e);
        } catch (Exception e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while creating OAuthApp " + applicationName, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(baseUser);
        }
    }

    protected Registry getConfigSystemRegistry() {
        return (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.
                SYSTEM_CONFIGURATION);
    }

    @Override
    public boolean unregisterOAuthApplication(String userId, String applicationName, String consumerKey) throws
            DynamicClientRegistrationException {
        DynamicClientRegistrationUtil.validateUsername(userId);
        DynamicClientRegistrationUtil.validateApplicationName(applicationName);
        DynamicClientRegistrationUtil.validateConsumerKey(consumerKey);

        boolean status = false;
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        String userName = MultitenantUtils.getTenantAwareUsername(userId);

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);

        OAuthAdminService oAuthAdminService;
        OAuthConsumerAppDTO oAuthConsumerApp;
        try {
            oAuthAdminService = new OAuthAdminService();
            oAuthConsumerApp = oAuthAdminService.getOAuthApplicationData(consumerKey);
        } catch (Exception e) {
            //We had to catch Exception here because getOAuthApplicationData can throw exceptions of java.lang.Exception
            // class.
            throw new DynamicClientRegistrationException("Error occurred while retrieving application data", e);
        }

        if (oAuthConsumerApp == null) {
            throw new DynamicClientRegistrationException(
                    "No OAuth Consumer Application is associated with the given consumer key: " + consumerKey);
        }

        try {
            oAuthAdminService.removeOAuthApplicationData(consumerKey);
            ApplicationManagementService appMgtService = DynamicClientRegistrationDataHolder.getInstance().
                    getApplicationManagementService();

            if (appMgtService == null) {
                throw new IllegalStateException(
                        "Error occurred while retrieving Application Management" +
                                "Service");
            }
            ServiceProvider createdServiceProvider = appMgtService.getServiceProvider(applicationName, tenantDomain);
            if (createdServiceProvider == null) {
                throw new DynamicClientRegistrationException(
                        "Couldn't retrieve Service Provider Application " + applicationName);
            }
            appMgtService.deleteApplication(applicationName, tenantDomain, userName);
            status = true;
        } catch (IdentityApplicationManagementException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while removing ServiceProvider for application '" + applicationName + "'", e);
        } catch (IdentityOAuthAdminException e) {
            throw new DynamicClientRegistrationException("Error occurred while removing application '" +
                    applicationName + "'", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }

    @Override
    public boolean isOAuthApplicationAvailable(String applicationName) throws DynamicClientRegistrationException {
        ApplicationManagementService appMgtService = DynamicClientRegistrationDataHolder.getInstance().
                getApplicationManagementService();
        if (appMgtService == null) {
            throw new IllegalStateException(
                    "Error occurred while retrieving Application Management" +
                            "Service");
        }
        try {
            return appMgtService.getServiceProvider(applicationName,
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain()) !=
                    null;
        } catch (IdentityApplicationManagementException e) {
            throw new DynamicClientRegistrationException(
                    "Error occurred while retrieving information of OAuthApp " + applicationName, e);
        }
    }

    private String replaceInvalidChars(String username) {
        return username.replaceAll("@", "_AT_");
    }

}
