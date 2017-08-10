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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.PlatformDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic Implementation for handling Platform management related database operations.
 */
public class GenericPlatformDAOImpl extends AbstractDAOImpl implements PlatformDAO {
    private static Log log = LogFactory.getLog(GenericPlatformDAOImpl.class);

    @Override
    public int register(int tenantId, Platform platform) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        try {
            int platformId = getPlatformId(tenantId, platform.getIdentifier());
            if (platformId == -1) {
                Connection connection = this.getDBConnection();
                if (!platform.isFileBased()) {
                    String insertToPlatform = "INSERT INTO APPM_PLATFORM (IDENTIFIER, TENANT_ID, NAME, FILE_BASED,  "
                            + "DESCRIPTION, IS_SHARED, ICON_NAME, IS_DEFAULT_TENANT_MAPPING)" + " VALUES (?, ?, ?, ?, "
                            + "?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.setString(3, platform.getName());
                    preparedStatement.setBoolean(4, false);
                    preparedStatement.setString(5, platform.getDescription());
                    preparedStatement.setBoolean(6, platform.isShared());
                    preparedStatement.setString(7, platform.getIconName());
                    preparedStatement.setBoolean(8, platform.isDefaultTenantMapping());
                    preparedStatement.execute();

                    platformId = getPlatformId(tenantId, platform.getIdentifier());
                    String insertPlatformProps =
                            "INSERT INTO APPM_PLATFORM_PROPERTIES (PLATFORM_ID, PROP_NAME, OPTIONAL, "
                                    + "DEFAULT_VALUE) VALUES ( ? , ?, ? , ?)";

                    if (platform.getProperties() != null) {
                        for (Platform.Property property : platform.getProperties()) {
                            preparedStatement = connection.prepareStatement(insertPlatformProps);
                            preparedStatement.setInt(1, platformId);
                            preparedStatement.setString(2, property.getName());
                            preparedStatement.setBoolean(3, property.isOptional());
                            preparedStatement.setString(4, property.getDefaultValue());
                            preparedStatement.execute();
                        }
                    }
                } else {
                    String insertToPlatform =
                            "INSERT INTO APPM_PLATFORM (IDENTIFIER, TENANT_ID, FILE_BASED, IS_SHARED, "
                                    + "IS_DEFAULT_TENANT_MAPPING) VALUES (?, ?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.setBoolean(3, true);
                    preparedStatement.setBoolean(4, platform.isShared());
                    preparedStatement.setBoolean(5, platform.isDefaultTenantMapping());
                    preparedStatement.execute();
                }
                if (platformId == -1) {
                    platformId = getPlatformId(tenantId, platform.getIdentifier());
                }
                return platformId;
            } else {
                if (!platform.isFileBased()) {
                    throw new PlatformManagementDAOException(
                            "Platform - " + platform.getIdentifier() + " is already registered for tenant - "
                                    + tenantId);
                } else {
                    return platformId;
                }
            }
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Unable to obtain the connection while trying to register the platform - " + platform
                            .getIdentifier() + " for tenant - " + tenantId, e);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public void update(int tenantId, String oldPlatformIdentifier, Platform platform)
            throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        try {
            int platformId = getPlatformId(tenantId, oldPlatformIdentifier);
            boolean isIdentifierNull = platform.getIdentifier() == null;
            boolean isNameNull = platform.getName() == null;

            if (platformId != -1) {
                Connection connection = this.getDBConnection();
                if (!platform.isFileBased()) {
                    String insertToPlatform = "UPDATE APPM_PLATFORM SET DESCRIPTION=?, IS_SHARED=?, ICON_NAME=?, "
                            + "IS_DEFAULT_TENANT_MAPPING=?";
                    if (!isIdentifierNull) {
                        insertToPlatform += ",IDENTIFIER = ? ";
                    }
                    if (!isNameNull) {
                        insertToPlatform += ", NAME =?";
                    }
                    insertToPlatform += " WHERE ID = ?";
                    preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getDescription());
                    preparedStatement.setBoolean(2, platform.isShared());
                    preparedStatement.setString(3, platform.getIconName());
                    preparedStatement.setBoolean(4, platform.isDefaultTenantMapping());

                    if (!isIdentifierNull && !isNameNull) {
                        preparedStatement.setString(5, platform.getIdentifier());
                        preparedStatement.setString(6, platform.getName());
                        preparedStatement.setInt(7, platformId);
                    } else if (isIdentifierNull && !isNameNull) {
                        preparedStatement.setString(5, platform.getName());
                        preparedStatement.setInt(6, platformId);
                    } else if (!isIdentifierNull) {
                        preparedStatement.setString(5, platform.getIdentifier());
                        preparedStatement.setInt(6, platformId);
                    } else {
                        preparedStatement.setInt(5, platformId);
                    }
                    preparedStatement.execute();

                    platformId = getPlatformId(tenantId, platform.getIdentifier());
                    String deletePlatformProps = "DELETE FROM APPM_PLATFORM_PROPERTIES WHERE PLATFORM_ID=?";
                    preparedStatement = connection.prepareStatement(deletePlatformProps);
                    preparedStatement.setInt(1, platformId);
                    preparedStatement.execute();

                    String insertPlatformProps =
                            "INSERT INTO APPM_PLATFORM_PROPERTIES (PLATFORM_ID, PROP_NAME, OPTIONAL,"
                                    + " DEFAULT_VALUE) VALUES ( ? , ?, ? , ?)";

                    if (platform.getProperties() != null) {
                        for (Platform.Property property : platform.getProperties()) {
                            preparedStatement = connection.prepareStatement(insertPlatformProps);
                            preparedStatement.setInt(1, platformId);
                            preparedStatement.setString(2, property.getName());
                            preparedStatement.setBoolean(3, property.isOptional());
                            preparedStatement.setString(4, property.getDefaultValue());
                            preparedStatement.execute();
                        }
                    }
                } else if (!isIdentifierNull) {
                    String insertToPlatform = "UPDATE APPM_PLATFORM SET IDENTIFIER = ? WHERE ID = ?";
                    preparedStatement = connection.prepareStatement(insertToPlatform);
                    preparedStatement.setString(1, platform.getIdentifier());
                    preparedStatement.setInt(2, platformId);
                    preparedStatement.execute();
                }
            } else {
                throw new PlatformManagementDAOException(
                        "Cannot find any platform that was registered with identifier - " + platform.getIdentifier()
                                + " for tenant - " + tenantId);
            }
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Unable to obtain the connection while trying to register the platform - " + platform
                            .getIdentifier() + " for tenant - " + tenantId, e);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    private int getPlatformId(int tenantId, String platformIdentifier) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = SQLQueries.queryToGetPlatformId;
        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, platformIdentifier);
            preparedStatement.setString(3, platformIdentifier);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error when trying to obtaining the database connection.", e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error in executing the query - " + query, e);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }

    @Override
    public void unregister(int tenantId, String platformIdenfier, boolean isFileBased)
            throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        try {
            Platform platform = getPlatform(tenantId, platformIdenfier);

            if (platform != null) {
                if (isFileBased == platform.isFileBased()) {
                    Connection connection = this.getDBConnection();
                    String deletePlatform = "DELETE FROM APPM_PLATFORM WHERE ID = ?";
                    preparedStatement = connection.prepareStatement(deletePlatform);
                    preparedStatement.setInt(1, platform.getId());
                    preparedStatement.execute();
                } else {
                    if (isFileBased) {
                        throw new PlatformManagementDAOException("Platform with identifier - " + platformIdenfier
                                + " is not a file based platform. Try to remove that using PlatformManagement APIs");
                    } else {
                        throw new PlatformManagementDAOException("Platform with identifier - " + platformIdenfier
                                + " is a file based platform. Try to remove that by un-deploying the relevant file.");
                    }
                }
            } else {
                throw new PlatformManagementDAOException(
                        "Platform identifier - " + platformIdenfier + " is not registered for tenant - " + tenantId);
            }
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Unable to obtain the connection while trying to register the platform - " + platformIdenfier
                            + " for tenant - " + tenantId, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error while executing the SQL query. ", e);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementDAOException {
        String insertMapping = "INSERT INTO APPM_PLATFORM_TENANT_MAPPING(TENANT_ID, PLATFORM_ID) VALUES (?, ?)";
        PreparedStatement preparedStatement = null;
        try {
            for (String platformIdentifier : platformIdentifiers) {
                if (getTenantPlatformMapping(tenantId, platformIdentifier) == -1) {
                    int platformId = getPlatformId(tenantId, platformIdentifier);
                    Connection connection = this.getDBConnection();
                    preparedStatement = connection.prepareStatement(insertMapping);
                    preparedStatement.setInt(1, tenantId);
                    preparedStatement.setInt(2, platformId);
                    preparedStatement.execute();
                } else {
                    log.error("Platform identifier - " + platformIdentifier + " is already assigned to tenant domain"
                            + " - " + tenantId);
                }
            }
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occurred when getting the connection for the database. ",
                    e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occured while executing the SQL query - " + insertMapping,
                    e);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    private int getTenantPlatformMapping(int tenantId, String platformIdentifier)
            throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String getMapping = "SELECT MAPPING.ID as ID FROM (SELECT ID, PLATFORM_ID FROM APPM_PLATFORM_TENANT_MAPPING "
                + "WHERE TENANT_ID=?) MAPPING JOIN (SELECT ID FROM APPM_PLATFORM WHERE APPM_PLATFORM.IDENTIFIER=?) "
                + "PLATFORM ON MAPPING.PLATFORM_ID=PLATFORM.ID";
        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(getMapping);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setString(2, platformIdentifier);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred while obtaining the connection to get the existing " + "Tenant - Platform Mapping.",
                    e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occured while executing the SQL query - " + getMapping, e);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }

    @Override
    public void removeMapping(int tenantId, String platformIdentifier) throws PlatformManagementDAOException {
        String deleteMapping = "DELETE FROM APPM_PLATFORM_TENANT_MAPPING WHERE ID = ?";
        PreparedStatement preparedStatement = null;
        try {
            int mappingId = getTenantPlatformMapping(tenantId, platformIdentifier);
            if (mappingId != -1) {
                Connection connection = this.getDBConnection();
                preparedStatement = connection.prepareStatement(deleteMapping);
                preparedStatement.setInt(1, mappingId);
                preparedStatement.execute();
            } else {
                throw new PlatformManagementDAOException(
                        "Platform - " + platformIdentifier + " is already unassigned for tenant - " + tenantId);
            }
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred while unassigning the platform - " + platformIdentifier + " for tenant - "
                            + tenantId);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occurred while executing the query - " + deleteMapping);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public void removeMappingTenants(String platformIdentifier) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        int platformId = getPlatformId(MultitenantConstants.SUPER_TENANT_ID, platformIdentifier);
        String getMapping = "DELETE FROM APPM_PLATFORM_TENANT_MAPPING WHERE TENANT_ID != ? AND PLATFORM_ID=?";
        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(getMapping);
            preparedStatement.setInt(1, MultitenantConstants.SUPER_TENANT_ID);
            preparedStatement.setInt(2, platformId);
            preparedStatement.execute();
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred while obtaining the connection to remove existing " + "Tenant - Platform Mapping"
                            + " for the platform : " + platformIdentifier, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occurred while executing the SQL query - " + getMapping, e);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public List<Platform> getPlatforms(int tenantId) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        if (log.isDebugEnabled()) {
            log.debug("GetPlaforms request received for the tenant ID " + tenantId);
        }
        String selectQuery = SQLQueries.queryToGetPlatforms;
        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.setInt(2, tenantId);
            resultSet = preparedStatement.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            if (log.isDebugEnabled()) {
                log.debug("Platform retrieved for the tenant Id " + tenantId);
            }
            while (resultSet.next()) {
                int mappingID = resultSet.getInt(1);
                String identifier = resultSet.getString(2);
                Platform platform = getPlatform(tenantId, identifier);
                if (mappingID != 0) {
                    platform.setEnabled(true);
                } else {
                    platform.setEnabled(false);
                }
                platforms.add(platform);
                if (log.isDebugEnabled()) {
                    log.debug("Platform Identifier - " + identifier + " isEnabled - " + platform.isEnabled());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Number of platforms available for the tenant ID - " + tenantId + " :" + platforms.size());
            }
            return platforms;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred when loading the platforms for tenant - " + tenantId, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occurred when executing query - " + selectQuery, e);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }

    public Platform getPlatform(String tenantDomain, String platformIdentifier) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String platformQuery = "SELECT * FROM (SELECT * FROM APPM_PLATFORM WHERE (TENANT_DOMAIN=? AND IDENTIFIER=?) "
                + "OR (IS_SHARED = TRUE AND IDENTIFIER=?) AND FILE_BASED = FALSE ) PLATFORM "
                + "LEFT JOIN APPM_PLATFORM_PROPERTIES PROPS ON PLATFORM.ID = PROPS.PLATFORM_ID";
        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(platformQuery);
            preparedStatement.setString(1, tenantDomain);
            preparedStatement.setString(2, platformIdentifier);
            preparedStatement.setString(3, platformIdentifier);
            resultSet = preparedStatement.executeQuery();
            Platform platform = new Platform();
            if (resultSet.next()) {
                platform.setId(resultSet.getInt("PLATFORM.ID"));
                platform.setIdentifier(platformIdentifier);
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
                platform.setIdentifier(platformIdentifier);
                platform.setFileBased(true);
            }
            return platform;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error when loading the platform - " + platformIdentifier, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error in executing the query - " + platformQuery, e);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }

    @Override
    public Platform getPlatform(int tenantId, String identifier) throws PlatformManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";

        try {
            conn = this.getDBConnection();
            sql = SQLQueries.queryToGetPlatform;
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, identifier);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();

            Platform platform = null;

            if (rs.next()) {
                platform = new Platform();
                platform.setFileBased(rs.getBoolean(3));
                platform.setIdentifier(rs.getString(2));
                platform.setShared(rs.getBoolean(8));
                platform.setDefaultTenantMapping(rs.getBoolean(9));
                platform.setId(rs.getInt(4));
                if (!platform.isFileBased()) {
                    platform.setName(rs.getString(5));
                    platform.setDescription(rs.getString(6));
                    platform.setIconName(rs.getString(7));
                    if (rs.getInt(1) != 0) {
                        platform.setEnabled(true);
                    } else {
                        platform.setEnabled(false);
                    }
                }
            }
            return platform;
        } catch (SQLException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred while getting platform with the identifier " + identifier + ", for the tenant : "
                            + tenantId, e);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void removePlatforms(int tenantId) throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        String sql = "DELETE FROM APPM_PLATFORM WHERE TENANT_ID = ?";

        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, tenantId);
            preparedStatement.executeUpdate();
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Database connection error while removing the platforms for the " + "tenant - " + tenantId);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException(
                    "SQL exception while executing the query " + sql + " for " + "the tenant : " + tenantId);
        } finally {
            Util.cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public int getSuperTenantAndOwnPlatforms(String platformIdentifier, int tenantId)
            throws PlatformManagementDAOException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = SQLQueries.queryToGetSupertenantAndOwnPlatforms;

        try {
            Connection connection = this.getDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, platformIdentifier);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, MultitenantConstants.SUPER_TENANT_ID);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -1;
            }
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Database connection error while removing the platfor for the " + "tenant - " + tenantId);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException(
                    "SQL exception while executing the query " + sql + " for " + "the tenant : " + tenantId);
        } finally {
            Util.cleanupResources(preparedStatement, resultSet);
        }
    }

    @Override
    public Platform getTenantOwnedPlatform(int tenantId, String platformIdentifier)
            throws PlatformManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";

        try {
            conn = this.getDBConnection();
            sql = "SELECT * from APPM_PLATFORM WHERE TENANT_ID = ? AND IDENTIFIER = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, platformIdentifier);
            rs = stmt.executeQuery();

            Platform platform = null;

            if (rs.next()) {
                platform = new Platform();
                platform.setFileBased(rs.getBoolean("FILE_BASED"));
                platform.setIdentifier(rs.getString("IDENTIFIER"));
                platform.setShared(rs.getBoolean("IS_SHARED"));
                platform.setDefaultTenantMapping(rs.getBoolean("IS_DEFAULT_TENANT_MAPPING"));
                if (!platform.isFileBased()) {
                    platform.setId(rs.getInt("ID"));
                    platform.setName(rs.getString("NAME"));
                    platform.setDescription(rs.getString("DESCRIPTION"));
                    platform.setIconName(rs.getString("ICON_NAME"));
                }
            }
            return platform;
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("Error occurred while executing the query : " + sql + " for "
                    + "getting platforms owned by tenant : " + tenantId, e);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Error occurred while obtaining the DB connection for getting " + "platforms owned by tenant : "
                            + tenantId, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int getMultiTenantPlatforms(String identifier) throws PlatformManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";

        try {
            conn = this.getDBConnection();
            sql = "SELECT ID from APPM_PLATFORM WHERE TENANT_ID != ? AND IDENTIFIER=?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, MultitenantConstants.SUPER_TENANT_ID);
            stmt.setString(2, identifier);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException("Database Connection exception while trying to get the tenants "
                    + "which has the platforms with the platform identifier : " + identifier, e);
        } catch (SQLException e) {
            throw new PlatformManagementDAOException("SQL exception while executing the query " + sql + " to get the"
                    + " tenants which has the platform with the platform identifier : " + identifier, e);
        }
    }
}
