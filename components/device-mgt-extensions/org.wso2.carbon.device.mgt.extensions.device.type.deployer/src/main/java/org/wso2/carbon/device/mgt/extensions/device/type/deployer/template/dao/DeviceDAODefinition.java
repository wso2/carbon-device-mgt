/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.dao;

import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Table;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeDeployerFileException;

import java.util.ArrayList;
import java.util.List;

/**
 * This holds the meta data of device definition table.
 * This is optional.
 */
public class DeviceDAODefinition {

    private String deviceTableName;
    private String primarykey;

    public List<String> getColumnNames() {
        return columnNames;
    }

    private List<String> columnNames = new ArrayList<>();


    public DeviceDAODefinition(Table table) {
        deviceTableName = table.getName();
        primarykey = table.getPrimaryKey();
        List<String> attributes = table.getAttributes().getAttribute();
        if (deviceTableName == null || deviceTableName.isEmpty()) {
            throw new DeviceTypeDeployerFileException("Missing deviceTableName");
        }

        if (primarykey == null || primarykey.isEmpty()) {
            throw new DeviceTypeDeployerFileException("Missing primaryKey ");
        }

        if (attributes == null || attributes.size() == 0) {
            throw new DeviceTypeDeployerFileException("Missing Attributes ");
        }
        for (String attribute : attributes) {
            if (attribute.isEmpty()) {
                throw new DeviceTypeDeployerFileException("Unsupported attribute format for device definition");
            }
            columnNames.add(attribute);
        }
    }

    public String getDeviceTableName() {
        return deviceTableName;
    }

    public String getPrimaryKey() {
        return primarykey;
    }


}
