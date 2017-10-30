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

import org.h2.jdbcx.JdbcDataSource;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataSource;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceDetails;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Properties;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceDAODefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypeDAOHandler;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOImpl;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOManager;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.PropertyBasedPluginDAOImpl;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * This class tests the negative scenarios in {@link DeviceTypeManager} initialization;
 */
public class DeviceTypeManagerNegativeTest {
    private DeviceTypeConfiguration defectiveDeviceTypeConfiguration1;
    private DeviceTypeConfiguration defectiveDeviceTypeConfiguration2;
    private DeviceTypeConfiguration defectiveDeviceTypeConfiguration3;
    private DeviceTypeConfiguration androidDeviceTypeConfiguration;
    private DeviceTypeConfigIdentifier deviceTypeConfigIdentifier;
    private DeviceTypeManager androidDeviceTypeManager;
    private DeviceTypeDAOHandler deviceTypeDAOHandler;
    private final String DEFECTIVE_DEVICE_TYPE = "defectiveDeviceType";
    private final String TABLE_NAME = "DEFECTIVE_DEVICE";
    private DeviceIdentifier deviceIdentifier;
    private final String ANDROID_DEVICE_TYPE = "android";
    private PropertyBasedPluginDAOImpl propertyBasedPluginDAO;
    private Device sampleDevice;

    @BeforeClass
    public void setup()
            throws SAXException, JAXBException, ParserConfigurationException, DeviceTypeConfigurationException,
            IOException, NoSuchFieldException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "defective-devicetype.xml");
        File configurationFile = null;
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            defectiveDeviceTypeConfiguration1 = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }
        deviceTypeConfigIdentifier = new DeviceTypeConfigIdentifier(DEFECTIVE_DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "defective-devicetype2.xml");
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            defectiveDeviceTypeConfiguration2 = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }

        resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "android.xml");
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            androidDeviceTypeConfiguration = Utils.getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }

        resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "defective-devicetype3.xml");
        if (resourceUrl != null) {
            configurationFile = new File(resourceUrl.getFile());
        }
        if (configurationFile != null) {
            defectiveDeviceTypeConfiguration3 = Utils
                    .getDeviceTypeConfiguration(configurationFile.getAbsoluteFile());
        }
        createDefectiveDeviceTypeManager();
        deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(Utils.TEST_STRING);
        deviceIdentifier.setType(ANDROID_DEVICE_TYPE);

        DeviceDetails deviceDetails = new DeviceDetails();
        Properties properties = new Properties();
        List<String> propertyList = new ArrayList<>();
        propertyList.add(Utils.TEST_STRING);
        properties.addProperties(propertyList);

        deviceDetails.setProperties(properties);
        propertyBasedPluginDAO = new PropertyBasedPluginDAOImpl(deviceDetails,
                deviceTypeDAOHandler, ANDROID_DEVICE_TYPE);
        sampleDevice = new Device();
        sampleDevice.setDeviceIdentifier(Utils.TEST_STRING);
        List<Device.Property> deviceProperties = new ArrayList<>();
        Device.Property property = new Device.Property();
        property.setName(Utils.TEST_STRING);
        property.setValue(Utils.TEST_STRING);
        deviceProperties.add(property);
        sampleDevice.setProperties(deviceProperties);
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

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation when having a "
            + "defective platform configuration ", expectedExceptions = {DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Error occurred while getting default platform configuration for the "
                    + "device type wrong *")
    public void testWithDefectivePlatformConfiguration() {
        DeviceTypeConfigIdentifier wrongDeviceTypeConfigIdentifier = new DeviceTypeConfigIdentifier("wrong",
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        new DeviceTypeManager(wrongDeviceTypeConfigIdentifier, androidDeviceTypeConfiguration);
    }

    @Test(description = "This test case tests the behaviour of the DeviceTypeManager creation when having a "
            + "defective platform configuration ", expectedExceptions = {DeviceTypeDeployerPayloadException.class},
            expectedExceptionsMessageRegExp = "Error while looking up the data source:.*")
    public void testWithoutDeviceSpecificTable() {
        new DeviceTypeManager(deviceTypeConfigIdentifier, defectiveDeviceTypeConfiguration3);
    }

    @Test(description = "This test case tests the behaviour of the isEnrolled when the relevant tables are not there",
            expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error while checking the enrollment status of android device.*")
    public void testIsEnrolled() throws DeviceManagementException {
        androidDeviceTypeManager.isEnrolled(deviceIdentifier);
    }

    @Test(description = "This test case tests the behaviour of the modifyEnrollment when the relevant tables "
            + "are not there",
            expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error while updating the enrollment of the.*")
    public void testModifyEnrollment() throws DeviceManagementException {
        Device device = new Device();
        device.setDeviceIdentifier(deviceIdentifier.getId());
        device.setType(deviceIdentifier.getType());
        androidDeviceTypeManager.modifyEnrollment(device);
    }

    @Test(description = "This test case tests the behaviour of the getAllDevices when the relevant tables "
            + "are not there",
            expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while fetching all.*")
    public void testGetAllDevices() throws DeviceManagementException {
        androidDeviceTypeManager.getAllDevices();
    }

    @Test(description = "This test case tests the behaviour of the updateDeviceInfo when the relevant tables "
            + "are not there",
            expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while fetching the.*")
    public void testUpdateDeviceInfo() throws DeviceManagementException {
        Device device = new Device();
        device.setDeviceIdentifier(deviceIdentifier.getId());
        device.setType(deviceIdentifier.getType());
        androidDeviceTypeManager.updateDeviceInfo(deviceIdentifier, device);
    }

    @Test(description = ("This test case tests the behaviour of the enrollDevice when the relevant tables are not "
            + "there"), expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error while enrolling the.*", dependsOnMethods = {"testIsEnrolled"})
    public void testEnrollDevice() throws DeviceManagementException {
        Device device = new Device();
        device.setDeviceIdentifier(deviceIdentifier.getId());
        device.setType(deviceIdentifier.getType());
        Mockito.doReturn(false).when(androidDeviceTypeManager).isEnrolled(Mockito.any());
        androidDeviceTypeManager.enrollDevice(device);
    }

    @Test(description = ("This test case tests the behaviour of the updateDeviceInfo when the relevant tables are not "
            + "there"), expectedExceptions = {DeviceManagementException.class},
            expectedExceptionsMessageRegExp = "Error occurred while updating the.*", dependsOnMethods =
            {"testUpdateDeviceInfo"})
    public void testUpdateDeviceWithMock() throws DeviceManagementException {
        Mockito.doReturn(new Device()).when(androidDeviceTypeManager).getDevice(Mockito.any());
        androidDeviceTypeManager.updateDeviceInfo(deviceIdentifier, sampleDevice);
    }

    @Test(description = "This test case tests the behaviour of addDevice when the relevant tables are not available",
            expectedExceptions = { DeviceTypeMgtPluginException.class },
            expectedExceptionsMessageRegExp = "Error occurred while adding the device .*")
    public void testAddDevice() throws DeviceTypeMgtPluginException {
        propertyBasedPluginDAO.addDevice(sampleDevice);
    }

    @Test(description = "This test case tests the behaviour of getDevice when the relevant tables are not available",
            expectedExceptions = { DeviceTypeMgtPluginException.class },
            expectedExceptionsMessageRegExp = "Error occurred while fetching device .*")
    public void testGetPropertyBasedDevice() throws DeviceTypeMgtPluginException {
        propertyBasedPluginDAO.getDevice("id");
    }

    @Test(description = "This test case tests the behaviour of the getAllDevices method of the PropertyBasedPuginDAO",
            expectedExceptions = {DeviceTypeMgtPluginException.class}, expectedExceptionsMessageRegExp = "Error "
            + "occurred while fetching all.*")
    public void testGetAllPropertyBasedDevices() throws DeviceTypeMgtPluginException {
        propertyBasedPluginDAO.getAllDevices();
    }

    @Test(description = "This test case tests the behaviour of the updateDevice method of the PropertyBasedPuginDAO",
            expectedExceptions = {DeviceTypeMgtPluginException.class}, expectedExceptionsMessageRegExp = "Error "
            + "occurred while modifying the device.*")
    public void testUpdateDevice() throws DeviceTypeMgtPluginException {
        propertyBasedPluginDAO.updateDevice(sampleDevice);
    }

    /**
     * To create a defective device type manager for testing.
     * @throws NoSuchFieldException No Such Field Exception.
     * @throws SAXException SAX Exception.
     * @throws JAXBException JAXB Exception
     * @throws ParserConfigurationException Parser Configuration Exception.
     * @throws DeviceTypeConfigurationException Device Type Configuration Exception.
     * @throws IOException IO Exception.
     * @throws IllegalAccessException Illegal Access Exception.
     */
    private void createDefectiveDeviceTypeManager()
            throws NoSuchFieldException, SAXException, JAXBException, ParserConfigurationException,
            DeviceTypeConfigurationException, IOException, IllegalAccessException {
        Field datasourceField = DeviceTypeDAOHandler.class.getDeclaredField("dataSource");
        datasourceField.setAccessible(true);
        Field currentConnection = DeviceTypeDAOHandler.class.getDeclaredField("currentConnection");
        currentConnection.setAccessible(true);
        Field deviceTypePluginDAOField = DeviceTypePluginDAOManager.class.getDeclaredField("deviceTypePluginDAO");
        deviceTypePluginDAOField.setAccessible(true);
        Field deviceTypeDAOHandlerField = DeviceTypePluginDAOManager.class.getDeclaredField("deviceTypeDAOHandler");
        deviceTypeDAOHandlerField.setAccessible(true);

        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(Utils.DEVICE_TYPE_FOLDER + "android.xml");
        File androidConfiguration = null;
        if (resourceUrl != null) {
            androidConfiguration = new File(resourceUrl.getFile());
        }
        DeviceTypeConfiguration androidDeviceConfiguration = Utils.getDeviceTypeConfiguration(androidConfiguration);
        androidDeviceTypeManager = Mockito.mock(DeviceTypeManager.class, Mockito.CALLS_REAL_METHODS);

        deviceTypeDAOHandler = Mockito
                .mock(DeviceTypeDAOHandler.class, Mockito.CALLS_REAL_METHODS);

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:notexist;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        datasourceField.set(deviceTypeDAOHandler, dataSource);
        currentConnection.set(deviceTypeDAOHandler, new ThreadLocal<Connection>());

        DeviceDAODefinition deviceDAODefinition = Utils.getDeviceDAODefinition(androidDeviceConfiguration);
        DeviceTypePluginDAOImpl deviceTypePluginDAO = new DeviceTypePluginDAOImpl(deviceDAODefinition,
                deviceTypeDAOHandler);
        DeviceTypePluginDAOManager deviceTypePluginDAOManager = Mockito
                .mock(DeviceTypePluginDAOManager.class, Mockito.CALLS_REAL_METHODS);
        deviceTypePluginDAOField.set(deviceTypePluginDAOManager, deviceTypePluginDAO);
        deviceTypeDAOHandlerField.set(deviceTypePluginDAOManager, deviceTypeDAOHandler);

        Field deviceTypePluginDAOManagerField = DeviceTypeManager.class.getDeclaredField("deviceTypePluginDAOManager");
        deviceTypePluginDAOManagerField.setAccessible(true);
        deviceTypePluginDAOManagerField.set(androidDeviceTypeManager, deviceTypePluginDAOManager);

        Field propertiesExist = DeviceTypeManager.class.getDeclaredField("propertiesExist");
        propertiesExist.setAccessible(true);
        Field deviceType = DeviceTypeManager.class.getDeclaredField("deviceType");
        deviceType.setAccessible(true);

        deviceType.set(androidDeviceTypeManager, ANDROID_DEVICE_TYPE);
        propertiesExist.set(androidDeviceTypeManager, true);
    }
}
