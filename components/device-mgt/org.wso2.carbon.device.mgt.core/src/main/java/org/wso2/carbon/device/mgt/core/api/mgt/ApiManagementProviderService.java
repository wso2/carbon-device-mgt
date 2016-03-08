/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.api.mgt;

import org.wso2.carbon.device.mgt.common.api.mgt.APIManagerException;
import org.wso2.carbon.device.mgt.common.api.mgt.ApiApplicationKey;

/**
 * This comprise on operation that is been done with api manager from CDMF.
 */
public interface APIManagementProviderService {

	/**
	 * This method registers an api application and then subscribe the application to the api.
	 *
	 * @param apiApplicationName name of the application.
	 * @param deviceTypes        are used retrieved the publish api details.
	 * @param username           subscription is created for the user.
	 * @param groupId            grouping of device api
	 * @throws APIManagerException
	 */
	int createApplicationAndSubscribeDeviceAPIs(String apiApplicationName, String deviceTypes[], String username,
												String groupId) throws APIManagerException;

	/**
	 * This method registers an api application and then subscribe the application to the api.
	 *
	 * @param username subscription is created for the user.
	 * @param groupId  grouping of device api
	 * @throws APIManagerException
	 */
	int createApplicationAndSubscribeToAllAPIs(String apiApplicationName, String username,
											   String groupId) throws APIManagerException;

	/**
	 * Generate and retreive application keys. if the application does not exist then create it.
	 *
	 * @param apiApplicationName name of the application.
	 * @param deviceTypes        names of the api that application needs to be subscribed.
	 * @param keyType            of the application.
	 * @param groupId            grouping of device apis
	 * @param username           to whom the application is created
	 * @return consumerkey and secrete of the created application.
	 * @throws APIManagerException
	 */
	ApiApplicationKey generateAndRetrieveApplicationKeys(String apiApplicationName, String deviceTypes[],
														 String keyType, String groupId, String username)
			throws APIManagerException;

	/**
	 * Register existing Oauth application as apim application.
	 */
	void registerExistingOAuthApplicationToAPIApplication(String jsonString, String applicationName, String clientId,
													   String username) throws APIManagerException;

	/**
	 * @return This returns a token client which is used to generate token.
	 */
	TokenClient getTokenClient();
}
