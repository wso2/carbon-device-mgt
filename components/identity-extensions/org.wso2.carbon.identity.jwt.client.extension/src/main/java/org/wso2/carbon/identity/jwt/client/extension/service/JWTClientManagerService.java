/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.identity.jwt.client.extension.service;

import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientConfigurationException;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;

import java.util.Properties;

/**
 * This is the JWTClientManagerServiceImpl Service that can be used to have JWT Client for tenant specific.
 */
public interface JWTClientManagerService {

	/**
	 * This return the jwt based token client to generate token for the tenant.
	 * @return JWTClient that can be used to generate token.
	 * @throws JWTClientException when the JWT Client creation fails
	 */
	JWTClient getJWTClient() throws JWTClientException;

	/**
	 * This will set the default JWT Client that will be used if there is any available for tenants.
	 * @param properties required to configure jwt client.
	 * @throws JWTClientConfigurationException throws when the configuration is invalid.
	 */
	void setDefaultJWTClient(Properties properties) throws JWTClientConfigurationException;
}
