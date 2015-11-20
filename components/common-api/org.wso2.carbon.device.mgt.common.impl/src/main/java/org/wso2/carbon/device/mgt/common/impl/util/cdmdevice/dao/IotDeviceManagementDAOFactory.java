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

package org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.impl.config.devicetype.datasource.DeviceTypeConfig;
import org.wso2.carbon.device.mgt.common.impl.util.cdmdevice.exception.IotDeviceMgtPluginException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.*;

/**
 * Factory class used to create IotDeviceManagement related DAO objects.
 */
public abstract class IotDeviceManagementDAOFactory implements IotDeviceManagementDAOFactoryInterface {

    private static final Log log = LogFactory.getLog(IotDeviceManagementDAOFactory.class);
    private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
    private static boolean isInitialized;

    public static void init(Map<String, DeviceTypeConfig> iotDataSourceConfigMap)
            throws IotDeviceMgtPluginException {
        DataSource dataSource;
        for (Map.Entry<String, DeviceTypeConfig> plugin : iotDataSourceConfigMap.entrySet()) {
            String pluginType = plugin.getKey();
            if (dataSourceMap.get(pluginType) == null) {
                dataSource = IotDeviceManagementDAOFactory.resolveDataSource(plugin.getValue().getDatasourceName());
                dataSourceMap.put(pluginType, dataSource);
            }
        }
        //Todo:check
        isInitialized = true;
    }


    /**
     * Resolve data source from the data source definition.
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(String dataSourceName) throws IotDeviceMgtPluginException{

        DataSource dataSource = null;
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new IotDeviceMgtPluginException("Error while looking up the data " +
                                                          "source: " + dataSourceName);
        }
        return dataSource;
    }

    public static Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }
}