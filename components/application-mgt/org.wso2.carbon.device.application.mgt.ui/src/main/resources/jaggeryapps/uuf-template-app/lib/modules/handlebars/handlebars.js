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
    var log = new Log("handlebars-module");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;
    var parseBoolean = utils.parseBoolean;
    var getFurthestChild = utils.getFurthestChild;

    var dataStructures = require("/lib/modules/handlebars/data-structures.js");
    var Zone = dataStructures.Zone;
    var ZoneContent = dataStructures.ZoneContent;

    /** @type {UIComponent[]} */
    var uiComponentsStack = [];
    /** @type {ZoneContent[]} */
    var zoneContentsStack = [];
    /** @type {ZoneContent[]} */
    var defZoneContentsStack = [];
    /** @type {Object.<string, Zone>} */
    var mainZones = {};
    /** @type {RenderingContext} */
    var renderingContext;
    /** @type {LookupTable} */
    var lookupTable;
    /** @type {{registerHelper: function, SafeString: function, compile: function}} */
    var handlebarsEnvironment;

    /**
     * Compares two UI Components based on their 'index' values.
     * @param a {UIComponent} this UI Component
     * @param b {UIComponent} will be compared against this one
     * @return {number} if a > b then 1; if a < b then -1
     */
    function compareUiComponents(a, b) {
        var aIndex = a.index, bIndex = b.index;
        if (aIndex == bIndex) {
            return 0;
        }
        return (aIndex > bIndex) ? 1 : -1;
    }

    /**
     * Compares two Resources based on their 'provider's.
     * @param a {Resource} this Resource
     * @param b {Resource} will be compared against this one
     * @return {number} if a > b then 1; if a < b then -1
     */
    function compareResources(a, b) {
        return compareUiComponents(a.provider, b.provider);
    }

    /**
     * Read the file in the specified path and returns its content.
     * @param filePath {string} file path
     * @return {?string} content of the file
     */
    function readFile(filePath) {
        var file = new File(filePath);
        try {
            file.open("r");
            return file.readAll();
        } catch (e) {
            log.error(e.message, e);
            return null;
        } finally {
            try {
                file.close();
            } catch (ee) {
                log.error(ee.message, ee);
            }
        }
    }

    /**
     * Returns the currently processing UI Component.
     * @returns {?UIComponent} currently processing UI Component
     */
    function getProcessingUiComponent() {
        var uiComponentsStackSize = uiComponentsStack.length;
        if (uiComponentsStackSize > 0) {
            // Inside an UI Component.
            return uiComponentsStack[uiComponentsStackSize - 1];
        } else {
            // Not inside an UI Component.
            return null;
        }
    }

    /**
     *
     * @param resources {Resource[]} resources
     * @return {string[]}
     */
    function getResourcesPaths(resources) {
        var sortedResources = resources.sort(compareResources);
        var numberOfResources = sortedResources.length;
        var singleResourcesPaths = [];
        var combiningResourcesPaths = [];
        for (var i = 0; i < numberOfResources; i++) {
            var resource = sortedResources[i];
            if (resource.combine) {
                combiningResourcesPaths.push(resource.path);
            } else {
                singleResourcesPaths.push(resource.path);
            }
        }
        if (combiningResourcesPaths.length > 0) {
            var firstResourceType = sortedResources[0].type;
            var extension = (firstResourceType == "less") ? "css" : firstResourceType;
            var crp = combiningResourcesPaths.join(constants.COMBINED_RESOURCES_SEPARATOR);
            singleResourcesPaths.push(crp + constants.COMBINED_RESOURCES_URL_TAIL + extension);
        }
        return singleResourcesPaths;
    }

    /**
     * Whether the specified unit is processable or not.
     * @param unit {UIComponent} unit to be checked
     * @param user {User} current user
     * @return {boolean} <code>true</code> if processable, otherwise <code>false</code>
     */
    function isUnitProcessable(unit, user) {
        if (unit.disabled) {
            // This unit is disabled.
            return false;
        }

        var unitDefinition = unit.definition;
        if (utils.parseBoolean(unitDefinition[constants.UI_COMPONENT_DEFINITION_IS_ANONYMOUS])) {
            // This is an anonymous unit. So no need for an user session or checking permissions.
            return true;
        }

        // This is not an anonymous unit.
        if (user) {
            // An user has logged in.
            var unitPermissions = unitDefinition[constants.UI_COMPONENT_DEFINITION_PERMISSIONS];
            if (unitPermissions && Array.isArray(unitPermissions)) {
                // A permissions array is specified in the unit definition.
                var numberOfUnitPermissions = unitPermissions.length;
                var userPermissionsMap = user.permissions;
                for (var i = 0; i < numberOfUnitPermissions; i++) {
                    if (!userPermissionsMap.hasOwnProperty(unitPermissions[i])) {
                        if (log.isDebugEnabled()) {
                            log.debug("User '" + user.username + "' in domain '" + user.domain
                                      + "' does not have permission '" + unitPermissions[i]
                                      + "' to view unit '" + unit.fullName + "'.");
                        }
                        return false;
                    }
                }
                // User has all permissions.
                return true;
            } else {
                // Permissions are not specified in the unit definition.
                return true;
            }
        } else {
            // Currently no user has logged in.
            return false;
        }
    }

    /**
     * Executes the JS script of the specified UI Component and returns the result.
     * @param uiComponent {UIComponent} UI component to be processed
     * @param scriptContext {Object} script context
     * @returns {Object} return value
     */
    function executeScript(uiComponent, scriptContext) {
        var scriptFunctionName = constants.UI_COMPONENT_JS_FUNCTION_ON_REQUEST;

        // If this UI component has a script file with 'onRequest' function, then get it.
        var componentScriptFilePath = uiComponent.scriptFilePath;
        var scriptFilePath = null;
        if (componentScriptFilePath) {
            var componentScript = require(componentScriptFilePath);
            if (componentScript.hasOwnProperty(scriptFunctionName)) {
                scriptFilePath = componentScriptFilePath;
            }
        }

        // Otherwise, get the script with 'onRequest' function from the nearest parent.
        // Meanwhile construct the 'super' object.
        var superScript = {};
        var currentSuperScript = superScript;
        var parentComponents = uiComponent.parents;
        var numberOfParentComponents = parentComponents.length;
        for (var i = 0; i < numberOfParentComponents; i++) {
            var parentScriptFilePath = parentComponents[i].scriptFilePath;
            if (parentScriptFilePath) {
                var parentScript = require(parentScriptFilePath);
                if (parentScript.hasOwnProperty(scriptFunctionName)) {
                    if (!scriptFilePath) {
                        scriptFilePath = parentScriptFilePath;
                    }
                    currentSuperScript[scriptFunctionName] = parentScript[scriptFunctionName];
                } else {
                    currentSuperScript[scriptFunctionName] = null;
                }
            } else {
                currentSuperScript[scriptFunctionName] = null;
            }
            currentSuperScript.super = {};
            currentSuperScript = currentSuperScript.super;
        }

        if (!scriptFilePath) {
            // No script found.
            return {};
        }

        try {
            var script = require(scriptFilePath);
            script.super = superScript;
            script.getFile = function (relativeFilePath) {
                return utils.getFileInUiComponent(uiComponent, relativeFilePath);
            };
            var rv = script[constants.UI_COMPONENT_JS_FUNCTION_ON_REQUEST](scriptContext);
            return (rv) ? rv : {};
        } catch (e) {
            log.error("An exception thrown when executing the script '" + scriptFilePath + "'.");
            throw e;
        }
    }

    /**
     * 'page' Handlebars helper function.
     * @param mentionedPageFullName {string}
     * @param handlebarsOptions {Object}
     * @return {string} empty string
     */
    function pageHelper(mentionedPageFullName, handlebarsOptions) {
        var mentionedPage = lookupTable.pages[mentionedPageFullName];
        if (!mentionedPage) {
            var msg = "Page '" + mentionedPageFullName + "' does not exists.";
            log.error(msg);
            throw new Error(msg);
        }

        var processingPage = getFurthestChild(mentionedPage);
        if (log.isDebugEnabled() && (mentionedPage.fullName != processingPage.fullName)) {
            log.debug("Page '" + processingPage.fullName + "' is processed for page '"
                      + mentionedPage.fullName + "'.");
        }

        // Backup current 'ZoneContent' stack and set a new stack.
        var prevZoneContentsStack = zoneContentsStack;
        zoneContentsStack = [];
        // Backup current 'defineZoneContents' stack and set a new stack.
        var prevDefZoneContentsStack = defZoneContentsStack;
        defZoneContentsStack = [];

        // Start processing page 'processingPage'.
        uiComponentsStack.push(processingPage);

        // Execute the script and get the template context.
        var appName = "";
        var appContext = renderingContext.app.context;
        var appConf = renderingContext.app.conf;
        var optionsHash = handlebarsOptions.hash;
        var optionsHashParams = optionsHash[constants.HELPER_PARAM_PARAMS];
        var pageParams = (optionsHashParams) ? optionsHashParams : optionsHash;
        var pagePublicUri = appContext + "/" + constants.DIRECTORY_APP_UNIT_PUBLIC + "/"
                            + processingPage.fullName;
        var uriParams = renderingContext.uriParams;
        var user = renderingContext.user;
        var scriptContext = {
            app: {name: appName, context: appContext, conf: appConf},
            page: {params: pageParams, publicUri: pagePublicUri},
            uriParams: uriParams,
            user: user,
            handlebars: handlebarsEnvironment
        };
        var templateContext = executeScript(processingPage, scriptContext);
        // Additional parameters to the template context.
        var templateOptions = {
            data: {
                app: {name: appName, context: appContext, conf: appConf},
                page: {params: pageParams, publicUri: pagePublicUri},
                uriParams: uriParams,
                user: user
            }
        };

        // Get this page's template.
        var pageTemplateFilePath = processingPage.templateFilePath;
        if (pageTemplateFilePath) {
            var pageContent = readFile(pageTemplateFilePath);
            if (!pageContent) {
                var msg = "Cannot read template '" + pageTemplateFilePath + "' of page '"
                          + processingPage.fullName + "'.";
                log.error(msg);
                throw new Error(msg);
            }
            compileTemplate(pageContent)(templateContext, templateOptions);
        }
        // Process parents' templates from nearest to furthest.
        var parentPages = processingPage.parents;
        var numberOfParentPages = parentPages.length;
        for (var i = 0; i < numberOfParentPages; i++) {
            var parentPage = parentPages[i];
            var parentPageTemplateFilePath = parentPage.templateFilePath;
            if (parentPageTemplateFilePath) {
                var parentPageContent = readFile(parentPageTemplateFilePath);
                if (!parentPageContent) {
                    var msg = "Cannot read template '" + parentPageTemplateFilePath + "' of page '"
                              + parentPage.fullName + "'.";
                    log.error(msg);
                    throw new Error(msg);
                }
                compileTemplate(parentPageContent)(templateContext, templateOptions);
            }
        }
        // If has inner HTMl, then process it.
        if (handlebarsOptions.fn) {
            // {{#page "pageName"}} {{#zone "_pushedUnits"}} ... {{/zone}} {{/page}}
            handlebarsOptions.fn(templateContext, templateOptions);
        }

        // Process layout.
        var layoutName = processingPage.definition[constants.PAGE_DEFINITION_LAYOUT];
        var layoutPath = lookupTable.layouts[layoutName].path;
        var layoutContent = readFile(layoutPath);
        if (!layoutContent) {
            var msg = "Cannot read layout '" + layoutName + "' from path '" + layoutPath
                      + "' of page '" + processingPage.fullName + "'.";
            log.error(msg);
            throw new Error(msg);
        }
        var html = compileTemplate(layoutContent)(templateContext, templateOptions);
        // Finished processing page 'processingPage'.

        // Restore 'ZoneContents' stack and 'defineZoneContents' stack.
        zoneContentsStack = prevZoneContentsStack;
        defZoneContentsStack = prevDefZoneContentsStack;

        return html;
    }

    /**
     * 'unit' Handlebars helper function.
     * @param mentionedUnitFullName {string}
     * @param handlebarsOptions {Object}
     * @return {SafeString}
     */
    function unitHelper(mentionedUnitFullName, handlebarsOptions) {
        var mentionedUnit = lookupTable.units[mentionedUnitFullName];
        if (!mentionedUnit) {
            var msg = "Unit '" + mentionedUnitFullName + "' does not exists.";
            log.error(msg);
            throw new Error(msg);
        }

        var processingUnit = getFurthestChild(mentionedUnit);
        var currentUser = renderingContext.user;
        if (!isUnitProcessable(processingUnit, currentUser)) {
            return new handlebarsEnvironment.SafeString("");
        }

        if (log.isDebugEnabled() && (mentionedUnit.fullName != processingUnit.fullName)) {
            log.debug("Unit '" + processingUnit.fullName + "' is processed for unit '"
                      + mentionedUnit.fullName + "'.");
        }

        // Backup current 'ZoneContents' stack and set a new stack.
        var prevZoneContentsStack = zoneContentsStack;
        zoneContentsStack = [];
        // Backup current 'defineZoneContents' stack and set a new stack.
        var prevDefZoneContentsStack = defZoneContentsStack;
        defZoneContentsStack = [];

        // Start processing unit 'processingUnit'.
        uiComponentsStack.push(processingUnit);

        // Execute the script and get the template context.
        var appName = "";
        var appContext = renderingContext.app.context;
        var appConf = renderingContext.app.conf;
        var optionsHash = handlebarsOptions.hash;
        var optionsHashParams = optionsHash[constants.HELPER_PARAM_PARAMS];
        var unitParams = (optionsHashParams) ? optionsHashParams : optionsHash;
        var unitPublicUri = appContext + "/" + constants.DIRECTORY_APP_UNIT_PUBLIC + "/"
                            + processingUnit.fullName;
        var uriParams = renderingContext.uriParams;
        var user = renderingContext.user;
        var scriptContext = {
            app: {name: appName, context: appContext, conf: appConf},
            unit: {params: unitParams, publicUri: unitPublicUri},
            uriParams: uriParams,
            user: user,
            handlebars: handlebarsEnvironment
        };
        var templateContext = executeScript(processingUnit, scriptContext);
        // Additional parameters to the template context.
        var templateOptions = {
            data: {
                app: {name: appName, context: appContext, conf: appConf},
                unit: {params: unitParams, publicUri: unitPublicUri},
                uriParams: uriParams,
                user: user
            }
        };

        var returningHtml = "";
        // Process this unit's template.
        var processingUnitTemplateFilePath = processingUnit.templateFilePath;
        if (processingUnitTemplateFilePath) {
            var unitContent = readFile(processingUnitTemplateFilePath);
            if (!unitContent) {
                var msg = "Cannot read template '" + processingUnitTemplateFilePath + "' of unit '"
                          + processingUnit.fullName + "'.";
                log.error(msg);
                throw new Error(msg);
            }
            var unitHtml = compileTemplate(unitContent)(templateContext, templateOptions).trim();
            if (unitHtml.length > 0) {
                returningHtml = unitHtml;
            }
        }
        // Process parents' templates from nearest to furthest.
        var parentUnits = processingUnit.parents;
        var numberOfParentUnits = parentUnits.length;
        for (var i = 0; i < numberOfParentUnits; i++) {
            var parentUnit = parentUnits[i];
            var parentUnitTemplateFilePath = parentUnit.templateFilePath;
            if (parentUnitTemplateFilePath) {
                var parentUnitContent = readFile(parentUnitTemplateFilePath);
                if (!parentUnitContent) {
                    var msg = "Cannot read template '" + parentUnitTemplateFilePath + "' of unit '"
                              + parentUnit.fullName + "'.";
                    log.error(msg);
                    throw new Error(msg);
                }
                var parentUnitCompiledTemplate = compileTemplate(parentUnitContent);
                var parentUnitHtml = parentUnitCompiledTemplate(templateContext,
                                                                templateOptions).trim();
                if ((returningHtml.length == 0) && (parentUnitHtml.length > 0)) {
                    // Child unit haven't given any "returning" HTML.
                    returningHtml = parentUnitHtml;
                }
            }
        }
        uiComponentsStack.pop();
        // Finished processing unit 'processingUnit'.

        // Restore 'ZoneContents' stack and 'defineZoneContents' stack.
        zoneContentsStack = prevZoneContentsStack;
        defZoneContentsStack = prevDefZoneContentsStack;

        return new handlebarsEnvironment.SafeString(returningHtml);
    }

    /**
     * 'zone' Handlebars helper function.
     * @param zoneName {string}
     * @param handlebarsOptions {Object}
     * @return {string} empty string
     */
    function zoneHelper(zoneName, handlebarsOptions) {
        var contentProvider = getProcessingUiComponent();
        if (!contentProvider) {
            // 'zone' helper is called outside of an UI Component.
            return "";
        }

        var currentZoneContent;
        var zoneContentsStackSize = zoneContentsStack.length;
        if (zoneContentsStackSize == 0) {
            // This is a main-zone.
            var mainZone = mainZones[zoneName];
            if (mainZone) {
                var mainZoneContents = mainZone.getContents(contentProvider.fullName);
                if (mainZoneContents
                    && mainZoneContents[mainZoneContents.length - 1].isOverridden) {
                    // Previously processed child of 'contentProvider' has overridden this main-zone
                    return "";
                }
            } else {
                mainZone = new Zone(zoneName);
                mainZones[zoneName] = mainZone;
            }
            currentZoneContent = new ZoneContent(zoneName, contentProvider);
            mainZone.addContent(currentZoneContent);
        } else {
            // This is a sub-zone.
            // {{#zone "parentZoneName"}} ... {{#zone "subZoneName"}} ... {{/zone}} ... {{/zone}}
            var parentZoneContent = zoneContentsStack[zoneContentsStackSize - 1];
            currentZoneContent = new ZoneContent(zoneName, contentProvider);
            parentZoneContent.addSubZoneContent(zoneName, currentZoneContent);
        }

        var isOverride = parseBoolean(handlebarsOptions.hash[constants.HELPER_PARAM_OVERRIDE],
                                      true);
        zoneContentsStack.push(currentZoneContent);
        currentZoneContent.isOverridden = isOverride;
        currentZoneContent.addContent(handlebarsOptions.fn(this));
        zoneContentsStack.pop();
        return "";
    }

    /**
     * 'resource' Handlebars helper function.
     * @param type {string} resource type
     * @param path {string} resource file path
     * @param handlebarsOptions {Object}
     * @returns {string} empty string
     */
    function resourceHelper(type, path, handlebarsOptions) {
        var resourceProvider = getProcessingUiComponent();
        if (!resourceProvider) {
            // 'resource' helper is called outside of an UI Component.
            throw new Error("'" + type
                            + "' Handlebars helper should be used inside a page or an unit.");
        }
        if (zoneContentsStack.length != 1) {
            throw new Error("'" + type
                            + "' Handlebars helper should be used inside a main-zone.");
        }

        var mainZone = mainZones[zoneContentsStack[0].zoneName];
        var resourcePath = resourceProvider.fullName + "/" + path;
        var isCombine = parseBoolean(handlebarsOptions.hash[constants.HELPER_PARAM_COMBINE], true);
        mainZone.addResource(type, resourceProvider, resourcePath, isCombine);
        return "";
    }

    /**
     * 'defineZone' Handlebars helper function.
     * @param zoneName {string}
     * @param handlebarsOptions {Object}
     * @return {SafeString}
     */
    function defineZoneHelper(zoneName, handlebarsOptions) {
        var zoneHtml, optionsFn = handlebarsOptions.fn;

        var defZoneContentsStackSize = defZoneContentsStack.length;
        if (defZoneContentsStackSize == 0) {
            // This is a main-zone.
            var mainZone = mainZones[zoneName];
            if (mainZone) {
                var mainZoneBuffer = [];
                // First process resources in this main-zone.
                if (mainZone.hasResources()) {
                    var publicUri = renderingContext.app.context + "/"
                                    + constants.DIRECTORY_APP_UNIT_PUBLIC + "/";
                    var resourcesBuffer = [];
                    var cssResources = mainZone.getResources("css");
                    if (cssResources) {
                        var cssResourcesPaths = getResourcesPaths(cssResources);
                        var numberOfCssResourcesPaths = cssResourcesPaths.length;
                        for (var n = 0; n < numberOfCssResourcesPaths; n++) {
                            resourcesBuffer.push('<link href="', publicUri, cssResourcesPaths[n],
                                                 '" rel="stylesheet" type="text/css" />');
                        }
                    }
                    var jsResources = mainZone.getResources("js");
                    if (jsResources) {
                        var jsResourcesPaths = getResourcesPaths(jsResources);
                        var numberOfJsResourcesPaths = jsResourcesPaths.length;
                        for (var m = 0; m < numberOfJsResourcesPaths; m++) {
                            resourcesBuffer.push('<script src="', publicUri, jsResourcesPaths[m],
                                                 '"></script>');
                        }
                    }
                    mainZoneBuffer.push(resourcesBuffer.join(""));
                }

                // Then process HTML contents of this main-zone.
                var isInProtectedScope, childUnitFullName;
                if (handlebarsOptions.hash[constants.HELPER_PARAM_SCOPE] == "protected") {
                    // 'scope' parameter is specified with value 'protected' for this main-zone.
                    var uiComponentsStackSize = uiComponentsStack.length;
                    if (uiComponentsStackSize > 0) {
                        // Now we are inside an unit. Here 'scope' parameter can be used with
                        // 'defineZone' helper for main-zones.
                        isInProtectedScope = true;
                        // When processing a parent-unit, last element of the units stack contains
                        // its child-unit.
                        childUnitFullName = uiComponentsStack[uiComponentsStackSize - 1].fullName;
                    } else {
                        // Not inside an unit. So 'scope' parameter has no effect.
                        isInProtectedScope = false;
                        childUnitFullName = null;
                    }
                }
                var contentProviders = mainZone.getContentProviders().sort(compareUiComponents);
                var numberOfContentProviders = contentProviders.length;
                for (var i = 0; i < numberOfContentProviders; i++) {
                    var contentProviderFullName = contentProviders[i].fullName;
                    if (isInProtectedScope
                        && (childUnitFullName != contentProviderFullName)) {
                        // Scope of this main-zone is 'protected'. So only contents provided by
                        // child units should be processed. Content provider 'contentProviders[i]'
                        // is not a child-unit of the processing parent-unit.
                        continue;
                    }
                    var contentsOfProvider = mainZone.getContents(contentProviderFullName);
                    var numberOfContentsOfProvider = contentsOfProvider.length;
                    var tmpBuffer = [];
                    for (var j = 0; j < numberOfContentsOfProvider; j++) {
                        var contentOfProvider = contentsOfProvider[j];
                        if (isInProtectedScope && contentOfProvider.expired) {
                            // Scope of this zone is 'protected' and content 'contentOfProvider' is
                            // already processed. So do not process it again.
                            continue;
                        }
                        defZoneContentsStack.push(contentOfProvider);
                        if (contentOfProvider.hasSubZones() && optionsFn) {
                            tmpBuffer.push(optionsFn(this));
                        }
                        tmpBuffer.push(contentOfProvider.getContent());
                        if (isInProtectedScope) {
                            contentOfProvider.expired = true;
                            // Scope of this zone is 'protected', so mark this content as 'expired'
                            // since we have finished processing it.
                        }
                        defZoneContentsStack.pop();
                    }
                    mainZoneBuffer.push(tmpBuffer.join(""));
                }
                zoneHtml = mainZoneBuffer.join("");
            } else {
                // No content is given for this main-zone. If has, process inner HTML for default
                // content.
                zoneHtml = (optionsFn) ? optionsFn(this) : "";
            }
        } else {
            // This is a sub-zone.
            // {{#defineZone "A"}} {{#defineZone "B"}} ... {{/defineZone}} {{/defineZone}}
            var parentZoneContent = defZoneContentsStack[defZoneContentsStackSize - 1];
            var zoneContents = parentZoneContent.getSubZoneContents(zoneName);
            if (zoneContents) {
                var numberOfZoneContents = zoneContents.length;
                var subZoneBuffer = [];
                for (var k = 0; k < numberOfZoneContents; k++) {
                    var zoneContent = zoneContents[k];
                    // Here (parentZoneContent.provider == subZoneContent.provider) is true.
                    defZoneContentsStack.push(zoneContent);
                    if (zoneContent.hasSubZones() && optionsFn) {
                        subZoneBuffer.push(optionsFn(this));
                    }
                    subZoneBuffer.push(zoneContent.getContent());
                    defZoneContentsStack.pop();
                }
                zoneHtml = subZoneBuffer.join("");
            } else {
                // No content is given for this sub-zone. If has, process inner HTML for default
                // content.
                zoneHtml = (optionsFn) ? optionsFn(this) : "";
            }
        }

        return new handlebarsEnvironment.SafeString(zoneHtml);
    }

    function compileTemplate(template) {
        // TODO cache compiled templates or pre-compile templates.
        return handlebarsEnvironment.compile(template);
    }

    /**
     * Renders the specified Handlebars template.
     * @param template {string} Handlebars template
     * @param templateContext {Object} Handlebars template context
     * @param rc {RenderingContext} rendering context
     * @param lt {LookupTable} lookup table
     * @returns {string} HTML
     */
    render = function (template, templateContext, rc, lt) {
        renderingContext = rc;
        lookupTable = lt;
        handlebarsEnvironment = require("/lib/modules/handlebars/handlebars-v2.0.0.js").Handlebars;
        handlebarsEnvironment.registerHelper({
            page: pageHelper,
            unit: unitHelper,
            css: function (path, options) {
                return resourceHelper("css", path, options)
            },
            js: function (path, options) {
                return resourceHelper("js", path, options)
            },
            zone: zoneHelper,
            defineZone: defineZoneHelper
        });
        return compileTemplate(template)(templateContext);
    };
})();
