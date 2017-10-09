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

package org.wso2.carbon.device.application.mgt.core.dao.impl.category;

import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.CategoryDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the concrete implementation of {@link CategoryDAO}.
 */
public class GenericCategoryDAOImpl extends AbstractDAOImpl implements CategoryDAO {
    @Override
    public Category addCategory(Category category) throws ApplicationManagementDAOException {
        Connection connection;
        PreparedStatement statement = null;
        String sql = "INSERT INTO APPM_APPLICATION_CATEGORY (NAME, DESCRIPTION) VALUES (?, ?)";
        String[] generatedColumns = { "ID" };
        ResultSet rs = null;
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql, generatedColumns);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                category.setId(rs.getInt(1));
            }
            return category;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database connection while trying to update the categroy " + category.getName(), e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL exception while executing the query '" + sql + "' .", e);
        } finally {
            Util.cleanupResources(statement, rs);
        }
    }

    @Override
    public List<Category> getCategories() throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION_CATEGORY";
        List<Category> categories = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("ID"));
                category.setName(rs.getString("NAME"));
                category.setDescription(rs.getString("DESCRIPTION"));
                categories.add(category);
            }
            return categories;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database Connection Exception while trying to get the "
                    + "application categories", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL Exception while trying to get the application "
                    + "categories, while executing " + sql, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }

    }

    @Override
    public Category getCategory(String name) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM APPM_APPLICATION_CATEGORY WHERE NAME = ?";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setId(rs.getInt("ID"));
                category.setName(rs.getString("NAME"));
                category.setDescription(rs.getString("DESCRIPTION"));
                return category;
            }
            return null;
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Database Connection Exception while trying to get the "
                    + "application categories", e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("SQL Exception while trying to get the application "
                    + "categories, while executing " + sql, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteCategory(String name) throws ApplicationManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        String sql = "DELETE FROM APPM_APPLICATION_CATEGORY WHERE NAME = ?";
        try {
            conn = this.getDBConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException(
                    "Database Connection Exception while trying to delete the category " + name, e);
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to delete the category " + name + " while executing the query " +
                            sql, e);
        } finally {
            Util.cleanupResources(stmt, null);
        }
    }

}
