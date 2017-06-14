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
package org.wso2.carbon.device.application.mgt.core.dao.impl.platform;

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.core.dao.PlatformDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericPlatformDAOImpl extends AbstractDAOImpl implements PlatformDAO {

    @Override
    public int register(String tenantDomain, Platform platform) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            int platformId = getPlatformId(tenantDomain, platform.getIdentifier());
            if (platformId == -1) {
                Connection connection = ConnectionManagerUtil.getConnection();
                if (!platform.isFileBased()) {
                    String insertToPlatform = "INSERT INTO APPM_PLATFORM (IDENTIFIER, TENANT_DOMAIN, NAME, FILE_BASED,  DESCRIPTION, IS_SHARED, ICON_NAME)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setString(2, tenantDomain);
                    preparedStatement.setString(3, platform.getName());
                    preparedStatement.setBoolean(4, false);
                    preparedStatement.setString(5, platform.getDescription());
                    preparedStatement.setBoolean(6, platform.isShared());
                    preparedStatement.setString(7, platform.getIconName());
                    preparedStatement.execute();

                    platformId = getPlatformId(tenantDomain, platform.getIdentifier());
                    String insertPlatformProps = "INSERT INTO APPM_PLATFORM_PROPERTIES (PLATFORM_ID, PROP_NAME, OPTIONAL, DEFAULT_VALUE) VALUES " +
                            "( ? , ?, ? , ?)";
                    for (Platform.Property property : platform.getProperties()) {
                        preparedStatement = connection.prepareStatement(insertPlatformProps);
                        preparedStatement.setInt(1, platformId);
                        preparedStatement.setString(2, property.getName());
                        preparedStatement.setBoolean(3, property.isOptional());
                        preparedStatement.setString(4, property.getDefaultValue());
                        preparedStatement.execute();
                    }
                } else {
                    String insertToPlatform = "INSERT INTO APPM_PLATFORM (IDENTIFIER, TENANT_DOMAIN, FILE_BASED)" +
                            " VALUES (?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setString(2, tenantDomain);
                    preparedStatement.setBoolean(3, true);
                    preparedStatement.execute();
                }
                if (platformId == -1) {
                    platformId = getPlatformId(tenantDomain, platform.getIdentifier());
                }
                ConnectionManagerUtil.commitTransaction();
                return platformId;
            } else {
                if (!platform.isFileBased()) {
                    ConnectionManagerUtil.rollbackTransaction();
                    throw new PlatformManagementDAOException("Platform - " + platform.getIdentifier()
                            + " is already registered for tenant - " + tenantDomain);
                } else {
                    return platformId;
                }
            }
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platform.getIdentifier() + " for tenant - " + tenantDomain, e);
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred while performing the transaction on the database " +
                    "for adding the platform - " + platform.getIdentifier() + " , tenant domain - " + tenantDomain);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public void update(String tenantDomain, String oldPlatformIdentifier, Platform platform) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            int platformId = getPlatformId(tenantDomain, oldPlatformIdentifier);
            if (platformId != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();
                if (!platform.isFileBased()) {
                    String insertToPlatform = "UPDATE APPM_PLATFORM SET IDENTIFIER = ?, NAME =?, DESCRIPTION=?, " +
                            "IS_SHARED=?, ICON_NAME=? WHERE ID = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setString(2, platform.getName());
                    preparedStatement.setString(3, platform.getDescription());
                    preparedStatement.setBoolean(4, platform.isShared());
                    preparedStatement.setString(5, platform.getIconName());
                    preparedStatement.execute();

                    platformId = getPlatformId(tenantDomain, platform.getIdentifier());
                    String deletePlatformProps = "DELETE FROM APPM_PLATFORM_PROPERTIES WHERE PLATFORM_ID=?";
                    preparedStatement = connection.prepareStatement(deletePlatformProps);
                    preparedStatement.setInt(1, platformId);
                    preparedStatement.execute();

                    String insertPlatformProps = "INSERT INTO APPM_PLATFORM_PROPERTIES (PLATFORM_ID, PROP_NAME, OPTIONAL," +
                            " DEFAULT_VALUE) VALUES ( ? , ?, ? , ?)";
                    for (Platform.Property property : platform.getProperties()) {
                        preparedStatement = connection.prepareStatement(insertPlatformProps);
                        preparedStatement.setInt(1, platformId);
                        preparedStatement.setString(2, property.getName());
                        preparedStatement.setBoolean(3, property.isOptional());
                        preparedStatement.setString(4, property.getDefaultValue());
                        preparedStatement.execute();
                    }
                } else {
                    String insertToPlatform = "UPDATE APPM_PLATFORM SET IDENTIFIER = ? WHERE ID = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setInt(1, platformId);
                    preparedStatement.execute();
                }
                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Cannot find any platform that was registered with identifier - "
                        + platform.getIdentifier() + " for tenant - " + tenantDomain);
            }
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platform.getIdentifier() + " for tenant - " + tenantDomain, e);
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred while performing the transaction on the database " +
                    "for adding the platform - " + platform.getIdentifier() + " , tenant domain - " + tenantDomain);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    private int getPlatformId(String tenantDomain, String platformIdentifier) throws PlatformManagementDAOException {
        String query = "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND IDENTIFIER=?) OR (IS_SHARED = TRUE AND IDENTIFIER=?)";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformIdentifier);
            preparedStatement.setString(3, platformIdentifier);
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
    public void unregister(String tenantDomain, String platformIdenfier) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            int platformId = getPlatformId(tenantDomain, platformIdenfier);
            if (platformId != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();
                String deletePlatform = "DELETE FROM APPM_PLATFORM WHERE ID = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deletePlatform);
                preparedStatement.setInt(1, platformId);
                preparedStatement.execute();
                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Platform identifier - " + platformIdenfier
                        + " is already unregistered registered for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to start the transaction while trying to register the platform - "
                    + platformIdenfier + " for tenant - " + tenantDomain, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platformIdenfier + " for tenant - " + tenantDomain, e);
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

    public void addMapping(String tenantDomain, List<String> platformIdentifiers) throws PlatformManagementDAOException {
        String insertMapping = "INSERT INTO APPM_PLATFORM_TENANT_MAPPING(TENANT_DOMAIN, PLATFORM_ID) VALUES (?, ?)";
        try {
            ConnectionManagerUtil.beginTransaction();
            for (String platformIdentifier : platformIdentifiers) {
                if (getTenantPlatformMapping(tenantDomain, platformIdentifier) != -1) {
                    int platformId = getPlatformId(tenantDomain, platformIdentifier);
                    Connection connection = ConnectionManagerUtil.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(insertMapping);
                    preparedStatement.setString(1, tenantDomain);
                    preparedStatement.setInt(2, platformId);
                    preparedStatement.execute();
                } else {
                    throw new PlatformManagementDAOException("Platform identifier - " + platformIdentifier + " is already assigned to tenant domain - " + tenantDomain);
                }
            }
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occured while trying to add the mapping of platform - "
                    + platformIdentifiers.toString() + " for tenant - " + tenantDomain, e);
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

    private int getTenantPlatformMapping(String tenantDomain, String platformIdentifier) throws PlatformManagementDAOException {
        String getMapping = "SELECT MAPPING.ID as ID FROM (SELECT ID FROM APPM_PLATFORM_TENANT_MAPPING WHERE TENANT_DOMAIN=?) MAPPING JOIN " +
                "(SELECT ID FROM APPM_PLATFORM WHERE APPM_PLATFORM.IDENTIFIER=?) PLATFORM ON MAPPING.PLATFORM_ID=PLATFORM.ID";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getMapping);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformIdentifier);
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

    public void removeMapping(String tenantDomain, String platformIdentifier) throws PlatformManagementDAOException {
        String deleteMapping = "DELETE FROM APPM_PLATFORM_TENANT_MAPPING WHERE ID = ?";
        try {
            ConnectionManagerUtil.beginTransaction();
            int mappingId = getTenantPlatformMapping(tenantDomain, platformIdentifier);
            if (mappingId != -1) {
                Connection connection = ConnectionManagerUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(deleteMapping);
                preparedStatement.setInt(1, mappingId);
                preparedStatement.execute();
                ConnectionManagerUtil.commitTransaction();
            } else {
                throw new PlatformManagementDAOException("Platform - " + platformIdentifier
                        + " is already unassigned for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred while unassigning the platform - " + platformIdentifier
                    + " for tenant - " + tenantDomain);
        } catch (SQLException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occurred while executing the query - " + deleteMapping);
        } catch (PlatformManagementDAOException ex) {
            ConnectionManagerUtil.rollbackTransaction();
            throw ex;
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public void removeMappingTenants(String platformIdentifier) throws PlatformManagementDAOException {
        int platformId = getPlatformId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, platformIdentifier);
        String getMapping = "DELETE FROM APPM_PLATFORM_TENANT_MAPPING WHERE TENANT_DOMAIN != ? AND PLATFORM_ID=?";
        try {
            ConnectionManagerUtil.openConnection();
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(getMapping);
            preparedStatement.setString(1, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            preparedStatement.setInt(2, platformId);
            preparedStatement.execute();
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occured while obtaining the connection to get the existing " +
                    "Tenant - Platform Mapping.", e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occured while executing the SQL query - " + getMapping, e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }


    @Override
    public List<Platform> getPlatforms(String tenantDomain) throws PlatformManagementDAOException {
        String selectQuery = "SELECT MAPPING.ID, PLATFORM.IDENTIFIER FROM (SELECT * FROM APPM_PLATFORM WHERE TENANT_DOMAIN=? OR IS_SHARED = TRUE AND FILE_BASED = FALSE) PLATFORM " +
                "LEFT JOIN APPM_PLATFORM_TENANT_MAPPING MAPPING ON PLATFORM.ID = MAPPING.PLATFORM_ID";
        try {
            Connection connection = ConnectionManagerUtil.openConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, tenantDomain);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (resultSet.next()) {
                String identifier = resultSet.getString("PLATFORM.IDENTIFIER");
                int mappingID = resultSet.getInt("MAPPING.ID");
                Platform platform = getPlatform(tenantDomain, identifier);
                if (mappingID != 0) {
                    platform.setEnabled(true);
                } else {
                    platform.setEnabled(false);
                }
                platforms.add(platform);
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

    public Platform getPlatform(String tenantDomain, String platformIdenfier) throws PlatformManagementDAOException {
        String platformQuery = "SELECT * FROM (SELECT * FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND IDENTIFIER=?) " +
                "OR (IS_SHARED = TRUE AND IDENTIFIER=?) AND FILE_BASED = FALSE ) PLATFORM " +
                "LEFT JOIN APPM_PLATFORM_PROPERTIES PROPS ON PLATFORM.ID = PROPS.PLATFORM_ID";
        try {
            ConnectionManagerUtil.openConnection();
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(platformQuery);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformIdenfier);
            preparedStatement.setString(3, platformIdenfier);
            ResultSet resultSet = preparedStatement.executeQuery();
            Platform platform = new Platform();
            if (resultSet.next()) {
                platform.setId(resultSet.getInt("PLATFORM.ID"));
                platform.setIdentifier(platformIdenfier);
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
                platform.setIdentifier(platformIdenfier);
                platform.setFileBased(true);
            }
            return platform;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error when loading the platform - " + platformIdenfier, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error in executing the query - " + platformQuery, e);
        }
    }

}
