package org.wso2.carbon.certificate.mgt.core.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.certificate.mgt.core.util.TestUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDeviceManagementCertificateTest {
    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(BaseDeviceManagementCertificateTest.class);

    private static final String DATASOURCE_LOCATION = "src/test/resources/data-source-config.xml";

    @BeforeSuite
    public void setupDataSource() throws Exception {
        this.initDataSource();
        this.initSQLScript();
        initializeCarbonContext();
    }

    public void initDataSource() throws Exception {
        this.dataSource = this.getDataSource(this.readDataSourceConfig());
        DeviceManagementDAOFactory.init(dataSource);
        GroupManagementDAOFactory.init(dataSource);
    }

    @BeforeClass
    public abstract void init() throws Exception;

    private DataSource getDataSource(DataSourceConfig config) {
        PoolProperties properties = new PoolProperties();
        properties.setUrl(config.getUrl());
        properties.setDriverClassName(config.getDriverClassName());
        properties.setUsername(config.getUser());
        properties.setPassword(config.getPassword());
        return new org.apache.tomcat.jdbc.pool.DataSource(properties);
    }


    private DataSourceConfig readDataSourceConfig() throws DeviceManagementException {
        try {
            File file = new File(DATASOURCE_LOCATION);
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



    private void cleanApplicationMappingData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_DEVICE_APPLICATION_MAPPING")) {
            stmt.execute();
        }
    }

    private void cleanApplicationData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_APPLICATION")) {
            stmt.execute();
        }
    }


    private void cleanupEnrolmentData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_ENROLMENT")) {
            stmt.execute();
        }
    }

    private void cleanupDeviceData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_DEVICE")) {
            stmt.execute();
        }
    }

    private void cleanupDeviceTypeData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_DEVICE_TYPE")) {
            stmt.execute();
        }
    }

    private void cleanupGroupData(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM DM_GROUP")) {
            stmt.execute();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}
