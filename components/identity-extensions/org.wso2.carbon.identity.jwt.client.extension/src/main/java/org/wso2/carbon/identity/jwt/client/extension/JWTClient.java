/*
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.jwt.client.extension;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.identity.jwt.client.extension.constant.JWTConstants;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * this class represents an implementation of Token Client which is based on JWT
 */
public class JWTClient {

	private static Log log = LogFactory.getLog(JWTClient.class);
	private JWTConfig jwtConfig;
	private boolean isDefaultJWTClient;

	public JWTClient(JWTConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	public JWTClient(JWTConfig jwtConfig, boolean isDefaultJWTClient) {
		this.jwtConfig = jwtConfig;
		this.isDefaultJWTClient = isDefaultJWTClient;
	}

	public AccessTokenInfo getAccessToken(String consumerKey, String consumerSecret, String username, String scopes)
			throws JWTClientException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(JWTConstants.GRANT_TYPE_PARAM_NAME, jwtConfig.getJwtGrantType()));
		String assertion = JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient);
		if (assertion == null) {
			throw new JWTClientException("JWT is not configured properly for user : " + username);
		}
		params.add(new BasicNameValuePair(JWTConstants.JWT_PARAM_NAME, assertion));
        if (scopes != null && !scopes.isEmpty()) {
            params.add(new BasicNameValuePair(JWTConstants.SCOPE_PARAM_NAME, scopes));
        }
		return getTokenInfo(params, consumerKey, consumerSecret);
	}

    public AccessTokenInfo getAccessToken(String encodedAppcredential, String username, String scopes)
            throws JWTClientException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(JWTConstants.GRANT_TYPE_PARAM_NAME, jwtConfig.getJwtGrantType()));
        String assertion = JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient);
        if (assertion == null) {
            throw new JWTClientException("JWT is not configured properly for user : " + username);
        }
        params.add(new BasicNameValuePair(JWTConstants.JWT_PARAM_NAME, assertion));
        if (scopes != null && !scopes.isEmpty()) {
            params.add(new BasicNameValuePair(JWTConstants.SCOPE_PARAM_NAME, scopes));
        }
        String decodedKey[] = getDecodedKey(encodedAppcredential);
        if (decodedKey.length != 2) {
            throw new JWTClientException("Invalid app credential");
        }
        return getTokenInfo(params, decodedKey[0], decodedKey[1]);
    }

	public AccessTokenInfo getAccessToken(String consumerKey, String consumerSecret, String username, String scopes,
										  Map<String, String> paramsMap)
			throws JWTClientException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(JWTConstants.GRANT_TYPE_PARAM_NAME, jwtConfig.getJwtGrantType()));
		String assertion = JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient);
		if (assertion == null) {
			throw new JWTClientException("JWT is not configured properly for user : " + username);
		}
		params.add(new BasicNameValuePair(JWTConstants.JWT_PARAM_NAME, assertion));
        if (scopes != null && !scopes.isEmpty()) {
            params.add(new BasicNameValuePair(JWTConstants.SCOPE_PARAM_NAME, scopes));
        }
		if (paramsMap != null) {
			for (String key : paramsMap.keySet()) {
				params.add(new BasicNameValuePair(key, paramsMap.get(key)));
			}
		}
		return getTokenInfo(params, consumerKey, consumerSecret);
	}


	public AccessTokenInfo getAccessTokenFromRefreshToken(String refreshToken, String username, String scopes,
														  String consumerKey, String consumerSecret)
			throws JWTClientException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(JWTConstants.GRANT_TYPE_PARAM_NAME, JWTConstants.REFRESH_TOKEN_GRANT_TYPE));
		params.add(new BasicNameValuePair(JWTConstants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME, refreshToken));
		params.add(new BasicNameValuePair(JWTConstants.SCOPE_PARAM_NAME, scopes));
		return getTokenInfo(params, consumerKey, consumerSecret);
	}


	private AccessTokenInfo getTokenInfo(List<NameValuePair> nameValuePairs, String consumerKey, String consumerSecret)
			throws JWTClientException {
		String response = null;
		try {
			if (jwtConfig == null) {
				return null;
			}
			URL tokenEndpoint = new URL(jwtConfig.getTokenEndpoint());
			HttpClient httpClient = JWTClientUtil.getHttpClient(tokenEndpoint.getProtocol());
			HttpPost postMethod = new HttpPost(tokenEndpoint.toString());
			postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			postMethod.addHeader("Authorization", "Basic " + getBase64Encode(consumerKey, consumerSecret));
			postMethod.addHeader("Content-Type", "application/x-www-form-urlencoded");
			HttpResponse httpResponse = httpClient.execute(postMethod);
			response = JWTClientUtil.getResponseString(httpResponse);
			if (log.isDebugEnabled()) {
				log.debug(response);
			}
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
			AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
			String accessToken = (String) jsonObject.get(JWTConstants.ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME);
			if (accessToken != null && !accessToken.isEmpty()) {
				accessTokenInfo.setAccessToken(accessToken);
				accessTokenInfo.setRefreshToken((String) jsonObject.get(
                        JWTConstants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME));
				accessTokenInfo.setExpiresIn((Long) jsonObject.get(JWTConstants.OAUTH_EXPIRES_IN));
				accessTokenInfo.setTokenType((String) jsonObject.get(JWTConstants.OAUTH_TOKEN_TYPE));
                accessTokenInfo.setScopes((String) jsonObject.get(JWTConstants.OAUTH_TOKEN_SCOPE));

			}
			return accessTokenInfo;
		} catch (MalformedURLException e) {
			throw new JWTClientException("Invalid URL for token endpoint " + jwtConfig.getTokenEndpoint(), e);
		} catch (ParseException e) {
			throw new JWTClientException("Error when parsing the response " + response, e);
		} catch (IOException e) {
			throw new JWTClientException("Error when reading the response from buffer.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new JWTClientException("No such algorithm found when loading the ssl socket", e);
		} catch (KeyStoreException e) {
			throw new JWTClientException("Failed loading the keystore.", e);
		} catch (KeyManagementException e) {
			throw new JWTClientException("Failed setting up the ssl http client.", e);
		}
	}

	private String getBase64Encode(String consumerKey, String consumerSecret) {
		return new String(Base64.encodeBase64((consumerKey + ":" + consumerSecret).getBytes()));
	}

    private String[] getDecodedKey(String encodedKey) {
        return (new String(Base64.decodeBase64((encodedKey).getBytes()))).split(":");
    }

    public String getJwtToken(String username) throws JWTClientException {
		return JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient);
	}

    public String getJwtToken(String username, Map<String, String> claims) throws JWTClientException {
        return JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient, claims);
    }

    public String getJwtToken(String username, Map<String, String> claims, boolean enableTenantSigning)
            throws JWTClientException {
        if (enableTenantSigning) {
            return JWTClientUtil.generateSignedJWTAssertion(username, jwtConfig, false, claims);
        } else {
            return getJwtToken(username, claims);
        }
    }
}



