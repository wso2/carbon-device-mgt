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
package org.wso2.carbon.device.mgt.extensions.api.mgt;

import org.wso2.carbon.device.mgt.extensions.api.mgt.dto.ApiApplicationKey;
import org.wso2.carbon.device.mgt.extensions.api.mgt.exception.APIManagerException;

/**
 * This comprise on operation that is been done with api manager from CDMF.
 */
public interface APIManagementProviderService {

	/**
	 * This method registers an api application and then subscribe the application to the api.
	 *
	 * @param apiApplicationName name of the application.
	 * @param tags        are used subscribe the apis with the tag.
	 * @param username           subscription is created for the user.
	 * @param groupId            grouping of device api
	 * @throws APIManagerException
	 */
	int createApplicationAndSubscribeToAPIs(String apiApplicationName, String tags[], String username,
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
	 * Generate and retreive application keys. if the application does  exist then return it.
	 *
	 * @param apiApplicationName name of the application.
	 * @param tags        names of the api that application needs to be subscribed.
	 * @param keyType            of the application.
	 * @param groupId            grouping of device apis
	 * @param username           to whom the application is created
	 * @return consumerkey and secrete of the created application.
	 * @throws APIManagerException
	 */
	ApiApplicationKey generateAndRetrieveApplicationKeys(String apiApplicationName, String tags[],
														 String keyType, String groupId, String username)
			throws APIManagerException;

	/**
	 * Register existing Oauth application as apim application.
	 */
	void registerExistingOAuthApplicationToAPIApplication(String jsonString, String applicationName, String clientId,
														  String username) throws APIManagerException;

}
