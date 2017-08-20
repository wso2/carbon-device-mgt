/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.util;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Application Management related constants.
 */
public class Constants {

    public static final String APPLICATION_CONFIG_XML_FILE = "application-mgt.xml";

    public static final String DEFAULT_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.APPLICATION_CONFIG_XML_FILE;
    public static final String PLATFORMS_DEPLOYMENT_DIR_NAME = "platforms";
    public static final String PLATFORM_DEPLOYMENT_EXT = ".xml";

    /**
     * Database types supported by Application Management.
     */
    public static final class DataBaseTypes {
        private DataBaseTypes() {
        }
        public static final String DB_TYPE_MYSQL = "MySQL";
        public static final String DB_TYPE_ORACLE = "Oracle";
        public static final String DB_TYPE_MSSQL = "Microsoft SQL Server";
        public static final String DB_TYPE_DB2 = "DB2";
        public static final String DB_TYPE_H2 = "H2";
        public static final String DB_TYPE_POSTGRESQL = "PostgreSQL";
    }

    /**
     * Lifecycle states of the application life-cycle.
     */
    public static final String[] LIFE_CYCLES = {"CREATED", "IN REVIEW", "APPROVED", "REJECTED", "PUBLISHED",
            "UNPUBLISHED", "RETIRED"};

    /**
     * Path to save the Application related artifacts.
     */
    public static String artifactPath = "";
}
