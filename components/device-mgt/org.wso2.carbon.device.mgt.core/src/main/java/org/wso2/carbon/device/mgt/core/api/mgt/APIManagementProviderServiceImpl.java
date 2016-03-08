/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.api.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.api.mgt.APIManagerException;
import org.wso2.carbon.device.mgt.common.api.mgt.ApiApplicationKey;
import org.wso2.carbon.device.mgt.core.api.mgt.impl.TokenClientImpl;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.apimgt.api.model.Application;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {

	private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);
	private static TokenClient tokenClient;
	private static final String DEFAULT_TOKEN_TYPE = "PRODUCTION";
	private static final String DEFAULT_TIER = "Unlimited";
	private static final String ALLOWED_DOMAINS[] = {"ALL"};

	public APIManagementProviderServiceImpl() {
		tokenClient = new TokenClientImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int createApplicationAndSubscribeDeviceAPIs(String apiApplicationName, String deviceTypes[],
													   String username, String groupId) throws APIManagerException {
		int applicationId = createApplication(apiApplicationName, username, groupId);
		try {
			APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			List<API> userVisibleAPIs = apiConsumer.getAllAPIs();
			Subscriber subscriber = apiConsumer.getSubscriber(username);
			if (subscriber == null) {
				addSubscriber(username, "", "");
				subscriber = apiConsumer.getSubscriber(username);
			}
			Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
			for (String deviceType : deviceTypes) {
				boolean isDeviceTypeApiSubscribed = false;
				for (API userVisbleAPI : userVisibleAPIs) {
					APIIdentifier apiIdentifier = userVisbleAPI.getId();
					if (apiIdentifier.getApiName().equals(deviceType)) {
						boolean isSubscribed = false;
						isDeviceTypeApiSubscribed = true;
						for (SubscribedAPI subscribedAPI : subscribedAPIs) {
							if (subscribedAPI.getApiId().equals(apiIdentifier)) {
								isSubscribed = true;
							}
						}
						if (!isSubscribed) {
							addSubscription(apiIdentifier, applicationId, username);
						}
					}
				}
				if (!isDeviceTypeApiSubscribed) {
					log.warn("No device type found for " + deviceType);
				}
			}
		} catch (APIManagementException e) {
			throw new APIManagerException("Failed to fetch device apis information for the user " + username, e);
		}
		return applicationId;
	}

	@Override
	public int createApplicationAndSubscribeToAllAPIs(String apiApplicationName, String username, String groupId)
			throws APIManagerException {
		int applicationId = createApplication(apiApplicationName, username, groupId);
		try {
			APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
			Set<API> tenantVisibleAPIs = apiConsumer.getAllPublishedAPIs(tenantDomain);
			Subscriber subscriber = apiConsumer.getSubscriber(username);
			if (subscriber == null) {
				addSubscriber(username, "", "");
				subscriber = apiConsumer.getSubscriber(username);
			}
			Set<SubscribedAPI> subscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber);
			for (API visibleApi : tenantVisibleAPIs) {
				APIIdentifier apiIdentifier = visibleApi.getId();
				boolean isSubscribed = false;
				for (SubscribedAPI subscribedAPI : subscribedAPIs) {
					if (subscribedAPI.getApiId().equals(apiIdentifier)) {
						isSubscribed = true;
					}
				}
				if (!isSubscribed) {
					addSubscription(apiIdentifier, applicationId, username);
				}
			}
		} catch (APIManagementException e) {
			throw new APIManagerException("Failed to fetch device apis information for the user " + username, e);
		}
		return applicationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApiApplicationKey generateAndRetrieveApplicationKeys(String apiApplicationName, String deviceTypes[],
																String keyType, String groupId, String username)
			throws APIManagerException {
		try {
			APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			Application apiApplication = apiConsumer.getApplicationsByName(username, apiApplicationName, groupId);
			if (apiApplication == null) {
				createApplicationAndSubscribeDeviceAPIs(apiApplicationName, deviceTypes, username, groupId);
				apiApplication = apiConsumer.getApplicationsByName(username, apiApplicationName, groupId);
			}
			if (apiApplication == null) {
				throw new APIManagerException("Failed to create an application with application name: " +
													  apiApplicationName + " for user: " + username);
			}
			boolean alreadyContainsKey = false;
			APIKey retrievedApiApplicationKey = null;
			for (APIKey apiKey : apiApplication.getKeys()) {
				String applicationKeyType = apiKey.getType();
				if (applicationKeyType != null && applicationKeyType.equals(keyType)) {
					alreadyContainsKey = true;
					retrievedApiApplicationKey = apiKey;
					break;
				}
			}
			if (alreadyContainsKey) {
				ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
				apiApplicationKey.setConsumerKey(retrievedApiApplicationKey.getConsumerKey());
				apiApplicationKey.setConsumerSecret(retrievedApiApplicationKey.getConsumerSecret());
				return apiApplicationKey;
			}
			String[] allowedDomains = ALLOWED_DOMAINS;
			String validityTime = "3600";
			String ownerJsonString = "{\"username\":\"" + username + "\"}";
			Map<String, Object> keyDetails = apiConsumer.requestApprovalForApplicationRegistration(username,
																								   apiApplicationName,
																								   keyType, "null",
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TokenClient getTokenClient() {
		return tokenClient;
	}


	private int createApplication(String applicationName, String username, String groupId) throws APIManagerException {
		try {
			APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			if (apiConsumer != null) {
				if (apiConsumer.getSubscriber(username) == null) {
					addSubscriber(username, "", groupId);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Subscriber [" + username + "] already exist");
					}
				}
				Application application = apiConsumer.getApplicationsByName(username, applicationName, groupId);
				if (application == null) {
					Subscriber subscriber = apiConsumer.getSubscriber(username);
					application = new Application(applicationName, subscriber);
					application.setTier(DEFAULT_TIER);
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

	private void addSubscription(APIIdentifier apiId, int applicationId, String username) throws APIManagerException {
		if (log.isDebugEnabled()) {
			log.debug("Creating subscription for API " + apiId);
		}
		try {
			APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			if (consumer != null) {
				APIIdentifier apiIdentifier = new APIIdentifier(apiId.getProviderName(), apiId.getApiName(),
																apiId.getVersion());
				consumer.addSubscription(apiIdentifier, username, applicationId);
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


	private void addSubscriber(String subscriberName, String subscriberEmail, String groupId)
			throws APIManagerException {
		if (log.isDebugEnabled()) {
			log.debug("Creating subscriber with name  " + subscriberName);
		}
		try {
			APIConsumer consumer = APIManagerFactory.getInstance().getAPIConsumer(subscriberName);
			if (consumer != null) {
				Subscriber subscriber = new Subscriber(subscriberName);
				subscriber.setSubscribedDate(new Date());
				subscriber.setEmail(subscriberEmail);
				subscriber.setTenantId(DeviceManagerUtil.getTenantId());
				consumer.addSubscriber(subscriber, groupId);
				if (log.isDebugEnabled()) {
					log.debug("Successfully created subscriber with name : " + subscriberName + " with groupID : " +
									  groupId);
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
	 * {@inheritDoc}
	 */
	@Override
	public void registerExistingOAuthApplicationToAPIApplication(String jsonString, String applicationName,
															  String clientId, String username)
			throws APIManagerException {
		APIConsumer apiConsumer;
		try {
			apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
			if (apiConsumer != null) {
				String[] allowedDomains = ALLOWED_DOMAINS;
				apiConsumer.mapExistingOAuthClient(jsonString, username, clientId, applicationName, DEFAULT_TOKEN_TYPE,
												   allowedDomains);
			}
		} catch (APIManagementException e) {
			throw new APIManagerException(
					"Failed registering the OAuth app [ clientId " + clientId + " ] with api manager application", e);
		}
	}

	/**
	 *  When an input is having '@',replace it with '-AT-' [This is required to persist API data in registry,as registry paths don't allow '@' sign.]
	 * @param input inputString
	 * @return String modifiedString
	 */
	private static String replaceEmailDomain(String input){
		if(input!=null&& input.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR) ){
			input=input.replace(APIConstants.EMAIL_DOMAIN_SEPARATOR,APIConstants.EMAIL_DOMAIN_SEPARATOR_REPLACEMENT);
		}
		return input;
	}
}
