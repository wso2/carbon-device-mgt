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
 * ----------------------------------------------------------------------------
 * Following module includes invokers
 * at Jaggery Layer for calling Backend Services, protected by OAuth Tokens.
 * These Services include both REST and SOAP Services.
 * ----------------------------------------------------------------------------
 */
var invokers = function () {
    var log = new Log("/app/modules/oauth/token-protected-service-invokers.js");

    var publicXMLHTTPInvokers = {};
    var publicHTTPClientInvokers = {};

    var privateMethods = {};
    var publicWSInvokers = {};

    var TOKEN_EXPIRED = "Access token expired";
    var TOKEN_INVALID = "Invalid input. Access token validation failed";

    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var constants = require("/app/modules/constants.js");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var tokenUtil = require("/app/modules/oauth/token-handlers.js")["handlers"];

    /**
     * This method reads the token pair from the session and return the access token.
     * If the token pair is not set in the session, this will return null.
     */
    privateMethods.getAccessToken = function () {
        if (session) {
            var tokenPair = session.get(constants["TOKEN_PAIR"]);
            if (tokenPair) {
                return parse(tokenPair)["accessToken"];
            }
        }
        return null;
    };

    /**
     * ---------------------------------------------------------------------------
     * Start of XML-HTTP-REQUEST based Interceptor implementations
     * ---------------------------------------------------------------------------
     */

    /**
     * This method add Oauth authentication header to outgoing XML-HTTP Requests if Oauth authentication is enabled.
     * @param httpMethod HTTP request type.
     * @param requestPayload payload/data if exists which is needed to be send.
     * @param endpoint Backend REST API url.
     * @param responseCallback a function to be called with response retrieved.
     * @param count a counter which hold the number of recursive execution
     * @param headers a list of name value pairs for additional http headers
     */
    privateMethods["execute"] = function (httpMethod, requestPayload, endpoint, responseCallback, count, headers) {
        var xmlHttpRequest = new XMLHttpRequest();

        xmlHttpRequest.open(httpMethod, endpoint);

        var contentTypeFound = false;
        var acceptTypeFound = false;
        for (var i in headers) {
            xmlHttpRequest.setRequestHeader(headers[i].name, headers[i].value);
            if(constants["CONTENT_TYPE_IDENTIFIER"] == headers[i].name){
                contentTypeFound = true;
            }
            if(constants["ACCEPT_IDENTIFIER"] == headers[i].name){
                acceptTypeFound = true;
            }
        }

        if (!contentTypeFound) {
            xmlHttpRequest.setRequestHeader(constants["CONTENT_TYPE_IDENTIFIER"], constants["APPLICATION_JSON"]);
        }

        if (!acceptTypeFound) {
            xmlHttpRequest.setRequestHeader(constants["ACCEPT_IDENTIFIER"], constants["APPLICATION_JSON"]);
        }

        if (devicemgtProps["isOAuthEnabled"]) {
            var accessToken = privateMethods.getAccessToken();
            if (accessToken == null) {
                userModule.logout(function () {
                    response.sendRedirect(devicemgtProps["appContext"] + "login");
                });
            } else {
                xmlHttpRequest.setRequestHeader(constants["AUTHORIZATION_HEADER"],
                                                constants["BEARER_PREFIX"] + accessToken);
            }
        }

        if (requestPayload) {
            xmlHttpRequest.send(requestPayload);
        } else {
            xmlHttpRequest.send();
        }

        log.debug("Request : " + httpMethod + " " + endpoint);
        log.debug("Request payload if any : " + stringify(requestPayload));
        log.debug("Response status : " + xmlHttpRequest.status);
        log.debug("Response payload if any : " + xmlHttpRequest.responseText);

        if (xmlHttpRequest.status == 401) {
            if ((xmlHttpRequest.responseText == TOKEN_EXPIRED ||
                xmlHttpRequest.responseText == TOKEN_INVALID ) && count < 5) {
                tokenUtil.refreshTokenPair();
                return privateMethods.execute(httpMethod, requestPayload, endpoint, responseCallback, ++count, headers);
            } else if (privateMethods.isInvalidCredential(xmlHttpRequest.responseText)) {
                tokenUtil.refreshTokenPair();
                return privateMethods.execute(httpMethod, requestPayload, endpoint, responseCallback, ++count, headers);
            }
         } else {
            return responseCallback(xmlHttpRequest);
         }
    };


    /**
     * This method verify whether the access token is expired using response payload.
     * This is required when using API gateway.
     * @param responsePayload response payload.
     * return true if it is invalid otherwise false.
     */
    privateMethods["isInvalidCredential"] =
        function (responsePayload) {
            if (responsePayload) {
                try {
                    payload = parse(responsePayload);
                    if (payload["fault"]["code"] == 900901) {
                        log.debug("Access token is invalid: " + payload["fault"]["code"]);
                        log.debug(payload["fault"]["description"]);
                        return true;
                    }
                } catch (err) {
                    // do nothing
                }
            }
            return false;
        };

    /**
     * This method add Oauth authentication header to outgoing XML-HTTP Requests if Oauth authentication is enabled.
     * @param httpMethod HTTP request type.
     * @param requestPayload payload/data if exists which is needed to be send.
     * @param endpoint Backend REST API url.
     * @param responseCallback a function to be called with response retrieved.
     */
    privateMethods["initiateXMLHTTPRequest"] =
        function (httpMethod, requestPayload, endpoint, responseCallback, headers) {
            return privateMethods.execute(httpMethod, requestPayload, endpoint, responseCallback, 0, headers);
        };

    /**
     * This method invokes return initiateXMLHttpRequest for get calls.
     * @param endpoint Backend REST API url.
     * @param responseCallback a function to be called with response retrieved.
     */
    publicXMLHTTPInvokers["get"] = function (endpoint, responseCallback, headers) {
        var requestPayload = null;
        return privateMethods.initiateXMLHTTPRequest(constants["HTTP_GET"], requestPayload, endpoint, responseCallback,
                                                     headers);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for post calls.
     * @param endpoint Backend REST API url.
     * @param requestPayload payload/data if exists which is needed to be send.
     * @param responseCallback a function to be called with response retrieved.
     */
    publicXMLHTTPInvokers["post"] = function (endpoint, requestPayload, responseCallback, headers) {
        return privateMethods.initiateXMLHTTPRequest(constants["HTTP_POST"], requestPayload, endpoint, responseCallback,
                                                     headers);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for put calls.
     * @param endpoint Backend REST API url.
     * @param requestPayload payload/data if exists which is needed to be send.
     * @param responseCallback a function to be called with response retrieved.
     */
    publicXMLHTTPInvokers["put"] = function (endpoint, requestPayload, responseCallback, headers) {
        return privateMethods.initiateXMLHTTPRequest(constants["HTTP_PUT"], requestPayload, endpoint, responseCallback,
                                                     headers);
    };

    /**
     * This method invokes return initiateXMLHttpRequest for delete calls.
     * @param endpoint Backend REST API url.
     * @param responseCallback a function to be called with response retrieved.
     */
    publicXMLHTTPInvokers["delete"] = function (endpoint, responseCallback, headers) {
        var requestPayload = null;
        return privateMethods.initiateXMLHTTPRequest(constants["HTTP_DELETE"], requestPayload, endpoint,
                                                     responseCallback, headers);
    };

    /**
     * ---------------------------------------------------------------------------
     * Start of WS-REQUEST based Interceptor implementations
     * ---------------------------------------------------------------------------
     */

    /**
     * This method add Oauth authentication header to outgoing WS Requests if Oauth authentication is enabled.
     * @param action
     * @param endpoint service end point to be triggered.
     * @param payload soap payload which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param soapVersion soapVersion which need to used.
     */
    privateMethods["initiateWSRequest"] = function (action, endpoint, successCallback,
                                                    errorCallback, soapVersion, payload) {
        var ws = require("ws");
        //noinspection JSUnresolvedFunction
        var wsRequest = new ws.WSRequest();
        var options = [];
        if (devicemgtProps["isOAuthEnabled"]) {
            var accessToken = privateMethods.getAccessToken();
            if (accessToken) {
                var authenticationHeaderName = String(constants["AUTHORIZATION_HEADER"]);
                var authenticationHeaderValue = String(constants["BEARER_PREFIX"] + accessToken);
                var headers = [];
                var oAuthAuthenticationData = {};
                oAuthAuthenticationData.name = authenticationHeaderName;
                oAuthAuthenticationData.value = authenticationHeaderValue;
                headers.push(oAuthAuthenticationData);
                options.HTTPHeaders = headers;
            } else {
                response.sendRedirect(devicemgtProps["appContext"] + "login");
            }
        }
        options.useSOAP = soapVersion;
        options.useWSA = constants["WEB_SERVICE_ADDRESSING_VERSION"];
        options.action = action;
        var wsResponse;
        try {
            wsRequest.open(options, endpoint, false);
            if (payload) {
                wsRequest.send(payload);
            } else {
                wsRequest.send();
            }
            wsResponse = wsRequest.responseE4X;
        } catch (e) {
            return errorCallback(e);
        }
        return successCallback(wsResponse);
    };

    /**
     * This method invokes return initiateWSRequest for soap calls.
     * @param action describes particular soap action.
     * @param requestPayload SOAP request payload which is needed to be send.
     * @param endpoint service end point to be triggered.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param soapVersion soapVersion which need to used.
     */
    publicWSInvokers["soapRequest"] = function (action, requestPayload, endpoint,
                                                successCallback, errorCallback, soapVersion) {
        return privateMethods.initiateWSRequest(action, endpoint, successCallback,
                                                errorCallback, soapVersion, requestPayload);
    };

    /**
     * ---------------------------------------------------------------------------
     * Start of HTTP-CLIENT-REQUEST based Interceptor implementations
     * ---------------------------------------------------------------------------
     */

    /**
     * This method add Oauth authentication header to outgoing HTTPClient Requests if Oauth authentication is enabled.
     * @param method HTTP request type.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param headers a list of name value pairs for additional http headers.
     */
    privateMethods["initiateHTTPClientRequest"] =
        function (method, url, successCallback, errorCallback, payload, headers) {
            //noinspection JSUnresolvedVariable
            var HttpClient = Packages.org.apache.commons.httpclient.HttpClient;
            var httpMethodObject;
            switch (method) {
                case constants["HTTP_GET"]:
                    //noinspection JSUnresolvedVariable
                    var GetMethod = Packages.org.apache.commons.httpclient.methods.GetMethod;
                    httpMethodObject = new GetMethod(url);
                    break;
                case constants["HTTP_POST"]:
                    //noinspection JSUnresolvedVariable
                    var PostMethod = Packages.org.apache.commons.httpclient.methods.PostMethod;
                    httpMethodObject = new PostMethod(url);
                    break;
                case constants["HTTP_PUT"]:
                    //noinspection JSUnresolvedVariable
                    var PutMethod = Packages.org.apache.commons.httpclient.methods.PutMethod;
                    httpMethodObject = new PutMethod(url);
                    break;
                case constants["HTTP_DELETE"]:
                    //noinspection JSUnresolvedVariable
                    var DeleteMethod = Packages.org.apache.commons.httpclient.methods.DeleteMethod;
                    httpMethodObject = new DeleteMethod(url);
                    break;
                default:
                    //noinspection JSUnresolvedFunction
                    throw new IllegalArgumentException("Invalid HTTP request method: " + method);
            }

            //noinspection JSUnresolvedVariable
            var Header = Packages.org.apache.commons.httpclient.Header;
            var contentTypeFound = false;
            var acceptTypeFound = false;
            var acceptTypeValue = constants["APPLICATION_JSON"];
            for (var i in headers) {
                var header = new Header();
                header.setName(headers[i].name);
                header.setValue(headers[i].value);
                httpMethodObject.addRequestHeader(header);

                if(constants["CONTENT_TYPE_IDENTIFIER"] == headers[i].name){
                    contentTypeFound = true;
                }
                if(constants["ACCEPT_IDENTIFIER"] == headers[i].name){
                    acceptTypeFound = true;
                    acceptTypeValue = headers[i].value;
                }
            }

            var header = new Header();
            if(!contentTypeFound){
                header.setName(constants["CONTENT_TYPE_IDENTIFIER"]);
                header.setValue(constants["APPLICATION_JSON"]);
                //noinspection JSUnresolvedFunction
                httpMethodObject.addRequestHeader(header);
            }

            if(!acceptTypeFound) {
                header = new Header();
                header.setName(constants["ACCEPT_IDENTIFIER"]);
                header.setValue(constants["APPLICATION_JSON"]);
                //noinspection JSUnresolvedFunction
                httpMethodObject.addRequestHeader(header);
            }

            if (devicemgtProps["isOAuthEnabled"]) {
                var accessToken = privateMethods.getAccessToken();
                if (accessToken) {
                    header = new Header();
                    header.setName(constants["AUTHORIZATION_HEADER"]);
                    header.setValue(constants["BEARER_PREFIX"] + accessToken);
                    //noinspection JSUnresolvedFunction
                    httpMethodObject.addRequestHeader(header);
                } else {
                    response.sendRedirect(devicemgtProps["appContext"] + "login");
                }
            }
            //noinspection JSUnresolvedFunction
            if (payload != null) {
                var StringRequestEntity = Packages.org.apache.commons.httpclient.methods.StringRequestEntity;
                var stringRequestEntity = new StringRequestEntity(stringify(payload));
                //noinspection JSUnresolvedFunction
                httpMethodObject.setRequestEntity(stringRequestEntity);
            }
            var client = new HttpClient();
            try {
                //noinspection JSUnresolvedFunction
                client.executeMethod(httpMethodObject);
                //noinspection JSUnresolvedFunction
                var status = httpMethodObject.getStatusCode();
                if (status >= 200 && status < 300) {
                    if (constants["STREAMING_FILES_ACCEPT_HEADERS"].indexOf(acceptTypeValue) > -1) {
                        return successCallback(httpMethodObject.getResponseBodyAsStream(),
                                               httpMethodObject.getResponseHeaders());
                    } else {
                        return successCallback(httpMethodObject.getResponseBodyAsString(),
                                               httpMethodObject.getResponseHeaders());
                    }
                } else {
                    return errorCallback(httpMethodObject.getResponseBodyAsString(),
                                         httpMethodObject.getResponseHeaders());
                }
            } catch (e) {
                return errorCallback(response);
            } finally {
                //noinspection JSUnresolvedFunction
                if (method != constants["HTTP_GET"]) {
                    method.releaseConnection();
                }
            }
        };

    /**
     * This method invokes return initiateHTTPClientRequest for get calls.
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param headers a list of name value pairs for additional http headers.
     */
    publicHTTPClientInvokers["get"] = function (url, successCallback, errorCallback, headers) {
        var requestPayload = null;
        return privateMethods.initiateHTTPClientRequest(constants["HTTP_GET"], url, successCallback, errorCallback,
                                                        requestPayload, headers);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for post calls.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param headers a list of name value pairs for additional http headers.
     */
    publicHTTPClientInvokers["post"] = function (url, payload, successCallback, errorCallback, headers) {
        return privateMethods.initiateHTTPClientRequest(constants["HTTP_POST"], url, successCallback, errorCallback,
                                                        payload, headers);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for put calls.
     * @param url target url.
     * @param payload payload/data which need to be send.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param headers a list of name value pairs for additional http headers.
     */
    publicHTTPClientInvokers["put"] = function (url, payload, successCallback, errorCallback, headers) {
        return privateMethods.initiateHTTPClientRequest(constants["HTTP_PUT"], url, successCallback, errorCallback,
                                                        payload, headers);
    };

    /**
     * This method invokes return initiateHTTPClientRequest for delete calls.
     * @param url target url.
     * @param successCallback a function to be called if the respond if successful.
     * @param errorCallback a function to be called if en error is reserved.
     * @param headers a list of name value pairs for additional http headers.
     */
    publicHTTPClientInvokers["delete"] = function (url, successCallback, errorCallback, headers) {
        var requestPayload = null;
        return privateMethods.initiateHTTPClientRequest(constants["HTTP_DELETE"], url, successCallback, errorCallback,
                                                        requestPayload, headers);
    };

    var publicMethods = {};
    publicMethods.XMLHttp = publicXMLHTTPInvokers;
    publicMethods.WS = publicWSInvokers;
    publicMethods.HttpClient = publicHTTPClientInvokers;

    return publicMethods;
}();
