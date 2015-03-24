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
package org.wso2.carbon.device.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.common.DBTypes;
import org.wso2.carbon.device.mgt.core.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.core.common.TestDBConfigurations;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class DeviceManagementBaseTest {

    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(DeviceManagementBaseTest.class);

    @BeforeClass(alwaysRun = true)
    @Parameters("dbType")
    public void setupDatabase(String dbTypeName) throws Exception {
        DBTypes type = DBTypes.valueOf(dbTypeName);
        TestDBConfiguration config = getTestDBConfiguration(type);
        switch (type) {
            case H2:
                PoolProperties properties = new PoolProperties();
                properties.setUrl(config.getConnectionUrl());
                properties.setDriverClassName(config.getDriverClass());
                properties.setUsername(config.getUserName());
                properties.setPassword(config.getPwd());
                dataSource = new org.apache.tomcat.jdbc.pool.DataSource(properties);
                this.initSQLScript();
            default:
        }
    }
    private TestDBConfiguration getTestDBConfiguration(DBTypes dbType) throws DeviceManagementDAOException,
                                                                              DeviceManagementException {
        File dbConfig = new File("src/test/resources/testdbconfig.xml");
        Document doc = DeviceManagerUtil.convertToDocument(dbConfig);
        TestDBConfigurations dbConfigs;
        JAXBContext testDBContext;

        try {
            testDBContext = JAXBContext.newInstance(TestDBConfigurations.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            dbConfigs = (TestDBConfigurations) unmarshaller.unmarshal(doc);
            for (TestDBConfiguration config : dbConfigs.getDbTypesList()) {
                if (config.getDbType().equals(dbType.toString())) {
                    return config;
                }
            }
            return null;
        } catch (JAXBException e) {
            throw new DeviceManagementDAOException("Error parsing test db configurations", e);
        }
    }

    private void initSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/CreateH2TestDB.sql'");
        } catch(Exception e){
            log.error(e);
            throw e;
        }finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

}
