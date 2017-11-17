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
package org.wso2.carbon.device.mgt.core.common;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.mock.MockDataSource;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

public abstract class BaseDeviceManagementTest {
    protected static final String DATASOURCE_EXT = ".xml";
    private DataSource dataSource;
    private static String datasourceLocation;
    private static boolean mock;

    @BeforeSuite
    @Parameters({"datasource", "isMock"})
    public void setupDataSource(@Optional("src/test/resources/config/datasource/data-source-config") String datasource,
                                @Optional("false") boolean isMock)
            throws Exception {
        datasourceLocation = datasource;
        mock = isMock;
        this.initDataSource();
        this.initSQLScript();
        this.initializeCarbonContext();
        this.initServices();
    }

    protected void initDataSource() throws Exception {
        this.dataSource = this.getDataSource(this.
                readDataSourceConfig(datasourceLocation + DATASOURCE_EXT));
        DeviceManagementDAOFactory.init(dataSource);
        GroupManagementDAOFactory.init(dataSource);
        OperationManagementDAOFactory.init(dataSource);
        NotificationManagementDAOFactory.init(dataSource);
    }

    private void initServices() throws DeviceManagementException, RegistryException, AxisFault {
        DeviceConfigurationManager.getInstance().initConfig();
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        DeviceManagementDataHolder.getInstance().setEmailSenderService(new TestEmailSenderService());
        DeviceManagementDataHolder.getInstance().setConfigurationContextService(getConfigContextService());
    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    private ConfigurationContextService getConfigContextService() throws RegistryException, AxisFault {
        ConfigurationContext context = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                ("src/test/resources/carbon-home/repository/conf/axis2/axis2.xml");
        return new ConfigurationContextService(context, null);
    }

    @BeforeClass
    public abstract void init() throws Exception;

    protected DataSource getDataSource(DataSourceConfig config) {
        if (!isMock()) {
            PoolProperties properties = new PoolProperties();
            properties.setUrl(config.getUrl());
            properties.setDriverClassName(config.getDriverClassName());
            properties.setUsername(config.getUser());
            properties.setPassword(config.getPassword());
            return new org.apache.tomcat.jdbc.pool.DataSource(properties);
        } else {
            return new MockDataSource(config.getUrl());
        }
    }

    private void initializeCarbonContext() {

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

    protected DataSourceConfig readDataSourceConfig(String configLocation) throws DeviceManagementException {
        try {
            File file = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementException("Error occurred while reading data source configuration", e);
        }
    }

    private void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/h2.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

    protected String getDatasourceLocation() throws Exception {
        if (datasourceLocation == null) {
            throw new Exception("Data source location is null!!!");
        }
        return datasourceLocation;
    }

    protected boolean isMock() {
        return mock;
    }
}
