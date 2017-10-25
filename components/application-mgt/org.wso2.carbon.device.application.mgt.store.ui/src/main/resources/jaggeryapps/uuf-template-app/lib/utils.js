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
 * User data.
 * @typedef {{username: string, domain: string, tenantId: string, permissions: Object.<string,
 *     string>}} User
 */

var utils = {};

(function (utils) {
    var log = new Log("utils");
    var constants = require("/lib/constants.js").constants;
    var models = require("/lib/models.js");

    /**
     * Extends the specified child object from the specified parent object.
     * @param child {Object} child object
     * @param parent {Object} parent object
     * @returns {Object} extended child object
     */
    function extend(child, parent) {
        child = child || {};
        for (var propertyName in parent) {
            if (!parent.hasOwnProperty(propertyName)) {
                continue;
            }
            var propertyValue = parent[propertyName];
            if (Array.isArray(propertyValue)) {
                child[propertyName] = propertyValue;
            } else if (typeof propertyValue === 'object') {
                child[propertyName] = extend(child[propertyName], propertyValue);
            } else if (!child.hasOwnProperty(propertyName)) {
                child[propertyName] = propertyValue;
            }
        }
        return child;
    }

    /**
     * Validates the definition object of the specified page.
     * @param page {UIComponent} page to be validated
     * @param layoutsData {Object.<string, Layout>} layouts data
     * @return {{success: boolean, message: string}} validation result
     */
    function validatePageDefinition(page, layoutsData) {
        var pageDefinition = page.definition;
        // mandatory fields
        if (!pageDefinition[constants.UI_COMPONENT_DEFINITION_VERSION]) {
            return {
                success: false,
                message: "Page '" + page.fullName + "' or its parents " + stringify(page.parents)
                         + " do not have a version."
            };
        }
        if (!pageDefinition[constants.PAGE_DEFINITION_URI]) {
            return {
                success: false,
                message: "Page '" + page.fullName + "' or its parents " + stringify(page.parents)
                         + " do not have a URI."
            };
        }
        var layoutFullName = pageDefinition[constants.PAGE_DEFINITION_LAYOUT];
        if (!layoutFullName) {
            return {
                success: false,
                message: "Page '" + page.fullName + "' or its parents " + stringify(page.parents)
                         + " do not have a layout."
            };
        } else if (!layoutsData[layoutFullName]) {
            return {
                success: false,
                message: "Layout '" + layoutFullName + "' of page '" + page.fullName
                         + "' does not exists."
            };
        }
        // optional fields
        // everything is correct
        return {success: true, message: "Valid page definition."};
    }

    /**
     * Validates the definition object of the specified unit.
     * @param unit {UIComponent} unit to be validated
     * @return {{success: boolean, message: string}} validation result
     */
    function validateUnitDefinition(unit) {
        var unitDefinition = unit.definition;
        // mandatory fields
        if (!unitDefinition[constants.UI_COMPONENT_DEFINITION_VERSION]) {
            return {
                success: false,
                message: "Unit '" + unit.fullName + "' or its parents " + stringify(unit.parents)
                         + " do not have a version."
            };
        }
        // optional fields
        var pushedUris = unitDefinition[constants.UNIT_DEFINITION_PUSHED_URIS];
        if (pushedUris && !Array.isArray(pushedUris)) {
            return {
                success: false,
                message: "Pushed URIs of unit '" + unit.fullName
                         + "' should be a string array. Instead found '" + (typeof pushedUris)
                         + "'."
            };
        }
        // everything is correct
        return {success: true, message: "Valid unit definition."};
    }

    /**
     * Compares two raw UI Components.
     * @param a {UIComponent} this UI Component
     * @param b {UIComponent} will be compared against this one
     * @return {number} if a > b then 1; if a < b then -1; if equals then 0
     */
    function compareRawUiComponents(a, b) {
        var tmpIndex = parseInt(a.definition[constants.UI_COMPONENT_DEFINITION_INDEX]);
        var aIndex = isNaN(tmpIndex) ? 1000000 : tmpIndex;
        tmpIndex = parseInt(b.definition[constants.UI_COMPONENT_DEFINITION_INDEX]);
        var bIndex = isNaN(tmpIndex) ? 1000000 : tmpIndex;

        if (aIndex == bIndex) {
            if (aIndex == 1000000) {
                // Neither 'a' nor 'b' specified index in the definition.
                return a.fullName.localeCompare(b.fullName);
            }
            if (a.children.indexOf(b.fullName) >= 0) {
                // 'b' is a child of 'a', hence 'b' should come before 'a'
                return 1;
            } else if (b.children.indexOf(a.fullName) >= 0) {
                // 'a' is a child of 'b', hence 'a' should come before 'b'
                return -1;
            } else {
                // 'a' and 'b' has same index value in their definitions.
                return a.fullName.localeCompare(b.fullName);
            }
        }
        // If 'a' should come after 'b' then return 1, otherwise -1.
        return (aIndex > bIndex) ? 1 : -1;
    }

    /**
     * Returns layout data.
     * @param layoutsDir {string} path to the layouts directory
     * @return {Object.<string, Layout>} layouts data
     */
    function getLayoutsData(layoutsDir) {
        var layoutsData = {};
        var layoutsFiles = new File(layoutsDir).listFiles();
        for (var i = 0; i < layoutsFiles.length; i++) {
            var layoutFile = layoutsFiles[i];
            if (layoutFile.isDirectory()) {
                // This is not a layout, so ignore.
                continue;
            }
            var layoutFileName = layoutFile.getName();
            var index = layoutFileName.lastIndexOf(".");
            if (layoutFileName.substr(index + 1) != "hbs") {
                // This is not a layout .hbs file.
                continue;
            }

            var layoutFullName = layoutFileName.substr(0, index);
            layoutsData[layoutFullName] = new models.Layout(layoutFullName,
                                                            layoutsDir + "/" + layoutFileName);
        }
        return layoutsData;
    }

    /**
     * Returns data of the specified UI components (pages or units).
     * @param componentType {string} UI component type (page or unit)
     * @param componentsPath {string} path to directory where UI components are located
     * @return {{map: Object.<string, UIComponent>, array: UIComponent[]}} UI components' data
     */
    function getUiComponentsData(componentType, componentsPath) {
        /** @type {Object.<string, UIComponent>} */
        var uiComponentsMap = {};
        /** @type {UIComponent[]} */
        var uiComponentsList = [];
        // Traverse all components and gather data
        var componentsDirectories = new File(componentsPath).listFiles();
        var numberOfFiles = componentsDirectories.length;
        for (var i = 0; i < numberOfFiles; i++) {
            var componentDirectory = componentsDirectories[i];
            if (!componentDirectory.isDirectory()) {
                // This is not an UI component, so ignore.
                continue;
            }

            // UI Component name should be in {namespace}.{short_name} format.
            var componentFullName = componentDirectory.getName();
            var componentShortName = componentFullName.substr(componentFullName.lastIndexOf(".")
                                                              + 1);
            if (!componentShortName || (componentShortName.length == 0)) {
                // Invalid name for an UI component
                throw new Error("Name '" + componentFullName + "' of " + componentType
                                + " is invalid. Name of a " + componentType
                                + " should be in {namespace}.{short_name} format.");
            }
            var componentPath = componentsPath + "/" + componentFullName;
            /** @type {UIComponent} */
            var uiComponent = new models.UIComponent();
            uiComponent.fullName = componentFullName;
            uiComponent.shortName = componentShortName;
            uiComponent.path = componentPath;
            uiComponent.type = componentType;
            // UI component's template is read form the <component_short_name>.hbs file.
            var templateFile = new File(componentPath + "/" + componentShortName + ".hbs");
            if (templateFile.isExists() && !templateFile.isDirectory()) {
                uiComponent.templateFilePath = templateFile.getPath();
            }
            // UI component's script is read form the <component_short_name>.js file.
            var scriptFile = new File(componentPath + "/" + componentShortName + ".js");
            if (scriptFile.isExists() && !scriptFile.isDirectory()) {
                uiComponent.scriptFilePath = scriptFile.getPath();
            }
            // UI component's definition is read form the <component_short_name>.json file.
            var definitionFile = new File(componentPath + "/" + componentShortName + ".json");
            if (!definitionFile.isExists() || definitionFile.isDirectory()) {
                throw new Error("Definition file of " + componentType + " '" + componentFullName
                                + "' does not exists.");
            } else {
                uiComponent.definition = require(definitionFile.getPath());
            }

            uiComponentsMap[componentFullName] = uiComponent;
            uiComponentsList.push(uiComponent);
        }

        // Inheritance chaining
        var numberOfComponents = uiComponentsList.length;
        var extendsKey = constants.UI_COMPONENT_DEFINITION_EXTENDS;
        for (i = 0; i < numberOfComponents; i++) {
            var component = uiComponentsList[i];
            var componentFullName = component.fullName;
            var componentParents = component.parents;
            var componentDefinition = component.definition;

            var parentComponentFullName = componentDefinition[extendsKey];
            while (parentComponentFullName) {
                var parentComponent = uiComponentsMap[parentComponentFullName];
                if (!parentComponent) {
                    var immediateChild = (componentParents.length == 0) ? componentFullName :
                                         componentParents[componentParents.length - 1];
                    throw new Error("Parent " + componentType + " '" + parentComponentFullName
                                    + "' of " + componentType + " '" + immediateChild
                                    + "' does not exists.");
                }

                parentComponent.children.push(component);
                componentParents.push(parentComponent);
                componentDefinition = extend(componentDefinition, parentComponent.definition);
                parentComponentFullName = parentComponent.definition[extendsKey];
            }
        }

        // Sorting
        uiComponentsList.sort(compareRawUiComponents);

        return {map: uiComponentsMap, array: uiComponentsList};
    }

    /**
     * Returns the boolean value of the specified object.
     * @param obj {Object} object to be converted to boolean
     * @param {boolean} [defaultValue=false] if <code>obj</code> is <code>null</code> or
     *     <code>undefined</code> then this values is returned
     * @return {boolean} boolean value of the parsed object
     */
    utils.parseBoolean = function (obj, defaultValue) {
        switch (typeof obj) {
            case 'boolean':
                return obj;
            case 'number':
                return (obj > 0);
            case 'string':
                var objLowerCased = obj.toLowerCase();
                return ((objLowerCased == "true") || (objLowerCased == "yes"));
            default:
                return (obj == null) ? ((defaultValue == null) ? false : defaultValue) : true;
        }
    };

    /**
     * Returns UUF configurations.
     * @return {Object} UUF configurations
     */
    utils.getConfigurations = function () {
        var confFile = new File(constants.FILE_UUF_CONF);
        if (!confFile.isExists() || confFile.isDirectory()) {
            throw new Error("Unified UI framework configurations file '" + constants.FILE_UUF_CONF
                            + "' does not exists.");
        }

        var cachedConf = application.get(constants.CACHE_KEY_UUF_CONF);
        var updateCache = false;
        if (cachedConf) {
            var cachedConfFileLMD = parseInt(application.get(constants.CACHE_KEY_UUF_CONF_FILE_LMD));
            var confFileLMD = parseInt(confFile.getLastModified());
            if (confFileLMD > cachedConfFileLMD) {
                updateCache = true;
            }
        } else {
            updateCache = true;
        }

        if (updateCache) {
            var conf = require(constants.FILE_UUF_CONF);
            application.put(constants.CACHE_KEY_UUF_CONF, conf);
            application.put(constants.CACHE_KEY_UUF_CONF_FILE_LMD,
                            String(confFile.getLastModified()));
            return conf;
        } else {
            return cachedConf;
        }
    };

    /**
     * Returns application configurations.
     * @return {Object} application configurations
     */
    utils.getAppConfigurations = function () {
        var appConfFile = new File(constants.FILE_APP_CONF);
        if (!appConfFile.isExists() || appConfFile.isDirectory()) {
            throw new Error("Application configurations file '" + constants.FILE_APP_CONF
                            + "' does not exists.");
        }

        var cachedAppConf = application.get(constants.CACHE_KEY_APP_CONF);
        var updateCache = false;
        if (cachedAppConf) {
            var cachedAppConfFileLMD = parseInt(application.get(constants.CACHE_KEY_APP_CONF_FILE_LMD));
            var appConfFileLMD = parseInt(appConfFile.getLastModified());
            if (appConfFileLMD > cachedAppConfFileLMD) {
                updateCache = true;
            }
        } else {
            updateCache = true;
        }

        if (updateCache) {
            appConfFile.open("r");
            var content = appConfFile.readAll();
            var process = require("process");
            var getProperty = process.getProperty;
            content = content.replace(/\$\{server\.ip}/g, getProperty("carbon.local.ip"));
            content = content.replace(/\$\{server\.http_port}/g, getProperty("carbon.http.port"));
            content = content.replace(/\$\{server\.https_port}/g, getProperty("carbon.https.port"));

            //parsing system params
            var paramPattern = new RegExp("%(.*?)%", "g");
            var out = content;
            while ((matches = paramPattern.exec(content)) !== null) {
                // This is necessary to avoid infinite loops with zero-width matches
                if (matches.index === paramPattern.lastIndex) {
                    paramPattern.lastIndex++;
                }
                if (matches.length == 2) {
                    var property = process.getProperty(matches[1]);
                    if (property) {
                        out = out.replace(new RegExp("%" + matches[1] + "%", "g"), property);
                    }
                }
            }
            content = out;
            
            var appConf = parse(content);
            application.put(constants.CACHE_KEY_APP_CONF, appConf);
            application.put(constants.CACHE_KEY_APP_CONF_FILE_LMD,
                            String(appConfFile.getLastModified()));
            return appConf;
        } else {
            return cachedAppConf;
        }
    };

    /**
     * Returns the lookup table.
     * @param configs {Object} application configurations
     * @return {LookupTable} lookup table
     */
    utils.getLookupTable = function (configs) {
        var isCachingEnabled = utils.parseBoolean(configs[constants.APP_CONF_CACHE_ENABLED]);
        if (isCachingEnabled) {
            var cachedLookupTable = application.get(constants.CACHE_KEY_LOOKUP_TABLE);
            if (cachedLookupTable) {
                return cachedLookupTable;
            }
        }

        // layouts
        var layoutsData = getLayoutsData(constants.DIRECTORY_APP_LAYOUTS);

        /** @type {Object.<string, UIComponent>} */
        var allUiComponents = {};

        // units
        var unitsData = getUiComponentsData("unit", constants.DIRECTORY_APP_UNITS);
        var unitsArray = unitsData.array;
        var numberOfUnits = unitsArray.length;
        /** @type {Object.<string, [string]>} */
        var pushedUnits = {};
        for (var i = 0; i < numberOfUnits; i++) {
            var unit = unitsArray[i];
            unit.index = i;
            var unitDefinition = unit.definition;
            unit.disabled =
                utils.parseBoolean(unitDefinition[constants.UI_COMPONENT_DEFINITION_DISABLED]);
            if (unit.disabled) {
                // This unit is disabled.
                continue;
            }
            allUiComponents[unit.fullName] = unit;

            if (unit.children.length != 0) {
                // This unit is extended by one or more child unit(s).
                continue;
            }
            var validationData = validateUnitDefinition(unit);
            if (!validationData.success) {
                // Invalid unit definition.
                throw new Error(validationData.message);
            }

            var uriPatterns = unitDefinition[constants.UNIT_DEFINITION_PUSHED_URIS];
            if (uriPatterns) {
                var numberOfUriPatterns = uriPatterns.length;
                var unitFullName = unit.fullName;
                for (var n = 0; n < numberOfUriPatterns; n++) {
                    var uriPattern = uriPatterns[n];
                    if (!pushedUnits[uriPattern]) {
                        pushedUnits[uriPattern] = [];
                    }
                    pushedUnits[uriPattern].push(unitFullName);
                }
            }
        }

        // pages
        var pagesData = getUiComponentsData("page", constants.DIRECTORY_APP_PAGES);
        var pagesArray = pagesData.array;
        var numberOfPages = pagesArray.length;
        /** @type {Object.<string, string>} */
        var uriPagesMap = {};
        for (var j = 0; j < numberOfPages; j++) {
            var page = pagesArray[j];
            page.index = i + j;
            var pageDefinition = page.definition;
            page.disabled =
                utils.parseBoolean(pageDefinition[constants.UI_COMPONENT_DEFINITION_DISABLED]);
            if (page.disabled) {
                // This page is disabled.
                continue;
            }
            allUiComponents[page.fullName] = page;

            if (page.children.length != 0) {
                // This page is extended by one or more child page(s).
                continue;
            }
            var validationData = validatePageDefinition(page, layoutsData);
            if (!validationData.success) {
                // Invalid page definition.
                throw new Error(validationData.message);
            }

            var pageUri = pageDefinition[constants.PAGE_DEFINITION_URI];
            if (uriPagesMap[pageUri]) {
                // Some other page is already registered for this URI.
                throw new Error("Cannot register page '" + page.fullName + "' for URI '" + pageUri
                                + "' since page '" + uriPagesMap[pageUri]
                                + "' already registered.");
            }
            uriPagesMap[pageUri] = page.fullName;
        }

        var lookupTable = {
            layouts: layoutsData,
            pages: pagesData.map,
            uriPagesMap: uriPagesMap,
            units: unitsData.map,
            pushedUnits: pushedUnits,
            uiComponents: allUiComponents
        };
        application.put(constants.CACHE_KEY_LOOKUP_TABLE, lookupTable);
        return lookupTable;
    };

    /**
     * Returns the furthest child UI component of the specified UI component.
     * @param parentUiComponent {UIComponent} UI component
     * @return {UIComponent} furthest child
     */
    utils.getFurthestChild = function (parentUiComponent) {
        if (parentUiComponent.children.length == 0) {
            // This UI component has no children.
            return parentUiComponent;
        }

        /** @type {UIComponent} */
        var furthestChild = null;
        var furthestChildDistance = -1;
        var childrenUiComponents = parentUiComponent.children;
        var numberOfChildrenUiComponents = childrenUiComponents.length;
        for (var i = 0; i < numberOfChildrenUiComponents; i++) {
            var currentChild = childrenUiComponents[i];
            // 'currentChild.parents' array contains parent UI components of the 'currentChild' UI
            // component, where first element has the nearest parent and last element has the
            // farthest parent.
            var currentChildDistance = -1;
            var currentChildParents = currentChild.parents;
            var numberOfCurrentChildParents = currentChildParents.length;
            for (var j = 0; j < numberOfCurrentChildParents; j++) {
                if (parentUiComponent.equals(currentChildParents[j])) {
                    currentChildDistance = j;
                    break;
                }
            }
            if (furthestChildDistance < currentChildDistance) {
                // Update 'furthestChild' because 'currentChild' is far away than 'furthestChild'.
                furthestChildDistance = currentChildDistance;
                furthestChild = currentChild;
            } else if (furthestChildDistance == currentChildDistance) {
                // UI component 'furthestChild' and unit 'currentChild' are in the same distance
                // from the 'uiComponent'. Hence, compare those two UI components.
                switch (currentChild.compareTo(furthestChild)) {
                    case 1:
                        // (currentChild > furthestChild)
                        // Update 'furthestChild' because index of the 'currentChild' has a higher
                        // priority than 'furthestChild'.
                        furthestChildDistance = currentChildDistance;
                        furthestChild = currentChild;
                        break;
                    case 0:
                        // (currentChild == furthestChild)
                        // With the current indexing mechanism, same index value for two different
                        // UI components cannot happen. However we log it here as a precaution.
                        log.warn("Child UI component '" + furthestChild.fullName + "' and '"
                                 + currentChild.fullName + "' are in the same distance ("
                                 + currentChildDistance + ") from their parent UI component '"
                                 + parentUiComponent.fullName
                                 + "' was ignored when calculating the furthest child.");
                        break;
                    case -1:
                    // (currentChild < furthestChild) No need update 'furthestChild'.
                }
            } else {
                // (furthestChildDistance > currentChildDistance) No need to update 'furthestChild'.
            }
        }
        return furthestChild;
    };

    /**
     * Returns the file.
     * @param uiComponent {UIComponent} UI component
     * @param relativeFilePath {string} file path
     * @returns {Object} file
     */
    utils.getFileInUiComponent = function (uiComponent, relativeFilePath) {
        if (relativeFilePath.charAt(0) != "/") {
            relativeFilePath = "/" + relativeFilePath;
        }
        var childUiComponent = utils.getFurthestChild(uiComponent);

        var file = new File(childUiComponent.path + relativeFilePath);
        if (file.isExists() && !file.isDirectory()) {
            // Furthest child UI components has the file.
            return file;
        }

        var parentUiComponents = childUiComponent.parents;
        var numberOfParentUiComponents = parentUiComponents.length;
        for (var i = 0; i < numberOfParentUiComponents; i++) {
            var parentUiComponent = parentUiComponents[i];
            var parentFile = new File(parentUiComponent.path + relativeFilePath);
            if (parentFile.isExists() && !parentFile.isDirectory()) {
                // Parent UI Component has the file.
                return parentFile;
            }
        }

        return null;
    };

    /**
     * Returns the current logged-in user.
     * @returns {?User}
     */
    utils.getCurrentUser = function () {
        /** @type {User} */
        var user = session.get(constants.CACHE_KEY_USER);
        if (user && user.username) {
            // load permissions
            return user;
        }
        return null;
    };

    /**
     * Sets the current user.
     * @param username {string} username
     * @param domain {string} domain
     * @param tenantId {string} tenant ID
     */
    utils.setCurrentUser = function (username, domain, tenantId) {
        var carbon = require('carbon');
        var userManager = new carbon.user.UserManager(new carbon.server.Server(), tenantId);
        var permissionRootPath = utils.getAppConfigurations()[constants.APP_CONF_PERMISSION_ROOT];
        if (!permissionRootPath) {
            permissionRootPath = "/";
        }
        var permissions = userManager.getAllowedUIResources(username, permissionRootPath);
        var numberOfPermissions = permissions.length;
        var permissionsMap = {};
        for (var i = 0; i < numberOfPermissions; i++) {
            permissionsMap[permissions[i]] = "ui-execute";
        }
        /** @type {User} */
        var user = {
            username: username,
            domain: domain,
            tenantId: tenantId,
            permissions: permissionsMap
        };
        session.put(constants.CACHE_KEY_USER, user);
    };

    /**
     * Returns web app context path.
     * @param request {Object} HTTP request
     * @returns {string} context path
     */
    utils.getAppContext = function (request) {
        var requestContextPath = request.getContextPath();
        return (requestContextPath == "/") ? "" : requestContextPath;
    };
})(utils);
