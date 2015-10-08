/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.authenticator.backend.oauth;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.core.services.authentication.CarbonServerAuthenticator;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuth2TokenValidator;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuthValidationRespond;
import org.wso2.carbon.identity.authenticator.backend.oauth.validator.OAuthValidatorFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

/**
 * This is a custom back end authenticator for enable OAuth token authentication for admin services
 */
public class OauthAuthenticator implements CarbonServerAuthenticator {

    private static final Log log = LogFactory.getLog(OauthAuthenticator.class);
    private static final int PRIORITY = 5;
    private static final int ACCESS_TOKEN_INDEX = 1;

    private static String hostUrl = "";
    private static boolean isRemote = false;

    static {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration.getAuthenticatorConfig(OauthAuthenticatorConstants.AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            isRemote = Boolean.parseBoolean(authenticatorConfig.getParameters().get("isRemote"));
            hostUrl = authenticatorConfig.getParameters().get("hostURL");

        }
    }

    /**
     * Checks whether the authentication of the context can be handled using this authenticator.
     *
     * @param messageContext containing the request need to be authenticated.
     * @return boolean indicating whether the request can be authenticated by this Authenticator.
     */
    public boolean isHandle(MessageContext messageContext) {
        HttpServletRequest httpServletRequest = getHttpRequest(messageContext);
        String headerValue = httpServletRequest.getHeader(HTTPConstants.HEADER_AUTHORIZATION);

        if (headerValue != null && !headerValue.trim().isEmpty()) {
            String[] headerPart = headerValue.trim().split(OauthAuthenticatorConstants.SPLITING_CHARACTOR);

            if (OauthAuthenticatorConstants.AUTHORIZATION_HEADER_PREFIX_BEARER.equals(headerPart[0])) {
                return true;
            }
        } else if (httpServletRequest.getParameter(OauthAuthenticatorConstants.BEARER_TOKEN_IDENTIFIER) != null) {
            return true;
        }
        return false;
    }

    /**
     * Authenticates the user using the provided OAuth token and returns the status as a boolean.
     * Sets the tenant domain and tenant friendly username to the session as attributes.
     *
     * @param messageContext containing the request need to be authenticated.
     * @return boolean indicating the authentication status.
     */
    public boolean isAuthenticated(MessageContext messageContext) {
        HttpServletRequest httpServletRequest = getHttpRequest(messageContext);
        String headerValue = httpServletRequest.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
        //split the header value to separate the identity type and the token.
        String[] headerPart = headerValue.trim().split(OauthAuthenticatorConstants.SPLITING_CHARACTOR);
        String accessToken = headerPart[ACCESS_TOKEN_INDEX];
        OAuth2TokenValidator tokenValidator = OAuthValidatorFactory.getValidator(isRemote,hostUrl);

        if (tokenValidator == null) {
            log.error("OAuthValidationFactory failed to return a validator",
                    new AuthenticatorException("OAuthValidatorFactory Failed to determine the validator"));
            return false;
        }

        OAuthValidationRespond respond = null;
        try {
            respond = tokenValidator.validateToken(accessToken);
        } catch (RemoteException e) {
            log.error("Failed to validate the OAuth token provided.", e);
        }

        if (respond != null && respond.isValid()) {
            HttpSession session;

            if ((session = httpServletRequest.getSession(false)) != null) {
                session.setAttribute(MultitenantConstants.TENANT_DOMAIN, respond.getTenantDomain());
                session.setAttribute(ServerConstants.USER_LOGGED_IN, respond.getUserName());

                if (log.isDebugEnabled()) {
                    log.debug("Authentication successful for " + session.getAttribute(ServerConstants.USER_LOGGED_IN));
                }
            }
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Authentication failed.Illegal attempt from session " + httpServletRequest.getSession().getId());
        }
        return false;
    }

    /**
     * this method is currently not implemented.
     *
     * @param messageContext containing the request need to be authenticated.
     * @return boolean
     */
    public boolean authenticateWithRememberMe(MessageContext messageContext) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return string Authenticator name.
     */
    public String getAuthenticatorName() {
        return OauthAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    /**
     * @return int priority of the authenticator.
     */
    public int getPriority() {
        return PRIORITY;
    }

    /**
     * @return boolean true for enable or otherwise for disable status.
     */
    public boolean isDisabled() {
        return false;
    }

    /**
     * Retrieve HTTP Servlet Request form thr Message Context.
     *
     * @param messageContext Containing the Servlet Request for backend authentication.
     * @return HTTPServletRequest.
     */
    private HttpServletRequest getHttpRequest(MessageContext messageContext) {
        return (HttpServletRequest) messageContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
    }

}
