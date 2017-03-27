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

package org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.saml.SAML2BearerGrantHandler;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This sets up user with tenant aware username.
 */
@SuppressWarnings("unused")
public class ExtendedSAML2BearerGrantHandler extends SAML2BearerGrantHandler {
    private static Log log = LogFactory.getLog(ExtendedSAML2BearerGrantHandler.class);

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        return ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        if(!super.validateGrant(tokReqMsgCtx)){
            return false;
        }
        AuthenticatedUser authenticatedUser = tokReqMsgCtx.getAuthorizedUser();
        authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(authenticatedUser.getUserName()));
        return true;
    }

}
