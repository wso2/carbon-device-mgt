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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.authenticator.backend.oauth.AuthenticatorException;
import org.wso2.carbon.identity.authenticator.backend.oauth.OauthAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.impl.ExternalOAuthValidator;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.impl.LocalOAuthValidator;

/**
 * the class validate the configurations and provide the most suitable implementation according to the configuration.
 * Factory class for OAuthValidator.
 */
public class OAuthValidatorFactory {
    private static Log log = LogFactory.getLog(OAuthValidatorFactory.class);

    /**
     * the method check the configuration and provide the appropriate implementation for OAuth2TokenValidator
     *
     * @return OAuth2TokenValidator
     */
    public static OAuth2TokenValidator getValidator(boolean isRemote ,String hostURL) {
       if(isRemote){
            if(!(hostURL == null || hostURL.trim().isEmpty())){
                hostURL = hostURL + OauthAuthenticatorConstants.OAUTH_ENDPOINT_POSTFIX;
                return new ExternalOAuthValidator(hostURL);
            }else {
                log.error("IDP Configuration error",
                        new AuthenticatorException("Remote server name and ip both can't be empty"));
                return null;
            }
        }
        return new LocalOAuthValidator();
    }
}
