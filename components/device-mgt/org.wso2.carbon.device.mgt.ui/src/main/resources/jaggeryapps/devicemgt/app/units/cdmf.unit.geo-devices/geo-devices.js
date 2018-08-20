/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onRequest(context) {

    var log = new Log("geo-devices.js");
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var viewModel = {};
    var carbonServer = require("carbon").server;
    var device = context.unit.params.device;
    var constants = require("/app/modules/constants.js");
    var wsEndpoint = null;
    var jwtService = carbonServer.osgiService(
        'org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService');
    var jwtClient = jwtService.getJWTClient();
    var encodedClientKeys = session.get(constants["ENCODED_TENANT_BASED_WEB_SOCKET_CLIENT_CREDENTIALS"]);
    var tokenPair = null;
    var token = "";
    if (encodedClientKeys) {
        var tokenUtil = require("/app/modules/oauth/token-handler-utils.js")["utils"];
        var resp = tokenUtil.decode(encodedClientKeys).split(":");
        if (context.user.domain == "carbon.super") {
            tokenPair = jwtClient.getAccessToken(resp[0], resp[1], context.user.username,"default", {});
            if (tokenPair) {
                token = tokenPair.accessToken;
                wsEndpoint = devicemgtProps["wssURL"].replace("https", "wss") + "/secured-websocket-proxy/secured-websocket/";
            }
        } else {
            tokenPair = jwtClient.getAccessToken(resp[0], resp[1], context.user.username + "@" + context.user.domain,"default", {});
            if (tokenPair) {
                token = tokenPair.accessToken;
                wsEndpoint = devicemgtProps["wssURL"].replace("https", "wss") + "/secured-websocket-proxy/secured-websocket/t/"+context.user.domain+"/";
            }

        }

    }
    viewModel.device = device;
    viewModel.wsToken = token;
    viewModel.wsEndpoint = wsEndpoint;
    viewModel.geoServicesEnabled = devicemgtProps.serverConfig.geoLocationConfiguration.enabled;
    return viewModel;
}