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
package org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.impl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_TokenValidationContextParam;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthConstants;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthTokenValidationException;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the OAuth2 token validation from remote IS servers using remote OAuthValidation service-stub.
 */
public class RemoteOAuthValidator implements OAuth2TokenValidator {

    private String hostURL;
    private String adminUserName;
    private String adminPassword;

    public RemoteOAuthValidator(String hostURL, String adminUserName, String adminPassword) {
        this.hostURL = hostURL;
        this.adminUserName = adminUserName;
        this.adminPassword = adminPassword;
    }

    private String getBasicAuthCredentials() {
        byte[] bytesEncoded = Base64.encodeBase64((adminUserName + ":" + adminPassword).getBytes());
        return new String(bytesEncoded);
    }

    @Override
    public OAuthValidationResponse validateToken(String accessToken, String resource) throws
                                                                                      OAuthTokenValidationException {
        OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken oauthToken =
                new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        oauthToken.setTokenType(OAuthConstants.BEARER_TOKEN_TYPE);
        oauthToken.setIdentifier(accessToken);
        validationRequest.setAccessToken(oauthToken);

        //Set the resource context param. This will be used in scope validation.
        OAuth2TokenValidationRequestDTO_TokenValidationContextParam resourceContextParam = new
                OAuth2TokenValidationRequestDTO_TokenValidationContextParam();
        resourceContextParam.setKey(OAuthConstants.RESOURCE_KEY);
        resourceContextParam.setValue(resource);

        OAuth2TokenValidationRequestDTO_TokenValidationContextParam[] tokenValidationContextParams =
                new OAuth2TokenValidationRequestDTO_TokenValidationContextParam[1];
        tokenValidationContextParams[0] = resourceContextParam;
        validationRequest.setContext(tokenValidationContextParams);

        OAuth2TokenValidationServiceStub tokenValidationService;
        try {
            tokenValidationService = new OAuth2TokenValidationServiceStub(hostURL);
        } catch (AxisFault axisFault) {
            throw new OAuthTokenValidationException("Exception occurred while obtaining the " +
                                                    "OAuth2TokenValidationServiceStub.", axisFault);
        }
        ServiceClient client = tokenValidationService._getServiceClient();
        Options options = client.getOptions();
        List<Header> headerList = new ArrayList<>();
        Header header = new Header();
        header.setName(HTTPConstants.HEADER_AUTHORIZATION);
        header.setValue(OAuthConstants.AUTHORIZATION_HEADER_PREFIX_BASIC + " " + getBasicAuthCredentials());
        headerList.add(header);
        options.setProperty(HTTPConstants.HTTP_HEADERS, headerList);
        client.setOptions(options);
        OAuth2TokenValidationResponseDTO tokenValidationResponse;
        try {
            tokenValidationResponse = tokenValidationService.
                                      findOAuthConsumerIfTokenIsValid(validationRequest).getAccessTokenValidationResponse();
        } catch (RemoteException e) {
            throw new OAuthTokenValidationException("Remote Exception occurred while invoking the Remote IS server for " +
                                                    "OAuth2 token validation.", e);
        }
        boolean isValid = tokenValidationResponse.getValid();
        String userName;
        String tenantDomain;
        if (isValid) {
            userName = MultitenantUtils.getTenantAwareUsername(
                    tokenValidationResponse.getAuthorizedUser());
            tenantDomain = MultitenantUtils.getTenantDomain(tokenValidationResponse.getAuthorizedUser());
        } else {
            OAuthValidationResponse oAuthValidationResponse = new OAuthValidationResponse();
            oAuthValidationResponse.setErrorMsg(tokenValidationResponse.getErrorMsg());
            return oAuthValidationResponse;
        }
        return new OAuthValidationResponse(userName,tenantDomain,isValid);
    }
}
