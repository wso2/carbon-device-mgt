
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
package org.wso2.carbon.identity.authenticator.backend.oauth.validator.impl;

import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.identity.authenticator.backend.oauth.OauthAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuth2TokenValidator;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuthValidationRespond;

/**
 * Handles the authentication using the inbuilt IS features.
 */
public class LocalOAuthValidator implements OAuth2TokenValidator {
    /**
     * This method gets a string accessToken and validates it and generate the OAuth2ClientApplicationDTO
     * containing the validity and user details if valid.
     *
     * @param token which need to be validated.
     * @return OAuthValidationRespond with the validated results.
     */
    public OAuthValidationRespond validateToken(String token) {
        // create an OAuth token validating request DTO
        OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
        // create access token object to validate and populate it
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken =
                validationRequest.new OAuth2AccessToken();
        accessToken.setTokenType(OauthAuthenticatorConstants.BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(token);
        //the workaround till the version is upgraded in both is and EMM to be the same.
        OAuth2TokenValidationRequestDTO.TokenValidationContextParam tokenValidationContextParam[] =
                new OAuth2TokenValidationRequestDTO.TokenValidationContextParam[1];
        //==
        validationRequest.setContext(tokenValidationContextParam);
        //set the token to the validation request
        validationRequest.setAccessToken(accessToken);
        OAuth2TokenValidationService validationService = new OAuth2TokenValidationService();
        OAuth2ClientApplicationDTO respond =  validationService.
                findOAuthConsumerIfTokenIsValid(validationRequest);
        boolean isValid = respond.getAccessTokenValidationResponse().isValid();
        String userName = null;
        String tenantDomain = null;
        if(isValid){
            userName = MultitenantUtils.getTenantAwareUsername(
                    respond.getAccessTokenValidationResponse().getAuthorizedUser());
            tenantDomain =
                    MultitenantUtils.getTenantDomain(respond.getAccessTokenValidationResponse().getAuthorizedUser());
        }
       return new OAuthValidationRespond(userName,tenantDomain,isValid);
    }
}
