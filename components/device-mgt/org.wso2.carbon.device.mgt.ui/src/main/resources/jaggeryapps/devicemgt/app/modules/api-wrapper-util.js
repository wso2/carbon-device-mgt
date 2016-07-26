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
    var log = new Log("/app/modules/api-wrapper-util.js");

    var tokenUtil = require("/app/modules/util.js")["util"];
    var constants = require("/app/modules/constants.js");
    var devicemgtProps = require("/app/conf/reader/main.js")["conf"];

    var privateMethods = {};
    var publicMethods = {};

    privateMethods.setUpEncodedTenantBasedClientCredentials = function (username) {
        if (!username) {
            log.error("Could not set up encoded tenant based client credentials " +
                "to session context. No username is found as input.");
        } else {
            var dynamicClientCredentials = tokenUtil.getDynamicClientCredentials();
            if (!dynamicClientCredentials) {
                log.error("Could not set up encoded tenant based client credentials " +
                    "to session context as the server is unable to obtain dynamic client credentials.");
            } else {
                var jwtToken = tokenUtil.getTokenWithJWTGrantType(dynamicClientCredentials);
                if (!jwtToken) {
                    log.error("Could not set up encoded tenant based client credentials " +
                        "to session context as the server is unable to obtain a jwt token.");
                } else {
                    var tenantBasedClientCredentials = tokenUtil.getTenantBasedAppCredentials(username, jwtToken);
                    if (!tenantBasedClientCredentials) {
                        log.error("Could not set up encoded tenant based client credentials " +
                            "to session context as the server is unable to obtain such credentials.");
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

    publicMethods.refreshToken = function () {
        var accessTokenPair = parse(session.get(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"]));
        // accessTokenPair includes current access token as well as current refresh token
        var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
        if (!accessTokenPair || !encodedClientCredentials) {
            log.error("Error in refreshing tokens. Either the access token pair, " +
                "encoded client credentials or both input are not found under session context.");
        } else {
            var newAccessTokenPair = tokenUtil.refreshToken(accessTokenPair, encodedClientCredentials);
            if (!newAccessTokenPair) {
                log.error("Error in refreshing tokens. Unable to update " +
                    "session context with new access token pair.");
            } else {
                session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(newAccessTokenPair));
            }
        }
    };

    publicMethods.setupAccessTokenPairByPasswordGrantType = function (username, password) {
        if (!username || !password) {
            log.error("Could not set up access token pair by password grant type. " +
                "Either username, password or both are missing as input.");
        } else {
            privateMethods.setUpEncodedTenantBasedClientCredentials(username);
            var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
            if (!encodedClientCredentials) {
                log.error("Could not set up access token pair by password grant type. " +
                    "Encoded client credentials are missing.");
            } else {
                var accessTokenPair;
                // accessTokenPair will include current access token as well as current refresh token
                var arrayOfScopes = devicemgtProps["scopes"];
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });
                accessTokenPair = tokenUtil.
                    getTokenWithPasswordGrantType(username,
                    encodeURIComponent(password), encodedClientCredentials, stringOfScopes);
                if (!accessTokenPair) {
                    log.error("Could not set up access token pair by password grant type. Error in token retrieval.");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(accessTokenPair));
                }
            }
        }
    };

    publicMethods.setupAccessTokenPairBySamlGrantType = function (username, samlToken) {
        if (!username || !samlToken) {
            log.error("Could not set up access token pair by saml grant type. " +
                "Either username, samlToken or both are missing as input.");
        } else {
            privateMethods.setUpEncodedTenantBasedClientCredentials(username);
            var encodedClientCredentials = session.get(constants["ENCODED_CLIENT_KEYS_IDENTIFIER"]);
            if (!encodedClientCredentials) {
                log.error("Could not set up access token pair by saml grant type. " +
                    "Encoded client credentials are missing.");
            } else {
                var accessTokenPair;
                // accessTokenPair will include current access token as well as current refresh token
                accessTokenPair = tokenUtil.
                    getTokenWithSAMLGrantType(samlToken, encodedClientCredentials, "PRODUCTION");
                if (!accessTokenPair) {
                    log.error("Could not set up access token pair by password grant type. Error in token retrieval.");
                } else {
                    // setting up access token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER"], stringify(accessTokenPair));
                }
            }
        }
    };

    return publicMethods;
}();