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

package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataSource;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This class tests the negative scenarios in {@link DeviceTypeManager} initialization;
 */
public class DeviceTypeManagerNegativeTest {
    private DeviceTypeConfiguration defectiveDeviceTypeConfiguration1;
    private DeviceTypeConfiguration defectiveDeviceTypeConfiguration2;
    private DeviceTypeConfiguration androidDeviceTypeConfiguration;
    private DeviceTypeConfigIdentifier deviceTypeConfigIdentifier;
    private final String DEFECTIVE_DEVICE_TYPE = "defectiveDeviceType";
    private final String TABLE_NAME = "DEFECTIVE_DEVICE";

    @BeforeTest
    public void setup()
            throws SAXException, JAXBException, ParserConfigurationException, DeviceTypeConfigurationException,
            IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("device-types/defective-devicetype.xml");
        File configurationFile = null;
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            defectiveDeviceTypeConfiguration1 = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }
        deviceTypeConfigIdentifier = new DeviceTypeConfigIdentifier(DEFECTIVE_DEVICE_TYPE,MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);

        resourceUrl = classLoader.getResource("device-types/defective-devicetype2.xml");
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            defectiveDeviceTypeConfiguration2 = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }

        resourceUrl = classLoader.getResource("device-types/android.xml");
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            androidDeviceTypeConfiguration = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }
    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation without defining the "
            + "datasource but by specifying the table id", expectedExceptions = { DeviceTypeDeployerPayloadException
            .class}, expectedExceptionsMessageRegExp = "Could not find the datasource related with the table id "
            + TABLE_NAME + " for the device type " + DEFECTIVE_DEVICE_TYPE)
    public void testWithoutDataSource() {
        new DeviceTypeManager(deviceTypeConfigIdentifier, defectiveDeviceTypeConfiguration1);

    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation without defining the "
            + "table config",expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Could not find the table config with the table id " + TABLE_NAME
                    + " for the device type " + DEFECTIVE_DEVICE_TYPE,
            dependsOnMethods = {"testWithoutDataSource"})
    public void testWithoutTableConfig() {
        DataSource dataSource = new DataSource();
        defectiveDeviceTypeConfiguration1.setDataSource(dataSource);
        new DeviceTypeManager(deviceTypeConfigIdentifier, defectiveDeviceTypeConfiguration1);
    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation without defining the "
            + "correct table as per the device details",
            expectedExceptions = { DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Could not find definition for table: " + TABLE_NAME)
    public void testWithoutTable() {
        new DeviceTypeManager(deviceTypeConfigIdentifier, defectiveDeviceTypeConfiguration2);
    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation without having the "
            + "actual datasource", expectedExceptions = {DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Error while looking up the data source.*")
    public void testWithoutProperDataSource() {
        new DeviceTypeManager(deviceTypeConfigIdentifier, androidDeviceTypeConfiguration);
    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation without having the "
            + "actual datasource", expectedExceptions = {DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Error while looking up the data source.*")
    public void testWithSetupParameters() {
        System.setProperty("setup", "true");
        new DeviceTypeManager(deviceTypeConfigIdentifier, androidDeviceTypeConfiguration);

    }
}
