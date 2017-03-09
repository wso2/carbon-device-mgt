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
    var utility = require("/app/modules/utility.js")["utility"];

    var publicMethods = {};
    var privateMethods = {};

    publicMethods["setupTokenPairByPasswordGrantType"] = function (username, password) {
        if (!username || !password) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                "password grant type. Either username of logged in user, password or both are missing " +
                    "as input - setupTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            privateMethods.setUpEncodedTenantBasedWebSocketClientAppCredentials(username);
            var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                    "password grant type. Encoded client credentials are " +
                        "missing - setupTokenPairByPasswordGrantType(x, y)");
            } else {
                var tokenData;
                // tokenPair will include current access token as well as current refresh token
                var arrayOfScopes = devicemgtProps["scopes"];
                arrayOfScopes = arrayOfScopes.concat(utility.getDeviceTypesScopesList());
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });
                tokenData = tokenUtil.
                    getTokenPairAndScopesByPasswordGrantType(username,
                        encodeURIComponent(password), encodedClientAppCredentials, stringOfScopes);
                if (!tokenData) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up " +
                        "token pair by password grant type. Error in token " +
                            "retrieval - setupTokenPairByPasswordGrantType(x, y)");
                } else {
                    var tokenPair = {};
                    tokenPair["accessToken"] = tokenData["accessToken"];
                    tokenPair["refreshToken"] = tokenData["refreshToken"];
                    // setting up token pair into session context as a string
                    session.put(constants["TOKEN_PAIR"], stringify(tokenPair));

                    var scopes = tokenData.scopes.split(" ");
                    // adding allowed scopes to the session
                    session.put(constants["ALLOWED_SCOPES"], scopes);
                }
            }
        }
    };

    publicMethods["setupTokenPairBySamlGrantType"] = function (username, samlToken) {
        if (!username || !samlToken) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                            "saml grant type. Either username of logged in user, samlToken or both are missing " +
                            "as input - setupTokenPairBySamlGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            privateMethods.setUpEncodedTenantBasedWebSocketClientAppCredentials(username);
            var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair " +
                                "by saml grant type. Encoded client credentials are " +
                                "missing - setupTokenPairBySamlGrantType(x, y)");
            } else {
                var tokenData;
                var arrayOfScopes = devicemgtProps["scopes"];
                arrayOfScopes = arrayOfScopes.concat(utility.getDeviceTypesScopesList());
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });

                // accessTokenPair will include current access token as well as current refresh token
                tokenData = tokenUtil.
                getTokenPairAndScopesBySAMLGrantType(samlToken, encodedClientAppCredentials, stringOfScopes);
                if (!tokenData) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up token " +
                                    "pair by password grant type. Error in token " +
                                    "retrieval - setupTokenPairBySamlGrantType(x, y)");
                } else {
                    var tokenPair = {};
                    tokenPair["accessToken"] = tokenData["accessToken"];
                    tokenPair["refreshToken"] = tokenData["refreshToken"];
                    // setting up access token pair into session context as a string
                    session.put(constants["TOKEN_PAIR"], stringify(tokenPair));

                    var scopes = tokenData.scopes.split(" ");
                    // adding allowed scopes to the session
                    session.put(constants["ALLOWED_SCOPES"], scopes);
                }
            }
        }
    };

	publicMethods["setupTokenPairByJWTGrantType"] = function (username, samlToken) {
		//samlToken is used to validate then if the user is a valid user then token is issued with JWT Grant Type.
		if (!username || !samlToken) {
			throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair by " +
			"saml grant type. Either username of logged in user, samlToken or both are missing " +
			"as input - setupTokenPairBySamlGrantType(x, y)");
		} else {
			privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
			//privateMethods.setUpEncodedTenantBasedWebSocketClientAppCredentials(username);
			var encodedClientAppCredentials = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"]);
			if (!encodedClientAppCredentials) {
				throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair " +
				"by saml grant type. Encoded client credentials are " +
				"missing - setupTokenPairBySamlGrantType(x, y)");
			} else {
				var tokenData;
				var arrayOfScopes = devicemgtProps["scopes"];
				arrayOfScopes = arrayOfScopes.concat(utility.getDeviceTypesScopesList());
				var stringOfScopes = "";
				arrayOfScopes.forEach(function (entry) {
					stringOfScopes += entry + " ";
				});

				// accessTokenPair will include current access token as well as current refresh token
				tokenData = tokenUtil.
					getTokenPairAndScopesByJWTGrantType(samlToken, encodedClientAppCredentials, stringOfScopes);
				if (!tokenData) {
					throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up token " +
					"pair by password grant type. Error in token " +
					"retrieval - setupTokenPairBySamlGrantType(x, y)");
				} else {
					var tokenPair = {};
					tokenPair["accessToken"] = tokenData["accessToken"];
					tokenPair["refreshToken"] = tokenData["refreshToken"];
					// setting up access token pair into session context as a string
					session.put(constants["TOKEN_PAIR"], stringify(tokenPair));

					var scopes = tokenData.scopes.split(" ");
					// adding allowed scopes to the session
					session.put(constants["ALLOWED_SCOPES"], scopes);
				}
			}
		}
	};

    publicMethods["refreshTokenPair"] = function () {
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

    privateMethods["setUpEncodedTenantBasedClientAppCredentials"] = function (username) {
        if (!username) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                "client credentials to session context. No username of logged in user is found as " +
                    "input - setUpEncodedTenantBasedClientAppCredentials(x)");
        } else {
            if (devicemgtProps["gatewayEnabled"]) {
				var tenantBasedClientAppCredentials = tokenUtil.getTenantBasedClientAppCredentials(username);
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
            } else {
                var dynamicClientAppCredentials = tokenUtil.getDynamicClientAppCredentials();
                if (!dynamicClientAppCredentials) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                    "client credentials to session context as the server is unable to obtain " +
                    "dynamic client credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
                }
                var encodedTenantBasedClientAppCredentials =
                    tokenUtil.encode(dynamicClientAppCredentials["clientId"] + ":" +
                    dynamicClientAppCredentials["clientSecret"]);
                // setting up encoded tenant based client credentials to session context.
                session.put(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS"],
                    encodedTenantBasedClientAppCredentials);
            }

        }
    };

    privateMethods["setUpEncodedTenantBasedWebSocketClientAppCredentials"] = function (username) {
        if (!username) {
            throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                "client credentials to session context. No username of logged in user is found as " +
                "input - setUpEncodedTenantBasedWebSocketClientAppCredentials(x)");
        } else {
            if (devicemgtProps["gatewayEnabled"]) {
                var tenantBasedWebSocketClientAppCredentials
                    = tokenUtil.getTenantBasedWebSocketClientAppCredentials(username);
                if (!tenantBasedWebSocketClientAppCredentials) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant " +
                        "based client credentials to session context as the server is unable " +
                        "to obtain such credentials - setUpEncodedTenantBasedWebSocketClientAppCredentials(x)");
                } else {
                    var encodedTenantBasedWebSocketClientAppCredentials =
                        tokenUtil.encode(tenantBasedWebSocketClientAppCredentials["clientId"] + ":" +
                            tenantBasedWebSocketClientAppCredentials["clientSecret"]);
                    // setting up encoded tenant based client credentials to session context.
                    session.put(constants["ENCODED_TENANT_BASED_WEB_SOCKET_CLIENT_CREDENTIALS"],
                        encodedTenantBasedWebSocketClientAppCredentials);
                }
            } else {
                var dynamicClientAppCredentials = tokenUtil.getDynamicClientAppCredentials();
                if (!dynamicClientAppCredentials) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                        "client credentials to session context as the server is unable to obtain " +
                        "dynamic client credentials - setUpEncodedTenantBasedWebSocketClientAppCredentials(x)");
                }
                var encodedTenantBasedWebSocketClientAppCredentials =
                    tokenUtil.encode(dynamicClientAppCredentials["clientId"] + ":" +
                        dynamicClientAppCredentials["clientSecret"]);
                // setting up encoded tenant based client credentials to session context.
                session.put(constants["ENCODED_TENANT_BASED_WEB_SOCKET_CLIENT_CREDENTIALS"],
                    encodedTenantBasedWebSocketClientAppCredentials);
            }

        }
    };

    return publicMethods;
}();
