/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * This module contains user and roles related functionality.
 */
var userModule = function () {
    var log = new Log("/app/modules/business-controllers/user.js");

    var constants = require("/app/modules/constants.js");
    var utility = require("/app/modules/utility.js")["utility"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var serviceInvokers = require("/app/modules/oauth/token-protected-service-invokers.js")["invokers"];

    /* Initializing user manager */
    var carbon = require("carbon");
    var url = carbon.server.address("https") + "/admin/services";
    var server = new carbon.server.Server(url);

    var publicMethods = {};
    var privateMethods = {};

    /**
     * Get the carbon user object from the session. If not found - it will throw a user not found error.
     * @returns {object} carbon user object
     */
    publicMethods.getCarbonUser = function () {
        var carbon = require("carbon");
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        return carbonUser;
    };

    /**
     * Only GET method is implemented for now since there are no other type of methods used this method.
     * @param url - URL to call the backend without the host
     * @param method - HTTP Method (GET, POST)
     * @returns An object with 'status': 'success'|'error', 'content': {}
     */
    privateMethods.callBackend = function (url, method) {
        if (constants["HTTP_GET"] == method) {
            return serviceInvokers.XMLHttp.get(url,
                function (backendResponse) {
                    var response = {};
                    response.content = backendResponse.responseText;
                    if (backendResponse.status == 200) {
                        response.status = "success";
                    } else if (backendResponse.status == 400 || backendResponse.status == 401 ||
                        backendResponse.status == 404 || backendResponse.status == 500) {
                        response.status = "error";
                    }
                    return response;
                }
            );
        } else {
            log.error("Runtime error : This method only support HTTP GET requests.");
        }
    };

    /**
     * Build default user claims.
     *
     * @param firstname First name of the user
     * @param lastname Last name of the user
     * @param emailAddress Email address of the user
     *
     * @returns {Object} Default user claims to be provided
     */
    privateMethods.buildDefaultUserClaims = function (firstname, lastname, emailAddress) {
        var defaultUserClaims = {
            "http://wso2.org/claims/givenname": firstname,
            "http://wso2.org/claims/lastname": lastname,
            "http://wso2.org/claims/emailaddress": emailAddress
        };
        if (log.isDebugEnabled()) {
            log.debug("ClaimMap created for new user : " + stringify(defaultUserClaims));
        }
        return defaultUserClaims;
    };

    /**
     * Register user to dc-user-store.
     *
     * @param username Username of the user
     * @param firstname First name of the user
     * @param lastname Last name of the user
     * @param emailAddress Email address of the user
     * @param password Password of the user
     * @param userRoles Roles assigned to the user
     *
     * @returns {number} HTTP Status code 201 if succeeded, 409 if user already exists
     */
    publicMethods.registerUser = function (username, firstname, lastname, emailAddress, password, userRoles) {
        var carbon = require('carbon');
        var tenantId = carbon.server.tenantId();
        var url = carbon.server.address('https') + "/admin/services";
        var server = new carbon.server.Server(url);
        var userManager = new carbon.user.UserManager(server, tenantId);

        try {
            if (userManager.userExists(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("A user with name '" + username + "' already exists.");
                }
                // http status code 409 refers to - conflict.
                return constants.HTTP_CONFLICT;
            } else {
                var defaultUserClaims = privateMethods.buildDefaultUserClaims(firstname, lastname, emailAddress);
                userManager.addUser(username, password, userRoles, defaultUserClaims, "default");
                if (log.isDebugEnabled()) {
                    log.debug("A new user with name '" + username + "' was created.");
                }
                // http status code 201 refers to - created.
                return constants.HTTP_CREATED;
            }
        } catch (e) {
            throw e;
        }
    };

    /*
     @Updated
     */
    publicMethods.getUsers = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/users?offset=0&limit=100";
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content).users;
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     Get users count from backend services.
     */
    publicMethods.getUsersCount = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/users/count";
            return serviceInvokers.XMLHttp.get(
                url, function (responsePayload) {
                    return parse(responsePayload["responseText"])["count"];
                },
                function (responsePayload) {
                    log.error(responsePayload["responseText"]);
                    return -1;
                }
            );
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Return a User object from the backend by calling the JAX-RS
     * @param username
     * @returns {object} a response object with status and content on success.
     */
    publicMethods.getUser = function (username) {
        var carbonUser = publicMethods.getCarbonUser();
        var domain;
        if (username.indexOf('/') > 0) {
            domain = username.substr(0, username.indexOf('/'));
            username = username.substr(username.indexOf('/') + 1);
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/users/" +
                encodeURIComponent(username);
            if (domain) {
                url += '?domain=' + encodeURIComponent(domain);
            }
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            response["content"] = parse(response.content);
            response["userDomain"] = carbonUser.domain;
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Returns a set of roles assigned to a particular user
     * @param username
     * @returns {object} a response object with status and content on success.
     */
    publicMethods.getRolesByUsername = function (username) {
        var carbonUser = publicMethods.getCarbonUser();
        var domain;
        if (username.indexOf('/') > 0) {
            domain = username.substr(0, username.indexOf('/'));
            username = username.substr(username.indexOf('/') + 1);
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/users/" +
                encodeURIComponent(username) + "/roles";
            if (domain) {
                url += '?domain=' + encodeURIComponent(domain);
            }
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content).roles;
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     @NewlyAdded
     */
    publicMethods.getUsersByUsername = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + "/mdm-admin/users/users-by-username";
            return privateMethods.callBackend(url, constants["HTTP_GET"]);
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     @Updated
     */
    /**
     * Get User Roles from user store (Internal roles not included).
     */
    publicMethods.getRoles = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                "/roles?offset=0&limit=100&user-store=all";
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content).roles;
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Get User Roles count from user store (Internal roles not included).
     */
    publicMethods.getRolesCount = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                "/roles?offset=0&limit=1&user-store=all";
            return serviceInvokers.XMLHttp.get(
                url, function (responsePayload) {
                    return parse(responsePayload["responseText"])["count"];
                },
                function (responsePayload) {
                    log.error(responsePayload["responseText"]);
                    return -1;
                }
            );
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /*
     @Updated
     */
    /**
     * Get User Roles from user store (Internal roles not included).
     * @returns {object} a response object with status and content on success.
     */
    publicMethods.getRolesByUserStore = function (userStore) {
        userStore = userStore ? userStore : "all";
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                "/roles?user-store=" + encodeURIComponent(userStore) + "&limit=100";
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content).roles;
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Get Platforms.
     * @deprecated moved this device module under getDeviceTypes.
     */
    //TODO Move this piece of logic out of user.js to somewhere else appropriate.
    publicMethods.getPlatforms = function () {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] + "/device-types";
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content);
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Get role
     */
    publicMethods.getRole = function (roleName) {
        var carbonUser = session.get(constants["USER_SESSION_KEY"]);
        var utility = require("/app/modules/utility.js")["utility"];
        var userStore;
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants["ERRORS"]["USER_NOT_FOUND"];
        }
        try {
            utility.startTenantFlow(carbonUser);
            if (roleName.indexOf('/') > 0) {
                userStore = roleName.substr(0, roleName.indexOf('/'));
                roleName = roleName.substr(roleName.indexOf('/') + 1);
            }
            var url = devicemgtProps["httpsURL"] + devicemgtProps["backendRestEndpoints"]["deviceMgt"] +
                "/roles/" + encodeURIComponent(roleName);
            if (userStore) {
                url += "?user-store=" + encodeURIComponent(userStore);
            }
            var response = privateMethods.callBackend(url, constants["HTTP_GET"]);
            if (response.status == "success") {
                response.content = parse(response.content);
            }
            return response;
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    /**
     * Authenticate a user when he or she attempts to login to MDM.
     *
     * @param username Username of the user
     * @param password Password of the user
     * @param successCallback Function to be called at the event of successful authentication
     * @param failureCallback Function to be called at the event of failed authentication
     */
    publicMethods.login = function (username, password, successCallback, failureCallback) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        try {
            // check if the user is an authenticated user.
            var isAuthenticated = carbonServer.authenticate(username, password);
            if (!isAuthenticated) {
                failureCallback("authentication");
                return;
            }
            var tenantUser = carbonModule.server.tenantUser(username);
            var isAuthorizedToLogin = privateMethods.isAuthorizedToLogin(tenantUser);
            if (!isAuthorizedToLogin) {
                failureCallback("authorization");
                return;
            }
            session.put(constants.USER_SESSION_KEY, tenantUser);
            successCallback(tenantUser);
        } catch (e) {
            throw e;
        }
    };

    publicMethods.logout = function (successCallback) {
        session.invalidate();
        successCallback();
    };

    publicMethods.isAuthorized = function (permission) {
        var carbon = require("carbon");
        var carbonServer = application.get("carbonServer");
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        var utility = require('/app/modules/utility.js').utility;
        if (!carbonUser) {
            log.error("User object was not found in the session");
            response.sendError(401, constants.ERRORS.USER_NOT_FOUND);
            exit();
        }

        try {
            utility.startTenantFlow(carbonUser);
            var tenantId = carbon.server.tenantId();
            var userManager = new carbon.user.UserManager(server, tenantId);
            var user = new carbon.user.User(userManager, carbonUser.username);
            return user.isAuthorized(permission, "ui.execute");
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    privateMethods.isAuthorizedToLogin = function(carbonUser) {
        var utility = require('/app/modules/utility.js').utility;
        try {
            utility.startTenantFlow(carbonUser);
            var tenantId = carbon.server.tenantId();
            var userManager = new carbon.user.UserManager(server, tenantId);
            var user = new carbon.user.User(userManager, carbonUser.username);
            return user.isAuthorized("/permission/admin/login", "ui.execute");
        } catch (e) {
            throw e;
        } finally {
            utility.endTenantFlow();
        }
    };

    publicMethods.getUIPermissions = function () {
        var permissions = {};
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/devices/any-device")) {
            permissions["LIST_DEVICES"] = true;
            permissions["LIST_OWN_DEVICES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/devices/owning-device")) {
            permissions["LIST_OWN_DEVICES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/admin/groups/view")) {
            permissions["LIST_ALL_GROUPS"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/view")) {
            permissions["LIST_GROUPS"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/users/list")) {
            permissions["LIST_USERS"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/roles/list")) {
            permissions["LIST_ROLES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/policies/list")) {
            permissions["LIST_ALL_POLICIES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/user/policies/list")) {
            permissions["LIST_POLICIES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/devices/enroll")) {
            permissions["ADD_DEVICE"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/add")) {
            permissions["ADD_GROUP"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/users/add")) {
            permissions["ADD_USER"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/devices/add")) {
            permissions["ADD_GROUP_DEVICES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/devices/remove")) {
            permissions["REMOVE_GROUP_DEVICES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/devices/view")) {
            permissions["VIEW_GROUP_DEVICES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/roles/view")) {
            permissions["VIEW_GROUP_ROLES"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/update")) {
            permissions["UPDATE_GROUP"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/share")) {
            permissions["SHARE_GROUP"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/users/remove")) {
            permissions["REMOVE_USER"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/groups/remove")) {
            permissions["REMOVE_GROUP"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/roles/add")) {
            permissions["ADD_ROLE"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/policies/add")) {
            permissions["ADD_ADMIN_POLICY"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/user/policies/add")) {
            permissions["ADD_POLICY"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/policies/priority")) {
            permissions["CHANGE_POLICY_PRIORITY"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/dashboard/view")) {
            permissions["VIEW_DASHBOARD"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/platform-configs/view")) {
            permissions["TENANT_CONFIGURATION"] = true;
        }
        if (publicMethods.isAuthorized("/permission/admin/device-mgt/devices/change-status")) {
            permissions["CHANGE_DEVICE_STATUS"] = true;
        }

        return permissions;
    };

    /**
     * Add new role with permissions.
     *
     * @param roleName    Name of the role
     * @param users       List of users to assign the role
     * @param permissions List of permissions
     */
    publicMethods.addRole = function (roleName, users, permissions) {
        var carbon = require('carbon');
        var tenantId = carbon.server.tenantId();
        var url = carbon.server.address('https') + "/admin/services";
        var server = new carbon.server.Server(url);
        var userManager = new carbon.user.UserManager(server, tenantId);
        try {
            if (!userManager.roleExists(roleName)) {
                userManager.addRole(roleName, users, permissions);
            } else {
                log.info("Role exist with name: " + roleName);
            }
        } catch (e) {
            throw e;
        }
    };

    publicMethods.addPermissions = function (permissionList, path, init) {
        var registry, carbon = require("carbon");
        var carbonServer = application.get("carbonServer");
        var utility = require('/app/modules/utility.js').utility;
        var options = {system: true};
        if (init == "login") {
            try {
                var carbonUser = session.get(constants.USER_SESSION_KEY);
                if (!carbonUser) {
                    log.error("User object was not found in the session");
                    throw constants.ERRORS.USER_NOT_FOUND;
                }
                utility.startTenantFlow(carbonUser);
                var tenantId = carbon.server.tenantId();
                if (carbonUser) {
                    options.tenantId = tenantId;
                }
                registry = new carbon.registry.Registry(carbonServer, options);
                var i, permission, resource;
                for (i = 0; i < permissionList.length; i++) {
                    permission = permissionList[i];
                    resource = {
                        collection: true,
                        name: permission.name,
                        properties: {
                            name: permission.name
                        }
                    };
                    if (path != "") {
                        registry.put("/_system/governance/permission/admin/" + path + "/" + permission.key, resource);
                    } else {
                        registry.put("/_system/governance/permission/admin/" + permission.key, resource);
                    }
                }
            } catch (e) {
                throw e;
            } finally {
                utility.endTenantFlow();
            }
        } else {
            registry = new carbon.registry.Registry(carbonServer, options);
            var i, permission, resource;
            for (i = 0; i < permissionList.length; i++) {
                permission = permissionList[i];
                resource = {
                    collection: true,
                    name: permission.name,
                    properties: {
                        name: permission.name
                    }
                };
                if (path != "") {
                    registry.put("/_system/governance/permission/admin/" + path + "/" + permission.key, resource);
                } else {
                    registry.put("/_system/governance/permission/admin/" + permission.key, resource);
                }
            }
        }
    };

    /**
     * Private method to be used by addUser() to
     * retrieve secondary user stores.
     * This needs Authentication since the method access admin services.
     *
     * @returns Array of secondary user stores.
     */
    publicMethods.getSecondaryUserStores = function () {
        var returnVal = [];
        // To call the userstore admin service, user needs to have admin permission
        if (publicMethods.isAuthorized("/permission/admin")) {
            var endpoint = devicemgtProps["adminService"] + constants["USER_STORE_CONFIG_ADMIN_SERVICE_END_POINT"];
            var wsPayload = "<xsd:getSecondaryRealmConfigurations  xmlns:xsd='http://org.apache.axis2/xsd'/>";
            serviceInvokers.WS.soapRequest(
                "urn:getSecondaryRealmConfigurations",
                wsPayload,
                endpoint,
                function (wsResponse) {
                    var domainIDs = stringify(wsResponse. * ::['return']. * ::domainId.text());
                    if (domainIDs != "\"\"") {
                        var regExpForSearch = new RegExp(constants["USER_STORES_NOISY_CHAR"], "g");
                        domainIDs = domainIDs.replace(regExpForSearch, "");
                        returnVal = domainIDs.split(constants["USER_STORES_SPLITTING_CHAR"]);
                    }
                }, function (e) {
                    log.error("Error retrieving secondary user stores", e);
                },
                constants["SOAP_VERSION"]);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("User does not have admin permission to get the secondary user store details.");
            }
        }
        return returnVal;
    };

    return publicMethods;
}();
