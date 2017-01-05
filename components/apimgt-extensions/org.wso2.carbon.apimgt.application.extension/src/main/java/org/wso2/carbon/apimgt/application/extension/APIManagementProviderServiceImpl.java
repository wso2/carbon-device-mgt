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
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.apimgt.application.extension.util.APIManagerUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.Set;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {

    private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExistingOAuthApplicationToAPIApplication(String jsonString, String applicationName,
                                                                 String clientId, String username,
                                                                 boolean isAllowedAllDomains, String keyType,
                                                                 String tags[]) throws APIManagerException {
        try {
            APIManagerUtil.loadTenantRegistry();
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            if (apiConsumer != null) {
                String groupId = getLoggedInUserGroupId(username, APIManagerUtil.getTenantDomain());
                int applicationId = createApplication(apiConsumer, applicationName, username, groupId);
                Subscriber subscriber = apiConsumer.getSubscriber(username);
                if (subscriber == null) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(username);
                    addSubscriber(username, "", groupId, APIManagerUtil.getTenantId(tenantDomain));
                    subscriber = apiConsumer.getSubscriber(username);
                }
                Application[] applications = apiConsumer.getApplications(subscriber, groupId);
                Application application = null;
                for (Application app : applications) {
                    if (app.getId() == applicationId) {
                        application = app;
                    }
                }
                if (application == null) {
                    throw new APIManagerException(
                            "Api application creation failed for " + applicationName + " to the user " + username);
                }

                OAuthApplicationInfo oAuthApp = application.getOAuthApp(keyType);
                if (oAuthApp != null) {
                    if (oAuthApp.getClientId().equals(clientId)) {
                        if (tags != null && tags.length > 0) {
                            createApplicationAndSubscribeToAPIs(applicationName, tags, username);
                        }
                        return;
                    } else {
                        throw new APIManagerException("Api application already mapped to another OAuth App");
                    }
                }

                apiConsumer.mapExistingOAuthClient(jsonString, username, clientId, applicationName,
                                                   ApiApplicationConstants.DEFAULT_TOKEN_TYPE);
                if (tags != null && tags.length > 0) {
                    createApplicationAndSubscribeToAPIs(applicationName, tags, username);
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagerException(
                    "Failed registering the OAuth app [ clientId " + clientId + " ] with api manager application", e);
        }
    }

    @Override
    public void removeAPIApplication(String applicationName, String username) throws APIManagerException {
        try {
            APIManagerUtil.loadTenantRegistry();
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            if (apiConsumer != null) {
                String groupId = getLoggedInUserGroupId(username, APIManagerUtil.getTenantDomain());
                Application[] applications = apiConsumer.getApplications(new Subscriber(username), groupId);
                for (Application application : applications) {
                    if (application.getName().equals(applicationName)) {
                        apiConsumer.removeApplication(application);
                        break;
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new APIManagerException(
                    "Failed to remove the application [ application name " + applicationName + " ]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String apiApplicationName, String tags[],
                                                                String keyType, String username,
                                                                boolean isAllowedAllDomains, String validityTime)
            throws APIManagerException {
        try {
            APIManagerUtil.loadTenantRegistry();
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            String groupId = getLoggedInUserGroupId(username, APIManagerUtil.getTenantDomain());
            int applicationId = createApplicationAndSubscribeToAPIs(apiApplicationName, tags, username);
            Application[] applications = apiConsumer.getApplications(apiConsumer.getSubscriber(username), groupId);
            Application application = null;
            for (Application app : applications) {
                if (app.getId() == applicationId) {
                    application = app;
                }
            }
            if (application == null) {
                throw new APIManagerException(
                        "Api application creation failed for " + apiApplicationName + " to the user " + username);
            }

            OAuthApplicationInfo oAuthApp = application.getOAuthApp(keyType);
            if (oAuthApp != null) {
                ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                apiApplicationKey.setConsumerKey(oAuthApp.getClientId());
                apiApplicationKey.setConsumerSecret(oAuthApp.getClientSecret());
                return apiApplicationKey;
            }
            String[] allowedDomains = new String[1];
            if (isAllowedAllDomains) {
                allowedDomains[0] = ApiApplicationConstants.ALLOWED_DOMAINS;
            } else {
                allowedDomains[0] = APIManagerUtil.getTenantDomain();
            }
            String ownerJsonString = "{\"username\":\"" + username + "\"}";
            Map<String, Object> keyDetails = apiConsumer.requestApprovalForApplicationRegistration(username,
                                                                                                   apiApplicationName,
                                                                                                   keyType, "",
                                                                                                   allowedDomains,
                                                                                                   validityTime,
                                                                                                   "null",
                                                                                                   groupId,
                                                                                                   ownerJsonString);
            ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
            apiApplicationKey.setConsumerKey((String) keyDetails.get(APIConstants.FrontEndParameterNames
                                                                             .CONSUMER_KEY));
            apiApplicationKey.setConsumerSecret((String) keyDetails.get(
                    APIConstants.FrontEndParameterNames.CONSUMER_SECRET));
            return apiApplicationKey;
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to register a api application : " + apiApplicationName, e);
        }
    }

    private int createApplication(APIConsumer apiConsumer, String applicationName, String username, String groupId)
            throws APIManagerException {
        try {
            if (apiConsumer != null) {
                if (apiConsumer.getSubscriber(username) == null) {
                    String tenantDomain = MultitenantUtils.getTenantDomain(username);
                    addSubscriber(username, "", groupId, APIManagerUtil.getTenantId(tenantDomain));
                }
                Application application = apiConsumer.getApplicationsByName(username, applicationName, groupId);
                if (application == null) {
                    Subscriber subscriber = apiConsumer.getSubscriber(username);
                    application = new Application(applicationName, subscriber);
                    application.setTier(ApiApplicationConstants.DEFAULT_TIER);
                    application.setGroupId(groupId);
                    return apiConsumer.addApplication(application, username);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Application [" + applicationName + "] already exists for Subscriber [" + username +
                                  "]");
                    }
                    return application.getId();
                }
            } else {
                throw new APIManagerException("Failed to retrieve the api consumer for username" + username);
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to create application [name:" + applicationName + " , username:"
                                          + username + ", " + "groupId:" + groupId, e);
        }
    }

    private void addSubscription(APIConsumer apiConsumer, APIIdentifier apiId, int applicationId, String username)
            throws APIManagerException {
        try {
            if (apiConsumer != null) {
                APIIdentifier apiIdentifier = new APIIdentifier(apiId.getProviderName(), apiId.getApiName(),
                                                                apiId.getVersion());
                apiIdentifier.setTier(ApiApplicationConstants.DEFAULT_TIER);
                apiConsumer.addSubscription(apiIdentifier, username, applicationId);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully created subscription for API : " + apiId + " from application : " +
                              applicationId);
                }
            } else {
                throw new APIManagerException("API provider configured for the given API configuration is null. " +
                                              "Thus, the API is not published");
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to create subscription for api name : " + apiId.getApiName(), e);
        }
    }


    private void addSubscriber(String subscriberName, String subscriberEmail, String groupId, int tenantId)
            throws APIManagerException {
        if (log.isDebugEnabled()) {
            log.debug("Creating subscriber with name  " + subscriberName);
        }
        try {
            APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
            if (consumer != null) {
                synchronized (consumer) {
                    if (consumer.getSubscriber(subscriberName) == null) {
                        consumer.addSubscriber(subscriberName, groupId);
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully created subscriber with name : " + subscriberName +
                                              " with groupID : " + groupId);
                        }
                    }
                }
            } else {
                throw new APIManagerException("API provider configured for the given API configuration is null. " +
                                              "Thus, the API is not published");
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("API provider configured for the given API configuration is null. " +
                                          "Thus, the API is not published", e);
        }
    }

    /**
     * This method registers an api application and then subscribe the application to the api.
     *
     * @param apiApplicationName name of the application.
     * @param tags               are used subscribe the apis with the tag.
     * @param username           subscription is created for the user.
     * @throws APIManagerException
     */
    private int createApplicationAndSubscribeToAPIs(String apiApplicationName, String tags[], String username)
            throws APIManagerException {
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            String groupId = getLoggedInUserGroupId(username, APIManagerUtil.getTenantDomain());
            int applicationId = createApplication(apiConsumer, apiApplicationName, username, groupId);
            Subscriber subscriber = apiConsumer.getSubscriber(username);
            Set<API> userVisibleAPIs = null;
            for (String tag : tags) {
                Set<API> tagAPIs = apiConsumer.getAPIsWithTag(tag, APIManagerUtil.getTenantDomain());
                if (userVisibleAPIs == null) {
                    userVisibleAPIs = tagAPIs;
                } else {
                    userVisibleAPIs.addAll(tagAPIs);
                }
            }
            if (userVisibleAPIs != null) {
                Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber, apiApplicationName,
                                                                                  groupId);
                for (API userVisibleAPI : userVisibleAPIs) {
                    APIIdentifier apiIdentifier = userVisibleAPI.getId();
                    boolean isSubscribed = false;
                    if (subscribedAPIs != null) {
                        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                            if (subscribedAPI.getApiId().equals(apiIdentifier)) {
                                isSubscribed = true;
                            }
                        }
                    }
                    if (!isSubscribed) {
                        addSubscription(apiConsumer, apiIdentifier, applicationId, username);
                    }
                }
            }
            return applicationId;
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to fetch device apis information for the user " + username, e);
        }
    }

    private String getLoggedInUserGroupId(String username, String tenantDomain) throws APIManagerException {
        JSONObject loginInfoJsonObj = new JSONObject();
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
            loginInfoJsonObj.put("user", username);
            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                loginInfoJsonObj.put("isSuperTenant", true);
            } else {
                loginInfoJsonObj.put("isSuperTenant", false);
            }
            String loginInfoString = loginInfoJsonObj.toString();
            return apiConsumer.getGroupIds(loginInfoString);
        } catch (APIManagementException e) {
            throw new APIManagerException("Unable to get groupIds of user " + username, e);
        }
    }

}
