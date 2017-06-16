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
package org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant.oauth.validator;

import org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant.oauth.validator.internal.OAuthAuthenticatorDataHolder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;

/**
 * Handles the authentication using the inbuilt IS features.
 */
public class LocalOAuthValidator {
    private static final String BEARER_TOKEN_TYPE = "bearer";

    /**
     * This method gets a string accessToken and validates it and generate the OAuth2ClientApplicationDTO
     * containing the validity and user details if valid.
     *
     * @param token which need to be validated.
     * @return OAuthValidationResponse with the validated results.
     */
    public OAuthValidationResponse validateToken(String token) throws RemoteException{
        OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken =
                validationRequest.new OAuth2AccessToken();
        accessToken.setTokenType(BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(token);
        validationRequest.setAccessToken(accessToken);
        OAuth2TokenValidationResponseDTO tokenValidationResponse = OAuthAuthenticatorDataHolder.getInstance().
                getOAuth2TokenValidationService().findOAuthConsumerIfTokenIsValid(validationRequest).getAccessTokenValidationResponse();
        boolean isValid = tokenValidationResponse.isValid();
        String userName = null;
        String tenantDomain = null;
        if (isValid) {
            userName = MultitenantUtils.getTenantAwareUsername(
                    tokenValidationResponse.getAuthorizedUser());
            tenantDomain =
                    MultitenantUtils.getTenantDomain(tokenValidationResponse.getAuthorizedUser());
        }
        return new OAuthValidationResponse(userName, tenantDomain, isValid);
    }
}
