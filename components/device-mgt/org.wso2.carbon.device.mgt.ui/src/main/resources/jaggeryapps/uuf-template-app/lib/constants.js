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
 * Application constants.
 */
var constants = {
    // Paths - Directories
    DIRECTORY_APP: "/app",
    DIRECTORY_APP_LAYOUTS: "/app/layouts",
    DIRECTORY_APP_PAGES: "/app/pages",
    DIRECTORY_APP_UNITS: "/app/units",
    DIRECTORY_APP_UNIT_PUBLIC: "public",
    DIRECTORY_CACHE: "/cache",
    DIRECTORY_CONF: "/conf",
    DIRECTORY_LIB: "/lib",
    DIRECTORY_MODULES: "/modules",
    // Paths - Files
    FILE_APP_CONF: "/app/conf/app-conf.json",
    FILE_UUF_CONF: "/app/conf/uuf-conf.json",
    // Configurations - App
    APP_CONF_APP_NAME: "appName",
    APP_CONF_CACHE_ENABLED: "cachingEnabled",
    APP_CONF_DEBUGGING_ENABLED: "debuggingEnabled",
    APP_CONF_PERMISSION_ROOT: "permissionRoot",
    APP_CONF_ADMIN_SERVICES_URL: "adminServicesUrl",
    APP_CONF_LOGIN_PAGE: "loginPage",
    APP_CONF_ERROR_PAGES: "errorPages",
    // Configurations - App - Auth module
    APP_CONF_AUTH_MODULE: "authModule",
    APP_CONF_AUTH_MODULE_ENABLED: "enabled",
    APP_CONF_AUTH_MODULE_TRANSPORT: "transport",
    APP_CONF_AUTH_MODULE_LOGIN: "login",
    APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS: "onSuccess",
    APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS_SCRIPT: "script",
    APP_CONF_AUTH_MODULE_LOGIN_ON_SUCCESS_PAGE: "page",
    APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL: "onFail",
    APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL_SCRIPT: "script",
    APP_CONF_AUTH_MODULE_LOGIN_ON_FAIL_PAGE: "page",
    APP_CONF_AUTH_MODULE_LOGOUT: "logout",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS: "onSuccess",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS_SCRIPT: "script",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_SUCCESS_PAGE: "page",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL: "onFail",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL_SCRIPT: "script",
    APP_CONF_AUTH_MODULE_LOGOUT_ON_FAIL_PAGE: "page",
    APP_CONF_AUTH_MODULE_SSO: "sso",
    APP_CONF_AUTH_MODULE_SSO_ENABLED: "enabled",
    APP_CONF_AUTH_MODULE_SSO_ISSUER: "issuer",
    APP_CONF_AUTH_MODULE_SSO_RESPONSE_SIGNING_ENABLED: "responseSigningEnabled",
    APP_CONF_AUTH_MODULE_SSO_KEY_STORE_NAME: "keyStoreName",
    APP_CONF_AUTH_MODULE_SSO_KEY_STORE_PASSWORD: "keyStorePassword",
    APP_CONF_AUTH_MODULE_SSO_IDENTITY_PROVIDER_ALIAS: "identityProviderAlias",
    APP_CONF_AUTH_MODULE_SSO_IDENTITY_PROVIDER_URL: "identityProviderUrl",
    APP_CONF_AUTH_MODULE_SSO_INTERMEDIATE_PAGE: "intermediatePage",
    // Configurations - UUF
    UUF_CONF_DISPLAY_NAME: "displayName",
    UUF_CONF_LOG_LEVEL: "logLevel",
    UUF_CONF_ERROR_PAGES: "errorPages",
    UUF_CONF_SECURITY_CONSTRAINTS: "securityConstraints",
    // UI Component - JS functions
    UI_COMPONENT_JS_FUNCTION_ON_REQUEST: "onRequest",
    // UI Component - Definition
    UI_COMPONENT_DEFINITION_VERSION: "version",
    UI_COMPONENT_DEFINITION_INDEX: "index",
    UI_COMPONENT_DEFINITION_EXTENDS: "extends",
    UI_COMPONENT_DEFINITION_PERMISSIONS: "permissions",
    UI_COMPONENT_DEFINITION_DISABLED: "disabled",
    UI_COMPONENT_DEFINITION_IS_ANONYMOUS: "isAnonymous",
    // Page - Definition
    PAGE_DEFINITION_URI: "uri",
    PAGE_DEFINITION_LAYOUT: "layout",
    // Unit - Definition
    UNIT_DEFINITION_PUSHED_URIS: "pushedUris",
    // Handlebars helper parameters
    HELPER_PARAM_PARAMS: "_params",
    HELPER_PARAM_OVERRIDE: "override",
    HELPER_PARAM_COMBINE: "combine",
    HELPER_PARAM_SCOPE: "scope",
    // Combined resources URLs
    COMBINED_RESOURCES_SEPARATOR: ",",
    COMBINED_RESOURCES_URL_TAIL: "_/combined.",
    // Cache keys
    CACHE_KEY_UUF_CONF: "_UUF_UUF_CONF",
    CACHE_KEY_UUF_CONF_FILE_LMD: "_UUF_UUF_CONF_FILE_LMD",
    CACHE_KEY_APP_CONF: "_UUF_APP_CONF",
    CACHE_KEY_APP_CONF_FILE_LMD: "_UUF_APP_CONF_FILE_LMD",
    CACHE_KEY_LOOKUP_TABLE: "_UUF_LOOKUP_TABLE",
    CACHE_KEY_USER: "_UUF_USER",
    CACHE_KEY_SSO_SESSIONS: "_UUF_SSO_SESSIONS",
    CACHE_KEY_HANDLEBARS_ROOT: "_UUF_HANDLEBARS_ROOT",
    // URL Query Params
    URL_PARAM_REFERER: "referer"
};
