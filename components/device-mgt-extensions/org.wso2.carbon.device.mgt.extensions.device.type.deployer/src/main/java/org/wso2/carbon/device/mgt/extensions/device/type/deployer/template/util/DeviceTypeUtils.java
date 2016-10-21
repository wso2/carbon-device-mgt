/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.internal.DeviceTypeManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Contains utility methods used by plugin.
 */
public class DeviceTypeUtils {

    private static Log log = LogFactory.getLog(DeviceTypeUtils.class);

    public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        cleanupResources(null, stmt, rs);
    }

    /**
     * Creates the device management schema.
     */
    public static void setupDeviceManagementSchema(DeviceManagementConfiguration deviceManagementConfiguration)
            throws DeviceTypeMgtPluginException {
        String datasourceName = deviceManagementConfiguration.getDeviceManagementConfigRepository()
                .getDataSourceConfig().getJndiLookupDefinition().getJndiName();
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(datasourceName);
            DeviceSchemaInitializer initializer = new DeviceSchemaInitializer(dataSource, deviceManagementConfiguration
                    .getDeviceType());
            String checkSql = "select * from VIRTUAL_FIREALARM_DEVICE";
            if (!initializer.isDatabaseStructureCreated(checkSql)) {
                log.info("Initializing device management repository database schema");
                initializer.createRegistryDatabase();
            } else {
                log.info("Device management repository database already exists. Not creating a new database.");
            }
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + datasourceName, e);
        } catch (Exception e) {
            throw new DeviceTypeMgtPluginException("Error occurred while initializing Device " +
                                                                       "Management database schema", e);
        }
    }

    public static Registry getConfigurationRegistry() throws DeviceTypeMgtPluginException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return DeviceTypeManagementDataHolder.getInstance().getRegistryService()
                    .getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error in retrieving conf registry instance: " + e.getMessage(), e);
        }
    }

    public static boolean putRegistryResource(String path, Resource resource) throws DeviceTypeMgtPluginException {
        boolean status;
        try {
            DeviceTypeUtils.getConfigurationRegistry().beginTransaction();
            DeviceTypeUtils.getConfigurationRegistry().put(path, resource);
            DeviceTypeUtils.getConfigurationRegistry().commitTransaction();
            status = true;
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error occurred while persisting registry resource : " +
                            e.getMessage(), e);
        }
        return status;
    }

    public static Resource getRegistryResource(String path) throws DeviceTypeMgtPluginException {
        try {
            if(DeviceTypeUtils.getConfigurationRegistry().resourceExists(path)){
                return DeviceTypeUtils.getConfigurationRegistry().get(path);
            }
            return null;
        } catch (RegistryException e) {
            throw new DeviceTypeMgtPluginException("Error in retrieving registry resource : " + e.getMessage(), e);
        }
    }

    public static License getDefaultLicense(String deviceType) {
        License license = new License();
        license.setName(deviceType);
        license.setLanguage("en_US");
        license.setVersion("1.0.0");
        license.setText("This End User License Agreement (\"Agreement\") is a legal agreement between you (\"You\") " +
                                "and WSO2, Inc., regarding the enrollment of Your personal mobile device (\"Device\") in SoR's " +
                                "mobile device management program, and the loading to and removal from Your Device and Your use " +
                                "of certain applications and any associated software and user documentation, whether provided in " +
                                "\"online\" or electronic format, used in connection with the operation of or provision of services " +
                                "to WSO2, Inc.,  BY SELECTING \"I ACCEPT\" DURING INSTALLATION, YOU ARE ENROLLING YOUR DEVICE, AND " +
                                "THEREBY AUTHORIZING SOR OR ITS AGENTS TO INSTALL, UPDATE AND REMOVE THE APPS FROM YOUR DEVICE AS " +
                                "DESCRIBED IN THIS AGREEMENT.  YOU ARE ALSO EXPLICITLY ACKNOWLEDGING AND AGREEING THAT (1) THIS IS " +
                                "A BINDING CONTRACT AND (2) YOU HAVE READ AND AGREE TO THE TERMS OF THIS AGREEMENT.\n" +
                                "\n" +
                                "IF YOU DO NOT ACCEPT THESE TERMS, DO NOT ENROLL YOUR DEVICE AND DO NOT PROCEED ANY FURTHER.\n" +
                                "\n" +
                                "You agree that: (1) You understand and agree to be bound by the terms and conditions contained " +
                                "in this Agreement, and (2) You are at least 21 years old and have the legal capacity to enter " +
                                "into this Agreement as defined by the laws of Your jurisdiction.  SoR shall have the right, " +
                                "without prior notice, to terminate or suspend (i) this Agreement, (ii) the enrollment of Your " +
                                "Device, or (iii) the functioning of the Apps in the event of a violation of this Agreement or " +
                                "the cessation of Your relationship with SoR (including termination of Your employment if You are " +
                                "an employee or expiration or termination of Your applicable franchise or supply agreement if You " +
                                "are a franchisee of or supplier to the WSO2 WSO2, Inc., system).  SoR expressly reserves all " +
                                "rights not expressly granted herein.");
        return license;
    }

}
