/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.identity.authenticator.backend.oauth.validator;

import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.authenticator.backend.oauth.OauthAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.impl.ExternalOAuthValidator;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.impl.LocalOAuthValidator;

/**
 * The class validate the configurations and provide the most suitable implementation according to the configuration.
 * Factory class for OAuthValidator.
 */
public class OAuthValidatorFactory {

    private static final String AUTHENTICATOR_CONFIG_IS_REMOTE = "isRemote";
    private static final String AUTHENTICATOR_CONFIG_HOST_URL = "hostURL";
    private static final String AUTHENTICATOR_CONFIG_ADMIN_USERNAME = "adminUsername";
    private static final String AUTHENTICATOR_CONFIG_ADMIN_PASSWORD = "adminPassword";

    /**
     * The method check the configuration and provide the appropriate implementation for OAuth2TokenValidator
     *
     * @return OAuth2TokenValidator
     */
    public static OAuth2TokenValidator getValidator() throws IllegalArgumentException {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration.
                getAuthenticatorConfig(OauthAuthenticatorConstants.AUTHENTICATOR_NAME);
        boolean isRemote;
        String hostUrl;
        String adminUserName;
        String adminPassword;
        if (authenticatorConfig != null && authenticatorConfig.getParameters() != null) {
            isRemote = Boolean.parseBoolean(authenticatorConfig.getParameters().get(
                    AUTHENTICATOR_CONFIG_IS_REMOTE));
            hostUrl = authenticatorConfig.getParameters().get(AUTHENTICATOR_CONFIG_HOST_URL);
            adminUserName = authenticatorConfig.getParameters().get(AUTHENTICATOR_CONFIG_ADMIN_USERNAME);
            adminPassword = authenticatorConfig.getParameters().get(AUTHENTICATOR_CONFIG_ADMIN_PASSWORD);
        } else {
            throw new IllegalArgumentException("Configuration parameters need to be defined in Authenticators.xml");
        }
        if (isRemote) {
            if (!(hostUrl == null || hostUrl.trim().isEmpty())) {
                hostUrl = hostUrl + OauthAuthenticatorConstants.OAUTH_ENDPOINT_POSTFIX;
                return new ExternalOAuthValidator(hostUrl, adminUserName, adminPassword);
            } else {
                throw new IllegalArgumentException("Remote server name and ip both can't be empty");
            }
        }
        return new LocalOAuthValidator();
    }
}
