/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var apiWrapperUtil = function () {
    // var log = new Log("/app/modules/api-wrapper-util.js");

    var tokenUtil = require("/app/modules/util.js")["util"];
    var constants = require("/app/modules/constants.js");
    var devicemgtProps = require("/app/conf/reader/main.js")["conf"];

    var publicMethods = {};

    publicMethods.refreshToken = function () {
        var accessTokenPair = session.get(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"]);
        // accessTokenPair includes current access token as well as current refresh token
        var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
        accessTokenPair = tokenUtil.refreshToken(accessTokenPair, encodedClientCredentials);
        session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], accessTokenPair);
    };

    publicMethods.setupAccessTokenPair = function (type, properties) {
        var dynamicClientCredentials = tokenUtil.getDyanmicCredentials(properties);
        var jwtToken = tokenUtil.getTokenWithJWTGrantType(dynamicClientCredentials);
        var tenantBasedClientCredentials = tokenUtil.getTenantBasedAppCredentials(properties["username"], jwtToken);
        var encodedTenantBasedClientCredentials = tokenUtil.
            encode(tenantBasedClientCredentials["clientId"] + ":" + tenantBasedClientCredentials["clientSecret"]);

        session.put(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"], encodedTenantBasedClientCredentials);

        var accessTokenPair;
        // accessTokenPair will include current access token as well as current refresh token
        if (type == constants["GRANT_TYPE_PASSWORD"]) {
            var arrayOfScopes = devicemgtProps["scopes"];
            var stringOfScopes = "";
            arrayOfScopes.forEach(function (entry) { stringOfScopes += entry + " "; });
            accessTokenPair = tokenUtil.getTokenWithPasswordGrantType(properties["username"],
                encodeURIComponent(properties["password"]), encodedTenantBasedClientCredentials, stringOfScopes);
        } else if (type == constants["GRANT_TYPE_SAML"]) {
            accessTokenPair = tokenUtil.getTokenWithSAMLGrantType(properties["samlToken"],
                encodedTenantBasedClientCredentials, "PRODUCTION");
        }

        session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], accessTokenPair);
    };

    return publicMethods;
}();