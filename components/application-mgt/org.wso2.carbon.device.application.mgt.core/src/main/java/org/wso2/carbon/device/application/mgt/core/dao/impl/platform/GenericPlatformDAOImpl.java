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
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericPlatformDAOImpl extends AbstractDAOImpl implements PlatformDAO {

    @Override
    public Platform getPlatformByIdentifier(String identifier) throws PlatformManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";

        try {
            conn = this.getConnection();
            sql += "SELECT * ";
            sql += "FROM APPM_PLATFORM ";
            sql += "WHERE IDENTIFIER = ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, identifier);
            rs = stmt.executeQuery();

            Platform platform = null;

            if (rs.next()) {
                platform = new Platform();
                platform.setId(rs.getInt("ID"));
                platform.setName(rs.getString("NAME"));
                platform.setIdentifier(rs.getString("IDENTIFIER"));
                platform.setPublished(rs.getBoolean("PUBLISHED"));
            }

            return platform;

        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }


    }

    @Override
    public void register(String tenantDomain, Platform platform) throws PlatformManagementDAOException {
        try {
            ConnectionManagerUtil.beginTransaction();
            if (getPlatformId(tenantDomain, platform.getIdentifier()) == -1) {
                Connection connection = ConnectionManagerUtil.getConnection();

                String insertToPlatform = "INSERT INTO APPM_PLATFORM (CODE, TENANT_DOMAIN, NAME, DESCRIPTION, IS_SHARED, ICON_NAME)" +
                        " VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(insertToPlatform);
                preparedStatement.setString(1, platform.getIdentifier());
                preparedStatement.setString(2, tenantDomain);
                preparedStatement.setString(3, platform.getName());
                preparedStatement.setString(4, platform.getDescription());
                preparedStatement.setBoolean(5, platform.isShared());
                preparedStatement.setString(6, platform.getIconName());
                preparedStatement.execute();

                int platformID = getPlatformId(tenantDomain, platform.getIdentifier());

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
                throw new PlatformManagementDAOException("Platform - " + platform.getIdentifier()
                        + " is already registered for tenant - " + tenantDomain);
            }
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to start the transaction while trying to register the platform - "
                    + platform.getIdentifier() + " for tenant - " + tenantDomain, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Unable to obtain the connection while trying to register the platform - "
                    + platform.getIdentifier() + " for tenant - " + tenantDomain, e);
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

    public void addMapping(String tenantDomain, List<String> platformCodes) throws PlatformManagementDAOException {
        String insertMapping = "INSERT INTO APPM_PLATFORM_TENANT_MAPPING(TENANT_DOMAIN, PLATFORM_CODE) VALUES (?, ?)";
        try {
            ConnectionManagerUtil.beginTransaction();
            for (String platformCode : platformCodes) {
                if (getTenantPlatformMapping(tenantDomain, platformCode) != -1) {
                    Connection connection = ConnectionManagerUtil.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(insertMapping);
                    preparedStatement.setString(1, tenantDomain);
                    preparedStatement.setString(2, platformCode);
                    preparedStatement.execute();
                } else {
                    throw new PlatformManagementDAOException("Platform - " + platformCode + " is already assigned to tenant domain - " + tenantDomain);
                }
            }
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException("Error occured while trying to add the mapping of platform - "
                    + platformCodes.toString() + " for tenant - " + tenantDomain, e);
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
        String selectQuery = "SELECT * FROM (SELECT * FROM APPM_PLATFORM WHERE TENANT_DOMAIN=? OR IS_SHARED = TRUE) PLATFORM " +
                "LEFT JOIN APPM_PLATFORM_TENANT_MAPPING MAPPING ON PLATFORM.CODE = MAPPING.PLATFORM_CODE";
        try {
            Connection connection = ConnectionManagerUtil.openConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (resultSet.next()) {
                String platformCode = resultSet.getString("PLATFORM.CODE");
                int mappingID = resultSet.getInt("MAPPING.ID");
                Platform platform = getPlatform(tenantDomain, platformCode);
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

    public Platform getPlatform(String tenantDomain, String platformCode) throws PlatformManagementDAOException {
        String platformQuery = "SELECT * FROM (SELECT * FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND CODE=?) OR (IS_SHARED = TRUE AND CODE=?)) PLATFORM " +
                "LEFT JOIN APPM_PLATFORM_PROPERTIES PROPS ON PLATFORM.ID = PROPS.PLATFORM_ID";
        try {
            Connection connection = ConnectionManagerUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(platformQuery);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformCode);
            preparedStatement.setString(3, platformCode);
            ResultSet resultSet = preparedStatement.executeQuery();
            Platform platform = new Platform();
            if (resultSet.next()) {
                platform.setId(resultSet.getInt("PLATFORM.ID"));
                platform.setIdentifier(platformCode);
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
                platform.setIdentifier(platformCode);
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
