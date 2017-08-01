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
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.core.util.JSONUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    private static final Log log = LogFactory.getLog(Util.class);

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
        while ((rsTags.next())){
            tags.add(rsTags.getString("NAME"));
        }
        application.setTags(tags);

        Category category = new Category();
        category.setId(rs.getInt("CAT_ID"));
        category.setName(rs.getString("CAT_NAME"));
        application.setCategory(category);
        return application;
    }

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
