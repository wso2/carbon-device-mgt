/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.io.File;

/**
 * Provides methods for initializing the database script.
 */
public class DeviceSchemaInitializer extends DatabaseCreator{

	private static final Log log = LogFactory.getLog(DeviceSchemaInitializer.class);
	private String setupSQLScriptBaseLocation;

	public DeviceSchemaInitializer(DataSource dataSource, String deviceType, String tenantDomain) {
		super(dataSource);
		String tenantSeperator = "";
		if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
			tenantSeperator = tenantDomain + File.separator;
		}

		setupSQLScriptBaseLocation = CarbonUtils.getCarbonHome() + File.separator + "dbscripts"
				+ File.separator + "cdm" + File.separator + "plugins" + File.separator + tenantSeperator
				+ deviceType + File.separator;
	}

	@Override
	protected String getDbScriptLocation(String databaseType) {
		String scriptName = databaseType + ".sql";
		if (log.isDebugEnabled()) {
			log.debug("Loading database script from :" + scriptName);
		}
		return setupSQLScriptBaseLocation.replaceFirst("DBTYPE", databaseType) + scriptName;
	}
}
