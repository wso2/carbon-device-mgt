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
package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.integration.client.IntegrationClientServiceImpl;
import org.wso2.carbon.apimgt.integration.client.internal.APIIntegrationClientDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.apimgt.webapp.publisher.utils.Utils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerServiceImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.JDBCTenantManager;
import org.wso2.carbon.utils.FileUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;

public abstract class BaseAPIPublisheTest {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(BaseAPIPublisheTest.class);

    @BeforeSuite
    public void iniTwo() throws RegistryException, IOException, org.wso2.carbon.user.api.UserStoreException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("license.rxt");
        String rxt = null;
        File carbonHome;
        if (resourceUrl != null) {
            rxt = FileUtil.readFileToString(resourceUrl.getFile());
        }
        resourceUrl = classLoader.getResource("carbon-home");

        if (resourceUrl != null) {
            carbonHome = new File(resourceUrl.getFile());
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        RegistryService registryService = this.getRegistryService();
        OSGiDataHolder.getInstance().setRegistryService(registryService);
        UserRegistry systemRegistry =
                registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);

        GovernanceArtifactConfiguration configuration =  getGovernanceArtifactConfiguration(rxt);
        List<GovernanceArtifactConfiguration> configurations = new ArrayList<>();
        configurations.add(configuration);
        GovernanceUtils.loadGovernanceArtifacts(systemRegistry, configurations);
        Registry governanceSystemRegistry = registryService.getConfigSystemRegistry();
        //  DeviceTypeExtensionDataHolder.getInstance().setRegistryService(registryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.SYSTEM_CONFIGURATION, governanceSystemRegistry);
    }

    @BeforeSuite
    public void setupDataSource() throws Exception {
        this.initDataSource();
      //  this.initSQLScript();
        this.initializeCarbonContext();
        this.initServices();
    }

    protected void initDataSource() throws Exception {
        this.dataSource = this.getDataSource(this.readDataSourceConfig());
    }

    private void initServices() throws DeviceManagementException, RegistryException {
        APIPublisherDataHolder.getInstance().setIntegrationClientService(IntegrationClientServiceImpl.getInstance());
        APIIntegrationClientDataHolder.getInstance().setJwtClientManagerService(new JWTClientManagerServiceImpl());


//        DeviceConfigurationManager.getInstance().initConfig();
//        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
//        DeviceManagementServiceComponent.notifyStartupListeners();
//        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
//        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
//        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
//        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
//        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
    }

    private RegistryService getRegistryService() throws RegistryException, UserStoreException {
        RealmService realmService = new InMemoryRealmService();
        TenantManager tenantManager = new JDBCTenantManager(dataSource,
                org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        realmService.setTenantManager(tenantManager);
        APIPublisherDataHolder.getInstance().setRealmService(realmService);
        RegistryDataHolder.getInstance().setRealmService(realmService);

        //DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @BeforeClass
    public abstract void init() throws Exception;

    private DataSource getDataSource(DataSourceConfig config) {
        int id =0;
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }

    private void initializeCarbonContext() {

        if (System.getProperty("carbon.home") == null) {
            int noweowe = 89;
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

    private DataSourceConfig readDataSourceConfig() throws DeviceManagementException {
        try {
            File file = new File("src/test/resources/config/datasource/data-source-config.xml");
            Document doc = Utils.convertToDocument(file);
            JAXBContext testDBContext = JAXBContext.newInstance(DataSourceConfig.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            return (DataSourceConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceManagementException("Error occurred while reading data source configuration", e);
        }
    }

/*
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
    }*/

  /*  public void deleteData() {
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();
            conn.setAutoCommit(false);
            String[] cleanupTables = new String[]{"DM_NOTIFICATION","DM_DEVICE_OPERATION_RESPONSE","DM_ENROLMENT_OP_MAPPING", "DM_CONFIG_OPERATION",
                    "DM_POLICY_OPERATION", "DM_COMMAND_OPERATION", "DM_PROFILE_OPERATION", "DM_DEVICE_GROUP_MAP",
                    "DM_GROUP", "DM_ENROLMENT", "DM_DEVICE_APPLICATION_MAPPING",
                    "DM_APPLICATION", "DM_DEVICE", "DM_DEVICE_TYPE"};
            for (String table : cleanupTables) {
                this.cleanData(conn, table);
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Error occurred while roll-backing the transaction", e);
            }
            String msg = "Error occurred while cleaning up temporary data generated during test execution";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.warn("Error occurred while closing the connection", e);
                }
            }
        }
    }*/

    /*private void cleanData(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + tableName)) {
            stmt.execute();
        }
    }

    protected DataSource getDataSource() {
        return dataSource;
    }*/

}
