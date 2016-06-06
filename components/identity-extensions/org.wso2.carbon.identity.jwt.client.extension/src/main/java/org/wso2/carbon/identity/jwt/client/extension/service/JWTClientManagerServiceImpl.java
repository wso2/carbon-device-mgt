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

package org.wso2.carbon.identity.jwt.client.extension.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.constant.JWTConstants;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientAlreadyExistsException;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientConfigurationException;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This creates JWT Client for each tenant and implements the JWTClientManagerService interface.
 */
public class JWTClientManagerServiceImpl implements JWTClientManagerService {

    private static Map<String, JWTClient> jwtClientMap;
    private static final Log log = LogFactory.getLog(JWTClientManagerServiceImpl.class);
    private static final String TENANT_JWT_CONFIG_LOCATION = "/jwt-config/jwt.properties";
    private static JWTClient defaultJWTClient;


    public JWTClientManagerServiceImpl() {
        jwtClientMap = new ConcurrentHashMap<>();
    }

    /**
     * this return the jwt based token client to generate token for the tenant.
     */
    @Override
    public JWTClient getJWTClient() throws JWTClientException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (tenantId == -1) {
            throw new JWTClientException("Invalid tenant domain :" + tenantDomain);
        }
        //Get jwt client which has been registered for the tenant.
        JWTClient jwtClient = getJWTClient(tenantDomain);
        if (jwtClient == null) {
            //Create a new jwt client for the tenant.
            try {
                Properties properties = getJWTConfigProperties(tenantId);
                if (properties == null) {
                    if (defaultJWTClient != null) {
                        return defaultJWTClient;
                    } else {
                        throw new JWTClientException("JWT Configuration is not available for tenant " + tenantDomain);
                    }
                }
                JWTConfig jwtConfig = new JWTConfig(properties);
                jwtClient = new JWTClient(jwtConfig);
                addJWTClient(tenantDomain, jwtClient);
            } catch (JWTClientAlreadyExistsException e) {
                log.warn("Attempting to register a jwt client for the tenant " + tenantDomain +
                        " when one already exists. Returning existing jwt client");
                return getJWTClient(tenantDomain);
            } catch (JWTClientConfigurationException e) {
                throw new JWTClientException("Failed to parse jwt configuration for tenant " + tenantDomain, e);
            }
        }
        return jwtClient;
    }

    /**
     * This will set the default JWT Client that will be used if there is any available for tenants.
     */
    @Override
    public void setDefaultJWTClient(Properties properties) throws JWTClientConfigurationException {
        if (properties == null) {
            throw new JWTClientConfigurationException("Failed to load jwt configuration for super tenant.");
        }
        String defaultJWTClientMode = properties.getProperty(JWTConstants.DEFAULT_JWT_CLIENT);
        boolean isDefaultJwtClient = false;
        if (defaultJWTClientMode != null && !defaultJWTClientMode.isEmpty()) {
            isDefaultJwtClient = Boolean.parseBoolean(defaultJWTClientMode);
        }
        if (isDefaultJwtClient) {
            try {
                JWTConfig jwtConfig = new JWTConfig(properties);
                defaultJWTClient = new JWTClient(jwtConfig, true);
                addJWTClient(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, defaultJWTClient);
            } catch (JWTClientAlreadyExistsException e) {
                log.warn("Attempting to register a jwt client for the super tenant" +
                        " when one already exists. Returning existing jwt client");
            }
        }
    }

    /**
     * Fetch the jwt client which has been registered under the tenant domain.
     *
     * @param tenantDomain - The tenant domain under which the jwt client is registered
     * @return - Instance of the jwt client which was registered. Null if not registered.
     */
    private JWTClient getJWTClient(String tenantDomain) {
        if (jwtClientMap.containsKey(tenantDomain)) {
            return jwtClientMap.get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a jwt client to the jwt client map.
     *
     * @param tenantDomain - The tenant domain under which the jwt client will be registered.
     * @param jwtClient    - Instance of the jwt client
     * @throws JWTClientAlreadyExistsException - If a jwt client has already been registered under the tenantdomain
     */
    private void addJWTClient(String tenantDomain, JWTClient jwtClient) throws JWTClientAlreadyExistsException {
        synchronized (jwtClientMap) {
            if (jwtClientMap.containsKey(tenantDomain)) {
                throw new JWTClientAlreadyExistsException(
                        "A jwt client has already been created for the tenant " + tenantDomain);
            }
            jwtClientMap.put(tenantDomain, jwtClient);
        }
    }

    /**
     * Retrieve JWT configs from registry.
     */
    private Properties getJWTConfigProperties(int tenantId) throws JWTClientConfigurationException {
        try {
            Resource config = JWTClientUtil.getConfigRegistryResourceContent(tenantId, TENANT_JWT_CONFIG_LOCATION);
            Properties properties = null;
            if (config != null) {
                properties = new Properties();
                properties.load(config.getContentStream());
            }
            return properties;
        } catch (RegistryException e) {
            throw new JWTClientConfigurationException("Failed to load the content from registry for tenant " +
                    tenantId, e);
        } catch (IOException e) {
            throw new JWTClientConfigurationException(
                    "Failed to parse the content from the registry for tenant " + tenantId, e);
        }
    }
}
