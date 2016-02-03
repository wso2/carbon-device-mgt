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

package org.wso2.carbon.devicemgt.grant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.handlers.ScopesIssuer;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AbstractAuthorizationGrantHandler;

/**
 * The grant type responsible for issuing access tokens for IOT devices
 * device_id and username should be passed in as parameters.
 */
public class DeviceGrant extends AbstractAuthorizationGrantHandler {

    private static Log log = LogFactory.getLog(AbstractAuthorizationGrantHandler.class);

    /**
     * The tokReqMsgCtx should contain username and device_id
     * the username field in the IDN_OAUTH2_ACCESS_TOKEN field
     * will be updated with a concatenated string consisting of username and device_id
     * token will be issued as usual
     *
     * @param tokReqMsgCtx
     * @return
     * @throws IdentityOAuth2Exception
     */
    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        RequestParameter[] parameters = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();
        boolean result = super.validateGrant(tokReqMsgCtx);

        int tenantId = tokReqMsgCtx.getTenantID();
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        String username = null;
        String deviceId = null;
        String deviceType = null;
        String scopeValues = null;

        for (RequestParameter parameter : parameters) {
            if (OauthGrantConstants.DEVICE_ID.equals(parameter.getKey())) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    if (parameter.getValue()[0].equals("0")) {
                        deviceId = null;
                    } else {
                        deviceId = parameter.getValue()[0];
                    }
                }
            } else if (OauthGrantConstants.USER_NAME.equals(parameter.getKey())) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    if (parameter.getValue()[0].equals("0")) {
                        username = null;
                    } else {
                        username = parameter.getValue()[0];
                    }

                }
            } else if (OauthGrantConstants.SCOPE.equals(parameter.getKey())) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    if (parameter.getValue()[0].equals("0")) {
                        scopeValues = null;
                    } else {
                        scopeValues = parameter.getValue()[0];
                    }

                }
            } else if (OauthGrantConstants.DEVICE_TYPE.equals(parameter.getKey())) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    if (parameter.getValue()[0].equals("0")) {
                        deviceType = null;
                    } else {
                        deviceType = parameter.getValue()[0];
                    }

                }
            }

        }

        if (deviceId == null || deviceType == null || username == null) {
            return false;
        }

        AuthenticatedUser user = new AuthenticatedUser();
        user.setTenantDomain(tenantDomain);
        user.setUserName(username.concat(":").concat(deviceId).concat(deviceType));
        //user.setUserStoreDomain("PRIMARY");
        tokReqMsgCtx.setAuthorizedUser(user);
        tokReqMsgCtx.setTenantID(-1234);

        if (scopeValues != null) {
            String[] scopes = scopeValues.split(" ");
            tokReqMsgCtx.setScope(scopes);
        }

        return true;
    }

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext var1)
            throws IdentityOAuth2Exception {
        return true;
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        return ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);
    }


}
