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
 * Resource
 * @param type {string}
 * @param provider {UIComponent}
 * @param path {string}
 * @param combine {boolean}
 * @constructor
 */
function Resource(type, provider, path, combine) {
    this.type = type;
    this.provider = provider;
    this.path = path;
    this.combine = combine;
}

/**
 * ZoneContent
 * @param zoneName {string}
 * @param provider {UIComponent}
 * @constructor
 */
function ZoneContent(zoneName, provider) {
    this.zoneName = zoneName;
    this.provider = provider;
    /** @type {boolean} */
    this.isOverridden = false;
    /** @type {string[]} */
    this.buffer = [];
    /** @type {Object.<string, ZoneContent[]>} */
    this.subZoneContents = null;
}

/**
 *
 * @memberOf ZoneContent
 * @param content {string}
 */
ZoneContent.prototype.addContent = function (content) {
    this.buffer.push(content.trim());
};

/**
 *
 * @memberOf ZoneContent
 * @return {string}
 */
ZoneContent.prototype.getContent = function () {
    return this.buffer.reverse().join("");
};

/**
 *
 * @memberOf ZoneContent
 * @param subZoneName {string}
 * @param subZoneContent {ZoneContent}
 */
ZoneContent.prototype.addSubZoneContent = function (subZoneName, subZoneContent) {
    var subZoneContents = this.subZoneContents;
    if (subZoneContents) {
        var zoneContentsOfSubZone = subZoneContents[subZoneName];
        if (zoneContentsOfSubZone) {
            zoneContentsOfSubZone.push(subZoneContent);
        } else {
            subZoneContents[subZoneName] = [subZoneContent];
        }
    } else {
        subZoneContents = {};
        subZoneContents[subZoneName] = [subZoneContent];
        this.subZoneContents = subZoneContents;
    }
};

/**
 *
 * @memberOf ZoneContent
 * @param subZoneName {string}
 * @returns {?ZoneContent[]}
 */
ZoneContent.prototype.getSubZoneContents = function (subZoneName) {
    var subZoneContents = this.subZoneContents;
    if (subZoneContents) {
        return subZoneContents[subZoneName];
    } else {
        return null;
    }
};

/**
 *
 * @memberOf ZoneContent
 * @return {boolean}
 */
ZoneContent.prototype.hasSubZones = function () {
    return (this.subZoneContents) ? true : false;
};

/**
 * Zone
 * @param name {string}
 * @constructor
 */
function Zone(name) {
    this.name = name;
    /** @type {Object.<string, Resource[]>} */
    this.resources = null;
    /** @type {Object.<string, ZoneContent[]>} */
    this.contents = {};
}

/**
 *
 * @memberOf Zone
 * @param type {string} type of the resource
 * @param provider {UIComponent}
 * @param path {string} relative path of the resource
 * @param combine {boolean}
 * @returns {boolean}
 */
Zone.prototype.addResource = function (type, provider, path, combine) {
    var resources = this.resources;
    if (resources) {
        var resourcesOfType = resources[type];
        if (resourcesOfType) {
            var numberOfResources = resourcesOfType.length;
            for (var i = 0; i < numberOfResources; i++) {
                if (resourcesOfType[i].path == path) {
                    // 'path' already exists
                    return false;
                }
            }
            resourcesOfType.push(new Resource(type, provider, path, combine));
        } else {
            resources[type] = [new Resource(type, provider, path, combine)];
        }
    } else {
        resources = {};
        resources[type] = [new Resource(type, provider, path, combine)];
        this.resources = resources;
    }
    return true;
};

/**
 *
 * @memberOf Zone
 * @param type {string} resource type
 * @returns {?Resource[]}
 */
Zone.prototype.getResources = function (type) {
    var resources = this.resources;
    if (resources) {
        return resources[type];
    } else {
        return null;
    }
};

/**
 *
 * @memberOf Zone
 * @returns {boolean}
 */
Zone.prototype.hasResources = function () {
    return (this.resources) ? true : false;
};

/**
 *
 * @memberOf Zone
 * @param zoneContent {ZoneContent}
 */
Zone.prototype.addContent = function (zoneContent) {
    var contents = this.contents;
    var contentProviderFullName = zoneContent.provider.fullName;
    var contentsOfProvider = contents[contentProviderFullName];
    if (contentsOfProvider) {
        contentsOfProvider.push(zoneContent);
    } else {
        contents[contentProviderFullName] = [zoneContent];
    }
};

/**
 *
 * @memberOf Zone
 * @param providerFullName {string}
 * @returns {?ZoneContent[]}
 */
Zone.prototype.getContents = function (providerFullName) {
    return this.contents[providerFullName];
};

/**
 *
 * @memberOf Zone
 * @param providerFullName {string}
 * @param index {number}
 * @returns {?ZoneContent}
 */
Zone.prototype.getContent = function (providerFullName, index) {
    var contents = this.contents;
    var contentsOfProvider = contents[providerFullName];
    if (contentsOfProvider) {
        return contentsOfProvider[index];
    }
    return null;
};

/**
 *
 * @memberOf Zone
 * @return {UIComponent[]}
 */
Zone.prototype.getContentProviders = function () {
    var contents = this.contents;
    var rv = [];
    for (var providerFullName in contents) {
        if (contents.hasOwnProperty(providerFullName)) {
            rv.push(contents[providerFullName][0].provider);
        }
    }
    return rv;
};
