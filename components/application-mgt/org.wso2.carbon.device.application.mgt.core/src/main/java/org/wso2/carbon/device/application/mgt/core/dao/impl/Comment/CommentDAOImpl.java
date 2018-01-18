/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.Comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.CommentDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles CommentDAO related operations.
 */

public class CommentDAOImpl extends AbstractDAOImpl implements CommentDAO {

    private static final Log log = LogFactory.getLog(CommentDAOImpl.class);
    String sql = "";

    @Override
    public int addComment(int tenantId, Comment comment, String createdBy, int parentId, String uuid)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add comment for application release (" + uuid + ")");
        }
        Connection conn = this.getDBConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        int commentId = -1;
        sql += "INSERT INTO AP_APP_COMMENT (TENANT_ID, COMMENT_TEXT, CREATED_BY, PARENT_ID,AP_APP_RELEASE_ID,"
                + "AP_APP_ID) VALUES (?,?,?,?,(SELECT ID FROM AP_APP_RELEASE WHERE UUID= ?),"
                + "(SELECT AP_APP_ID FROM AP_APP_RELEASE WHERE UUID=?));";
        try {
            statement = conn.prepareStatement(sql, new String[] { "id" });
            statement.setInt(1, tenantId);
            statement.setString(2, comment.getComment());
            statement.setString(3, createdBy);
            statement.setInt(4, parentId);
            statement.setString(5, uuid);
            statement.setString(6, uuid);
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                commentId = rs.getInt(1);
            }
            return commentId;
        } finally {
            Util.cleanupResources(statement, rs);
        }
    }

    @Override
    public int addComment(int tenantId, Comment comment, String createdBy, String appType, String appName,
            String version) throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add to application (" + appName + ") and version (" + version
                    + ")");
        }
        Connection conn = this.getDBConnection();
        PreparedStatement statement = null;
        ResultSet rs;
        int commentId = -1;
        sql += "INSERT INTO AP_APP_COMMENT ( TENANT_ID,COMMENT_TEXT, CREATED_BY,AP_APP_RELEASE_ID,AP_APP_ID) "
                + "VALUES (?,?,?,(SELECT ID FROM AP_APP_RELEASE WHERE VERSION =? AND (SELECT ID FROM AP_APP WHERE "
                + "TYPE=? AND NAME=?)),(SELECT ID FROM AP_APP WHERE TYPE=? AND NAME=?));";
        try {
            statement = conn.prepareStatement(sql, new String[] { "id" });
            statement.setInt(1, tenantId);
            statement.setString(2, comment.getComment());
            statement.setString(3, createdBy);
            statement.setString(4, version);
            statement.setString(5, appType);
            statement.setString(6, appName);
            statement.setString(7, appType);
            statement.setString(8, appName);
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            if (rs.next()) {
                commentId = rs.getInt(1);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentId;
    }

    @Override
    public Comment updateComment(int commentId, String updatedComment, String modifiedBy,
            Timestamp modifiedAt) throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update the comment with ID (" + commentId + ")");
        }
        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;
        sql += "UPDATE AP_APP_COMMENT SET COMMENT_TEXT=?, MODEFIED_BY=? WHERE ID=?;";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, updatedComment);
            statement.setString(2, modifiedBy);
            statement.setInt(3, commentId);
            statement.executeUpdate();
            rs = statement.executeQuery();
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return getComment(commentId);
    }

    @Override
    public Comment updateComment(String uuid, int commentId, String updatedComment, String modifiedBy,
            Timestamp modifiedAt) throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to update the comment with application (" + uuid + ") and "
                    + "comment id ( " + commentId + ")");
        }
        Connection connection;
        PreparedStatement statement = null;
        ResultSet rs = null;
        sql += "UPDATE AP_APP_COMMENT SET COMMENT_TEXT=?,MODEFIED_BY=? WHERE ID=?; ";
        try {
            connection = this.getDBConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, updatedComment);
            statement.setString(2, modifiedBy);
            statement.setInt(3, commentId);
            statement.executeUpdate();
            rs = statement.getResultSet();
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return getComment(commentId);
    }

    @Override
    public Comment getComment(int commentId) throws CommentManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment with the comment id(" + commentId + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        Comment comment = new Comment();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT FROM AP_APP_COMMENT WHERE ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, commentId);
            rs = statement.executeQuery();
            if (rs.next()) {
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("MODEFIED_AT"));
                comment.setParent(rs.getInt("PARENT_ID"));
                Util.cleanupResources(statement, rs);
                return comment;
            }
        } catch (SQLException e) {
            throw new CommentManagementException(
                    "SQL Error occurred while retrieving information of the comment " + commentId, e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving information of the comment " + commentId, e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comment;
    }

    @Override
    public List<Comment> getComment(String uuid) throws CommentManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment with the application release(" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT FROM AP_APP_COMMENT WHERE (SELECT ID FROM AP_APP_RELEASE where UUID=?)AND "
                    + "(SELECT AP_APP_ID FROM AP_APP_RELEASE where UUID=?);";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("MODEFIED_AT"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving comments", e);
        } catch (SQLException e) {
            throw new CommentManagementException("SQL Error occurred while retrieving comments", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getAllComments(String uuid, PaginationRequest request)
            throws CommentManagementException, SQLException, DBConnectionException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comment of the application release (" + uuid + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT FROM AP_APP_COMMENT, AP_APP_RELEASE WHERE AP_APP_COMMENT.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID "
                    + "AND AP_APP_RELEASE.UUID =? LIMIT ? OFFSET ?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setInt(2, request.getLimit());
            statement.setInt(3, request.getOffSet());
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("MODEFIED_BY"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return comments;
    }

    @Override
    public int getCommentCount(PaginationRequest request, String uuid) throws CommentManagementException {

        int commentCount = 0;
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs = null;
        boolean isUuidProvided = false;
        try {
            conn = this.getDBConnection();
            if (uuid != null) {
                isUuidProvided = true;
            }
            if (isUuidProvided) {
                sql += "SELECT COUNT(AP_APP_COMMENT.ID) FROM AP_APP_COMMENT,AP_APP_RELEASE "
                        + "WHERE AP_APP_COMMENT.AP_APP_RELEASE_ID= AP_APP_RELEASE.ID AND "
                        + "AP_APP_COMMENT.AP_APP_ID= AP_APP_RELEASE.AP_APP_ID AND AP_APP_RELEASE.UUID=?;";
                statement = conn.prepareStatement(sql);
                statement.setString(1, uuid);
                rs = statement.executeQuery();
                if (rs.next()) {
                    commentCount = rs.getInt("COMMENTS_COUNT");
                }
            }
        } catch (SQLException e) {
            throw new CommentManagementException("SQL Error occurred while retrieving count of comments", e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving count of comments", e);
        } finally {
            Util.cleanupResources(statement, rs);
        }
        return commentCount;
    }

    @Override
    public List<Comment> getComments(int appReleasedId, int appId) throws CommentManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the application release id(" + appReleasedId + ") and " + "application id("
                    + appId + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT FROM AP_APP_COMMENT WHERE AP_APP_RELEASE_ID=? AND AP_APP_ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, appReleasedId);
            statement.setInt(2, appId);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while retrieving information of comments", e);
        } catch (SQLException e) {
            throw new CommentManagementException("SQL Error occurred while retrieving information of comments", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getComments(String appType, String appName, String version)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the application name(" + appName + "),application type(" + appType + ") "
                    + "and application version (" + version + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT,PARENT_ID,TENANT_ID FROM AP_APP_COMMENT C ,"
                    + "(SELECT ID AS RELEASE_ID, AP_APP_ID AS RELEASE_AP_APP_ID FROM AP_APP_RELEASE R WHERE VERSION=?) R,"
                    + "(SELECT ID AS APP_ID FROM AP_APP P WHERE NAME=? AND TYPE=?)P"
                    + " WHERE AP_APP_RELEASE_ID=RELEASE_ID AND RELEASE_AP_APP_ID=APP_ID AND AP_APP_ID=RELEASE_AP_APP_ID"
                    + "ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getComments(int tenantId)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the tenant_id(" + tenantId + ")  from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT FROM AP_APP_COMMENT WHERE TENANT_ID='?';";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, tenantId);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getCommentsByUser(String createdBy)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the created by(" + createdBy + ")  from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT ,PARENT_ID,TENANT_ID,CREATED_AT FROM AP_APP_COMMENT WHERE CREATED_BY= ?"
                    + " ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, createdBy);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getCommentsByUser(String createdBy, Timestamp createdAt)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug(
                    "Getting comments with the created by(" + createdBy + ") at (" + createdAt + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT,PARENT_ID,TENANT_ID FROM AP_APP_COMMENT WHERE CREATED_BY=?"
                    + "AND CREATED_AT= ? ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, createdBy);
            statement.setTimestamp(2, createdAt);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getCommentsByModifiedUser(String modifiedBy)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the modified by(" + modifiedBy + ")  from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT,PARENT_ID,TENANT_ID,CREATED_AT,MODEFIED_AT FROM AP_APP_COMMENT "
                    + "WHERE MODEFIED_BY= ? ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, modifiedBy);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getCommentsByModifiedUser(String modifiedBy, Timestamp modifiedAt)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Getting comments with the modified by(" + modifiedBy + ") at (" + modifiedAt + ") from the "
                    + "database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT,PARENT_ID,TENANT_ID,CREATED_AT FROM AP_APP_COMMENT WHERE MODEFIED_BY= ?,"
                    + "MODEFIED_AT=? ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, modifiedBy);
            statement.setTimestamp(2, modifiedAt);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public List<Comment> getComments(String appType, String appName, String version, int parentId)
            throws CommentManagementException, DBConnectionException, SQLException {

        if (log.isDebugEnabled()) {
            log.debug(
                    "Getting comments with the application name(" + appName + "),application type(" + appType + ") and"
                            + "application version (" + version + ") from the database");
        }
        Connection conn;
        PreparedStatement statement = null;
        ResultSet rs;
        List<Comment> comments = new ArrayList<>();
        try {
            conn = this.getDBConnection();
            sql += "SELECT COMMENT_TEXT,TENANT_ID FROM AP_APP_COMMENT C ,"
                    + "(SELECT ID AS RELEASE_ID, AP_APP_ID AS RELEASE_AP_APP_ID FROM AP_APP_RELEASE R WHERE VERSION=? ) "
                    + "R,(SELECT ID AS APP_ID FROM AP_APP P WHERE NAME=? AND TYPE=?)P "
                    + "WHERE PARENT_ID=? AND AP_APP_RELEASE_ID=RELEASE_ID AND RELEASE_AP_APP_ID=APP_ID AND "
                    + "AP_APP_ID=RELEASE_AP_APP_ID ORDER BY CREATED_AT DESC;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            statement.setInt(4, parentId);
            rs = statement.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("ID"));
                comment.setTenantId(rs.getInt("TENANT_ID"));
                comment.setComment(rs.getString("COMMENT_TEXT"));
                comment.setCreatedAt(rs.getTimestamp("CREATED_AT"));
                comment.setCreatedBy(rs.getString("CREATED_BY"));
                comment.setModifiedAt(rs.getTimestamp("MODEFIED_AT"));
                comment.setModifiedBy(rs.getString("modifiedBy"));
                comment.setParent(rs.getInt("PARENT_ID"));
                comments.add(comment);
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return comments;
    }

    @Override
    public int getCommentCount(String uuid)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(AP_APP_COMMENT.ID) FROM AP_APP_COMMENT,AP_APP_RELEASE WHERE "
                    + "AP_APP_COMMENT.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID AND AP_APP_COMMENT.AP_APP_ID="
                    + "AP_APP_RELEASE.AP_APP_ID AND AP_APP_RELEASE.UUID=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("ID");
            }
        } finally {
            Util.cleanupResources(statement, null);
            return commentCount;
        }
    }

    @Override
    public int getCommentCountByUser(String createdBy)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(ID) FROM AP_APP_COMMENT WHERE CREATED_BY= ?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, createdBy);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    @Override
    public int getCommentCountByUser(String modifiedBy, Timestamp modifedAt)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(ID) FROM AP_APP_COMMENT WHERE MODEFIED_BY= ? AND MODEFIED_AT=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, modifiedBy);
            statement.setTimestamp(2, modifedAt);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    @Override
    public int getCommentCountByApp(int appId, int appReleaseId)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(ID) FROM AP_APP_COMMENT WHERE AP_APP_RELEASE_ID=? AND AP_APP_ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, appReleaseId);
            statement.setInt(2, appId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    @Override
    public int getCommentCountByApp(String appType, String appName, String version)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(ID) AS COMMENT_COUNT FROM AP_APP_COMMENT C, "
                    + "(SELECT ID AS RELEASE_ID, AP_APP_ID AS RELEASE_AP_APP_ID FROM AP_APP_RELEASE R WHERE VERSION=? )R,"
                    + "(SELECT ID AS APP_ID FROM AP_APP P WHERE NAME=? and TYPE=?)P "
                    + "WHERE AP_APP_RELEASE_ID=RELEASE_ID AND RELEASE_AP_APP_ID=APP_ID AND AP_APP_ID=RELEASE_AP_APP_ID;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    public int getCommentCountByParent(String uuid, int parentId)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        int commentCount = 0;
        try {
            conn = this.getDBConnection();
            sql += "SELECT COUNT(AP_APP_COMMENT.ID) FROM AP_APP_COMMENT,AP_APP_RELEASE WHERE "
                    + "AP_APP_COMMENT.AP_APP_RELEASE_ID=AP_APP_RELEASE.ID AND "
                    + "AP_APP_COMMENT.AP_APP_ID=AP_APP_RELEASE.AP_APP_ID and AP_APP_RELEASE.UUID=? and PARENT_ID=?;";

            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setInt(2, parentId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                commentCount = rs.getInt("COMMENT_COUNT");
            }
        } finally {
            Util.cleanupResources(statement, null);
        }
        return commentCount;
    }

    @Override
    public void deleteComment(int commentId)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, commentId);
            statement.executeUpdate();
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    public void deleteComment(String uuid) throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE "
                    + "(SELECT ID FROM AP_APP_RELEASE WHERE UUID=?)AND (SELECT AP_APP_ID FROM AP_APP_RELEASE "
                    + "WHERE UUID=?);";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            statement.executeUpdate();
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteComments(int appId, int appReleaseID)
            throws CommentManagementException, DBConnectionException, SQLException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE AP_APP_RELEASE_ID=? and AP_APP_ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, appReleaseID);
            statement.setInt(2, appId);
            statement.executeUpdate();
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteComments(String appType, String appName, String version)
            throws CommentManagementException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE "
                    + "(SELECT AP_APP_RELEASE_ID FROM AP_APP_RELEASE WHERE VERSION=? AND "
                    + "(SELECT AP_APP_ID FROM AP_APP WHERE NAME=? AND TYPE=?)) AND "
                    + "(SELECT AP_APP_ID FROM AP_APP AND NAME=? AND TYPE=?);";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            statement.setString(4, appName);
            statement.setString(5, appType);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while deleting comments", e);
        } catch (SQLException e) {
            throw new CommentManagementException("SQL Error occurred while deleting comments", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteComments(String appType, String appName, String version, String createdBy)
            throws CommentManagementException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE "
                    + "(SELECT AP_APP_RELEASE_ID FROM AP_APP_RELEASE WHERE VERSION=? AND "
                    + "(SELECT AP_APP_ID FROM AP_APP WHERE NAME=? AND TYPE=?)) AND "
                    + "(SELECT AP_APP_ID FROM AP_APP WHERE NAME=? and TYPE=?) AND CREATED_BY=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, version);
            statement.setString(2, appName);
            statement.setString(3, appType);
            statement.setString(4, appName);
            statement.setString(5, appType);
            statement.setString(6, createdBy);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while deleting comments ", e);
        } catch (SQLException e) {
            throw new CommentManagementException("Error occurred while deleting comments", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public void deleteComments(String uuid, int parentId) throws CommentManagementException {

        Connection conn;
        PreparedStatement statement = null;
        try {
            conn = this.getDBConnection();
            sql += "DELETE FROM AP_APP_COMMENT WHERE AP_APP_RELEASE_ID=(SELECT ID FROM AP_APP_RELEASE WHERE UUID=?) "
                    + "AND AP_APP_ID=(SELECT AP_APP_ID FROM AP_APP_RELEASE where UUID=?)AND PARENT_ID=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, uuid);
            statement.setString(2, uuid);
            statement.setInt(3, parentId);
            statement.executeUpdate();
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurred while deleting comments.", e);
        } catch (SQLException e) {
            throw new CommentManagementException("Error occurred while deleting comments", e);
        } finally {
            Util.cleanupResources(statement, null);
        }
    }

    @Override
    public int updateStars(int stars, String uuid) throws ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = this.getDBConnection();
            sql += "UPDATE AP_APP_RELEASE SET STARS=?, NO_OF_RATED_USERS=(NO_OF_RATED_USERS+1) WHERE UUID=?;";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, stars);
            statement.setString(2, uuid);
            resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                resultSet.getInt("STARS");
            }
            int numORows = resultSet.getRow();
            if (resultSet.next()) {
                ApplicationRelease applicationRelease = new ApplicationRelease();
                applicationRelease.setStars(resultSet.getInt("STARS"));
                Util.cleanupResources(statement, resultSet);
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to add stars to an application (UUID : " + uuid + "), by executing "
                            + "the query " + e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception  while trying to add stars to an application (UUID : " + uuid + "), ",
                    e);
        } finally {
            Util.cleanupResources(statement, null);
            return getStars(uuid);
        }
    }

    @Override
    public int getStars(String uuid) throws ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int Stars = 0;
        try {
            connection = this.getDBConnection();
            sql += "SELECT STARS FROM AP_APP_RELEASE WHERE UUID=?;";
            statement = connection.prepareStatement(sql);
            statement.setString(1, uuid);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ApplicationRelease applicationRelease = new ApplicationRelease();
                Stars = resultSet.getInt("STARS");
                return Stars;
            }
        } catch (SQLException e) {
            throw new ApplicationManagementDAOException(
                    "SQL Exception while trying to get stars from an application (UUID : " + uuid + "), by executing "
                            + "the query " + e);
        } catch (DBConnectionException e) {
            log.error(
                    "DB Connection Exception while trying to get stars from an application (UUID : " + uuid + ")," + e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
        return Stars;
    }

    @Override
    public int getRatedUser(String uuid) throws ApplicationManagementDAOException {

        Connection connection;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int ratedUsers = 0;
        try {
            connection = this.getDBConnection();
            sql += "SELECT NO_OF_RATED_USERS FROM AP_APP_RELEASE WHERE UUID=?;";
            statement = connection.prepareStatement(sql);
            statement.setString(1, uuid);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ApplicationRelease applicationRelease = new ApplicationRelease();
                ratedUsers = resultSet.getInt("NO_OF_RATED_USERS");
                applicationRelease.setNoOfRatedUsers(ratedUsers);
                Util.cleanupResources(statement, resultSet);
                return ratedUsers;
            }
        } catch (SQLException e) {
            log.error("SQL Exception occurs.", e);
        } catch (DBConnectionException e) {
            log.error("DB Connection Exception occurs.", e);
        } finally {
            Util.cleanupResources(statement, resultSet);
        }
        return ratedUsers;
    }
}
