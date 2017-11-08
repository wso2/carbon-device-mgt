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

import org.h2.jdbcx.JdbcDataSource;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceDetails;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Table;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceDAODefinition;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * This class handles the test utility tasks.
 */
public class Utils {
    public static final String DEVICE_TYPE_FOLDER = "device-types" + File.separator;
    public static final String TEST_STRING = "test";

    /**
     * To get the device type configuration based on the configuration file
     *
     * @param configurationFile Relevant configuration file of a device type
     * @return the DeviceTypeConfiguration object of the relevant Device Type
     * @throws DeviceTypeConfigurationException DeviceType Configuration Exception
     * @throws IOException                      IO Exception
     * @throws SAXException                     SAX Exception
     * @throws ParserConfigurationException     Parser Configuration Exception
     * @throws JAXBException                    JAXB Exception
     */
    public static  DeviceTypeConfiguration getDeviceTypeConfiguration(File configurationFile)
            throws DeviceTypeConfigurationException, IOException, SAXException, ParserConfigurationException,
            JAXBException {

        Document doc = convertToDocument(configurationFile);

        /* Un-marshaling Webapp Authenticator configuration */
        JAXBContext ctx = JAXBContext.newInstance(DeviceTypeConfiguration.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        //unmarshaller.setSchema(getSchema());
        return (DeviceTypeConfiguration) unmarshaller.unmarshal(doc);

    }

    private static Document convertToDocument(File file)
            throws DeviceTypeConfigurationException, ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        return docBuilder.parse(file);
    }

    /**
     * To create the database tables for the particular device-type based on the scripts
     *
     * @param databaseName Name of the Database
     * @param scriptFilePath Path of the SQL script File
     * @throws IOException  IO Exception
     * @throws SQLException SQL Exception.
     */
    public static DataSource createDataTables(String databaseName, String scriptFilePath) throws IOException,
            SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");

        File file = new File(scriptFilePath);

        final String LOAD_DATA_QUERY = "RUNSCRIPT FROM '" + file.getCanonicalPath() + "'";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(LOAD_DATA_QUERY);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return dataSource;
    }

    /**
     * To get the DeviceDAO Definition from the DeviceTypeConfiguration.
     *
     * @param deviceTypeConfiguration Device Type Configuration which we need the DeviceDAO Definition
     * @return DeviceDAO Definition of the particular devicetype
     */
    public static DeviceDAODefinition getDeviceDAODefinition(DeviceTypeConfiguration deviceTypeConfiguration) {
        DeviceDetails deviceDetails = deviceTypeConfiguration.getDeviceDetails();
        DeviceDAODefinition deviceDAODefinition = null;

        if (deviceDetails != null) {
            String tableName = deviceTypeConfiguration.getDeviceDetails().getTableId();
            if (tableName != null && !tableName.isEmpty()) {
                List<Table> tables = deviceTypeConfiguration.getDataSource().getTableConfig().getTable();
                Table deviceDefinitionTable = null;
                for (Table table : tables) {
                    if (tableName.equals(table.getName())) {
                        deviceDefinitionTable = table;
                        break;
                    }
                }
                if (deviceDefinitionTable != null) {
                    deviceDAODefinition = new DeviceDAODefinition(deviceDefinitionTable);
                }
            }
        }
        return deviceDAODefinition;
    }

    /**
     * To get the registry service.
     * @return RegistryService
     * @throws RegistryException Registry Exception
     */
    public static RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = Utils.class.getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }
}
