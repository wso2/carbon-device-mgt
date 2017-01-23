/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

public final class DeviceManagementConstants {

    public static final class DataSourceProperties {
        private DataSourceProperties() {
            throw new AssertionError();
        }

        public static final String DB_CHECK_QUERY = "SELECT * FROM DM_DEVICE";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
        public static final String DEVICE_CONFIG_XML_NAME = "cdm-config.xml";
    }

    public static final class SecureValueProperties {
        private SecureValueProperties() {
            throw new AssertionError();
        }

        public static final String SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE = "secretAlias";
        public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";
    }

    public static final class MobileDeviceTypes {
        private MobileDeviceTypes() {
            throw new AssertionError();
        }

        public final static String MOBILE_DEVICE_TYPE_ANDROID = "android";
        public final static String MOBILE_DEVICE_TYPE_IOS = "ios";
        public final static String MOBILE_DEVICE_TYPE_WINDOWS = "windows";
    }

    public static final class LanguageCodes {
        private LanguageCodes() {
            throw new AssertionError();
        }

        public final static String LANGUAGE_CODE_ENGLISH_US = "en_US";
        public final static String LANGUAGE_CODE_ENGLISH_UK = "en_UK";
    }

    public static final class LicenseProperties {
        private LicenseProperties() {
            throw new AssertionError();
        }
        public static final String PROVIDER = "overview_provider";
        public static final String NAME = "overview_name";
        public static final String LANGUAGE = "overview_language";
        public static final String VERSION = "overview_version";
        public static final String VALID_FROM = "overview_validityFrom";
        public static final String VALID_TO = "overview_validityTo";
        public static final String TEXT = "overview_license";
        public static final String LICENSE_REGISTRY_KEY = "license";
        public static final String ARTIFACT_NAME = "name";
    }

    public static final class NotificationProperties {
        private NotificationProperties() {
            throw new AssertionError();
        }
        public static final String NOTIFICATION_CONFIG_FILE = "notification-messages.xml";
    }

    public static final class DataBaseTypes {
        private DataBaseTypes() {
            throw new AssertionError();
        }
        public static final String DB_TYPE_MYSQL = "MySQL";
        public static final String DB_TYPE_ORACLE = "Oracle";
        public static final String DB_TYPE_MSSQL = "Microsoft SQL Server";
        public static final String DB_TYPE_DB2 = "DB2";
        public static final String DB_TYPE_H2 = "H2";
        public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";
    }

}
