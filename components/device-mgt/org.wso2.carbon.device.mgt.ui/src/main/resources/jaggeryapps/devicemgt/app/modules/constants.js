/*
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var USER_SESSION_KEY = "_UUF_USER";
var UNSPECIFIED = "Unspecified";
var httpURL = "httpURL";
var httpsURL = "httpsURL";

var DEVICE_IDENTIFIER = "deviceIdentifier";
var DEVICE_NAME = "name";
var DEVICE_OWNERSHIP = "ownership";
var DEVICE_OWNER = "owner";
var DEVICE_TYPE = "type";
var DEVICE_VENDOR = "vendor";
var DEVICE_MODEL = "model";
var DEVICE_PRODUCT = "PRODUCT";
var DEVICE_OS_VERSION = "osVersion";
var DEVICE_OS_BUILD_DATE = "osBuildDate";
var DEVICE_PROPERTIES = "properties";
var DEVICE_ENROLLMENT_INFO = "enrolmentInfo";
var DEVICE_STATUS = "status";
var DEVICE_INFO = "deviceInfo";

var FEATURE_NAME = "featureName";
var FEATURE_DESCRIPTION = "featureDescription";

var PLATFORM_ANDROID = "android";
var PLATFORM_WINDOWS = "windows";
var PLATFORM_IOS = "ios";

var LANGUAGE_US = "en_US";

var VENDOR_APPLE = "Apple";
var ERRORS = {
    "USER_NOT_FOUND": "USER_NOT_FOUND"
};

var USER_STORES_NOISY_CHAR = "\"";
var USER_STORES_SPLITTING_CHAR = "\\n";
var USER_STORE_CONFIG_ADMIN_SERVICE_END_POINT =
    "/services/UserStoreConfigAdminService.UserStoreConfigAdminServiceHttpsSoap12Endpoint/";

var SOAP_VERSION = 1.2;
var WEB_SERVICE_ADDRESSING_VERSION = 1.0;
var TOKEN_PAIR = "tokenPair";
var ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS = "encodedTenantBasedClientAppCredentials";
var CONTENT_TYPE_IDENTIFIER = "Content-Type";
var ENCODED_TENANT_BASED_WEB_SOCKET_CLIENT_CREDENTIALS = "encodedTenantBasedWebSocketClientCredentials";

var CONTENT_DISPOSITION_IDENTIFIER = "Content-Disposition";
var APPLICATION_JSON = "application/json";
var APPLICATION_ZIP = "application/zip";
var STREAMING_FILES_ACCEPT_HEADERS = ["application/zip", "application/pdf", "application/octet-stream"];
var ACCEPT_IDENTIFIER = "Accept";
var AUTHORIZATION_HEADER= "Authorization";
var BEARER_PREFIX = "Bearer ";
var HTTP_GET = "GET";
var HTTP_POST = "POST";
var HTTP_PUT = "PUT";
var HTTP_DELETE = "DELETE";

var HTTP_CONFLICT = 409;
var HTTP_CREATED = 201;

var CACHED_CREDENTIALS = "tenantBasedCredentials";
var CACHED_CREDENTIALS_FOR_WEBSOCKET_APP = "tenantBasedWebSocketClientCredentials";

var ALLOWED_SCOPES = "scopes";
