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

var serverAddress = function () {
    var log = new Log("serverAddress.js");
    var process = require("process"),
        host = process.getProperty('server.host'),
        ip = process.getProperty('carbon.local.ip');
    var publicMethods = {};
    publicMethods.getHPPSTSAddress = function () {
        var port = process.getProperty('mgt.transport.https.proxyPort');
        if (!port) {
            port = process.getProperty('mgt.transport.https.port');
        }
        if (host === "localhost") {
            return "https://" + ip + ":" + port;
        } else {
            return "https://" + host + ":" + port;
        }
    };
    publicMethods.getHPPTAddress = function () {
        var port = process.getProperty('mgt.transport.http.proxyPort');
        if (!port) {
            port = process.getProperty('mgt.transport.http.port');
        }
        if (host === "localhost") {
            return "http://" + ip + ":" + port;
        } else {
            return "http://" + host + ":" + port;
        }
    };
    publicMethods.getWSSAddress = function () {
        var port = process.getProperty('mgt.transport.https.proxyPort');
        if (!port) {
            port = process.getProperty('mgt.transport.https.port');
        }
        if (host === "localhost") {
            return "wss://" + ip + ":" + port;
        } else {
            return "wss://" + host + ":" + port;
        }
    };
    publicMethods.getWSAddress = function () {
        var port = process.getProperty('mgt.transport.http.proxyPort');
        if (!port) {
            port = process.getProperty('mgt.transport.http.port');
        }
        if (host === "localhost") {
            return "ws://" + ip + ":" + port;
        } else {
            return "ws://" + host + ":" + port;
        }
    };
    return publicMethods;
}();