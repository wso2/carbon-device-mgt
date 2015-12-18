/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.*;
import org.wso2.carbon.webapp.authenticator.framework.Utils.Utils;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuth2TokenValidator;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthTokenValidationException;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidationResponse;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.oauth.OAuthValidatorFactory;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthAuthenticator implements WebappAuthenticator {

    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static final String REGEX_BEARER_PATTERN = "[B|b]earer\\s";
    private static final Pattern PATTERN = Pattern.compile(REGEX_BEARER_PATTERN);
    private static final String BEARER_TOKEN_TYPE = "bearer";
    private static final String RESOURCE_KEY = "resource";


    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);

    @Override
    public boolean canHandle(Request request) {
        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders().getValue(Constants.HTTPHeaders.HEADER_HTTP_AUTHORIZATION);
        String tokenValue;
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            tokenValue = authBC.toString();
            Matcher matcher = PATTERN.matcher(tokenValue);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AuthenticationInfo authenticate(Request request, Response response) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        if (requestUri == null || "".equals(requestUri)) {
            authenticationInfo.setStatus(Status.CONTINUE);
            return authenticationInfo;
        }

        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/");
        String context = tokenizer.nextToken();
        if (context == null || "".equals(context)) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }
        String apiVersion = tokenizer.nextToken();
        //String authLevel = authenticator.getResourceAuthenticationScheme(context, apiVersion, requestUri, requestMethod);
        String authLevel = "any";
        try {
            if (Constants.NO_MATCHING_AUTH_SCHEME.equals(authLevel)) {
                AuthenticationFrameworkUtil.handleNoMatchAuthScheme(request, response, requestMethod, apiVersion,
                                                                    context);
                authenticationInfo.setStatus(Status.CONTINUE);
            } else {
                String bearerToken = this.getBearerToken(request);
                //Set the resource context param. This will be used in scope validation.
                String resource = requestUri + ":" + requestMethod;
                //Get the appropriate OAuth validator from OAuthValidatorFactory.
                OAuth2TokenValidator oAuth2TokenValidator = OAuthValidatorFactory.getValidator();
                OAuthValidationResponse oAuthValidationResponse = oAuth2TokenValidator.validateToken(bearerToken, resource);

                if (oAuthValidationResponse.isValid()) {
                    String username = oAuthValidationResponse.getUserName();
                    String tenantDomain = oAuthValidationResponse.getTenantDomain();
                    //Remove the userstore domain from username
                    /*if (username.contains("/")) {
                        username = username.substring(username.indexOf('/') + 1);
                    }*/
                    authenticationInfo.setUsername(username);
                    authenticationInfo.setTenantDomain(tenantDomain);
                    authenticationInfo.setTenantId(Utils.getTenantIdOFUser(username + "@" + tenantDomain));
                    if (oAuthValidationResponse.isValid()) {
                        authenticationInfo.setStatus(Status.CONTINUE);
                    }
                } else {
                    authenticationInfo.setMessage(oAuthValidationResponse.getErrorMsg());
                }
            }
        } catch (AuthenticationException e) {
            log.error("Failed to authenticate the incoming request", e);
        } catch (OAuthTokenValidationException e) {
            log.error("Failed to authenticate the incoming request due to oauth token validation error.", e);
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return OAuthAuthenticator.OAUTH_AUTHENTICATOR;
    }

    private String getBearerToken(Request request) {
        MessageBytes authorization =
                request.getCoyoteRequest().getMimeHeaders().
                        getValue(Constants.HTTPHeaders.HEADER_HTTP_AUTHORIZATION);
        String tokenValue = null;
        if (authorization != null) {
            authorization.toBytes();
            ByteChunk authBC = authorization.getByteChunk();
            tokenValue = authBC.toString();
            Matcher matcher = PATTERN.matcher(tokenValue);
            if (matcher.find()) {
                tokenValue = tokenValue.substring(matcher.end());
            }
        }
        return tokenValue;
    }

}
