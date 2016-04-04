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

var render;

(function () {
    var log = new Log("less-module");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;

    /**
     * LESS library.
     * @type {Object}
     */
    var lessLib;
    /**
     * LESS parser options.
     * @type {{depends: boolean, compress: boolean, cleancss: boolean, max_line_len: number,
     *     optimization: number, silent: boolean, verbose: boolean, lint: boolean, paths: string[],
     *     color: boolean, strictImports: boolean, rootpath: string, relativeUrls: boolean,
     *     ieCompat: boolean, strictMath: boolean, strictUnits: boolean, filename: string}}
     */
    var parserOptions;

    /**
     * Returns the file name without the extension.
     * @param fullFileName {string} full file name with the extension
     * @returns {string} file name without the extension
     */
    function getFileName(fullFileName) {
        return fullFileName.substr(0, fullFileName.lastIndexOf("."));
    }

    /**
     * Closes the specified file without throwing any exceptions.
     * @param file {Object} file to be closed
     */
    function closeQuietly(file) {
        try {
            file.close();
        } catch (e) {
            log.error(e);
        }
    }

    /**
     * File reading function for the LESS parser.
     * @param file {string} file path
     * @param currentFileInfo {Object}
     * @param callback {Function}
     * @param env {Object}
     * @param lookupTable {LookupTable} lookup table
     */
    function fileLoader(file, currentFileInfo, callback, env, lookupTable) {
        // Adopted from https://github.com/less/less.js/blob/v1.7.5/lib/less/rhino.js#L89
        var lessFilePath, lessFile;
        if (currentFileInfo && currentFileInfo.currentDirectory && (file.charAt(0) != "/")) {
            // File path does not start with '/', indicates a relative path.
            lessFilePath = lessLib.modules.path.join(currentFileInfo.currentDirectory, file);
            lessFile = new File(lessFilePath);
        }
        if (/{{.+}}/.test(file)) {
            // File path has '{{uiComponentFullName}}'.
            var startIndex = file.indexOf("{{");
            var endIndex = file.indexOf("}}", (startIndex + 2));
            var uiComponentFullName = file.substr((startIndex + 2), (endIndex - 2));
            var uiComponent = lookupTable.uiComponents[uiComponentFullName];
            if (!uiComponent) {
                callback({
                    type: 'UI Component',
                    message: "UI Component '" + uiComponentFullName
                             + "' mentioned in file '" + file + "' cannot be found."
                });
                return;
            }
            var relativePath = constants.DIRECTORY_APP_UNIT_PUBLIC + file.substr(endIndex + 2);
            lessFile = utils.getFileInUiComponent(uiComponent, relativePath);
            if (!lessFile) {
                callback({
                    type: 'File',
                    message: "File '" + relativePath + "' cannot be found in UI Component '"
                             + uiComponent.fullName + "'."
                });
                return;
            }
            lessFilePath = lessFile.getPath();
        }
        if (!lessFilePath) {
            lessFilePath = file;
            lessFile = new File(file);
        }

        var path = lessLib.modules.path.dirname(lessFilePath);
        var newFileInfo = {
            currentDirectory: path,
            filename: lessFilePath
        };
        if (currentFileInfo) {
            newFileInfo.entryPath = currentFileInfo.entryPath;
            newFileInfo.rootpath = currentFileInfo.rootpath;
            newFileInfo.rootFilename = currentFileInfo.rootFilename;
            newFileInfo.relativeUrls = currentFileInfo.relativeUrls;
        } else {
            newFileInfo.entryPath = path;
            newFileInfo.rootpath = lessLib.rootpath || path;
            newFileInfo.rootFilename = lessFilePath;
            newFileInfo.relativeUrls = env.relativeUrls;
        }
        var j = file.lastIndexOf('/');
        if (newFileInfo.relativeUrls && !/^(?:[a-z-]+:|\/)/.test(file) && j != -1) {
            var relativeSubDirectory = file.slice(0, j + 1);
            // append (sub|sup) directory  path of imported file
            newFileInfo.rootpath = newFileInfo.rootpath + relativeSubDirectory;
        }

        var lessFileContent;
        try {
            lessFile.open('r');
            lessFileContent = lessFile.readAll();
        } catch (e) {
            log.error(e.message, e);
            callback({
                type: 'File',
                message: "Cannot read '" + lessFilePath + "' file."
            });
            return;
        } finally {
            closeQuietly(lessFile);
        }

        try {
            callback(null, lessFileContent, lessFilePath, newFileInfo, {lastModified: 0});
        } catch (e) {
            callback(e, null, lessFilePath);
        }
    }

    /**
     * Initializes and returns the LESS parser.
     * @param lessFilePath {string} file path of the LESS file
     * @param lookupTable {LookupTable} lookup table
     * @returns {Object} LESS parser
     */
    function getLessParser(lessFilePath, lookupTable) {
        if (lessLib) {
            // Update LESS parse options.
            parserOptions.filename = lessFilePath;
        } else {
            // Initialize LESS parser.
            lessLib = require("/lib/modules/less/less-rhino-1.7.5.js").less;
            lessLib.Parser.fileLoader = function (file, currentFileInfo, callback, env) {
                fileLoader(file, currentFileInfo, callback, env, lookupTable);
            };
            // Adopted from https://github.com/less/less.js/blob/v1.7.5/lib/less/rhino.js#L149
            parserOptions = {
                depends: false,
                compress: false,
                cleancss: false,
                max_line_len: -1.0,
                optimization: 1.0,
                silent: false,
                verbose: false,
                lint: false,
                paths: [],
                color: true,
                strictImports: false,
                rootpath: "",
                relativeUrls: false,
                ieCompat: true,
                strictMath: false,
                strictUnits: false,
                filename: lessFilePath
            };
        }
        return lessLib.Parser(parserOptions);
    }

    /**
     * Renders the specified LESS resource.
     * @param lessResource {StaticResource} LESS resource to be rendered
     * @param isCachingEnabled {boolean} whether caching is enabled or not
     * @param lookupTable {LookupTable} lookup table
     * @returns {{success: boolean, message: string, cssFile: Object}}
     */
    render = function (lessResource, isCachingEnabled, lookupTable) {
        var lessFile = lessResource.file;
        var uiComponent = lessResource.provider;
        // cached CSS file name pattern: {uiComponentFullName}_{cssFileName}.css
        var cachedFilePath = [constants.DIRECTORY_CACHE, "/", uiComponent.fullName, "_",
                              getFileName(lessFile.getName()), ".css"].join("");
        var cachedFile = new File(cachedFilePath);
        var rv = {success: true, message: "Success", cssFile: cachedFile};

        if (isCachingEnabled && cachedFile.isExists() &&
            (parseInt(cachedFile.getLastModified()) > parseInt(lessFile.getLastModified()))) {
            // Caching is enabled AND cached file exists AND cached file is newer than the LESS
            // file. Hence just return the cached file.
            return rv;
        }
        var lessFileContent;
        try {
            lessFile.open("r");
            lessFileContent = lessFile.readAll();
        } catch (e) {
            rv.success = false;
            rv.message = "Cannot read LESS file '" + options.filename + ".";
            rv.cssFile = null;
            log.error(e);
            return rv;
        } finally {
            closeQuietly(lessFile);
        }
        var callback = function (error, root) {
            if (error) {
                // something went wrong when processing the LESS file
                rv.success = false;
                rv.message = "Failed to process '" + lessFile.getPath() + "' file due to '"
                             + error.message + "' happened in file '" + error.filename
                             + "' at line " + error.line + ".";
                rv.cssFile = null;
                return;
            }

            var result = root.toCSS(parserOptions);
            try {
                cachedFile.open("w");
                cachedFile.write(result);
            } catch (e) {
                rv.success = false;
                rv.message = "Cannot write to cached CSS file '" + cachedFilePath + ".";
                rv.cssFile = null;
            } finally {
                closeQuietly(cachedFile);
            }
        };
        getLessParser(lessFile.getPath(), lookupTable).parse(lessFileContent, callback);
        return rv;
    };
})();
