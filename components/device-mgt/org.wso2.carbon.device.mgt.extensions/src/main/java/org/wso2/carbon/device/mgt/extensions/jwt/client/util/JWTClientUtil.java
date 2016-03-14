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
package org.wso2.carbon.device.mgt.extensions.jwt.client.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceExtensionDataHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.utils.CarbonUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This is the utility class that is used for JWT Client.
 */
public class JWTClientUtil {

	private static final Log log = LogFactory.getLog(JWTClientUtil.class);
	private static final String HTTPS_PROTOCOL = "https";
	private static final String TENANT_JWT_CONFIG_LOCATION = "/jwt-config/jwt.properties";
	private static final String JWT_CONFIG_FILE_NAME = "jwt.properties";
	private static final String SUPERTENANT_JWT_CONFIG_LOCATION =
			CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + JWT_CONFIG_FILE_NAME;
	/**
	 * Return a http client instance
	 * @param protocol- service endpoint protocol http/https
	 * @return
	 */
	public static HttpClient getHttpClient(String protocol)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		HttpClient httpclient;
		if (HTTPS_PROTOCOL.equals(protocol)) {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, (TrustStrategy) new TrustSelfSignedStrategy());
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

	public static void initialize() throws RegistryException, IOException {
		Resource resource = getConfigRegistryResourceContent(MultitenantConstants.SUPER_TENANT_ID, TENANT_JWT_CONFIG_LOCATION);
		if (resource == null) {
			File configFile = new File(SUPERTENANT_JWT_CONFIG_LOCATION);
			String contents = FileUtils.readFileToString(configFile, "UTF-8");
			addJWTConfigResourceToRegistry(MultitenantConstants.SUPER_TENANT_ID, contents);
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
			RegistryService registryService = DeviceExtensionDataHolder.getInstance().getRegistryService();
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
	 * @param tenantId  for accesing tenant space.
	 * @return the config for tenant
	 * @throws RegistryException
	 */
	public static void addJWTConfigResourceToRegistry(int tenantId, String content)
			throws RegistryException {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
			RegistryService registryService = DeviceExtensionDataHolder.getInstance().getRegistryService();
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
		TenantRegistryLoader tenantRegistryLoader = DeviceExtensionDataHolder.getInstance().getTenantRegistryLoader();
		DeviceExtensionDataHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
		tenantRegistryLoader.loadTenantRegistry(tenantId);
	}
}
