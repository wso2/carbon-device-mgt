/**
 * Page rendering data.
 * @typedef {{
 *     context: {appData: {name: string, uri: string}, uriData: {uri: string, params:
 *     Object.<string, string>}}, pageData: {page: UIComponent, pageUri: string},
 *     zonesTree: ZoneTree, renderedUnits: string[]}} RenderingData
 */

/**
 * Routes a HTTP request which requests a dynamic page.
 * @param request {Object} HTTP request to be processed
 * @param response {Object} HTTP response to be served
 */
var route;

(function () {
    var log = new Log("[dynamic-file-router]");
    /** @type {UtilsModule} */
    var Utils = require("utils.js");

    /**
     * Returns the Handlebars environment.
     * @param renderingData {RenderingData} rendering data
     * @param lookupTable {LookupTable} lookup table
     * @return {Object} Handlebars environment
     */
    function getHandlebarsEnvironment(renderingData, lookupTable) {
        var rhh = require("rendering-handlebars-helpers.js");
        return rhh.registerHelpers(renderingData, lookupTable);
    }

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
     *
     * @param renderingData {RenderingData} page rendering data
     * @param lookupTable {LookupTable} lookup table
     * @return {string}
     */
    function getPushedUnitsHandlebarsTemplate(renderingData, lookupTable) {
        var uriMatcher = new URIMatcher(renderingData.pageData.pageUri);
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
     *
     * @param renderingData {RenderingData} page rendering data
     * @param lookupTable {LookupTable} lookup table
     * @param handlebarsEnvironment {Object} Handlebars environment
     * @param response {Object} HTTP response
     */
    function renderPage(renderingData, lookupTable, handlebarsEnvironment, response) {
        var page = renderingData.pageData.page;
        var buffer = ['{{#page "', page.fullName, '"}}'];
        var pushedUnitsHbsTemplate = getPushedUnitsHandlebarsTemplate(renderingData, lookupTable);
        if (pushedUnitsHbsTemplate) {
            buffer.push(' {{#zone "_pushedUnits"}} ', pushedUnitsHbsTemplate, ' {{/zone}} ');
        }
        buffer.push('{{/page}}');

        try {
            var compiledTemplate = handlebarsEnvironment.compile(buffer.join(""));
            response.addHeader("Content-type", "text/html");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setHeader("Expires", "0"); // Proxies.
            print(compiledTemplate({}));
        } catch (e) {
            if (e.message) {
                log.error(e.message, e);
                response.sendError(500, e.message);
            } else {
                log.error(e);
                response.sendError(500, e);
            }
        }
    }

    route = function (request, response) {
        var appConf = Utils.getAppConfigurations();
        /** @type {LookupTable} */
        var lookupTable = Utils.getLookupTable(appConf);

        // lets assume URL looks like https://my.domain.com/appName/{one}/{two}/{three}/{four}
        var requestUri = request.getRequestURI(); // = /appName/{one}/{two}/{three}/{four}
        var positionOfSecondSlash = requestUri.indexOf("/", 1);
        var pageUri = requestUri.substring(positionOfSecondSlash); // /{one}/{two}/{three}/{four}

        var pageData = getPageData(pageUri, lookupTable);
        // TODO: decide whether this page or its furthest child is rendered
        if (!pageData) {
            response.sendError(404, "Requested page '" + pageUri + "' does not exists.");
            return;
        }

        /** @type {RenderingData} */
        var renderingData = {
            context: {
                appData: {
                    name: requestUri.substring(1, positionOfSecondSlash),
                    uri: request.getContextPath()
                },
                uriData: {
                    uri: requestUri,
                    params: pageData.uriParams
                }
            },
            pageData: {
                page: pageData.page,
                pageUri: pageUri
            },
            /** @type {ZoneTree} */
            zonesTree: null,
            renderedUnits: []
        };
        var handlebarsEnvironment = getHandlebarsEnvironment(renderingData, lookupTable);
        renderPage(renderingData, lookupTable, handlebarsEnvironment, response);
        //print(stringify(renderingData));
    };
})();
