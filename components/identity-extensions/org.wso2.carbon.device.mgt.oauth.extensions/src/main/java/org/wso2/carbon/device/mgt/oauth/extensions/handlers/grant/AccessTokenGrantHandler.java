/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant.oauth.validator.LocalOAuthValidator;
import org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant.oauth.validator.OAuthValidationResponse;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractAuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import java.rmi.RemoteException;

/**
 * This allows user to generate a new access token using an existing access token.
 */
@SuppressWarnings("unused")
public class AccessTokenGrantHandler extends AbstractAuthorizationGrantHandler {
    private static Log log = LogFactory.getLog(AccessTokenGrantHandler.class);
    private static final String TENANT_DOMAIN_KEY = "tenantDomain";

    private LocalOAuthValidator tokenValidator;
    public static final String TOKEN_GRANT_PARAM = "admin_access_token";

    public AccessTokenGrantHandler() {
        try {
            tokenValidator = new LocalOAuthValidator();
        } catch (IllegalArgumentException e) {
            log.error("Failed to initialise Authenticator", e);
        }
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        return ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        if (!super.validateGrant(tokReqMsgCtx)) {
            return false;
        } else {
            OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
            String username = null;
            String userTenantDomain = null;
            String clientId = oAuth2AccessTokenReqDTO.getClientId();
            String spTenantDomain = null;
            OAuthValidationResponse response;
            ServiceProvider serviceProvider;
            boolean authStatus = false;

            String accessToken = null;
            RequestParameter[] parameters = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();

            for (RequestParameter parameter : parameters) {
                if (TOKEN_GRANT_PARAM.equals(parameter.getKey())) {
                    if (parameter.getValue() != null && parameter.getValue().length > 0) {
                        accessToken = parameter.getValue()[0];
                    }
                }
            }

            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    response = tokenValidator.validateToken(accessToken);
                } catch (RemoteException e) {
                    log.error("Failed to validate the OAuth token provided.", e);
                    return false;
                }
                if (response != null && response.isValid()) {
                    authStatus = true;
                    username = response.getUserName() + "@" + response.getTenantDomain();
                    userTenantDomain = MultitenantUtils.getTenantDomain(username);
                    spTenantDomain = response.getTenantDomain();
                } else if (response != null && !response.isValid()) {
                    throw new IdentityOAuth2Exception("Authentication failed for the provided access token");
                }
            }

            try {
                serviceProvider = OAuth2ServiceComponentHolder.getApplicationMgtService()
                        .getServiceProviderByClientId(clientId, "oauth2", spTenantDomain);
            } catch (IdentityApplicationManagementException var15) {
                throw new IdentityOAuth2Exception("Error occurred while retrieving OAuth2 application data for client id "
                        + clientId, var15);
            }

            if (!serviceProvider.isSaasApp() && !userTenantDomain.equals(spTenantDomain)) {
                if (log.isDebugEnabled()) {
                    log.debug("Non-SaaS service provider tenant domain is not same as user tenant domain; "
                            + spTenantDomain + " != " + userTenantDomain);
                }

                return false;
            } else {
                String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
                username = tenantAwareUserName + "@" + userTenantDomain;
                if (authStatus) {
                    if (!username.contains("/") && StringUtils.isNotBlank(UserCoreUtil.getDomainFromThreadLocal())) {
                        username = UserCoreUtil.getDomainFromThreadLocal() + "/" + username;
                    }

                    AuthenticatedUser user = OAuth2Util.getUserFromUserName(username);
                    user.setAuthenticatedSubjectIdentifier(user.toString());
                    tokReqMsgCtx.setAuthorizedUser(user);
                    tokReqMsgCtx.setScope(oAuth2AccessTokenReqDTO.getScope());
                    return authStatus;
                } else {
                    throw new IdentityOAuth2Exception("Authentication failed for " + username);
                }
            }
        }
    }
}
