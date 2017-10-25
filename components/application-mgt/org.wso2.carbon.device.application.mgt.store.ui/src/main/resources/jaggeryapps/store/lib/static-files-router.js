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
 * Data of a static resource.
 * @typedef {{type: string, file: Object, provider: UIComponent }} StaticResource
 */

var route;

(function () {
    var log = new Log("static-file-router");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;
    var lessRenderer;

    /**
     * Splits a full file name to its name and extension.
     * @param fullFileName {string} file name to be split e.g. foo.txt
     * @return {{name: string, extension: string}} splited parts
     */
    function splitFileName(fullFileName) {
        var index = fullFileName.lastIndexOf(".");
        return {name: fullFileName.substr(0, index), extension: fullFileName.substr(index + 1)}
    }

    function nthIndexOf(str, pat, n) {
        // Adopted from https://stackoverflow.com/a/14482123/1577286
        var strLength = str.length, i = -1;
        while (n-- && (i++ < strLength)) {
            i = str.indexOf(pat, i);
        }
        return i;
    }

    /**
     * Returns the hash code of the specified string.
     * @param str {string} string to be converted
     * @returns {string} hash code
     */
    function hashCode(str) {
        // Adopted from https://stackoverflow.com/a/7616484/1577286
        var strLength = str.length;
        if (strLength == 0) {
            return "0";
        }
        var hash = 0, chr;
        for (var i = 0; i < strLength; i++) {
            chr = str.charCodeAt(i);
            hash = ((hash << 5) - hash) + chr;
            hash |= 0; // Convert to 32bit integer
        }
        return hash.toString();
    }

    /**
     * Closes the specified file without throwing any exceptions.
     * @param file {Object} file to be closed
     */
    function closeQuietly(file) {
        try {
            file.close();
        } catch (e) {
            log.error(e.message, e);
        }
    }

    /**
     * Parses the specifies LESS resource.
     * @param lessResource {StaticResource} LESS resource to be parse
     * @param isCachingEnabled {boolean} whether caching is enabled or not
     * @param lookupTable {LookupTable} lookup table
     * @return {{success: boolean, message: string, cssFile: Object}} LESS parsing result
     */
    function parseLessResource(lessResource, isCachingEnabled, lookupTable) {
        if (!lessRenderer) {
            lessRenderer = require("/lib/modules/less/less.js");
        }
        return lessRenderer.render(lessResource, isCachingEnabled, lookupTable);
    }

    /**
     * Returns resources in the specified URIs.
     * @param requestedResourcesUris {String[]} resource URIs
     * @param lookupTable {LookupTable} lookup table
     * @param utils {UtilsModule} utils module
     * @return {{success: boolean, status: number, message: string, resources: StaticResource[],
     *     hashCode: string}} resources data
     */
    function getResourcesData(requestedResourcesUris, lookupTable, utils) {
        var rv = {success: true, status: 0, message: "Success", resources: null, hashCode: null};
        /** @type {StaticResource[]} */
        var requestedResources = [];
        var hashCodeBuffer = [];

        var uiComponentPublicDirectory = constants.DIRECTORY_APP_UNIT_PUBLIC;
        var numberOfRequestedResourcesUris = requestedResourcesUris.length;
        var uiComponents = lookupTable.uiComponents;
        var getFileInUiComponent = utils.getFileInUiComponent;
        for (var i = 0; i < numberOfRequestedResourcesUris; i++) {
            var requestedResourceUri = requestedResourcesUris[i];
            if (requestedResourceUri.length == 0) {
                // ignore
                continue;
            }

            var parts = requestedResourceUri.split("/");
            var numberOfParts = parts.length;
            switch (numberOfParts) {
                case 0:
                case 1:
                    // An invalid resource URI.
                    rv.success = false;
                    rv.status = 400;
                    rv.message =
                        "Requested resource URI '" + requestedResourceUri + "' is invalid.";
                    return rv;
                case 2:
                    // An invalid resource URI. parts = [{uiComponentFullName}, {fileName}]
                    rv.success = false;
                    rv.status = 400;
                    rv.message = "Request resource URI '" + requestedResourceUri + "' is invalid. "
                                 + "Uncategorized resources inside the 'public' directory of "
                                 + "an UI Component are restricted.";
                    return rv;
                default:
                    // requestedResource = {uiComponentFullName}/{sub-directory}/{+filePath}
                    // For valid URIs : parts.length >= 3
                    // parts = ["uiComponentFullName", "sub-directory", ... ]
                    hashCodeBuffer.push(requestedResourceUri);
            }

            var uiComponentFullName = parts[0];
            var uiComponent = uiComponents[uiComponentFullName];
            if (!uiComponent) {
                rv.success = false;
                rv.status = 404;
                rv.message =
                    "Requested UI Component '" + uiComponentFullName + "' does not exists.";
                return rv;
            }

            var resourceType = splitFileName(parts[numberOfParts - 1]).extension;
            var relativeFilePath = uiComponentPublicDirectory
                                   + requestedResourceUri.substr(uiComponentFullName.length);
            var resourceFile = getFileInUiComponent(uiComponent, relativeFilePath, lookupTable);
            if (resourceFile) {
                requestedResources.push({
                    type: resourceType,
                    file: resourceFile,
                    provider: uiComponent
                });
            } else {
                // Requested file either does not exists or it is a directory.
                rv.success = false;
                rv.status = 404;
                rv.message = "Requested resource '" + relativeFilePath
                             + "' does not exists in UI Component '" + uiComponent.fullName
                             + "' or its parents " + stringify(uiComponent.parents) + ".";
                return rv;
            }
        }

        rv.hashCode = hashCode(hashCodeBuffer.join(","));
        rv.resources = requestedResources;
        return rv;
    }

    /**
     * Returns the combined file for the specified resources.
     * @param requestedResources {StaticResource[]} resources
     * @param hashCode {string} hash code of the resources
     * @return {Object} combined file
     */
    function getCombinedFile(requestedResources, hashCode) {
        var firstResourceType = requestedResources[0].type;
        var extension = (firstResourceType == "less") ? "css" : firstResourceType;
        return new File(constants.DIRECTORY_CACHE + "/" + hashCode + "." + extension);
    }

    /**
     * Updates the combined file of the specified resources.
     * @param combinedFile {Object} combined file
     * @param requestedResources {StaticResource[]} resources
     * @param isCachingEnabled {boolean} whether caching is enabled or not
     * @param lookupTable {LookupTable} lookup table
     * @return {{success: boolean, message: string}} combined file updating result
     */
    function updateCombinedFile(combinedFile, requestedResources, isCachingEnabled, lookupTable) {
        var rv = {success: true, message: "Success"};
        try {
            combinedFile.open('w');
        } catch (e) {
            rv.success = false;
            rv.message = "Cannot open cache file '" + combinedFile.getPath() + "' for writing.";
            log.error(e);
            return rv;
        }

        var numberOfRequestedResources = requestedResources.length;
        for (var i = 0; i < numberOfRequestedResources; i++) {
            var processingResource = requestedResources[i];
            var processingFile;
            if (processingResource.type == "less") {
                var lessRenderingResult = parseLessResource(processingResource, isCachingEnabled,
                                                            lookupTable);
                if (!lessRenderingResult.success) {
                    rv.success = false;
                    rv.message = lessRenderingResult.message;
                    closeQuietly(combinedFile);
                    combinedFile.del();
                    return rv;
                }
                processingFile = lessRenderingResult.cssFile;
            } else {
                processingFile = processingResource.file;
            }

            var inputStream;
            try {
                inputStream = processingFile.getStream();
            } catch (e) {
                rv.success = false;
                rv.message = "Cannot open file '" + processingFile.getPath() + " for reading.";
                log.error(e);
                closeQuietly(processingFile);
                return rv;
            }
            try {
                combinedFile.write(inputStream);
            } catch (e) {
                rv.success = false;
                rv.message = "Cannot write to cache file '" + combinedFile.getPath() + ".";
                log.error(e);
                closeQuietly(combinedFile);
                return rv;
            }
        }

        closeQuietly(combinedFile);
        return rv;
    }

    /**
     *
     * @param request {Object} HTTP response
     * @returns {number} milliseconds from epoch
     */
    function getIfModifiedSinceDate(request) {
        var httpDateStr = request.getHeader("If-Modified-Since");
        return new Date(httpDateStr).getTime();
    }

    /**
     *
     * @param file {Object} file
     * @returns {string} HTTP-date
     */
    function getLastModifiedDate(file) {
        var utcMilliseconds = parseInt(file.getLastModified());
        return new Date(utcMilliseconds).toUTCString();
    }

    /**
     * Sets necessary HTTP headers in the specified HTTP response.
     * @param file {Object} file which will be served in the HTTP response.
     * @param mimeType {string} MIME type of the file
     * @param response {Object} HTTP response
     */
    function setResponseHeaders(file, mimeType, response) {
        response.addHeader("Content-type", mimeType);
        response.addHeader("Cache-Control", "public,max-age=12960000");
        response.addHeader("Last-Modified", getLastModifiedDate(file));
    }

    /**
     * Sends the specified file via the response.
     * @param file {Object} file to be served
     * @param response {Object} HTTP response
     */
    function serveFile(file, response) {
        // TODO: use Tomcat file serving mechanism
        try {
            print(file.getStream());
        } catch (e) {
            log.error(e.message, e);
        } finally {
            closeQuietly(file);
        }
    }

    /**
     * Process a HTTP request that requests a static file.
     * @param request {Object} HTTP request to be processed
     * @param response {Object} HTTP response to be served
     */
    route = function (request, response) {
        var appConfigs = utils.getAppConfigurations();
        /** @type {LookupTable} */
        var lookupTable = utils.getLookupTable(appConfigs);

        // URI = /{appName}/public/[{uiComponentFullName}/{resourceType}/{+filePath},...]
        var requestUri = decodeURIComponent(request.getRequestURI());
        var rawResourcesString = requestUri.substr(nthIndexOf(requestUri, "/", 3) + 1);
        if (rawResourcesString.length == 0) {
            // An invalid URI.
            var msg = "Request URI '" + requestUri + "' is invalid.";
            log.warn(msg);
            response.sendError(400, msg);
            return;
        }

        // separate resources
        var resourcesString = rawResourcesString.split(constants.COMBINED_RESOURCES_URL_TAIL)[0];
        var requestedResourcesUris = resourcesString.split(constants.COMBINED_RESOURCES_SEPARATOR);
        if (requestedResourcesUris.length == 0) {
            // An invalid URI.
            var msg = "Request URI '" + requestUri + "' is invalid.";
            log.warn(msg);
            response.sendError(400, msg);
            return;
        }

        // get resources data
        var resourcesData = getResourcesData(requestedResourcesUris, lookupTable, utils);
        if (!resourcesData.success) {
            log.warn(resourcesData.message);
            response.sendError(resourcesData.status, resourcesData.message);
            return;
        }

        var requestedResources = resourcesData.resources;
        var numberOfRequestedResources = requestedResources.length;
        if (numberOfRequestedResources == 0) {
            // An invalid URI.
            var msg = "Request URI '" + requestUri + "' is invalid.";
            log.warn(msg);
            response.sendError(400, msg);
            return;
        }

        var parseBoolean = utils.parseBoolean;
        var noCacheParam = parseBoolean(request.getParameter("nocache"));
        var cachingEnabledConfig = parseBoolean(appConfigs[constants.APP_CONF_CACHE_ENABLED], true);
        var isCachingEnabled = (!noCacheParam && cachingEnabledConfig);

        if (numberOfRequestedResources == 1) {
            // only one file
            var resource = requestedResources[0];
            var servingFile;
            if (resource.type == "less") {
                var lessRenderingResult = parseLessResource(resource, isCachingEnabled,
                                                            lookupTable);
                if (!lessRenderingResult.success) {
                    log.error(lessRenderingResult.message);
                    response.sendError(500, lessRenderingResult.message);
                    return;
                }
                servingFile = lessRenderingResult.cssFile;
            } else {
                servingFile = resource.file;
            }
            if (isCachingEnabled && (parseInt(servingFile.getLastModified())
                                     <= getIfModifiedSinceDate(request))) {
                // Requested file has not changed since last serve.
                response.status = 304;
                return;
            }
            setResponseHeaders(servingFile, servingFile.getContentType(), response);
            serveFile(servingFile, response);
            return;
        }

        // many files
        var cachedFile = getCombinedFile(requestedResources, resourcesData.hashCode);
        var cachedFileLMD = parseInt(cachedFile.getLastModified());
        var updateCachedFile;
        if (isCachingEnabled) {
            for (var i = 0; i < numberOfRequestedResources; i++) {
                if (cachedFileLMD < parseInt(requestedResources[i].file.getLastModified())) {
                    updateCachedFile = true;
                    break;
                }
            }
        } else {
            updateCachedFile = true;
        }

        if (updateCachedFile) {
            var cachedFileUpdateData = updateCombinedFile(cachedFile, requestedResources,
                                                          isCachingEnabled, lookupTable);
            if (!cachedFileUpdateData.success) {
                log.error(cachedFileUpdateData.message);
                response.sendError(500, cachedFileUpdateData.message);
                return;
            }
        } else {
            if (cachedFileLMD <= getIfModifiedSinceDate(request)) {
                // Requested files have not changed since the last serve.
                response.status = 304;
                return;
            }
        }
        setResponseHeaders(cachedFile, cachedFile.getContentType(), response);
        serveFile(cachedFile, response);
    };
})();
