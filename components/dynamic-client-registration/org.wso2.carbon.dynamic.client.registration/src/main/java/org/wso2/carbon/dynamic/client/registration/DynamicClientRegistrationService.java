/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.dynamic.client.registration;

import org.wso2.carbon.dynamic.client.registration.profile.RegistrationProfile;

/**
 * This class represents the interface to be implemented by DynamicClientRegistrationService.
 */
public interface DynamicClientRegistrationService {

	/**
	 * This method will register a new OAuth application using the data provided by
	 * RegistrationProfile.
	 *
	 * @param profile - RegistrationProfile of the OAuth application to be created.
	 * @return OAuthApplicationInfo object which holds the necessary data of created OAuth app.
	 * @throws DynamicClientRegistrationException
	 */
	public OAuthApplicationInfo registerOAuthApplication(RegistrationProfile profile) throws
	                                                                                DynamicClientRegistrationException;

	/**
	 * This method will unregister a created OAuth application.
	 *
	 * @param userName - Username of the owner
	 * @param applicationName - OAuth application name
	 * @param consumerKey - ConsumerKey of the OAuth application
	 * @return The status of the operation
	 * @throws DynamicClientRegistrationException
	 */
	public boolean unregisterOAuthApplication(String userName, String applicationName,
	                                      String consumerKey) throws DynamicClientRegistrationException;

	/**
	 * This method will check the existence of an OAuth application provided application-name.
	 *
	 * @param applicationName  - OAuth application name
	 * @return The status of the operation
	 * @throws DynamicClientRegistrationException
	 */
	public boolean isOAuthApplicationExists(String applicationName)
			throws DynamicClientRegistrationException;

}
