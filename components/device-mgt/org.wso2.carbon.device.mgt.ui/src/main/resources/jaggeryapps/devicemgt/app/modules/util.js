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

var util = function () {
    var log = new Log("/app/modules/util.js");

    var privateMethods = {};
    var publicMethods = {};

    var Base64 = Packages.org.apache.commons.codec.binary.Base64;
    var String = Packages.java.lang.String;

    var deviceMgtProps = require("/app/conf/reader/main.js")["conf"];
    var constants = require("/app/modules/constants.js");
    var carbon = require("carbon");

    publicMethods.encode = function (payload) {
        return new String(Base64.encodeBase64(new String(payload).getBytes()));
    };

    publicMethods.decode = function (payload) {
        return new String(Base64.decodeBase64(new String(payload).getBytes()));
    };

    publicMethods.getDynamicClientAppCredentials = function () {
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

        var dynamicClientCredentials = {};
        if (xhr["status"] == 201 && xhr["responseText"]) {
            var responsePayload = parse(xhr["responseText"]);
            dynamicClientCredentials["clientId"] = responsePayload["client_id"];
            dynamicClientCredentials["clientSecret"] = responsePayload["client_secret"];
        } else if (xhr["status"] == 400) {
            log.error("{/app/modules/util.js - getDynamicClientAppCredentials()} " +
                "Bad request. Invalid data provided as dynamic client application properties.");
            dynamicClientCredentials = null;
        } else {
            log.error("{/app/modules/util.js - getDynamicClientAppCredentials()} " +
                "Error in retrieving dynamic client credentials.");
            dynamicClientCredentials = null;
        }
        // returning dynamic client credentials
        return dynamicClientCredentials;
    };

    publicMethods.getAccessTokenByPasswordGrantType = function (username, password, encodedClientCredentials, scopes) {
        if (!username || !password || !encodedClientCredentials || !scopes) {
            log.error("{/app/modules/util.js} Error in retrieving access token by password " +
                "grant type. No username, password, encoded client credentials or scopes are " +
                "found - getAccessTokenByPasswordGrantType(a, b, c, d)");
            return null;
        } else {
            // calling oauth provider token service endpoint
            var requestURL = deviceMgtProps["oauthProvider"]["tokenServiceURL"];
            var requestPayload = "grant_type=password&username=" +
                username + "&password=" + password + "&scope=" + scopes;

            var xhr = new XMLHttpRequest();
            xhr.open("POST", requestURL, false);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenPair = {};
                tokenPair["accessToken"] = responsePayload["access_token"];
                tokenPair["refreshToken"] = responsePayload["refresh_token"];
                return tokenPair;
            } else {
                log.error("{/app/modules/util.js} Error in retrieving access token by password " +
                    "grant type - getAccessTokenByPasswordGrantType(a, b, c, d)");
                return null;
            }
        }
    };

    publicMethods.getAccessTokenBySAMLGrantType = function (assertion, encodedClientCredentials, scopes) {
        if (!assertion || !encodedClientCredentials || !scopes) {
            log.error("{/app/modules/util.js} Error in retrieving access token by saml " +
                "grant type. No assertion, encoded client credentials or scopes are " +
                "found - getAccessTokenBySAMLGrantType(x, y, z)");
            return null;
        } else {
            var assertionXML = publicMethods.decode(assertion);
            /*
             TODO: make assertion extraction with proper parsing. Since Jaggery XML parser seem
             to add formatting which causes signature verification to fail.
             */
            var assertionStartMarker = "<saml2:Assertion";
            var assertionEndMarker = "<\/saml2:Assertion>";
            var assertionStartIndex = assertionXML.indexOf(assertionStartMarker);
            var assertionEndIndex = assertionXML.indexOf(assertionEndMarker);

            var extractedAssertion;
            if (assertionStartIndex == -1 || assertionEndIndex == -1) {
                log.error("{/app/modules/util.js} Error in retrieving access token by saml grant type. " +
                    "Issue in assertion format - getAccessTokenBySAMLGrantType(x, y, z)");
                return null;
            } else {
                extractedAssertion = assertionXML.
                    substring(assertionStartIndex, assertionEndIndex) + assertionEndMarker;
                var encodedAssertion = publicMethods.encode(extractedAssertion);

                // calling oauth provider token service endpoint
                var requestURL = deviceMgtProps["oauthProvider"]["tokenServiceURL"];
                var requestPayload = "grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer&" +
                    "assertion=" + encodeURIComponent(encodedAssertion) + "&scope=" + scopes;

                var xhr = new XMLHttpRequest();
                xhr.open("POST", requestURL, false);
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                xhr.setRequestHeader("Authorization", "Basic " + encodedClientCredentials);
                xhr.send(requestPayload);

                if (xhr["status"] == 200 && xhr["responseText"]) {
                    var responsePayload = parse(xhr["responseText"]);
                    var tokenPair = {};
                    tokenPair["accessToken"] = responsePayload["access_token"];
                    tokenPair["refreshToken"] = responsePayload["refresh_token"];
                    return tokenPair;
                } else {
                    log.error("{/app/modules/util.js} Error in retrieving access token by password " +
                        "grant type - getAccessTokenBySAMLGrantType(x, y, z)");
                    return null;
                }
            }
        }
    };

    publicMethods.getNewAccessTokenByRefreshToken = function (refreshToken, encodedClientCredentials, scopes) {
        if (!refreshToken || !encodedClientCredentials) {
            log.error("{/app/modules/util.js} Error in retrieving new access token by current " +
                "refresh token. No refresh token or encoded client credentials are " +
                "found - getNewAccessTokenByRefreshToken(x, y, z)");
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
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenPair = {};
                tokenPair["accessToken"] = responsePayload["access_token"];
                tokenPair["refreshToken"] = responsePayload["refresh_token"];
                return tokenPair;
            } else {
                log.error("{/app/modules/util.js} Error in retrieving new access token by " +
                    "current refresh token - getNewAccessTokenByRefreshToken(x, y, z)");
                return null;
            }
        }
    };

    publicMethods.getAccessTokenByJWTGrantType =  function (clientCredentials) {
        if (!clientCredentials) {
            log.error("{/app/modules/util.js} Error in retrieving new access token by current refresh " +
                "token. No client credentials are found as input - getAccessTokenByJWTGrantType(x)");
            return null;
        } else {
            var JWTClientManagerServicePackagePath =
                "org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
            var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
            var jwtClient = JWTClientManagerService.getJWTClient();
            // returning access token by JWT grant type
            return jwtClient.getAccessToken(clientCredentials["clientId"], clientCredentials["clientSecret"],
                deviceMgtProps["oauthProvider"]["appRegistration"]["owner"], null)["accessToken"];
        }
    };

    publicMethods.getTenantBasedClientAppCredentials = function (username, jwtToken) {
        if (!username || !jwtToken) {
            log.error("{/app/modules/util.js} Error in retrieving tenant based client application credentials. " +
                "No username or jwt token is found as input - getTenantBasedClientAppCredentials(x, y)");
            return null;
        } else {
            var tenantDomain = carbon.server.tenantDomain({username: username});
            if (!tenantDomain) {
                log.error("{/app/modules/util.js} Error in retrieving tenant based client application " +
                    "credentials. Unable to obtain a valid tenant domain for provided " +
                    "username - getTenantBasedClientAppCredentials(x, y)");
                return null;
            } else {
                var cachedTenantBasedClientAppCredentials = privateMethods.
                    getCachedTenantBasedClientAppCredentials(tenantDomain);
                if (cachedTenantBasedClientAppCredentials) {
                    return cachedTenantBasedClientAppCredentials;
                } else {
                    // register a tenant based client app at API Manager
                    var applicationName = "webapp_" + tenantDomain;
                    var requestURL = deviceMgtProps["oauthProvider"]["appRegistration"]
                        ["apiManagerClientAppRegistrationServiceURL"] +
                        "?tenantDomain=" + tenantDomain + "&applicationName=" + applicationName;

                    var xhr = new XMLHttpRequest();
                    xhr.open("POST", requestURL, false);
                    xhr.setRequestHeader("Content-Type", "application/json");
                    xhr.setRequestHeader("Authorization", "Bearer " + jwtToken);
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
                        log.error("{/app/modules/util.js} Error in retrieving tenant based client " +
                            "application credentials from API Manager - getTenantBasedClientAppCredentials(x, y)");
                        return null;
                    }
                }
            }
        }
    };

    privateMethods.setCachedTenantBasedClientAppCredentials = function (tenantDomain, clientCredentials) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS"]);
        if (!cachedTenantBasedClientAppCredentialsMap) {
            cachedTenantBasedClientAppCredentialsMap = {};
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientCredentials;
            application.put(constants["CACHED_CREDENTIALS"], cachedTenantBasedClientAppCredentialsMap);
        } else if (!cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientCredentials;
        }
    };

    privateMethods.getCachedTenantBasedClientAppCredentials = function (tenantDomain) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS"]);
        if (!cachedTenantBasedClientAppCredentialsMap ||
            !cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            return null;
        } else {
            return cachedTenantBasedClientAppCredentialsMap[tenantDomain];
        }
    };

    return publicMethods;
}();
