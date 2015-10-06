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

package org.wso2.carbon.device.mgt.oauth.extensions.handlers;

import org.wso2.carbon.identity.oauth.callback.AbstractOAuthCallbackHandler;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * This class represents a Custom OAuthCallback Handler implementation. This should be implemented
 * if there's any necessity of custom logic to authorize OAuthCallbacks.
 */
public class DeviceMgtOAuthCallbackHandler  extends AbstractOAuthCallbackHandler {

    @Override
    public boolean canHandle(Callback[] callbacks) throws IdentityOAuth2Exception {
        return true;
    }

    @Override
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        if (callbacks != null && callbacks.length > 0){
            OAuthCallback oauthCallback = (OAuthCallback) callbacks[0];
            if (OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION_AUTHZ.equals(
                    oauthCallback.getCallbackType())){
                oauthCallback.setAuthorized(true);
            } else if (OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION_TOKEN.equals(
                    oauthCallback.getCallbackType())){
                oauthCallback.setAuthorized(true);
            } else if (OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION_AUTHZ.equals(
                    oauthCallback.getCallbackType())){
                oauthCallback.setValidScope(true);
            } else if (OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION_TOKEN.equals(
                    oauthCallback.getCallbackType())){
                String[] scopes = oauthCallback.getRequestedScope();
                oauthCallback.setApprovedScope(scopes);
                oauthCallback.setValidScope(true);
                //Add the necessary logic if we are doing the scope validation upon token issue
            }
        }

    }
}
