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
package org.wso2.carbon.device.mgt.analytics.datapublisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherAlreadyExistsException;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.internal.DeviceAnalyticsDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.analytics.AnalyticsConfigurations;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is used to manage data publisher per tenant.
 */
public class DeviceDataPublisher {
	private static final Log log = LogFactory.getLog(DeviceDataPublisher.class);
	private static final String TENANT_DAS_CONFIG_LOCATION = "/das/config.json";
	private static final String USERNAME_CONFIG_TAG = "username";
	private static final String PASSWORD_CONFIG_TAG = "password";
	/**
	 * map to store data publishers for each tenant.
	 */
	private static Map<String, DataPublisher> dataPublisherMap;
	private static DeviceDataPublisher deviceDataPublisher;

	public static DeviceDataPublisher getInstance() {
		if (deviceDataPublisher == null) {
			synchronized (DeviceDataPublisher.class) {
				if (deviceDataPublisher == null) {
					deviceDataPublisher = new DeviceDataPublisher();
				}
			}
		}
		return deviceDataPublisher;
	}

	private DeviceDataPublisher() {
		dataPublisherMap = new ConcurrentHashMap<>();

	}

	/**
	 * this return the data publisher for the tenant.
	 *
	 * @return
	 * @throws DataPublisherConfigurationException
	 */
	public DataPublisher getDataPublisher() throws DataPublisherConfigurationException {

		String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
		//Get LoadBalancingDataPublisher which has been registered for the tenant.
		DataPublisher dataPublisher = getDataPublisher(tenantDomain);
		//If a LoadBalancingDataPublisher had not been registered for the tenant.
		if (dataPublisher == null) {
			AnalyticsConfigurations analyticsConfig =
					DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
							getDeviceManagementConfigRepository().getAnalyticsConfigurations();
			if (!analyticsConfig.isEnable()) return null;
			String analyticsServerUrlGroups = analyticsConfig.getReceiverServerUrl();
			String analyticsServerUsername = analyticsConfig.getAdminUsername();
			String analyticsServerPassword = analyticsConfig.getAdminPassword();
			if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
				int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
				String userInfo[] = getAnalyticsServerUserInfo(tenantId);
				if (userInfo != null) {
					analyticsServerUsername = userInfo[0];
					analyticsServerPassword = userInfo[1];
				}
			}
			//Create new DataPublisher for the tenant.
			try {
				dataPublisher = new DataPublisher(analyticsServerUrlGroups, analyticsServerUsername,
												  analyticsServerPassword);
				//Add created DataPublisher.
				addDataPublisher(tenantDomain, dataPublisher);
			} catch (DataEndpointAgentConfigurationException e) {
				String errorMsg = "Configuration Exception on data publisher for ReceiverGroup = " +
						analyticsServerUrlGroups + " for username " + analyticsServerUsername;
				throw new DataPublisherConfigurationException(errorMsg, e);
			} catch (DataEndpointException e) {
				String errorMsg = "Invalid ReceiverGroup = " + analyticsServerUrlGroups;
				throw new DataPublisherConfigurationException(errorMsg, e);
			} catch (DataEndpointConfigurationException e) {
				String errorMsg = "Invalid Data endpoint configuration.";
				throw new DataPublisherConfigurationException(errorMsg, e);
			} catch (DataEndpointAuthenticationException e) {
				String errorMsg = "Authentication Failed for user " + analyticsServerUsername;
				throw new DataPublisherConfigurationException(errorMsg, e);
			} catch (TransportException e) {
				throw new DataPublisherConfigurationException(e);
			} catch (DataPublisherAlreadyExistsException e) {
				log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
								 " when one already exists. Returning existing data publisher");
				return getDataPublisher(tenantDomain);
			}
		}
		return dataPublisher;
	}

	/**
	 * Fetch the data publisher which has been registered under the tenant domain.
	 *
	 * @param tenantDomain - The tenant domain under which the data publisher is registered
	 * @return - Instance of the DataPublisher which was registered. Null if not registered.
	 */
	private DataPublisher getDataPublisher(String tenantDomain) {
		if (dataPublisherMap.containsKey(tenantDomain)) {
			return dataPublisherMap.get(tenantDomain);
		}
		return null;
	}

	/**
	 * Adds a LoadBalancingDataPublisher to the data publisher map.
	 *
	 * @param tenantDomain  - The tenant domain under which the data publisher will be registered.
	 * @param dataPublisher - Instance of the LoadBalancingDataPublisher
	 * @throws DataPublisherAlreadyExistsException -
	 *                                             If a data publisher has already been registered under the tenant
	 *                                             domain
	 */
	private void addDataPublisher(String tenantDomain, DataPublisher dataPublisher)
			throws DataPublisherAlreadyExistsException {
		if (dataPublisherMap.containsKey(tenantDomain)) {
			throw new DataPublisherAlreadyExistsException(
					"A DataPublisher has already been created for the tenant " +
							tenantDomain);
		}

		dataPublisherMap.put(tenantDomain, dataPublisher);
	}

	/**
	 * retrieve the credential from registry
	 *
	 */
	private String[] getAnalyticsServerUserInfo(int tenantId) throws DataPublisherConfigurationException {
		try {
			String config = getConfigRegistryResourceContent(tenantId, TENANT_DAS_CONFIG_LOCATION);
			JSONObject jsonConfigforDas = new JSONObject(config);
			String credential[] = new String[2];
			credential[0] = jsonConfigforDas.getString(USERNAME_CONFIG_TAG);
			credential[1] = jsonConfigforDas.getString(PASSWORD_CONFIG_TAG);
			return credential;
		} catch (RegistryException e) {
			throw new DataPublisherConfigurationException("Failed to load the registry for tenant " + tenantId, e);
		} catch (JSONException e) {
			throw new DataPublisherConfigurationException(
					"Failed to parse the credential from the registry for tenant " + tenantId, e);
		}
	}

	/**
	 * get the credential detail from the registry for tenants.
	 *
	 * @param tenantId         for identify tenant space.
	 * @param registryLocation retrive the config file from tenant space.
	 * @return the config for tenant
	 * @throws RegistryException
	 */
	private String getConfigRegistryResourceContent(int tenantId, final String registryLocation)
			throws RegistryException {
		String content = null;
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
			RegistryService registryService = DeviceAnalyticsDataHolder.getInstance().getRegistryService();
			if (registryService != null) {
				Registry registry = registryService.getConfigSystemRegistry(tenantId);
				this.loadTenantRegistry(tenantId);
				if (registry.resourceExists(registryLocation)) {
					Resource resource = registry.get(registryLocation);
					content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
				}
			}
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}

		return content;
	}

	private void loadTenantRegistry(int tenantId) throws RegistryException {
		TenantRegistryLoader tenantRegistryLoader = DeviceAnalyticsDataHolder.getInstance().getTenantRegistryLoader();
		DeviceAnalyticsDataHolder.getInstance().getIndexLoaderService().loadTenantIndex(tenantId);
		tenantRegistryLoader.loadTenantRegistry(tenantId);
	}
}
