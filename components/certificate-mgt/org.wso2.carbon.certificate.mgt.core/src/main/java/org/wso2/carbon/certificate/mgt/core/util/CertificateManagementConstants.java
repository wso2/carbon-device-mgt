/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.certificate.mgt.core.util;

public final class CertificateManagementConstants {

    public static final String CERTIFICATE_CONFIG_XML_FILE = "certificate-config.xml";
    public static final String SETUP_PROPERTY = "setup";
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_HOME_ENTRY = "${carbon.home}";

    public static final String PROVIDER = "BC";
    public static final String CERTIFICATE_KEYSTORE = "CertificateKeystoreType";
    public static final String RSA = "RSA";
    public static final String SHA256_RSA = "SHA256WithRSAEncryption";
    public static final String X_509 = "X.509";
    public static final String POST_BODY_CA_CAPS = "POSTPKIOperation\nSHA-1\nDES3\n";
    public static final String DES_EDE = "DESede";
    public static final String CONF_LOCATION = "conf.location";
    public static final String DEFAULT_PRINCIPAL = "O=WSO2, OU=Mobile, C=LK";
    public static final String RSA_PRIVATE_KEY_BEGIN_TEXT = "-----BEGIN RSA PRIVATE KEY-----\n";
    public static final String RSA_PRIVATE_KEY_END_TEXT = "-----END RSA PRIVATE KEY-----";
    public static final String EMPTY_TEXT = "";
    public static final int RSA_KEY_LENGTH = 1024;

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
