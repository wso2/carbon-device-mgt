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

package org.wso2.carbon.identity.jwt.client.extension;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.jwt.client.extension.constant.JWTConstants;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * this class represents an implementation of Token Client which is based on JWT
 */
public class JWTClient {

	private static Log log = LogFactory.getLog(JWTClient.class);
	private static final String JWT_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	private static final String GRANT_TYPE_PARAM_NAME = "grant_type";
	private static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
	private static final String REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME = "refresh_token";
	private static final String JWT_PARAM_NAME = "assertion";
	private static final String SCOPE_PARAM_NAME = "scope";
	private JWTConfig jwtConfig;

	public JWTClient(JWTConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessTokenInfo getAccessToken(String consumerKey, String consumerSecret, String username, String scopes)
			throws JWTClientException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME, JWT_GRANT_TYPE));
		String assertion = generateSignedJWTAssertion(username);
		if (assertion == null) {
			throw new JWTClientException("JWT is not configured properly for user : " + username);
		}
		params.add(new BasicNameValuePair(JWT_PARAM_NAME, assertion));
		params.add(new BasicNameValuePair(SCOPE_PARAM_NAME, scopes));
		return getTokenInfo(params, consumerKey, consumerSecret);
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessTokenInfo getAccessTokenFromRefreshToken(String refreshToken, String username, String scopes,
														  String consumerKey, String consumerSecret)
			throws JWTClientException {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(GRANT_TYPE_PARAM_NAME, REFRESH_TOKEN_GRANT_TYPE));
		params.add(new BasicNameValuePair(REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME, refreshToken));
		params.add(new BasicNameValuePair(SCOPE_PARAM_NAME, scopes));
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
			accessTokenInfo.setAccess_token((String) jsonObject.get(JWTConstants.OAUTH_ACCESS_TOKEN));
			accessTokenInfo.setRefresh_token((String) jsonObject.get(JWTConstants.OAUTH_REFRESH_TOKEN));
			accessTokenInfo.setExpires_in((Long) jsonObject.get(JWTConstants.OAUTH_EXPIRES_IN));
			accessTokenInfo.setToken_type((String) jsonObject.get(JWTConstants.OAUTH_TOKEN_TYPE));
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

	public String generateSignedJWTAssertion(String username) throws JWTClientException {
		try {
			String subject = username;
			long currentTimeMillis = System.currentTimeMillis();
			// add the skew between servers
			String iss = jwtConfig.getIssuer();
			if (iss == null || iss.isEmpty()) {
				return null;
			}
			currentTimeMillis += jwtConfig.getSkew();
			long iat = currentTimeMillis + jwtConfig.getIssuedInternal() * 60 * 1000;
			long exp = currentTimeMillis + jwtConfig.getExpirationTime() * 60 * 1000;
			long nbf = currentTimeMillis + jwtConfig.getValidityPeriodFromCurrentTime() * 60 * 1000;
			String jti = jwtConfig.getJti();
			if (jti == null) {
				String defaultTokenId = currentTimeMillis + "" + new Random().nextInt();
				jti = defaultTokenId;
			}
			List<String> aud = jwtConfig.getAudiences();
			//set up the basic claims
			JWTClaimsSet claimsSet = new JWTClaimsSet();
			claimsSet.setIssueTime(new Date(iat));
			claimsSet.setExpirationTime(new Date(exp));
			claimsSet.setIssuer(iss);
			claimsSet.setSubject(username);
			claimsSet.setNotBeforeTime(new Date(nbf));
			claimsSet.setJWTID(jti);
			claimsSet.setAudience(aud);

			// get Keystore params
			String keyStorePath = jwtConfig.getKeyStorePath();
			String privateKeyAlias = jwtConfig.getPrivateKeyAlias();
			String privateKeyPassword = jwtConfig.getPrivateKeyPassword();
			KeyStore keyStore;
			RSAPrivateKey rsaPrivateKey;
			if (keyStorePath != null && !keyStorePath.isEmpty()) {
				String keyStorePassword = jwtConfig.getKeyStorePassword();
				keyStore = loadKeyStore(new File(keyStorePath), keyStorePassword, "JKS");
				rsaPrivateKey = (RSAPrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword.toCharArray());
			} else {
				int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
				KeyStoreManager tenantKeyStoreManager = KeyStoreManager.getInstance(tenantId);
				rsaPrivateKey = (RSAPrivateKey) tenantKeyStoreManager.getDefaultPrivateKey();
			}
			JWSSigner signer = new RSASSASigner(rsaPrivateKey);
			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
			signedJWT.sign(signer);
			String assertion = signedJWT.serialize();
			return assertion;
		} catch (KeyStoreException e) {
			throw new JWTClientException("Failed loading the keystore.", e);
		} catch (IOException e) {
			throw new JWTClientException("Failed parsing the keystore file.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new JWTClientException("No such algorithm found RS256.", e);
		} catch (CertificateException e) {
			throw new JWTClientException("Failed loading the certificate from the keystore.", e);
		} catch (UnrecoverableKeyException e) {
			throw new JWTClientException("Failed loading the keys from the keystore.", e);
		} catch (JOSEException e) {
			throw new JWTClientException(e);
		} catch (Exception e) {
			//This is thrown when loading default private key.
			throw new JWTClientException("Failed loading the private key.", e);
		}
	}

	private KeyStore loadKeyStore(final File keystoreFile, final String password, final String keyStoreType)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		if (null == keystoreFile) {
			throw new IllegalArgumentException("Keystore url may not be null");
		}
		URI keystoreUri = keystoreFile.toURI();
		URL keystoreUrl = keystoreUri.toURL();
		KeyStore keystore = KeyStore.getInstance(keyStoreType);
		InputStream is = null;
		try {
			is = keystoreUrl.openStream();
			keystore.load(is, null == password ? null : password.toCharArray());
		} finally {
			if (null != is) {
				is.close();
			}
		}
		return keystore;
	}
}



