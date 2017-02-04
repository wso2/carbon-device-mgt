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

var utils = function () {
    var log = new Log("/app/modules/oauth/token-handler-utils.js");

    var deviceMgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var constants = require("/app/modules/constants.js");
    var carbon = require("carbon");
	var authModule = require("/lib/modules/auth/auth.js").module;

    //noinspection JSUnresolvedVariable
    var Base64 = Packages.org.apache.commons.codec.binary.Base64;
    //noinspection JSUnresolvedVariable
    var String = Packages.java.lang.String;

    var publicMethods = {};
    var privateMethods = {};

    publicMethods["encode"] = function (payload) {
        //noinspection JSUnresolvedFunction
        return String(Base64.encodeBase64(String(payload).getBytes()));
    };

    publicMethods["decode"] = function (payload) {
        //noinspection JSUnresolvedFunction
        return String(Base64.decodeBase64(String(payload).getBytes()));
    };

    publicMethods["getDynamicClientAppCredentials"] = function () {
        // setting up dynamic client application properties
        var dcAppProperties = {
            "applicationType": deviceMgtProps["oauthProvider"]["appRegistration"]["appType"],
            "clientName": deviceMgtProps["oauthProvider"]["appRegistration"]["clientName"],
            "owner": deviceMgtProps["oauthProvider"]["appRegistration"]["owner"],
            "tokenScope": deviceMgtProps["oauthProvider"]["appRegistration"]["tokenScope"],
            "grantType": deviceMgtProps["oauthProvider"]["appRegistration"]["grantType"],
            "callbackUrl": deviceMgtProps["oauthProvider"]["appRegistration"]["callbackUrl"],
            "saasApp" : true
        };
        // calling dynamic client app registration service endpoint
        var requestURL = deviceMgtProps["oauthProvider"]["appRegistration"]
            ["dynamicClientAppRegistrationServiceURL"];
        var requestPayload = dcAppProperties;

        var xhr = new XMLHttpRequest();
        xhr.open("POST", requestURL, false);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(stringify(requestPayload));

        var dynamicClientAppCredentials = {};
        if (xhr["status"] == 201 && xhr["responseText"]) {
            var responsePayload = parse(xhr["responseText"]);
            dynamicClientAppCredentials["clientId"] = responsePayload["client_id"];
            dynamicClientAppCredentials["clientSecret"] = responsePayload["client_secret"];
        } else if (xhr["status"] == 400) {
            log.error("{/app/modules/oauth/token-handler-utils.js - getDynamicClientAppCredentials()} " +
                "Bad request. Invalid data provided as dynamic client application properties.");
            dynamicClientAppCredentials = null;
        } else {
            log.error("{/app/modules/oauth/token-handler-utils.js - getDynamicClientAppCredentials()} " +
                "Error in retrieving dynamic client credentials.");
            dynamicClientAppCredentials = null;
        }
        // returning dynamic client credentials
        return dynamicClientAppCredentials;
    };

    publicMethods["getTenantBasedClientAppCredentials"] = function (username) {
        if (!username) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                "based client app credentials. No username " +
                    "as input - getTenantBasedClientAppCredentials(x)");
            return null;
        } else {
			//noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var tenantDomain = carbon.server.tenantDomain({username: username});
            if (!tenantDomain) {
                log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                    "based client application credentials. Unable to obtain a valid tenant domain for provided " +
                        "username - getTenantBasedClientAppCredentials(x, y)");
                return null;
            } else {
                var cachedTenantBasedClientAppCredentials = privateMethods.
                    getCachedTenantBasedClientAppCredentials(tenantDomain);
                if (cachedTenantBasedClientAppCredentials) {
                    return cachedTenantBasedClientAppCredentials;
                } else {
					var adminUsername = deviceMgtProps["adminUser"];
					var adminUserTenantId = deviceMgtProps["adminUserTenantId"];
					//claims required for jwtAuthenticator.
					var claims = {"http://wso2.org/claims/enduserTenantId": adminUserTenantId,
						"http://wso2.org/claims/enduser": adminUsername};
					var jwtToken = publicMethods.getJwtToken(adminUsername, claims);

                    // register a tenant based client app at API Manager
                    var applicationName =  deviceMgtProps["oauthProvider"]["appRegistration"]
							["clientName"] + "_" + tenantDomain;
                    var requestURL = deviceMgtProps["oauthProvider"]["appRegistration"]
                        ["apiManagerClientAppRegistrationServiceURL"] +
                            "?tenantDomain=" + tenantDomain + "&applicationName=" + applicationName;

                    var xhr = new XMLHttpRequest();
                    xhr.open("POST", requestURL, false);
                    xhr.setRequestHeader("Content-Type", "application/json");
                    xhr.setRequestHeader("X-JWT-Assertion", "" + jwtToken);
                    xhr.send();

                    if (xhr["status"] == 201 && xhr["responseText"]) {
                        var responsePayload = parse(xhr["responseText"]);
                        var tenantBasedClientAppCredentials = {};
                        tenantBasedClientAppCredentials["clientId"] = responsePayload["client_id"];
                        tenantBasedClientAppCredentials["clientSecret"] = responsePayload["client_secret"];
                        privateMethods.
                            setCachedTenantBasedClientAppCredentials(tenantDomain, tenantBasedClientAppCredentials);
                        return tenantBasedClientAppCredentials;
                    } else {
                        log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                            "based client application credentials from API " +
                                "Manager - getTenantBasedClientAppCredentials(x, y)");
                        return null;
                    }
                }
            }
        }
    };

    publicMethods["getTenantBasedWebSocketClientAppCredentials"] = function (username) {
        if (!username) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                "based client app credentials. No username " +
                "as input - getTenantBasedWebSocketClientAppCredentials(x)");
            return null;
        } else {
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var tenantDomain = carbon.server.tenantDomain({username: username});
            if (!tenantDomain) {
                log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                    "based client application credentials. Unable to obtain a valid tenant domain for provided " +
                    "username - getTenantBasedWebSocketClientAppCredentials(x, y)");
                return null;
            } else {
                var cachedBasedWebsocketClientAppCredentials = privateMethods.
                getCachedBasedWebSocketClientAppCredentials(tenantDomain);
                if (cachedBasedWebsocketClientAppCredentials) {
                    return cachedBasedWebsocketClientAppCredentials;
                } else {
                    var adminUsername = deviceMgtProps["adminUser"];
                    var adminUserTenantId = deviceMgtProps["adminUserTenantId"];
                    //claims required for jwtAuthenticator.
                    var claims = {"http://wso2.org/claims/enduserTenantId": adminUserTenantId,
                        "http://wso2.org/claims/enduser": adminUsername};
                    var jwtToken = publicMethods.getJwtToken(adminUsername, claims);

                    // register a tenant based  app at API Manager
                    var applicationName = "websocket_webapp_" + tenantDomain;
                    var requestURL = (deviceMgtProps["oauthProvider"]["appRegistration"]
                        ["apiManagerClientAppRegistrationServiceURL"]).replace("/tenants","");
                    var xhr = new XMLHttpRequest();
                    xhr.open("POST", requestURL, false);
                    xhr.setRequestHeader("Content-Type", "application/json");
                    xhr.setRequestHeader("X-JWT-Assertion", "" + jwtToken);
                    xhr.send(stringify({applicationName:applicationName, tags:["device_management"],
                        isAllowedToAllDomains:false, isMappingAnExistingOAuthApp:false, validityPeriod: 3600}));
                    if (xhr["status"] == 201 && xhr["responseText"]) {
                        var responsePayload = parse(xhr["responseText"]);
                        var tenantTenantBasedWebsocketClientAppCredentials = {};
                        tenantTenantBasedWebsocketClientAppCredentials["clientId"] = responsePayload["client_id"];
                        tenantTenantBasedWebsocketClientAppCredentials["clientSecret"] =
                            responsePayload["client_secret"];
                        privateMethods.setCachedBasedWebSocketClientAppCredentials(tenantDomain,
                            tenantTenantBasedWebsocketClientAppCredentials);
                        return tenantTenantBasedWebsocketClientAppCredentials;
                    } else {
                        log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                            "based client application credentials from API " +
                            "Manager - getTenantBasedWebSocketClientAppCredentials(x, y)");
                        return null;
                    }
                }
            }
        }
    };

    privateMethods["setCachedTenantBasedClientAppCredentials"] = function (tenantDomain, clientAppCredentials) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS"]);
        if (!cachedTenantBasedClientAppCredentialsMap) {
            cachedTenantBasedClientAppCredentialsMap = {};
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
            application.put(constants["CACHED_CREDENTIALS"], cachedTenantBasedClientAppCredentialsMap);
        } else if (!cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
        }
    };

    privateMethods["getCachedTenantBasedClientAppCredentials"] = function (tenantDomain) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS"]);
        if (!cachedTenantBasedClientAppCredentialsMap ||
            !cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            return null;
        } else {
            return cachedTenantBasedClientAppCredentialsMap[tenantDomain];
        }
    };

    privateMethods["getCachedBasedWebSocketClientAppCredentials"] = function (tenantDomain) {
        var cachedBasedWebSocketClientAppCredentialsMap
            = application.get(constants["CACHED_CREDENTIALS_FOR_WEBSOCKET_APP"]);
        if (!cachedBasedWebSocketClientAppCredentialsMap ||
            !cachedBasedWebSocketClientAppCredentialsMap[tenantDomain]) {
            return null;
        } else {
            return cachedBasedWebSocketClientAppCredentialsMap[tenantDomain];
        }
    };

    privateMethods["setCachedBasedWebSocketClientAppCredentials"] = function (tenantDomain, clientAppCredentials) {
        var cachedBasedWebSocketClientAppCredentialsMap
            = application.get(constants["CACHED_CREDENTIALS_FOR_WEBSOCKET_APP"]);
        if (!cachedBasedWebSocketClientAppCredentialsMap) {
            cachedBasedWebSocketClientAppCredentialsMap = {};
            cachedBasedWebSocketClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
            application.put(constants["CACHED_CREDENTIALS_FOR_WEBSOCKET_APP"]
                , cachedBasedWebSocketClientAppCredentialsMap);
        } else if (!cachedBasedWebSocketClientAppCredentialsMap[tenantDomain]) {
            cachedBasedWebSocketClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
        }
    };

    publicMethods["getTokenPairAndScopesByPasswordGrantType"] = function (username, password
        , encodedClientAppCredentials, scopes) {
        if (!username || !password || !encodedClientAppCredentials || !scopes) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving access token by password " +
                "grant type. No username, password, encoded client app credentials or scopes are " +
                    "found - getTokenPairAndScopesByPasswordGrantType(a, b, c, d)");
            return null;
        } else {
            // calling oauth provider token service endpoint
            var requestURL = deviceMgtProps["oauthProvider"]["tokenServiceURL"];
            var requestPayload = "grant_type=password&username=" +
                username + "&password=" + password + "&scope=" + scopes;

            var xhr = new XMLHttpRequest();
            xhr.open("POST", requestURL, false);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientAppCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenData = {};
                tokenData["accessToken"] = responsePayload["access_token"];
                tokenData["refreshToken"] = responsePayload["refresh_token"];
                tokenData["scopes"] = responsePayload["scope"];
                return tokenData;
            } else {
                log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving access token " +
                    "by password grant type - getTokenPairAndScopesByPasswordGrantType(a, b, c, d)");
                return null;
            }
        }
    };

	publicMethods["getTokenPairAndScopesByJWTGrantType"] = function (assertion, encodedClientAppCredentials, scopes) {
		if (!assertion || !encodedClientAppCredentials || !scopes) {
			log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving access token by jwt " +
			"grant type. No assertion, encoded client app credentials or scopes are " +
			"found - getTokenPairAndScopesByJWTGrantType(x, y, z)");
			return null;
		} else {
			var ssoLoginUser = authModule.ssoLogin(assertion);
			if (!ssoLoginUser.user.username) {
				return null;
			}
			var endUsername = ssoLoginUser.user.username + "@" + ssoLoginUser.user.domain;
			var JWTClientManagerServicePackagePath =
				"org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
			//noinspection JSUnresolvedFunction, JSUnresolvedVariable
			var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
			//noinspection JSUnresolvedFunction
			var jwtClient = JWTClientManagerService.getJWTClient();
			// returning access token by JWT grant type
			var tokenInfo = jwtClient.getAccessToken(encodedClientAppCredentials,
				endUsername, scopes);
			var tokenData = {};
			tokenData["accessToken"] = tokenInfo.getAccessToken();
			tokenData["refreshToken"] = tokenInfo.getRefreshToken();
			tokenData["scopes"] = tokenInfo.getScopes();
			return tokenData;
		}
	};

    publicMethods["getNewTokenPairByRefreshToken"] = function (refreshToken, encodedClientAppCredentials, scopes) {
        if (!refreshToken || !encodedClientAppCredentials) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving new access token " +
                "by current refresh token. No refresh token or encoded client app credentials are " +
                    "found - getNewTokenPairByRefreshToken(x, y, z)");
            return null;
        } else {
            var requestURL = deviceMgtProps["oauthProvider"]["tokenServiceURL"];
            var requestPayload = "grant_type=refresh_token&refresh_token=" + refreshToken;
            if (scopes) {
                requestPayload = requestPayload + "&scope=" + scopes;
            }

            var xhr = new XMLHttpRequest();
            xhr.open("POST", requestURL, false);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientAppCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenPair = {};
                tokenPair["accessToken"] = responsePayload["access_token"];
                tokenPair["refreshToken"] = responsePayload["refresh_token"];
                return tokenPair;
            } else {
                log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving new access token by " +
                    "current refresh token - getNewTokenPairByRefreshToken(x, y, z)");
                return null;
            }
        }
    };

    publicMethods["getAccessTokenByJWTGrantType"] =  function (clientAppCredentials) {
        if (!clientAppCredentials) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving new access token " +
                "by current refresh token. No client app credentials are found " +
                    "as input - getAccessTokenByJWTGrantType(x)");
            return null;
        } else {
            var JWTClientManagerServicePackagePath =
                "org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
            //noinspection JSUnresolvedFunction
            var jwtClient = JWTClientManagerService.getJWTClient();
            // returning access token by JWT grant type
            return jwtClient.getAccessToken(clientAppCredentials["clientId"], clientAppCredentials["clientSecret"],
                deviceMgtProps["oauthProvider"]["appRegistration"]["owner"], null)["accessToken"];
        }
    };

    publicMethods["getJwtToken"] =  function (username, claims) {
        if (!username) {
            log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving new jwt token");
            return null;
        } else {
            var JWTClientManagerServicePackagePath =
                "org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
            //noinspection JSUnresolvedFunction
            var jwtClient = JWTClientManagerService.getJWTClient();
            // returning access token by JWT grant type
			if (claims) {
				return jwtClient.getJwtToken(username, claims);
			} else {
				return jwtClient.getJwtToken(username);
			}

        }
    };

    return publicMethods;
}();
