package org.wso2.carbon.device.mgt.oauth.extensions.handlers.grant;

import org.wso2.carbon.device.mgt.oauth.extensions.OAuthExtUtils;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.grant.jwt.JWTBearerGrantHandler;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

public class ExtendedDeviceMgtJWTBearerGrantHandler extends JWTBearerGrantHandler {

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        return OAuthExtUtils.validateScope(tokReqMsgCtx);
    }
}
