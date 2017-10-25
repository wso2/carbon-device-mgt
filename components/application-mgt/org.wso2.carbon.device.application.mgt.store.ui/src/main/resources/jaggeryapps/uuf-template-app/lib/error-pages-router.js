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

/**
 * Renders configured error page when <code>response.sendError</code> is called.
 */
(function (request, response) {
    var log = new Log("error-pages-handler");
    /**
     * Default <code>response.sendError</code> function provided by Jaggery.
     * @type {function}
     */
    var oldSendErrorFunction = response.sendError;
    /**
     * Whether previous error page routing process of this HTTP request has finished or not.
     * @type {boolean}
     */
    var prevErrorPageRoutingFinished = true;

    /**
     * Custom sendError function.
     * @param status {number} HTTP status code
     * @param message {string} error message
     */
    response.sendError = function (status, message) {
        if (!prevErrorPageRoutingFinished) {
            // Previous error page routing process failed, at some point "response.sendError"
            // function was called. To avoid recursion, call the default "sendError" function.
            oldSendErrorFunction.call(this, status, message);
            return;
        }

        prevErrorPageRoutingFinished = false;
        try {
            route(status, message, request, response);
        } catch (e) {
            // An exception was thrown when routing the error page. Hence call the default
            // "sendError" function.
            if ((typeof e) == "string") {
                // JS "throw message" type errors
                log.error(e);
                oldSendErrorFunction.call(this, 500, e);
            } else {
                if (e.stack) {
                    // Java/Rhino Exceptions
                    log.error(e.message, e);
                    oldSendErrorFunction.call(this, 500, e.message);
                } else if (e.message) {
                    // JS "throw new Error(message)" type errors
                    log.error(e.message);
                    oldSendErrorFunction.call(this, 500, e.message);
                }
            }
        }
        prevErrorPageRoutingFinished = true;
    };

    /**
     *
     * @param status {number} HTTP status of the error
     * @param message {String} error message
     * @param request {Object} HTTP request
     * @param response {Object} HTTP response
     */
    function route(status, message, request, response) {
        var constants = require("/lib/constants.js").constants;
        var utils = require("/lib/utils.js").utils;

        var appConfigurations = utils.getAppConfigurations();
        var errorPagesConfigs = appConfigurations[constants.APP_CONF_ERROR_PAGES];
        if (!errorPagesConfigs) {
            return;
        }
        var errorPageFullName = errorPagesConfigs[status] || errorPagesConfigs["default"];
        if (!errorPageFullName) {
            return;
        }

        var lookupTable = utils.getLookupTable(appConfigurations);
        var errorPage = lookupTable.pages[errorPageFullName];
        if (!errorPage) {
            log.warn("Error page '" + errorPageFullName
                     + " mentioned in application configuration file '" + constants.FILE_APP_CONF
                     + "' does not exists.");
            return;
        } else if (errorPage.disabled) {
            log.warn("Error page '" + errorPageFullName
                     + " mentioned in application configuration file '" + constants.FILE_APP_CONF
                     + "' is disabled.");
            return;
        }

        /** @type {RenderingContext} */
        var renderingContext = {
            app: {
                context: utils.getAppContext(request),
                conf: appConfigurations
            },
            uri: errorPage.definition[constants.PAGE_DEFINITION_URI],
            uriParams: {},
            user: utils.getCurrentUser()
        };
        var templateContext = {status: status, message: message};
        var renderer = require("/lib/dynamic-files-renderer.js").renderer;
        renderer.renderUiComponent(errorPage, templateContext, renderingContext, lookupTable,
                                   response);
    }

})(request, response);
