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
package org.wso2.carbon.apimgt.application.extension;


import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;

/**
 * This comprise on operation that is been done with api manager from CDMF. This service needs to be implemented in APIM.
 */
public interface APIManagementProviderService {

    /**
     * Generate and retreive application keys. if the application does exist then
     * create it and subscribe to apis that are grouped with the tags.
     *
     * @param apiApplicationName name of the application.
     * @param tags               tags of the apis that application needs to be subscribed.
     * @param keyType            of the application.
     * @param username           to whom the application is created
     * @param isAllowedAllDomains application is allowed to all the tenants
     * @param validityTime       validity period of the application
     * @return consumerkey and secrete of the created application.
     * @throws APIManagerException
     */
    ApiApplicationKey generateAndRetrieveApplicationKeys(String apiApplicationName, String tags[],
                                                         String keyType, String username, boolean isAllowedAllDomains,
                                                         String validityTime)
            throws APIManagerException;

    /**
     * Register existing Oauth application as apim application.
     */
    void registerExistingOAuthApplicationToAPIApplication(String jsonString, String applicationName, String clientId,
                                                          String username, boolean isAllowedAllDomains, String keyType,
                                                          String tags[]) throws APIManagerException;

    /**
     * Remove APIM Application.
     */
    void removeAPIApplication(String applicationName, String username) throws APIManagerException;

}
