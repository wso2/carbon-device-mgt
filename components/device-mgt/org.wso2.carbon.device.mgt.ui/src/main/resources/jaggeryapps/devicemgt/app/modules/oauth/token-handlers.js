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

/**
 * -----------------------------------------------------
 * Following module includes handlers
 * at Jaggery Layer for handling OAuth tokens.
 * -----------------------------------------------------
 */
var handlers = function () {
    var log = new Log("/app/modules/oauth/token-handlers.js");

    var tokenUtil = require("/app/modules/oauth/token-handler-utils.js")["utils"];
    var constants = require("/app/modules/constants.js");
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];

    var publicMethods = {};
    var privateMethods = {};

    publicMethods.setupTokenPairByPasswordGrantType = function (username, password) {
        if (!username || !password) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                "password grant type. Either username of logged in user, password or both are missing " +
                    "as input - setupTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                    "password grant type. Encoded client credentials are " +
                        "missing - setupTokenPairByPasswordGrantType(x, y)");
            } else {
                var tokenPair;
                // tokenPair will include current access token as well as current refresh token
                var arrayOfScopes = devicemgtProps["scopes"];
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });
                tokenPair = tokenUtil.
                    getTokenPairByPasswordGrantType(username,
                        encodeURIComponent(password), encodedClientAppCredentials, stringOfScopes);
                if (!tokenPair) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up " +
                        "token pair by password grant type. Error in token " +
                            "retrieval - setupTokenPairByPasswordGrantType(x, y)");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["TOKEN_PAIR"], stringify(tokenPair));
                }
            }
        }
    };

    publicMethods.setupTokenPairBySamlGrantType = function (username, samlToken) {
        if (!username || !samlToken) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                "saml grant type. Either username of logged in user, samlToken or both are missing " +
                    "as input - setupTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair " +
                    "by saml grant type. Encoded client credentials are " +
                        "missing - setupTokenPairByPasswordGrantType(x, y)");
            } else {
                var tokenPair;
                // accessTokenPair will include current access token as well as current refresh token
                tokenPair = tokenUtil.
                    getTokenPairBySAMLGrantType(samlToken, encodedClientAppCredentials, "PRODUCTION");
                if (!tokenPair) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up token " +
                        "pair by password grant type. Error in token " +
                            "retrieval - setupTokenPairByPasswordGrantType(x, y)");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["TOKEN_PAIR"], stringify(tokenPair));
                }
            }
        }
    };

    publicMethods.refreshTokenPair = function () {
        var currentTokenPair = parse(session.get(constants["TOKEN_PAIR"]));
        // currentTokenPair includes current access token as well as current refresh token
        var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
        if (!currentTokenPair || !encodedClientAppCredentials) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Error in refreshing tokens. Either the " +
                "token pair, encoded client app credentials or both input are not found under " +
                    "session context - refreshTokenPair()");
        } else {
            var newTokenPair = tokenUtil.
                getNewTokenPairByRefreshToken(currentTokenPair["refreshToken"], encodedClientAppCredentials);
            if (!newTokenPair) {
                log.error("{/app/modules/oauth/token-handlers.js} Error in refreshing token pair. " +
                    "Unable to update session context with new access token pair - refreshTokenPair()");
            } else {
                session.put(constants["TOKEN_PAIR"], stringify(newTokenPair));
            }
        }
    };

    privateMethods.setUpEncodedTenantBasedClientAppCredentials = function (username) {
        if (!username) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                "client credentials to session context. No username of logged in user is found as " +
                    "input - setUpEncodedTenantBasedClientAppCredentials(x)");
        } else {
            var dynamicClientAppCredentials = tokenUtil.getDynamicClientAppCredentials();
            if (!dynamicClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                    "client credentials to session context as the server is unable to obtain " +
                        "dynamic client credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
            } else {
                var jwtToken = tokenUtil.getAccessTokenByJWTGrantType(dynamicClientAppCredentials);
                if (!jwtToken) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                        "client credentials to session context as the server is unable to obtain " +
                            "a jwt token - setUpEncodedTenantBasedClientAppCredentials(x)");
                } else {
                    var tenantBasedClientAppCredentials = tokenUtil.
                        getTenantBasedClientAppCredentials(username, jwtToken);
                    if (!tenantBasedClientAppCredentials) {
                        throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant " +
                            "based client credentials to session context as the server is unable " +
                                "to obtain such credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
                    } else {
                        var encodedTenantBasedClientAppCredentials =
                            tokenUtil.encode(tenantBasedClientAppCredentials["clientId"] + ":" +
                                tenantBasedClientAppCredentials["clientSecret"]);
                        // setting up encoded tenant based client credentials to session context.
                        session.put(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"],
                            encodedTenantBasedClientAppCredentials);
                    }
                }
            }
        }
    };

    return publicMethods;
}();