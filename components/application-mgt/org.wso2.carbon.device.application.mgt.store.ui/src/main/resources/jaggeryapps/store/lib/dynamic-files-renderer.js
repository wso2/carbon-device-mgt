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
 * Rendering context.
 * @typedef {{
 *     app: {context: string, conf: Object}, uri: string, uriParams: Object.<string,
 *     string>, user: User}} RenderingContext
 */

var renderer = {};

(function (renderer) {
    var log = new Log("dynamic-files-renderer");
    var constants = require("/lib/constants.js").constants;

    /**
     *
     * @param pageUri {string} page URI
     * @param lookupTable {LookupTable} lookup table
     * @return {string}
     */
    function getPushedUnitsHandlebarsTemplate(pageUri, lookupTable) {
        var uriMatcher = new URIMatcher(pageUri);
        var pushedUnits = lookupTable.pushedUnits;
        var uriPatterns = Object.keys(pushedUnits);
        var numberOfUriPatterns = uriPatterns.length;
        var buffer = [];
        for (var i = 0; i < numberOfUriPatterns; i++) {
            var uriPattern = uriPatterns[i];
            if (uriMatcher.match(uriPattern)) {
                buffer.push('{{unit "', pushedUnits[uriPattern].join('"}}{{unit "'), '" }}');
            }
        }
        return (buffer.length == 0) ? null : buffer.join("");
    }

    /**
     * Renders the specified UI Component.
     * @param uiComponent {UIComponent}
     * @param templateContext {Object}
     * @param renderingContext {RenderingContext}
     * @param lookupTable {LookupTable}
     * @param response {Object}
     * @returns {boolean}
     */
    renderer.renderUiComponent = function (uiComponent, templateContext, renderingContext,
                                           lookupTable, response) {
        var template;
        if (uiComponent.type == "page") {
            var templateBuffer = ["{{#page \"", uiComponent.fullName, "\" _params=this}}"];
            var pushedUnitsTemplate = getPushedUnitsHandlebarsTemplate(renderingContext.uri,
                                                                       lookupTable);
            if (pushedUnitsTemplate) {
                templateBuffer.push(" {{#zone \"_pushedUnits\"}} ", pushedUnitsTemplate,
                                    " {{/zone}} ");
            }
            templateBuffer.push("{{/page}}");
            template = templateBuffer.join("");
        } else {
            template = "{{unit \"" + uiComponent.fullName + "\" _params=this}}";
        }
        templateContext = (templateContext) ? templateContext : {};
        return renderer.renderTemplate(template, templateContext, renderingContext, lookupTable,
                                       response);
    };

    /**
     * Renders the specified Handlebars template.
     * @param template {string}
     * @param templateContext {Object}
     * @param renderingContext {RenderingContext}
     * @param lookupTable {LookupTable}
     * @param response {Object}
     * @returns {boolean}
     */
    renderer.renderTemplate = function (template, templateContext, renderingContext,
                                        lookupTable, response) {
        var handlebarsModule = require("/lib/modules/handlebars/handlebars.js");
        try {
            var html = handlebarsModule.render(template, templateContext, renderingContext,
                                               lookupTable, response);
            response.addHeader("Content-type", "text/html");
            // We don't want web browsers to cache dynamic HTML pages.
            // Adopted from http://stackoverflow.com/a/2068407/1577286
            response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.addHeader("Pragma", "no-cache");
            response.addHeader("Expires", "0");
            print(html);
            return true;
        } catch (e) {
            if ((typeof e) == "string") {
                // JS "throw message" type errors
                log.error(e);
                response.sendError(500, e);
            } else {
                if (e.stack) {
                    // Java/Rhino Exceptions
                    log.error(e.message, e);
                    response.sendError(500, e.message);
                } else if (e.message) {
                    // JS "throw new Error(message)" type errors
                    log.error(e.message);
                    response.sendError(500, e.message);
                }
            }
            return false;
        }
    };
})(renderer);
