/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.api.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.wso2.carbon.device.mgt.common.api.mgt.APIManagerException;
import org.wso2.carbon.device.mgt.common.api.mgt.AccessTokenException;
import org.wso2.carbon.device.mgt.common.api.mgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.common.api.mgt.ApiApplicationKey;
import org.wso2.carbon.device.mgt.core.api.mgt.TokenClient;
import org.wso2.carbon.device.mgt.core.api.mgt.util.APIUtil;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.tokenendpoint.TokenConfigurations;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

/**
 * this class represents an implementation of Token Client
 */
public class TokenClientImpl implements TokenClient {

	private static Log log = LogFactory.getLog(TokenClientImpl.class);
	private URL tokenURL;
	private String grantType;
	private static final String TOKEN_TYPE = "PRODUCTION";

	public TokenClientImpl() {
		TokenConfigurations tokenConfigurations = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
				getDeviceManagementConfigRepository().getTokenConfigurations();
		grantType = tokenConfigurations.getDeviceGrantType();
		try {
			tokenURL = new URL(tokenConfigurations.getTokenEndpointURL());
		} catch (MalformedURLException e) {
			log.error("Invalid Token Endpoint URL : " + tokenConfigurations.getTokenEndpointURL());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessTokenInfo getAccessToken(String apiApplicationName, String deviceType, String username,
										  String deviceId, String scopes, String applicationSubscriberUsername)
			throws AccessTokenException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("grant_type", grantType));
		params.add(new BasicNameValuePair("device_id", deviceId));
		params.add(new BasicNameValuePair("device_type", deviceType));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("scope", scopes));
		return getTokenInfo(params, deviceType, applicationSubscriberUsername, apiApplicationName);
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessTokenInfo getAccessTokenFromRefreshToken(String apiApplicationName, String deviceType,
														  String refreshToken, String username,
														  String scopes, String applicationSubscriberUsername)
			throws AccessTokenException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("grant_type", "refresh_token"));
		params.add(new BasicNameValuePair("refresh_token", refreshToken));
		params.add(new BasicNameValuePair("scope", scopes));
		return getTokenInfo(params, deviceType, applicationSubscriberUsername, apiApplicationName);
	}


	private AccessTokenInfo getTokenInfo(List<NameValuePair> nameValuePairs, String deviceType, String username,
										 String apiApplicationName)
			throws AccessTokenException {
		String response = null;
		try {
			if (tokenURL == null) {
				return null;
			}
			HttpClient httpClient = APIUtil.getHttpClient(tokenURL.getPort(), tokenURL.getProtocol());
			HttpPost postMethod = new HttpPost(tokenURL.toString());
			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			String deviceTypes[] = new String[1];
			deviceTypes[0] = deviceType;
			ApiApplicationKey apiApplicationKey =
					DeviceManagementDataHolder.getInstance().getAPIManagementProviderService()
							.generateAndRetrieveApplicationKeys(apiApplicationName, deviceTypes, TOKEN_TYPE, "",
																username);
			if (apiApplicationKey != null) {
				postMethod.addHeader("Authorization", "Basic " + getBase64Encode(apiApplicationKey.getConsumerKey(),
																				 apiApplicationKey.getConsumerSecret()));
				postMethod.addHeader("Content-Type", "application/x-www-form-urlencoded");
				HttpResponse httpResponse = httpClient.execute(postMethod);
				response = APIUtil.getResponseString(httpResponse);
				if (log.isDebugEnabled()) {
					log.debug(response);
				}
				JSONObject jsonObject = new JSONObject(response);
				AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
				accessTokenInfo.setAccess_token(jsonObject.getString("access_token"));
				accessTokenInfo.setRefresh_token(jsonObject.getString("refresh_token"));
				accessTokenInfo.setExpires_in(jsonObject.getInt("expires_in"));
				accessTokenInfo.setToken_type(jsonObject.getString("token_type"));
				return accessTokenInfo;
			}
			throw new AccessTokenException("No application registered for device type " + deviceType);
		} catch (JSONException e) {
			throw new AccessTokenException("Error when parsing the response " + response);
		} catch (IOException e) {
			throw new AccessTokenException("Error when reading the response from buffer " + response);
		} catch (APIManagerException e) {
			throw new AccessTokenException("Error when generating application keys for device type :" + deviceType);
		}
	}

	private String getBase64Encode(String consumerKey, String consumerSecret) {
		return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
	}
}



