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
    var log = new Log("pages-router");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;

    /**
     *
     * @param pageUri {string} page path requested in the URL
     * @param lookupTable {LookupTable} lookup table
     * @return {{page: UIComponent, uriParams: Object.<string, string>}|null}
     *     if exists full name, URI params and page of the specified URI, otherwise
     *     <code>null</code>
     */
    function getPageData(pageUri, lookupTable) {
        var uriPagesMap = lookupTable.uriPagesMap;
        var uriMatcher = new URIMatcher(pageUri);
        var uriPatterns = Object.keys(uriPagesMap);
        var numberOfUriPatterns = uriPatterns.length;

        for (var i = 0; i < numberOfUriPatterns; i++) {
            var uriPattern = uriPatterns[i];
            if (uriMatcher.match(uriPattern)) {
                return {
                    page: lookupTable.pages[uriPagesMap[uriPattern]],
                    uriParams: uriMatcher.elements()
                };
            }
        }
        // No page found
        return null;
    }

    /**
     * Returns the URI of the login page specified in application configurations file.
     * @param renderingContext {RenderingContext} application configurations
     * @param lookupTable {LookupTable} lookup table
     * @return {string} page URI.
     */
    function getLoginPageUri(renderingContext, lookupTable) {
        var appData = renderingContext.app;
        var loginPageFullName = appData.conf[constants.APP_CONF_LOGIN_PAGE];
        if (loginPageFullName) {
            var loginPage = lookupTable.pages[loginPageFullName];
            if (loginPage) {
                loginPage = utils.getFurthestChild(loginPage);
                if (loginPage.disabled) {
                    log.warn("Login page '" + loginPage.fullName + " mentioned in application "
                             + "configuration file '" + constants.FILE_APP_CONF + "' is disabled.");
                } else {
                    return (appData.context + loginPage.definition[constants.PAGE_DEFINITION_URI]);
                }
            } else {
                log.error("Login page '" + loginPageFullName + " mentioned in application "
                          + "configuration file '" + constants.FILE_APP_CONF
                          + "' does not exists.");
            }
        }
        return (appData.context + "/");
    }

    /**
     * Whether the specified page is processable or not.
     * @param page {UIComponent} page
     * @param renderingContext {RenderingContext} rendering context
     * @param lookupTable {LookupTable} lookup table
     * @param request {Object} HTTP request
     * @param response {Object} HTTP response
     * @return {boolean} <code>true</code> if processable, otherwise <code>false</code>
     */
    function isPageProcessable(page, renderingContext, lookupTable, request, response) {
        if (page.disabled) {
            // This page is disabled.
            response.sendError(404, "Requested page '" + renderingContext.pageUri
                                    + "' does not exists.");
            return false;
        }

        var pageDefinition = page.definition;
        if (utils.parseBoolean(pageDefinition[constants.UI_COMPONENT_DEFINITION_IS_ANONYMOUS])) {
            // This is an anonymous page. So no need for an user session or checking permissions.
            return true;
        }

        // This is not an anonymous page.
        var user = renderingContext.user;
        if (user) {
            // An user has logged in.
            var pagePermissions = pageDefinition[constants.UI_COMPONENT_DEFINITION_PERMISSIONS];
            if (pagePermissions && Array.isArray(pagePermissions)) {
                // A permissions array is specified in the page definition.
                var numberOfUnitPermissions = pagePermissions.length;
                var userPermissionsMap = user.permissions;
                for (var i = 0; i < numberOfUnitPermissions; i++) {
                    if (!userPermissionsMap.hasOwnProperty(pagePermissions[i])) {
                        // User does not have this permission.
                        if (log.isDebugEnabled()) {
                            log.debug("User '" + user.username + "' in domain '" + user.domain
                                      + "' does not have permission '" + pagePermissions[i]
                                      + "' to view page '" + page.fullName + "'.");
                        }
                        response.sendError(403, "You do not have enough permissions to access the "
                                                + "requested page '" + renderingContext.pageUri
                                                + "'.");
                        return false;
                    }
                }
                // User has all permissions.
                return true;
            } else {
                // Permissions are not specified in the page definition.
                return true;
            }
        } else {
            // Currently no user has logged in. So redirect to the login page.
            var queryString = request.getQueryString();
            var referer = renderingContext.uri + ((queryString) ? ("?" + queryString) : "");
            var redirectUri = getLoginPageUri(renderingContext, lookupTable) + "?"
                              + constants.URL_PARAM_REFERER + "=" + referer;
            response.sendRedirect(encodeURI(redirectUri));
            return false;
        }
    }

    /**
     * Process a HTTP request that requests a page.
     * @param request {Object} HTTP request to be processed
     * @param response {Object} HTTP response to be served
     */
    route = function (request, response) {
        var appConfigurations = utils.getAppConfigurations();
        var lookupTable = utils.getLookupTable(appConfigurations);

        // Lets assume URL looks like https://my.domain.com/appName/{foo}/{bar}/...
        var requestUri = decodeURIComponent(request.getRequestURI()); // /appName/{foo}/{bar}/...
        var pageUri = requestUri.substring(requestUri.indexOf("/", 1)); // /{foo}/{bar}/...

        var pageData = getPageData(pageUri, lookupTable);
        if (!pageData) {
            response.sendError(404, "Requested page '" + pageUri + "' does not exists.");
            return;
        }

        /** @type {RenderingContext} */
        var renderingContext = {
            app: {
                context: utils.getAppContext(request),
                conf: appConfigurations
            },
            uri: pageUri,
            uriParams: pageData.uriParams,
            user: utils.getCurrentUser()
        };

        var page = pageData.page;
        if (!isPageProcessable(page, renderingContext, lookupTable, request, response)) {
            return;
        }

        var renderer = require("/lib/dynamic-files-renderer.js").renderer;
        renderer.renderUiComponent(pageData.page, {}, renderingContext, lookupTable, response);
    };
})();
