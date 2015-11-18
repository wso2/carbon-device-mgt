/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.api.util.cdmdevice.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.api.util.cdmdevice.constants.PluginConstants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import javax.sql.DataSource;

import java.io.File;

/**
 *
 * Provides methods for initializing the database script.
 *
 */
public final class IotDeviceManagementSchemaInitializer extends DatabaseCreator {

    private static final Log log = LogFactory.getLog(IotDeviceManagementSchemaInitializer.class);
    private static final String setupSQLScriptBaseLocation =
            CarbonUtils.getCarbonHome() + File.separator + "dbscripts" + File.separator
                    + PluginConstants.DB_SCRIPTS_FOLDER +
                    File.separator + "plugins" + File.separator;

    public IotDeviceManagementSchemaInitializer(DataSource dataSource) {
        super(dataSource);
    }

    protected String getDbScriptLocation(String databaseType) {
        String scriptName = databaseType + ".sql";
        if (log.isDebugEnabled()) {
            log.debug("Loading database script from :" + scriptName);
        }
        //TODO:test
        return setupSQLScriptBaseLocation.replaceFirst("DBTYPE", databaseType) + scriptName;
    }

}
