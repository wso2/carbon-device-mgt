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

package org.wso2.carbon.device.mgt.extensions.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.DeviceTypeConfigIdentifier;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.util.DeviceSchemaInitializer;
import org.wso2.carbon.device.mgt.extensions.device.type.template.util.DeviceTypeUtils;
import org.wso2.carbon.device.mgt.extensions.license.mgt.file.FileSystemBasedLicenseManager;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This is a test case for testing common utilities used.
 */
public class UtilsTest {
    private FileSystemBasedLicenseManager fileSystemBasedLicenseManager;

    @BeforeTest
    public void setup() {
        fileSystemBasedLicenseManager = new FileSystemBasedLicenseManager();
    }

    @Test(description = "This testcase tests the functionality of the DeviceSchemaInitializer")
    public void testDeviceSchemaInitializer()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String deviceType = "sample2";
        String expectedDBLocation =
                System.getProperty("carbon.home") + File.separator + "dbscripts" + File.separator + "cdm"
                        + File.separator + "plugins" + File.separator + deviceType + File.separator + "h2.sql";
        DeviceSchemaInitializer deviceSchemaInitializer = new DeviceSchemaInitializer(null, deviceType,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Method getDbScriptLocation = DeviceSchemaInitializer.class
                .getDeclaredMethod("getDbScriptLocation", String.class);
        getDbScriptLocation.setAccessible(true);

        String dbLocation = (String) getDbScriptLocation.invoke(deviceSchemaInitializer, "h2");
        Assert.assertEquals(dbLocation, expectedDBLocation,
                "Expected DB location for the device type is not retrieved");
    }

    @Test(description = "This test case tests the getLicense method of the FileBasedLicenseManager")
    public void testFileBasedLicenseManagerGetLicense() throws LicenseManagementException {
        License fileBasedLicense = fileSystemBasedLicenseManager.getLicense(Utils.TEST_STRING,"en_US");
        Assert.assertEquals(fileBasedLicense.getText(), "This is a file based license",
                "FileBased License cannot " + "be retrieved by FileBasedLicenseManager");
    }

    @Test(description = "This test case tests the behaviour of file based license manager when the relevant license "
            + "is missing in file system", expectedExceptions = {LicenseManagementException.class},
            expectedExceptionsMessageRegExp = "License file not found in the path for the device type test2")
    public void testFileBasedLicenseManagerGetNonExistingLicense() throws LicenseManagementException {
       fileSystemBasedLicenseManager.getLicense("test2","en_US");
    }

    @Test(description = "This test case make sure the File Based License cannot be added without adding directly to "
            + "file system", expectedExceptions = {UnsupportedOperationException.class},
            expectedExceptionsMessageRegExp = "'addLicense' method is not supported in FileSystemBasedLicenseManager")
    public void testFileBasedLicenseManagerAddLicense() throws LicenseManagementException {
       fileSystemBasedLicenseManager.addLicense(Utils.TEST_STRING, null);
    }

    @Test(description = "This test case tests the DeviceTypeConfigIdentifier equals method")
    public void testDeviceTypeConfigIdentifier() {
        DeviceTypeConfigIdentifier deviceTypeConfigIdentifier = new DeviceTypeConfigIdentifier("sample",
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        DeviceTypeConfigIdentifier clonedDeviceTypeConfigIdentifier = new DeviceTypeConfigIdentifier("sample",
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Assert.assertTrue(deviceTypeConfigIdentifier.equals(clonedDeviceTypeConfigIdentifier),
                "Clone device type config identifier gives wrong results for equals check.");
        Assert.assertFalse(deviceTypeConfigIdentifier.equals(null),
                "Device Type config identifier object comparison gives wrong results.");
    }

    @Test(description = "This test cases tests the registry based license addition")
    public void testAddRegistryBasedLicense() throws LicenseManagementException {
        String newLicenseString = "New License";
        License fileBasedLicense = fileSystemBasedLicenseManager.getLicense(Utils.TEST_STRING, "en_US");
        RegistryBasedLicenseManager registryBasedLicenseManager = new RegistryBasedLicenseManager();
        registryBasedLicenseManager.addLicense(Utils.TEST_STRING, fileBasedLicense);
        Assert.assertEquals(fileBasedLicense.getText(),
                registryBasedLicenseManager.getLicense(Utils.TEST_STRING, "en_US").getText(),
                "Registry license addition failed");
        fileBasedLicense.setText(newLicenseString);
        registryBasedLicenseManager.addLicense(Utils.TEST_STRING, fileBasedLicense);
        Assert.assertEquals(registryBasedLicenseManager.getLicense(Utils.TEST_STRING, "en_US").getText(),
                newLicenseString, "Registry license update failed");
    }

    @Test(description = "This test case tests the GetConfigurationRegistry method when the registry service is "
            + "unavailable", dependsOnMethods = {"testDeviceSchemaInitializer", "testDeviceTypeConfigIdentifier",
            "testAddRegistryBasedLicense"},
            expectedExceptions = {DeviceTypeMgtPluginException.class},
            expectedExceptionsMessageRegExp = "Error in retrieving conf registry instance:.*")
    public void testGetConfigurationRegistry() throws DeviceTypeMgtPluginException {
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(4);
        DeviceTypeUtils.getConfigurationRegistry();
    }
}
