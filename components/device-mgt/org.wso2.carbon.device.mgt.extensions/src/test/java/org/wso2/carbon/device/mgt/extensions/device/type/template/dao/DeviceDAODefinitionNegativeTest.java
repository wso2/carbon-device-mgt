/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.device.type.template.dao;

import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Table;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;

/**
 * This class tests the negative scenarios related with {@link DeviceDAODefinition}
 */
public class DeviceDAODefinitionNegativeTest {
    private final String DEVICE_TABLE_NAME = "DEVICE_TABLE";

    @Test(description = "This test case tests the behavior of the DeviceDAODefinition when the table is null",
            expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Table is null. Cannot create DeviceDAODefinition")
    public void testWhenTableIsNull() {
        new DeviceDAODefinition(null);
    }

    @Test(description = "This test case tests the behavior of the DeviceDAODefinition when the table name is null",
            expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Missing deviceTableName")
    public void testWhenTableNameIsNull() {
        new DeviceDAODefinition(new Table());
    }

    @Test(description = "This test case tests the behavior of the DeviceDAODefinition when the primary key is null",
            expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Missing primaryKey for the table " + DEVICE_TABLE_NAME)
    public void testWhenPrimaryKeyIsEmpty() {
        Table deviceTable = new Table();
        deviceTable.setName(DEVICE_TABLE_NAME);
        deviceTable.setPrimaryKey("");
        new DeviceDAODefinition(deviceTable);
    }

    @Test(description = "This test case tests the behavior of the DeviceDAODefinition when the attributes is null",
            expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Table " + DEVICE_TABLE_NAME + " attributes are not specified. "
                    + "Cannot created DeviceDAODefinition")
    public void testWhenAttributesIsNull() {
        Table deviceTable = new Table();
        deviceTable.setName(DEVICE_TABLE_NAME);
        deviceTable.setPrimaryKey("primaryKey");
        new DeviceDAODefinition(deviceTable);
    }
}
