/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*
*/

package org.wso2.carbon.device.application.mgt.core.dao.impl;


import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.core.dao.PlatformDAO;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlatformDAOImpl implements PlatformDAO {

    @Override
    public void register(String tenantDomain, Platform platform) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            if (getPlatformId(tenantDomain, platform.getCode()) == -1) {
                Connection connection = ConnectionManagerUtil.getConnection();

                String insertToPlatform = "INSERT INTO APPM_PLATFORM (CODE, TENANT_DOMAIN, NAME, DESCRIPTION, IS_SHARED, ICON_NAME)" +
                        " VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                preparedStatement.setString(1, platform.getCode());
                preparedStatement.setString(2, tenantDomain);
                preparedStatement.setString(3, platform.getName());
                preparedStatement.setString(4, platform.getDescription());
                preparedStatement.setBoolean(5, platform.isShared());
                preparedStatement.setString(6, platform.getIconName());
                preparedStatement.execute();

                int platformID = getPlatformId(tenantDomain, platform.getCode());

                String insertPlatformProps = "INSERT INTO APPM_PLATFORM_PROPERTIES (PLATFORM_ID, PROP_NAME, OPTIONAL, DEFAULT_VALUE) VALUES " +
                        "( ? , ?, ? , ?)";
                for (Platform.Property property : platform.getProperties()) {
                    preparedStatement = connection.prepareStatement(insertPlatformProps);
                    preparedStatement.setInt(1, platformID);
                    preparedStatement.setString(2, property.getName());
                    preparedStatement.setBoolean(3, property.isOptional());
                    preparedStatement.setString(4, property.getDefaultValue());
                    preparedStatement.execute();
                }
                ConnectionManagerUtil.commitTransaction();
            } else {
                ConnectionManagerUtil.rollbackTransaction();
                throw new PlatformManagementDAOException("Platform - " + platform.getCode()
                        + " is already registered for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to start the transaction while trying to register the platform - "
                    + platform.getCode() + " for tenant - " + tenantDomain, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platform.getCode() + " for tenant - " + tenantDomain, e);
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    private int getPlatformId(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String query = "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND CODE=?) OR (IS_SHARED = TRUE AND CODE=?)";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformCode);
            preparedStatement.setString(3, platformCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error when trying to obtaining the database connection.", e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error in executing the query - " + query, e);
        }
    }


    @Override
    public void unregister(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            int platformId = getPlatformId(tenantDomain, platformCode);
            if (platformId != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();

                String deletePlatform = "DELETE FROM APPM_PLATFORM WHERE ID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deletePlatform);
                preparedStatement.setInt(1, platformId);
                preparedStatement.execute();

                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Platform - " + platformCode
                        + " is already unregistered registered for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to start the transaction while trying to register the platform - "
                    + platformCode + " for tenant - " + tenantDomain, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platformCode + " for tenant - " + tenantDomain, e);
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    public void addMapping(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String insertMapping = "INSERT INTO APPM_PLATFORM_TENANT_MAPPING(TENANT_DOMAIN, PLATFORM_CODE) VALUES (?, ?)";
        try {
            ConnectionManagerUtil.beginTransaction();
            if (getTenantPlatformMapping(tenantDomain, platformCode) != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(insertMapping);
                preparedStatement.execute();
                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Platform - " + platformCode + " is already assigned to tenant domain - " + tenantDomain);
            }
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occured while trying to add the mapping of platform - "
                    + platformCode + " for tenant - " + tenantDomain, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred when getting the connection for the database. ", e);
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occured while executing the SQL query - " + insertMapping, e);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    private int getTenantPlatformMapping(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String getMapping = "SELECT ID FROM APPM_PLATFORM_TENANT_MAPPING WHERE TENANT_DOMAIN=? AND PLATFORM_CODE=?";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getMapping);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occured while obtaining the connection to get the existing " +
                    "Tenant - Platform Mapping.", e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occured while executing the SQL query - " + getMapping, e);
        }
    }

    public void removeMapping(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String deleteMapping = "DELETE FROM APPM_PLATFORM_TENANT_MAPPING WHERE ID = ?";
        try {
            ConnectionManagerUtil.beginTransaction();
            int mappingId = getTenantPlatformMapping(tenantDomain, platformCode);
            if (mappingId != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();

                PreparedStatement preparedStatement = connection.prepareStatement(deleteMapping);
                preparedStatement.setInt(1, mappingId);
                preparedStatement.execute();

                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Platform - " + platformCode
                        + " is already unassigned for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred while unassigning the platform - " + platformCode
                    + " for tenant - " + tenantDomain);
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occured while executing the query - " + deleteMapping);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public List<Platform> getPlatforms(String tenantDomain) throws PlatformManagementDAOException {
        String selectQuery = "SELECT PLATFORM_CODE FROM APPM_PLATFORM_TENANT_MAPPING WHERE TENANT_DOMAIN=?";
        try {
            Connection connection = ConnectionManagerUtil.openConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (resultSet.next()) {
                String platformCode = resultSet.getString(1);
                platforms.add(getPlatform(tenantDomain, platformCode));
            }
            return platforms;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occured when loading the platforms for tenant - " + tenantDomain, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occured when executing query - " + selectQuery, e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    private Platform getPlatform(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String platformQuery = "SELECT * FROM (SELECT * FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND CODE=?) PLATFORM " +
                "LEFT JOIN APPM_PLATFORM_PROPERTIES PROPS ON PLATFORM.ID = PROPS.PLATFORM_ID ORDER BY PLATFORM.CODE DESC";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(platformQuery);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            Platform platform = new Platform();
            if (resultSet.next()) {
                platform.setId(resultSet.getInt("PLATFORM.ID"));
                platform.setCode(platformCode);
                platform.setName(resultSet.getString("PLATFORM.NAME"));
                platform.setIconName(resultSet.getString("PLATFORM.DESCRIPTION"));
                platform.setIconName(resultSet.getString("PLATFORM.ICON_NAME"));
                platform.setShared(resultSet.getBoolean("PLATFORM.IS_SHARED"));
                platform.setFileBased(false);
                List<Platform.Property> properties = new ArrayList<>();
                do {
                    if (resultSet.getString("PROPS.PROP_NAME") != null) {
                        Platform.Property property = new Platform.Property();
                        property.setName(resultSet.getString("PROPS.PROP_NAME"));
                        property.setOptional(resultSet.getBoolean("PROPS.OPTIONAL"));
                        property.setDefaultValue(resultSet.getString("PROPS.DEFAUL_VALUE"));
                        properties.add(property);
                    }
                } while (resultSet.next());
                platform.setProperties(properties);
            } else {
                platform.setCode(platformCode);
                platform.setFileBased(true);
            }
            return platform;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error when loading the platform - " + platformCode, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error in executing the query - " + platformQuery, e);
        }
    }
}
