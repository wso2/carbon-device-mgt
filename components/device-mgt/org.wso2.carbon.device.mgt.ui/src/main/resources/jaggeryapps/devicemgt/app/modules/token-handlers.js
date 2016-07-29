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
    var log = new Log("/app/modules/token-handlers.js");

    var tokenUtil = require("/app/modules/util.js")["util"];
    var constants = require("/app/modules/constants.js");
    var devicemgtProps = require("/app/conf/reader/main.js")["conf"];

    var privateMethods = {};
    var publicMethods = {};

    privateMethods.setUpEncodedTenantBasedClientAppCredentials = function (username) {
        if (!username) {
            throw new Error("{/app/modules/token-handlers.js} Could not set up encoded tenant based " +
                "client credentials to session context. No username is found as " +
                "input - setUpEncodedTenantBasedClientAppCredentials(x)");
        } else {
            var dynamicClientAppCredentials = tokenUtil.getDynamicClientAppCredentials();
            if (!dynamicClientAppCredentials) {
                throw new Error("{/app/modules/token-handlers.js} Could not set up encoded tenant based " +
                    "client credentials to session context as the server is unable to obtain " +
                    "dynamic client credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
            } else {
                var jwtToken = tokenUtil.getAccessTokenByJWTGrantType(dynamicClientAppCredentials);
                if (!jwtToken) {
                    throw new Error("{/app/modules/token-handlers.js} Could not set up encoded tenant based " +
                        "client credentials to session context as the server is unable to obtain " +
                        "a jwt token - setUpEncodedTenantBasedClientAppCredentials(x)");
                } else {
                    var tenantBasedClientCredentials = tokenUtil.
                        getTenantBasedClientAppCredentials(username, jwtToken);
                    if (!tenantBasedClientCredentials) {
                        throw new Error("{/app/modules/token-handlers.js} Could not set up encoded tenant " +
                            "based client credentials to session context as the server is unable " +
                            "to obtain such credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
                    } else {
                        var encodedTenantBasedClientCredentials =
                            tokenUtil.encode(tenantBasedClientCredentials["clientId"] + ":" +
                                tenantBasedClientCredentials["clientSecret"]);
                        // setting up encoded tenant based client credentials to session context.
                        session.put(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"], encodedTenantBasedClientCredentials);
                    }
                }
            }
        }
    };

    publicMethods.setupAccessTokenPairByPasswordGrantType = function (username, password) {
        if (!username || !password) {
            throw new Error("{/app/modules/token-handlers.js} Could not set up access token pair by " +
                "password grant type. Either username, password or both are missing as " +
                "input - setupAccessTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
            if (!encodedClientCredentials) {
                throw new Error("{/app/modules/token-handlers.js} Could not set up access token pair by " +
                    "password grant type. Encoded client credentials are " +
                    "missing - setupAccessTokenPairByPasswordGrantType(x, y)");
            } else {
                var accessTokenPair;
                // accessTokenPair will include current access token as well as current refresh token
                var arrayOfScopes = devicemgtProps["scopes"];
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });
                accessTokenPair = tokenUtil.
                    getAccessTokenByPasswordGrantType(username,
                    encodeURIComponent(password), encodedClientCredentials, stringOfScopes);
                if (!accessTokenPair) {
                    throw new Error("{/app/modules/token-handlers.js} Could not set up access " +
                        "token pair by password grant type. Error in token " +
                        "retrieval - setupAccessTokenPairByPasswordGrantType(x, y)");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(accessTokenPair));
                }
            }
        }
    };

    publicMethods.setupAccessTokenPairBySamlGrantType = function (username, samlToken) {
        if (!username || !samlToken) {
            throw new Error("{/app/modules/token-handlers.js} Could not set up access token pair by " +
                "saml grant type. Either username, samlToken or both are missing as " +
                "input - setupAccessTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
            if (!encodedClientCredentials) {
                throw new Error("{/app/modules/token-handlers.js} Could not set up access token pair " +
                    "by saml grant type. Encoded client credentials are " +
                    "missing - setupAccessTokenPairByPasswordGrantType(x, y)");
            } else {
                var accessTokenPair;
                // accessTokenPair will include current access token as well as current refresh token
                accessTokenPair = tokenUtil.
                    getAccessTokenBySAMLGrantType(samlToken, encodedClientCredentials, "PRODUCTION");
                if (!accessTokenPair) {
                    throw new Error("{/app/modules/token-handlers.js} Could not set up access token " +
                        "pair by password grant type. Error in token " +
                        "retrieval - setupAccessTokenPairByPasswordGrantType(x, y)");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(accessTokenPair));
                }
            }
        }
    };

    publicMethods.refreshAccessToken = function () {
        var accessTokenPair = parse(session.get(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"]));
        // accessTokenPair includes current access token as well as current refresh token
        var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
        if (!accessTokenPair || !encodedClientCredentials) {
            throw new Error("{/app/modules/token-handlers.js} Error in refreshing tokens. Either the access " +
                "token pair, encoded client credentials or both input are not found under " +
                "session context - refreshAccessToken()");
        } else {
            var newTokenPair = tokenUtil.
                getNewAccessTokenByRefreshToken(accessTokenPair["refreshToken"], encodedClientCredentials);
            if (!newTokenPair) {
                log.error("{/app/modules/token-handlers.js} Error in refreshing access token. Unable to update " +
                    "session context with new access token pair - refreshAccessToken()");
            } else {
                session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(newTokenPair));
            }
        }
    };

    return publicMethods;
}();