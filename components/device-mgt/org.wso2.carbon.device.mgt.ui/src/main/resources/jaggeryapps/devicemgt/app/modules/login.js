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

var onSuccess;
var onFail;

(function () {
    var log = new Log("/app/modules/login.js");
    var constants = require("/app/modules/constants.js");
    onSuccess = function (context) {
        var utility = require("/app/modules/utility.js").utility;
        var apiWrapperUtil = require("/app/modules/oauth/token-handlers.js")["handlers"];
        try {
            utility.startTenantFlow(context.user);
            var APIManagementProviderService = utility.getAPIManagementProviderService();
            var isLoaded = APIManagementProviderService.isTierLoaded();
            if (!isLoaded && (context.input.samlToken || context.input.backchannelauth)) {
                session.put(constants.SKIP_WELCOME_SCREEN, false);
                if (context.input.samlToken) {
                    session.put(constants.SAML_TOKEN_KEY, context.input.samlToken);
                }
                else if (context.input.backchannelauth) {
                    session.put(constants.BACK_CHANNEL_AUTH, context.input.backchannelauth);
                }
                return;
            }
        } finally {
            utility.endTenantFlow();
        }
        session.put(constants.SKIP_WELCOME_SCREEN, true);
        if (context.input.samlToken || context.input.backchannelauth) {
            //apiWrapperUtil.setupTokenPairBySamlGrantType(context.user.username + '@' + context.user.domain, context.input.samlToken);
			/**
			 * Since the user can be verified using the sso.client.js we can use JWT grant type to issue the token for the user.
			 */
			apiWrapperUtil.setupTokenPairByJWTGrantType(context.user.username + '@' + context.user.domain, context.input.backchannelauth, context.input.samlToken);
        } else {
            apiWrapperUtil.setupTokenPairByPasswordGrantType(context.input.username, context.input.password);
        }
        var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
        var carbonServer = require("carbon").server;
        if (!(context.input.samlToken || context.input.backchannelauth)) {
            (new carbonServer.Server({url: devicemgtProps["adminService"]}))
                .login(context.input.username, context.input.password);
        }
    };

    onFail = function (error) {
        log.error(error.message);
    }
})();
