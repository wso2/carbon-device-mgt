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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.jwt.client.extension.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.solr.common.util.Hash;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientConfigurationException;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.internal.JWTClientExtensionDataHolder;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is the utility class that is used for JWT Client.
 */
public class JWTClientUtil {

	private static final Log log = LogFactory.getLog(JWTClientUtil.class);
	private static final String HTTPS_PROTOCOL = "https";
	private static final String TENANT_JWT_CONFIG_LOCATION = File.separator + "jwt-config" + File.separator + "jwt.properties";
	private static final String JWT_CONFIG_FILE_NAME = "jwt.properties";
	private static final String SUPERTENANT_JWT_CONFIG_LOCATION =
			CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + JWT_CONFIG_FILE_NAME;
    /**
     * This is added for the carbon authenticator.
     */
    public static final String SIGNED_JWT_AUTH_USERNAME = "Username";

	/**
	 * Return a http client instance
	 *
	 * @param protocol- service endpoint protocol http/https
	 * @return
	 */
	public static HttpClient getHttpClient(String protocol)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		HttpClient httpclient;
		if (HTTPS_PROTOCOL.equals(protocol)) {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} else {
			httpclient = HttpClients.createDefault();
		}
		return httpclient;
	}

	public static String getResponseString(HttpResponse httpResponse) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			String readLine;
			String response = "";
			while (((readLine = br.readLine()) != null)) {
				response += readLine;
			}
			return response;
		} finally {
			EntityUtils.consumeQuietly(httpResponse.getEntity());
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.warn("Error while closing the connection! " + e.getMessage());
				}
			}
		}
	}

	public static void initialize(JWTClientManagerService jwtClientManagerService)
			throws RegistryException, IOException, JWTClientConfigurationException {
		File configFile = new File(SUPERTENANT_JWT_CONFIG_LOCATION);
		if (configFile.exists()) {
			InputStream propertyStream = null;
			try {
				propertyStream = configFile.toURI().toURL().openStream();
				Properties properties = new Properties();
				properties.load(propertyStream);
				jwtClientManagerService.setDefaultJWTClient(properties);
			} finally {
				if (propertyStream != null) {
					propertyStream.close();
				}
			}

		}
	}

	/**
	 * Get the jwt details from the registry for tenants.
	 *
	 * @param tenantId         for identify tenant space.
	 * @param registryLocation retrive the config file from tenant space.
	 * @return the config for tenant
	 * @throws RegistryException
	 */
	public static Resource getConfigRegistryResourceContent(int tenantId, final String registryLocation)
			throws RegistryException {
		try {
			Resource resource = null;
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
			RegistryService registryService = JWTClientExtensionDataHolder.getInstance().getRegistryService();
			if (registryService != null) {
				Registry registry = registryService.getConfigSystemRegistry(tenantId);
				JWTClientUtil.loadTenantRegistry(tenantId);
				if (registry.resourceExists(registryLocation)) {
					resource = registry.get(registryLocation);
				}
			}
			return resource;
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * Get the jwt details from the registry for tenants.
	 *
	 * @param tenantId for accesing tenant space.
	 * @return the config for tenant
	 * @throws RegistryException
	 */
	public static void addJWTConfigResourceToRegistry(int tenantId, String content)
			throws RegistryException {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
			RegistryService registryService = JWTClientExtensionDataHolder.getInstance().getRegistryService();
			if (registryService != null) {
				Registry registry = registryService.getConfigSystemRegistry(tenantId);
				JWTClientUtil.loadTenantRegistry(tenantId);
				if (!registry.resourceExists(TENANT_JWT_CONFIG_LOCATION)) {
					Resource resource = registry.newResource();
					resource.setContent(content.getBytes());
					registry.put(TENANT_JWT_CONFIG_LOCATION, resource);
				}
			}
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	private static void loadTenantRegistry(int tenantId) throws RegistryException {
		TenantRegistryLoader tenantRegistryLoader =
				JWTClientExtensionDataHolder.getInstance().getTenantRegistryLoader();
		JWTClientExtensionDataHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
		tenantRegistryLoader.loadTenantRegistry(tenantId);
	}

    public static String generateSignedJWTAssertion(String username, JWTConfig jwtConfig, boolean isDefaultJWTClient)
            throws JWTClientException {
        return generateSignedJWTAssertion(username, jwtConfig, isDefaultJWTClient, null);
    }

	public static String generateSignedJWTAssertion(String username, JWTConfig jwtConfig, boolean isDefaultJWTClient,
                                                    Map<String, String> customClaims) throws JWTClientException {
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
				String defaultTokenId = currentTimeMillis + "" + new SecureRandom().nextInt();
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
            claimsSet.setClaim(SIGNED_JWT_AUTH_USERNAME, username);
            if (customClaims != null && !customClaims.isEmpty()) {
                for (String key : customClaims.keySet()) {
                    claimsSet.setClaim(key, customClaims.get(key));
                }
            }

			// get Keystore params
			String keyStorePath = jwtConfig.getKeyStorePath();
			String privateKeyAlias = jwtConfig.getPrivateKeyAlias();
			String privateKeyPassword = jwtConfig.getPrivateKeyPassword();
			KeyStore keyStore;
			RSAPrivateKey rsaPrivateKey = null;
			if (keyStorePath != null && !keyStorePath.isEmpty()) {
				String keyStorePassword = jwtConfig.getKeyStorePassword();
				keyStore = loadKeyStore(new File(keyStorePath), keyStorePassword, "JKS");
				rsaPrivateKey = (RSAPrivateKey) keyStore.getKey(privateKeyAlias, privateKeyPassword.toCharArray());
			} else {
				int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
				JWTClientUtil.loadTenantRegistry(tenantId);
				if (!(MultitenantConstants.SUPER_TENANT_ID == tenantId) && !isDefaultJWTClient) {
					KeyStoreManager tenantKeyStoreManager = KeyStoreManager.getInstance(tenantId);
					String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
					String ksName = tenantDomain.trim().replace('.', '-');
					String jksName = ksName + ".jks";
					rsaPrivateKey = (RSAPrivateKey) tenantKeyStoreManager.getPrivateKey(jksName, tenantDomain);
				} else {
					PrivilegedCarbonContext.startTenantFlow();
					PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
					KeyStoreManager tenantKeyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
					rsaPrivateKey = (RSAPrivateKey) tenantKeyStoreManager.getDefaultPrivateKey();
					PrivilegedCarbonContext.endTenantFlow();
				}
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

	private static KeyStore loadKeyStore(final File keystoreFile, final String password, final String keyStoreType)
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
