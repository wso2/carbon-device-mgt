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
package org.wso2.carbon.device.application.mgt.core.dao.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.common.Lifecycle;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.User;
import org.wso2.carbon.device.application.mgt.core.util.JSONUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for handling the utils of the Application Management DAO.
 */
public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs           ResultSet
     * @param rsProperties Properties resultset.
     * @param rsTags       Tags resultset
     * @return Application that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static Application loadApplication(ResultSet rs, ResultSet rsProperties, ResultSet rsTags)
            throws SQLException, JSONException {
        Application application = new Application();
        application.setId(rs.getInt("ID"));
        application.setName(rs.getString("NAME"));
        application.setUuid(rs.getString("UUID"));
        application.setIdentifier(rs.getString("IDENTIFIER"));
        application.setShortDescription(rs.getString("SHORT_DESCRIPTION"));
        application.setDescription(rs.getString("DESCRIPTION"));
        application.setIconName(rs.getString("ICON_NAME"));
        application.setBannerName(rs.getString("BANNER_NAME"));
        application.setVideoName(rs.getString("VIDEO_NAME"));
        application.setScreenshots(JSONUtil.jsonArrayStringToList(rs.getString("SCREENSHOTS")));
        application.setCreatedAt(rs.getDate("CREATED_AT"));
        application.setModifiedAt(rs.getDate("MODIFIED_AT"));
        application.setUser(new User(rs.getString("CREATED_BY"), rs.getInt("TENANT_ID")));

        Platform platform = new Platform();
        platform.setName(rs.getString("APL_NAME"));
        platform.setIdentifier(rs.getString("APL_IDENTIFIER"));
        application.setPlatform(platform);

        Map<String, String> properties = new HashMap<>();
        while (rsProperties.next()) {
            properties.put(rsProperties.getString("PROP_KEY"), rsProperties.getString("PROP_VAL"));
        }
        application.setProperties(properties);

        List<String> tags = new ArrayList<>();
        while ((rsTags.next())) {
            tags.add(rsTags.getString("NAME"));
        }
        application.setTags(tags);

        Category category = new Category();
        category.setId(rs.getInt("CAT_ID"));
        category.setName(rs.getString("CAT_NAME"));
        application.setCategory(category);

        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setId(rs.getInt("LIFECYCLE_STATE_ID"));
        lifecycleState.setName(rs.getString("LS_NAME"));
        lifecycleState.setIdentifier(rs.getString("LS_IDENTIFIER"));
        lifecycleState.setDescription(rs.getString("LS_DESCRIPTION"));

        Lifecycle lifecycle = new Lifecycle();
        lifecycle.setLifecycleState(lifecycleState);
        application.setCurrentLifecycle(lifecycle);
        return application;
    }

    /**
     * Cleans up the statement and resultset after executing the query
     *
     * @param stmt Statement executed.
     * @param rs   Resultset retrived.
     */
    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
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
    }
}
