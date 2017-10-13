/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.internal.collections.Pair;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.common.DataSourceConfig;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.ProfileManager;
import org.wso2.carbon.policy.mgt.core.mgt.impl.ProfileManagerImpl;
import org.wso2.carbon.policy.mgt.core.services.SimplePolicyEvaluationTest;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BasePolicyManagementDAOTest {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(BasePolicyManagementDAOTest.class);

    protected DeviceManagementProviderService deviceMgtService;
    protected GroupManagementProviderService groupMgtService;
    protected ProfileManager profileManager;

    private static final String ADMIN_USER = "admin";

    @BeforeSuite
    public void setupDataSource() throws Exception {
        this.initDatSource();
        this.initSQLScript();
        this.initiatePrivilegedCaronContext();
        DeviceConfigurationManager.getInstance().initConfig();
    }

    protected void initializeServices() throws Exception{
        initSQLScript();

        DeviceConfigurationManager.getInstance().initConfig();

        deviceMgtService = new DeviceManagementProviderServiceImpl();
        groupMgtService = new GroupManagementProviderServiceImpl();

        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(
                new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupMgtService);
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);

        PolicyEvaluationPoint policyEvaluationPoint = new SimplePolicyEvaluationTest();
        PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint("Simple", policyEvaluationPoint);
        PolicyManagementDataHolder.getInstance().setDeviceManagementService(deviceMgtService);

        profileManager = new ProfileManagerImpl();
    }

    public void initDatSource() throws Exception {
        this.dataSource = this.getDataSource(this.readDataSourceConfig());
        DeviceManagementDAOFactory.init(dataSource);
        PolicyManagementDAOFactory.init(dataSource);
        OperationManagementDAOFactory.init(dataSource);
        GroupManagementDAOFactory.init(dataSource);
    }

    public void initiatePrivilegedCaronContext() throws Exception {


        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

    }

    private DataSource getDataSource(DataSourceConfig config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    private DataSourceConfig readDataSourceConfig() throws PolicyManagementException {
        try {
            File file = new File("src/test/resources/config/datasource/data-source-config.xml");
            Document doc = PolicyManagerUtil.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new PolicyManagementException("Error occurred while reading data source configuration", e);
        }
    }

    protected void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/CreateH2TestDB.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }


    protected Object changeFieldValue(Object targetObj, String fieldName, Object valueObj)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = targetObj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object oldVal = field.get(targetObj);
        field.set(targetObj, valueObj);
        return oldVal;
    }

    protected RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(
                "carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    protected boolean enrollDevice(String deviceName, String deviceType) {
        boolean success = false;
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo(
                ADMIN_USER, EnrolmentInfo.OwnerShip.BYOD, EnrolmentInfo.Status.ACTIVE);
        Device device1 = new Device(deviceName, deviceType, deviceName, deviceName, enrolmentInfo, null, null);
        try {
            success = deviceMgtService.enrollDevice(device1);
        } catch (DeviceManagementException e) {
            String msg = "Failed to enroll a device.";
            log.error(msg, e);
            Assert.fail();
        }
        return success;
    }

    protected void createDeviceGroup(String groupName) {
        DeviceGroup deviceGroup = new DeviceGroup(groupName);
        deviceGroup.setDescription(groupName);
        deviceGroup.setOwner(ADMIN_USER);
        try {
            groupMgtService.createGroup(deviceGroup, null, null);
        } catch (GroupAlreadyExistException | GroupManagementException e) {
            String msg = "Failed to create group: " + groupName;
            log.error(msg, e);
            Assert.fail(msg);
        }
    }

    protected void addDeviceToGroup(DeviceIdentifier deviceIdentifier, String groupName) {
        List<DeviceIdentifier> groupDevices = new ArrayList<>();
        groupDevices.add(deviceIdentifier);
        try {
            DeviceGroup group = groupMgtService.getGroup(groupName);
            groupMgtService.addDevices(group.getGroupId(), groupDevices);
        } catch (DeviceNotFoundException | GroupManagementException e) {
            String msg = "Failed to add device " + deviceIdentifier.getId() + " to group " + groupName;
            log.error(msg, e);
            Assert.fail(msg);
        }
    }

    public interface Command {
        void call(Profile profile) throws Exception;
    }

    protected void testThrowingException(Profile profile, Command command, String fieldName, Object mockObj,
                                       Class<?> exceptionClass) throws Exception {
        Object oldObj = changeFieldValue(profileManager, fieldName, mockObj);
        try {
            command.call(profile);
        } catch (Exception e) {
            if (!(e.getCause().getClass().getName().equals(exceptionClass.getName()))) {
                throw e;
            }
        } finally {
            changeFieldValue(profileManager, fieldName, oldObj);
        }
    }

    protected Pair<Connection, Pair<DataSource, DataSource>> mockConnection() throws Exception {
        //Throwing PolicyManagerDAOException while adding profile
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");

        Connection conn = mock(Connection.class);
        when(conn.getMetaData()).thenReturn(databaseMetaData);

        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(conn);

        Field dataSourceField = PolicyManagementDAOFactory.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        DataSource oldDataSource = (DataSource) dataSourceField.get(null);
        PolicyManagementDAOFactory.init(dataSource);

        return new Pair<>(conn, new Pair<>(oldDataSource, dataSource));
    }
}
