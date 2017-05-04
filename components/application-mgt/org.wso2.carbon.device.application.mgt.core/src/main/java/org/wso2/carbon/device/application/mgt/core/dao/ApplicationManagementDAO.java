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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.core.dto.Application;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ApplicationManagementDAO {

    public enum DatabaseType {

        H2("H2"),
        MYSQL("MySQL"),
        ORACLE("Oracle"),
        POSTGRESQL("PostgreSQL"),
        MSSQL("Microsoft SQL Server");

        private final String value;
        private static final Map<String, DatabaseType> lookup = new HashMap<String, DatabaseType>();

        static {
            for (DatabaseType databaseType : DatabaseType.values()) {
                lookup.put(databaseType.getValue(), databaseType);
            }
        }

        DatabaseType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static DatabaseType lookup(String value) {
            return lookup.get(value);
        }
    }

    public void createApplication(Application application) throws ApplicationManagementDAOException;

    public ApplicationList getApplications() throws ApplicationManagementDAOException;

}
