/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.policy.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.wso2.carbon.policy.mgt.common.Feature;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.common.DBTypes;
import org.wso2.carbon.policy.mgt.core.common.TestDBConfiguration;
import org.wso2.carbon.policy.mgt.core.common.TestDBConfigurations;
import org.wso2.carbon.policy.mgt.core.dao.FeatureManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;
import org.wso2.carbon.policy.mgt.core.dao.impl.FeatureDAOImpl;
import org.wso2.carbon.policy.mgt.core.dao.impl.PolicyDAOImpl;
import org.wso2.carbon.policy.mgt.core.util.FeatureCreator;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class PolicyDAOTestCase {


    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(PolicyDAOTestCase.class);

    @BeforeClass
    @Parameters("dbType")
    public void setUpDB(String dbTypeStr) throws Exception {
        DBTypes dbType = DBTypes.valueOf(dbTypeStr);
        TestDBConfiguration dbConfig = getTestDBConfiguration(dbType);
        PoolProperties properties = new PoolProperties();

        log.info("Database Type : " + dbTypeStr);

        switch (dbType) {

            case MySql:

                log.info("Mysql Called..................................................." + dbTypeStr);

                properties.setUrl(dbConfig.getConnectionUrl());
                properties.setDriverClassName(dbConfig.getDriverClass());
                properties.setUsername(dbConfig.getUserName());
                properties.setPassword(dbConfig.getPwd());
                dataSource = new org.apache.tomcat.jdbc.pool.DataSource(properties);
                PolicyManagementDAOFactory.init(dataSource);
                break;

            case H2:

                properties.setUrl(dbConfig.getConnectionUrl());
                properties.setDriverClassName(dbConfig.getDriverClass());
                properties.setUsername(dbConfig.getUserName());
                properties.setPassword(dbConfig.getPwd());
                dataSource = new org.apache.tomcat.jdbc.pool.DataSource(properties);
                this.initH2SQLScript();
                PolicyManagementDAOFactory.init(dataSource);
                break;

            default:
        }
    }

    private TestDBConfiguration getTestDBConfiguration(DBTypes dbType) throws PolicyManagerDAOException,
            PolicyManagementException {
        File deviceMgtConfig = new File("src/test/resources/testdbconfig.xml");
        Document doc;
        TestDBConfigurations dbConfigs;

        doc = PolicyManagerUtil.convertToDocument(deviceMgtConfig);
        JAXBContext testDBContext;

        try {
            testDBContext = JAXBContext.newInstance(TestDBConfigurations.class);
            Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
            dbConfigs = (TestDBConfigurations) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new PolicyManagerDAOException("Error parsing test db configurations", e);
        }
        for (TestDBConfiguration config : dbConfigs.getDbTypesList()) {
            if (config.getDbType().equals(dbType.toString())) {
                return config;
            }
        }
        return null;
    }

    private void initH2SQLScript() throws Exception {
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

    private void initMySQlSQLScript() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.getDataSource().getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/CreateMySqlTestDB.sql'");
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    private DataSource getDataSource() {
        return dataSource;
    }

    @Test
    public void addFeatures() throws FeatureManagerDAOException {

        FeatureDAOImpl policyDAO = new FeatureDAOImpl();
        List<Feature> featureList = FeatureCreator.getFeatureList();
        for (Feature feature : featureList) {
            policyDAO.addFeature(feature);
        }

    }

}
