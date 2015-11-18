/**
 * Application constants.
 * @type {{COMBINED_RESOURCES_SEPARATOR: string, COMBINED_RESOURCES_URI_TAIL: string,
 *     DIRECTORY_APP_ROOT: string, DIRECTORY_APP_CONF: string, DIRECTORY_APP_LAYOUTS: string,
 *     DIRECTORY_APP_PAGES: string, DIRECTORY_APP_UNITS: string, DIRECTORY_APP_UNIT_PUBLIC: string,
 *     DIRECTORY_CACHE: string, DIRECTORY_DEBUG: string, FILE_APP_CONF: string, FILE_LIB_CONF:
 *     string, LIBRARY_HANDLEBARS: string, LIBRARY_LESS: string, CACHE_KEY_LOOKUP_TABLE: string,
 *     APP_CONF_DISPLAY_NAME: string, APP_CONF_CACHE_ENABLED: string, APP_CONF_DEBUGGING_ENABLED:
 *     string, APP_CONF_LOG_LEVEL: string, APP_CONF_WELCOME_FILE: string, APP_CONF_ERROR_PAGES:
 *     string, APP_CONF_SECURITY_CONSTRAINTS: string, UI_COMPONENT_JS_FUNCTION_ON_REQUEST: string,
 *     UI_COMPONENT_DEFINITION_VERSION: string, UI_COMPONENT_DEFINITION_INDEX: string,
 *     UI_COMPONENT_DEFINITION_EXTENDS: string, UI_COMPONENT_DEFINITION_PERMISSIONS: string,
 *     UI_COMPONENT_DEFINITION_DISABLE: string, PAGE_DEFINITION_URI: string,
 *     PAGE_DEFINITION_LAYOUT: string, UNIT_DEFINITION_PUSHED_URIS: string,
 *     HELPER_PARAM_UNIT_PARAMS: string, HELPER_PARAM_OVERRIDE: string, HELPER_PARAM_COMBINE:
 *     string, HELPER_PARAM_SCOPE: string}}
 */
var constants = {
    COMBINED_RESOURCES_SEPARATOR: ",",
    COMBINED_RESOURCES_URI_TAIL: "_/combined.",
    // paths
    DIRECTORY_APP_ROOT: "/app",
    DIRECTORY_APP_CONF: "/app/conf",
    DIRECTORY_APP_LAYOUTS: "/app/layouts",
    DIRECTORY_APP_PAGES: "/app/pages",
    DIRECTORY_APP_UNITS: "/app/units",
    DIRECTORY_APP_UNIT_PUBLIC: "public",
    DIRECTORY_CACHE: "/cache",
    DIRECTORY_DEBUG: "/debug",
    FILE_APP_CONF: "/app/conf/app-conf.json",
    FILE_LIB_CONF: "/lib/conf.json",
    // libraries
    LIBRARY_HANDLEBARS: "handlebars-v2.0.0.js",
    LIBRARY_LESS: "less-rhino-1.7.5.js",
    // UUF cache keys
    CACHE_KEY_LOOKUP_TABLE: "_UUF_LOOKUP_TABLE",
    // app configurations
    APP_CONF_DISPLAY_NAME: "displayName",
    APP_CONF_CACHE_ENABLED: "cachingEnabled",
    APP_CONF_DEBUGGING_ENABLED: "debuggingEnabled",
    APP_CONF_LOG_LEVEL: "logLevel",
    APP_CONF_WELCOME_FILE: "welcomeFile",
    APP_CONF_ERROR_PAGES: "errorPages",
    APP_CONF_SECURITY_CONSTRAINTS: "securityConstraints",
    // UI Component's JS functions
    UI_COMPONENT_JS_FUNCTION_ON_REQUEST: "onRequest",
    // UI Component's definition
    UI_COMPONENT_DEFINITION_VERSION: "version",
    UI_COMPONENT_DEFINITION_INDEX: "index",
    UI_COMPONENT_DEFINITION_EXTENDS: "extends",
    UI_COMPONENT_DEFINITION_PERMISSIONS: "permissions",
    UI_COMPONENT_DEFINITION_DISABLE: "disable",
    // page's definition
    PAGE_DEFINITION_URI: "uri",
    PAGE_DEFINITION_LAYOUT: "layout",
    // unit's definition
    UNIT_DEFINITION_PUSHED_URIS: "pushedUris",
    // Handlebars helper parameters
    HELPER_PARAM_UNIT_PARAMS: "_unitParams",
    HELPER_PARAM_OVERRIDE: "override",
    HELPER_PARAM_COMBINE: "combine",
    HELPER_PARAM_SCOPE: "scope"
};
