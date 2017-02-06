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

var module = {};

(function (module) {
    var log = new Log("auth-module");
    var constants = require("/lib/constants.js").constants;
    var utils = require("/lib/utils.js").utils;

    var OPERATION_LOGIN = "login";
    var OPERATION_LOGOUT = "logout";
    var EVENT_SUCCESS = "success";
    var EVENT_FAIL = "fail";
    var cachedAppConfigs, cachedAuthModuleConfigs, cachedSsoConfigs, cachedLookupTable;

    /**
     * Returns application configurations.
     * @returns {Object} application configurations
     */
    function getAppConfigurations() {
        if (cachedAppConfigs) {
            return cachedAppConfigs;
        }

        return cachedAppConfigs = utils.getAppConfigurations();
    }

    /**
     * Returns the configurations of the 'uuf.auth' module.
     * @return {Object} configurations of the 'uuf.auth' modules
     */
    function getAuthModuleConfigurations() {
        if (cachedAuthModuleConfigs) {
            return cachedAuthModuleConfigs;
        }

        var authModuleConfigs = getAppConfigurations()[constants.APP_CONF_AUTH_MODULE];
        if (authModuleConfigs) {
            cachedAuthModuleConfigs = authModuleConfigs;
        } else {
            log.error("Cannot find User module configurations in application configuration file '"
                + constants.FILE_APP_CONF + "'.");
            cachedAuthModuleConfigs = {};
        }
        return cachedAuthModuleConfigs;
    }

    /**
     * Return login configurations.
     * @param event {?string} on success or on fail
     * @return {Object.<string, string>} SSO configurations
     */
    function getLoginConfigurations(event) {
        var authModuleConfigs = getAuthModuleConfigurations();
        var loginConfigs = authModuleConfigs[constants.APP_CONF_AUTH_MODULE_LOGIN];
        if (loginConfigs) {
            var rv;
            switch (event) {
                case EVENT_SUCCESS:
                    rv = loginConfigs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS];
                    break;
                case EVENT_FAIL:
                    rv = loginConfigs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL];
                    break;
                default:
                    rv = loginConfigs;
            }
            return (rv) ? rv : {};
        } else {
            log.error("Cannot find login configurations in Auth module configurations in "
                + "application configuration file '" + constants.FILE_APP_CONF + "'.");
            return {};
        }
    }

    /**
     * Return logout configurations.
     * @param event {string} on success or on fail
     * @return {Object.<string, string>} SSO configurations
     */
    function getLogoutConfigurations(event) {
        var authModuleConfigs = getAuthModuleConfigurations();
        var logoutConfigs = authModuleConfigs[constants.APP_CONF_AUTH_MODULE_LOGOUT];
        if (logoutConfigs) {
            var rv;
            switch (event) {
                case EVENT_SUCCESS:
                    rv = logoutConfigs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS];
                    break;
                case EVENT_FAIL:
                    rv = logoutConfigs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL];
                    break;
                default:
                    rv = logoutConfigs;
            }
            return (rv) ? rv : {};
        } else {
            log.error("Cannot find logout configurations in Auth module configurations in "
                + "application configuration file '" + constants.FILE_APP_CONF + "'.");
            return {};
        }
    }

    /**
     * Return SSO configurations.
     * @return {Object.<string, string>} SSO configurations
     */
    function getSsoConfigurations() {
        if (cachedSsoConfigs) {
            return cachedSsoConfigs;
        }

        var authModuleConfigs = getAuthModuleConfigurations();
        var ssoConfigs = authModuleConfigs[constants.APP_CONF_AUTH_MODULE_SSO];
        if (ssoConfigs) {
            cachedSsoConfigs = ssoConfigs;
        } else {
            log.error("Cannot find SSO configurations in Auth module configurations in application "
                + "configuration file '" + constants.FILE_APP_CONF + "'.");
            cachedSsoConfigs = {};
        }
        return cachedSsoConfigs;
    }

    /**
     * Returns the lookup table.
     * @returns {LookupTable}
     */
    function getLookupTable() {
        if (cachedLookupTable) {
            return cachedLookupTable;
        }

        return cachedLookupTable = utils.getLookupTable(getAppConfigurations());
    }

    function getRedirectUri(operation, event) {
        var configs, pageFullName;
        if (operation == OPERATION_LOGIN) {
            configs = getLoginConfigurations(event);
            pageFullName = (event == EVENT_SUCCESS) ?
                configs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS_PAGE] :
                configs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL_PAGE];
        } else {
            configs = getLogoutConfigurations(event);
            pageFullName = (event == EVENT_SUCCESS) ?
                configs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS_PAGE] :
                configs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL_PAGE];
        }

        if (pageFullName) {
            var page = getLookupTable().pages[pageFullName];
            if (page) {
                page = utils.getFurthestChild(page);
                if (!page.disabled) {
                    return page.definition[constants.PAGE_DEFINITION_URI];
                }
                log.warn("Page '" + pageFullName + "' mentioned in Auth module configurations in "
                    + "application configuration file '" + constants.FILE_APP_CONF
                    + "' is disabled.");

            } else {
                log.error("Page '" + pageFullName + "' mentioned in Auth module configurations in "
                    + "application configuration file '" + constants.FILE_APP_CONF
                    + "' does not exists.");
            }
        }
        return "/";
    }

    /**
     * Returns the relay state.
     * @param operation {string} either "login" or "logout"
     * @return {string} relay state
     */
    function getRelayState(operation) {
        var paramReferer = request.getParameter(constants.URL_PARAM_REFERER);
        if (paramReferer && (paramReferer.length > 0)) {
            return paramReferer;
        }
        var relayState = request.getParameter("RelayState");
        if (relayState && (relayState.length > 0)) {
            return relayState;
        }
        return getRedirectUri(operation, EVENT_SUCCESS);
    }

    function executeScript(operation, event, argument) {
        var configs, scriptFilePath;
        if (operation == OPERATION_LOGIN) {
            configs = getLoginConfigurations(event);
            scriptFilePath = (event == EVENT_SUCCESS) ?
                configs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS_SCRIPT] :
                configs[constants.APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL_SCRIPT];
        } else {
            configs = getLogoutConfigurations(event);
            scriptFilePath = (event == EVENT_SUCCESS) ?
                configs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS_SCRIPT] :
                configs[constants.APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL_SCRIPT];
        }

        if (!scriptFilePath || (scriptFilePath.length == 0)) {
            return true;
        }
        var scriptFile = new File(scriptFilePath);
        if (!scriptFile.isExists() || scriptFile.isDirectory()) {
            log.error("Script '" + scriptFilePath + "' mentioned in Auth module configurations in "
                + "application configuration file '" + constants.FILE_APP_CONF
                + "' does not exists.");
            return true;
        }

        try {
            var script = require(scriptFilePath);
            var functionName = (event == EVENT_SUCCESS) ? "onSuccess" : "onFail";
            if (script[functionName]) {
                script[functionName](argument);
            }
            return true;
        } catch (e) {
            log.error("An exception thrown when executing the script '" + scriptFilePath + "'.");
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
    }

    function handleEvent(operation, event, scriptArgument) {
        if (!executeScript(operation, event, scriptArgument)) {
            return; // Some error occurred when executing the script.
        }
        var redirectUri;
        if (event == EVENT_SUCCESS) {
            redirectUri = getRelayState(operation);
        } else {
            // event == EVENT_FAIL
            redirectUri = getRedirectUri(operation, EVENT_FAIL) + "?error=" + scriptArgument.message
                + "&" + constants.URL_PARAM_REFERER + "=" + getRelayState(operation);
        }
        response.sendRedirect(encodeURI(module.getAppContext() + redirectUri));
    }

    function getSsoLoginRequestParams() {
        var ssoConfigs = getSsoConfigurations();
        // Identity Provider URL
        var identityProviderUrl = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_IDENTITY_PROVIDER_URL];
        if (!identityProviderUrl || (identityProviderUrl.length == 0)) {
            var msg = "Identity Provider URL is not given in SSO configurations in Auth module "
                + "configurations in application configuration file '"
                + constants.FILE_APP_CONF + "'.";
            log.error(msg);
            response.sendError(500, msg);
            return null;
        }
        // Issuer
        var issuer = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_ISSUER];
        if (!issuer || (issuer.length == 0)) {
            var msg = "Issuer is not given in SSO configurations in Auth module configurations in "
                + "application configuration file '" + constants.FILE_APP_CONF + "'.";
            log.error(msg);
            response.sendError(500, msg);
            return null;
        }
        // SAML authentication request
        var encodedSAMLAuthRequest;
        try {
            encodedSAMLAuthRequest = (require("sso")).client.getEncodedSAMLAuthRequest(issuer);
        } catch (e) {
            log.error("Cannot create SAML login authorization token with issuer '" + issuer + "'.");
            log.error(e.message, e);
            response.sendError(500, e.message);
            return null;
        }

        return {
            identityProviderUrl: identityProviderUrl,
            encodedSAMLAuthRequest: encodedSAMLAuthRequest,
            relayState: getRelayState(OPERATION_LOGIN),
            sessionId: session.getId()
        }
    }

    function getSsoLogoutRequestParams() {
        var ssoConfigs = getSsoConfigurations();
        // Identity Provider URL
        var identityProviderUrl = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_IDENTITY_PROVIDER_URL];
        if (!identityProviderUrl || (identityProviderUrl.length == 0)) {
            var msg = "Identity Provider URL is not given in SSO configurations in Auth module "
                + "configurations in application configuration file '"
                + constants.FILE_APP_CONF + "'.";
            log.error(msg);
            response.sendError(500, msg);
            return null;
        }
        // Session ID, Username, SSO Session Index
        var sessionId = session.getId();
        var ssoSession = getSsoSessions()[sessionId];
        var username = ssoSession.loggedInUser;
        var ssoSessionIndex = (ssoSession.sessionIndex) ? ssoSession.sessionIndex : null;
        // Issuer
        var issuer = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_ISSUER];
        if (!issuer || (issuer.length == 0)) {
            var msg = "Issuer is not given in SSO configurations in Auth module configurations in "
                + "application configuration file '" + constants.FILE_APP_CONF + "'.";
            log.error(msg);
            response.sendError(500, msg);
            return null;
        }
        // SAML authentication request
        var encodedSAMLAuthRequest;
        try {
            var ssoClient = require("sso").client;
            encodedSAMLAuthRequest = ssoClient.getEncodedSAMLLogoutRequest(username,
                ssoSessionIndex, issuer);
        } catch (e) {
            log.error("Cannot create SAML logout authorization token for user '" + username
                + "'  with issuer '" + issuer + "'.");
            log.error(e.message, e);
            response.sendError(500, e.message);
            return null;
        }

        return {
            identityProviderUrl: identityProviderUrl,
            encodedSAMLAuthRequest: encodedSAMLAuthRequest,
            relayState: getRelayState(OPERATION_LOGOUT),
            sessionId: sessionId
        }
    }

    /**
     * Returns SSO sessions map.
     * @return {Object.<string, {sessionId: string, loggedInUser: string, sessionIndex: string,
     *     samlToken: string}>} SSO sessions
     */
    function getSsoSessions() {
        var ssoSessions = application.get(constants.CACHE_KEY_SSO_SESSIONS);
        if (!ssoSessions) {
            ssoSessions = {};
            application.put(constants.CACHE_KEY_SSO_SESSIONS, ssoSessions);
        }
        return ssoSessions;
    }

    /**
     * Returns whether the authentication module is enabled or disabled in the configurations.
     * @return {boolean} if authentication module is enabled <code>true</code>, otherwise
     *     <code>false</code>
     */
    module.isEnabled = function () {
        var authModuleConfigs = getAuthModuleConfigurations();
        return utils.parseBoolean(authModuleConfigs[constants.APP_CONF_AUTH_MODULE_ENABLED]);
    };

    /**
     * Returns whether the Single Sign-on feature is enabled or disabled in the authentication
     * module.
     * @return {boolean} if SSO is enabled <code>true</code>, otherwise <code>false</code>
     */
    module.isSsoEnabled = function () {
        var ssoConfigs = getSsoConfigurations();
        return utils.parseBoolean(ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_ENABLED]);
    };

    /**
     * Returns the current logged-in user.
     * @returns {?User}
     */
    module.getCurrentUser = function () {
        return utils.getCurrentUser();
    };

    /**
     * Retuns the application context path.
     * @returns {string}
     */
    module.getAppContext = function () {
        return utils.getAppContext(request);
    };

    /**
     * Renders the SSO intermediate page which redirects to the identity server.
     * @param operation {string} either "login" or "logout"
     * @param response {Object} HTTP response
     */
    module.renderSsoIntermediatePage = function (operation, response) {
        var requestParams, uri;
        if (operation == OPERATION_LOGIN) {
            requestParams = getSsoLoginRequestParams();
            uri = "/uuf/login";
        } else {
            requestParams = getSsoLogoutRequestParams();
            uri = "/uuf/logout";
        }
        if (!requestParams) {
            return;
        }

        var ssoConfigs = getSsoConfigurations();
        var lookupTable = getLookupTable();
        var renderingContext = {
            app: {
                context: module.getAppContext(),
                conf: getAppConfigurations()
            },
            uri: uri,
            uriParams: {},
            user: module.getCurrentUser()
        };
        var renderer = require("/lib/dynamic-files-renderer.js").renderer;

        var intermediatePageName = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_INTERMEDIATE_PAGE];
        if (intermediatePageName) {
            var intermediatePage = lookupTable.pages[intermediatePageName];
            if (intermediatePage) {
                intermediatePage = utils.getFurthestChild(intermediatePage);
                if (!intermediatePage.disabled) {
                    renderer.renderUiComponent(intermediatePage, requestParams, renderingContext,
                        lookupTable, response);
                    return;
                }
                log.warn("Intermediate page '" + intermediatePageName + " mentioned in Auth module "
                    + "configurations in application configuration file '"
                    + constants.FILE_APP_CONF + "' is disabled.");
            } else {
                log.error("Intermediate page '" + intermediatePageName
                    + " mentioned in Auth module "
                    + "configurations in application configuration file '"
                    + constants.FILE_APP_CONF + "' does not exists.");
            }
        }

        var template;
        var templateFile = new File("/lib/modules/auth/default-sso-intermediate-page.hbs");
        try {
            templateFile.open("r");
            template = templateFile.readAll();
        } catch (e) {
            log.error(e.message, e);
            response.sendError(500, e.message);
            return;
        } finally {
            try {
                templateFile.close();
            } catch (ee) {
                log.error(ee.message, ee);
            }
        }
        renderer.renderTemplate(template, requestParams, renderingContext, lookupTable, response);
    };

    /**
     * SSO Assertion Consumer Service.
     * @param request {Object} HTTP request
     * @param response {Object} HTTP response
     */
    module.ssoAcs = function (request, response) {
        var samlResponse = request.getParameter("SAMLResponse");
        var samlRequest = request.getParameter('SAMLRequest');
        var ssoClient = require("sso").client;
        var samlResponseObj;

        if (samlResponse) {
            try {
                samlResponseObj = ssoClient.getSamlObject(samlResponse);
            } catch (e) {
                log.error(e.message, e);
                response.sendError(500, e.message);
                return;
            }
            if (ssoClient.isLogoutResponse(samlResponseObj)) {
                // This is a logout response.
                module.logout(response);
            } else {
                // This is a login response.
                var ssoConfigs = getSsoConfigurations();
                var CarbonUtils = Packages.org.wso2.carbon.utils.CarbonUtils;
                var keyStorePassword = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Password");
                var keyStoreName = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Location");
                var identityAlias = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_IDENTITY_ALIAS];
                var keyStoreParams = {
                    KEY_STORE_NAME: keyStoreName,
                    KEY_STORE_PASSWORD: keyStorePassword,
                    IDP_ALIAS: identityAlias
                };
                var rsEnabled = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_RESPONSE_SIGNING_ENABLED];
                if (utils.parseBoolean(rsEnabled)) {
                    if (!ssoClient.validateSignature(samlResponseObj, keyStoreParams)) {
                        var msg = "Invalid signature found in the SAML response.";
                        log.error(msg);
                        response.sendError(500, msg);
                        return;
                    }
                }

                if (!ssoClient.validateSamlResponse(samlResponseObj, ssoConfigs, keyStoreParams)) {
                    var msg = "Invalid SAML response found.";
                    log.error(msg);
                    response.sendError(500, msg);
                    return;
                }
                
                /**
                 * @type {{sessionId: string, loggedInUser: string, sessionIndex: string, samlToken:
                 *     string}}
                 */
                var ssoSession = ssoClient.decodeSAMLLoginResponse(samlResponseObj, samlResponse,
                    session.getId());
                if (ssoSession.sessionId) {
                    var ssoSessions = getSsoSessions();
                    ssoSessions[ssoSession.sessionId] = ssoSession;
                     if (ssoSession.sessionIndex) {
                        var carbonUser = (require("carbon")).server.tenantUser(ssoSession.loggedInUser);
                        utils.setCurrentUser(carbonUser.username, carbonUser.domain, carbonUser.tenantId);
                        module.loadTenant(ssoSession.loggedInUser);
                        var scriptArgument = {input: {samlToken: ssoSession.samlToken}, user: module.getCurrentUser()};
                        handleEvent(OPERATION_LOGIN, EVENT_SUCCESS, scriptArgument);
                    }
                } else {
                    var msg = "Cannot decode SAML login response.";
                    log.error(msg);
                    response.sendError(500, msg);
                }
            }
        }
        // If it is a logout request
        if (samlRequest) {
            var index = ssoClient.decodeSAMLLogoutRequest(ssoClient.getSamlObject(samlRequest));
            if (log.isDebugEnabled()) {
                log.debug("Back end log out request received for the session Id : " + index);
            }
            var jSessionId = getSsoSessions()[index];
            delete getSsoSessions()[index];
            session.invalidate();
        }
    };


	/**
	 * saml token validation Service.
	 * @param request {Object} HTTP request
	 * @param response {Object} HTTP response
	 */
	module.ssoLogin = function (samlToken) {
		var samlResponse = samlToken;
		var ssoClient = require("sso").client;
		var samlResponseObj;

		if (samlResponse) {
			try {
				samlResponseObj = ssoClient.getSamlObject(samlResponse);
			} catch (e) {
				log.error(e.message, e);
				return;
			}

			// This is a login response.
			var ssoConfigs = getSsoConfigurations();
			var CarbonUtils = Packages.org.wso2.carbon.utils.CarbonUtils;
			var keyStorePassword = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Password");
			var keyStoreName = CarbonUtils.getServerConfiguration().getFirstProperty("Security.TrustStore.Location");
			var identityAlias = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_IDENTITY_ALIAS];
			var keyStoreParams = {
				KEY_STORE_NAME: keyStoreName,
				KEY_STORE_PASSWORD: keyStorePassword,
				IDP_ALIAS: identityAlias
			};
			var rsEnabled = ssoConfigs[constants.APP_CONF_AUTH_MODULE_SSO_RESPONSE_SIGNING_ENABLED];
			if (utils.parseBoolean(rsEnabled)) {
				if (!ssoClient.validateSignature(samlResponseObj, keyStoreParams)) {
					var msg = "Invalid signature found in the SAML response.";
					log.error(msg);
					return;
				}
			}

			if (!ssoClient.validateSamlResponse(samlResponseObj, ssoConfigs, keyStoreParams)) {
				var msg = "Invalid SAML response found.";
				log.error(msg);
				return;
			}

			/**
			 * @type {{sessionId: string, loggedInUser: string, sessionIndex: string, samlToken:
			 *     string}}
			 */
			var ssoSession = ssoClient.decodeSAMLLoginResponse(samlResponseObj, samlResponse,
				session.getId());
			if (ssoSession.sessionId) {
				var ssoSessions = getSsoSessions();
				ssoSessions[ssoSession.sessionId] = ssoSession;
				if (ssoSession.sessionIndex) {
					var carbonUser = (require("carbon")).server.tenantUser(ssoSession.loggedInUser);
					utils.setCurrentUser(carbonUser.username, carbonUser.domain, carbonUser.tenantId);
					module.loadTenant(ssoSession.loggedInUser);
					var user = {user: module.getCurrentUser()};
					return user;
				}
			} else {
				var msg = "Cannot decode SAML login response.";
				log.error(msg);
				return;
			}

		}
	};

    /**
     * Load current user tenant
     * @param username logged user name
     */
    module.loadTenant = function (username) {
        var carbon = require('carbon');
        var MultitenantUtils = Packages.org.wso2.carbon.utils.multitenancy.MultitenantUtils;
        var MultitenantConstants = Packages.org.wso2.carbon.base.MultitenantConstants;
        var TenantAxisUtils = Packages.org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
        var service;
        var ctx;
        var domain = MultitenantUtils.getTenantDomain(username);
        if (domain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
            service = carbon.server.osgiService('org.wso2.carbon.utils.ConfigurationContextService');
            ctx = service.getServerConfigContext();
            TenantAxisUtils.setTenantAccessed(domain, ctx);
        }
    };

    /**
     * Basic login.
     * @param request {Object} HTTP request
     * @param response {Object} HTTP response
     */
    module.login = function (request, response) {
        var username = request.getParameter("username");
        if (!username || (username.length == 0)) {
            handleEvent(OPERATION_LOGIN, EVENT_FAIL, new Error("Please enter username."));
            return;
        }
        var password = request.getParameter("password");
        if (!password || (password.length == 0)) {
            handleEvent(OPERATION_LOGIN, EVENT_FAIL, new Error("Please enter password."));
            return;
        }

        var carbonServer = require("carbon").server;
        var isAuthenticated;
        try {
            isAuthenticated = (new carbonServer.Server()).authenticate(username, password);
        } catch (e) {
            log.error(e.message);
            var messageForNotExistingDomain = "Could not find a domain for the username";
            if (e.message.indexOf(messageForNotExistingDomain) < 0) {
                response.sendError(500, e.message);
                return;
            } else {
                isAuthenticated = false;
            }
        }
        if (isAuthenticated) {
            var tenantUser = carbonServer.tenantUser(username);
            utils.setCurrentUser(tenantUser.username, tenantUser.domain, tenantUser.tenantId);
            module.loadTenant(username);
            var scriptArgument = {
                input: {username: username, password: password},
                user: module.getCurrentUser()
            };
            handleEvent(OPERATION_LOGIN, EVENT_SUCCESS, scriptArgument);
        } else {
            handleEvent(OPERATION_LOGIN, EVENT_FAIL, new Error("Incorrect username or password."));
        }
    };

    /**
     * Basic logout.
     * @param response {Object} HTTP response
     */
    module.logout = function (response) {
        var previousUser = module.getCurrentUser();
        try {
            session.invalidate();
        } catch (e) {
            log.error(e.message, e);
            response.sendError(500, e.message);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("User '" + previousUser.username + "' logged out.");
        }
        var scriptArgument = {input: {}, user: previousUser};
        handleEvent(OPERATION_LOGOUT, EVENT_SUCCESS, scriptArgument);
    };
})(module);
