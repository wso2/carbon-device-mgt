/**
 * @license
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var route;

(function () {
    var log = new Log("units-router");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;

    /**
     * Process a HTTP request that requests an unit.
     * @param request {Object} HTTP request to be processed
     * @param response {Object} HTTP response to be served
     */
    route = function (request, response) {
        // Lets assume URL looks like https://my.domain.com/appName/unit/{unitFulName}?...
        // URI = /appName/unit/{unitFulName}
        var requestUri = decodeURIComponent(request.getRequestURI());
        var parts = requestUri.split("/");
        if (parts.length != 4) {
            // An invalid URI.
            var msg = "Request URI '" + requestUri + "' is invalid.";
            log.warn(msg);
            response.sendError(400, msg);
            return;
        }

        var appConfigurations = utils.getAppConfigurations();
        /** @type {LookupTable} */
        var lookupTable = utils.getLookupTable(appConfigurations);
        var unit = lookupTable.units[parts[3]];
        if (!unit) {
            response.sendError(404, "Requested unit '" + parts[3] + "' does not exists.");
            return;
        }

        /** @type {RenderingContext} */
        var renderingContext = {
            app: {
                context: utils.getAppContext(request),
                conf: appConfigurations
            },
            uri: requestUri,
            uriParams: {},
            user: utils.getCurrentUser()
        };
        var renderer = require("/lib/dynamic-files-renderer.js").renderer;
        renderer.renderUiComponent(unit, request.getAllParameters(), renderingContext, lookupTable,
                                   response);
    };
})();
