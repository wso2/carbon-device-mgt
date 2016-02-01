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

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.wso2.carbon.identity.authenticator.backend.oauth.OauthAuthenticatorConstants;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuth2TokenValidator;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuthValidationResponse;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the Authentication form external IDP servers. Currently supports WSO2 IS only.
 */
public class ExternalOAuthValidator implements OAuth2TokenValidator{

    private String hostURL;
    private String adminUserName;
    private String adminPassword;

    public ExternalOAuthValidator(String hostURL, String adminUserName, String adminPassword) {
        this.hostURL = hostURL;
        this.adminUserName = adminUserName;
        this.adminPassword = adminPassword;
    }
    /**
     * This method gets a string accessToken and validates it and generate the OAuth2ClientApplicationDTO
     * containing the validity and user details if valid.
     *
     * @param token which need to be validated.
     * @return OAuthValidationResponse with the validated results.
     */
    public OAuthValidationResponse validateToken(String token) throws RemoteException {
        OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType(OauthAuthenticatorConstants.BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(token);
        validationRequest.setAccessToken(accessToken);
        OAuth2TokenValidationServiceStub tokenValidationService =
                new OAuth2TokenValidationServiceStub(hostURL);
        ServiceClient client = tokenValidationService._getServiceClient();
        Options options = client.getOptions();
        List<Header> headerList = new ArrayList<>();
        Header header = new Header();
        header.setName(HTTPConstants.HEADER_AUTHORIZATION);
        header.setValue(OauthAuthenticatorConstants.AUTHORIZATION_HEADER_PREFIX_BASIC + " " + getBasicAuthCredentials());
        headerList.add(header);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, headerList);
        client.setOptions(options);
        OAuth2TokenValidationResponseDTO tokenValidationResponse = tokenValidationService.
                findOAuthConsumerIfTokenIsValid(validationRequest).getAccessTokenValidationResponse();
        boolean isValid = tokenValidationResponse.getValid();
        String userName = null;
        String tenantDomain = null;
        if (isValid) {
            userName = MultitenantUtils.getTenantAwareUsername(
                    tokenValidationResponse.getAuthorizedUser());
            tenantDomain = MultitenantUtils.
                    getTenantDomain(tokenValidationResponse.getAuthorizedUser());
        }
        return new OAuthValidationResponse(userName,tenantDomain,isValid);
    }

    private String getBasicAuthCredentials() {
        byte[] bytesEncoded = Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes());
        return new String(bytesEncoded);
    }
}
