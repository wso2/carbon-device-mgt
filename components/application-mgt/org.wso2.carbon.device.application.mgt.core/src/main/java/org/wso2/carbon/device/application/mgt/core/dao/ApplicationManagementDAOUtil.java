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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.device.application.mgt.core.dto.Application;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationType;
import org.wso2.carbon.device.application.mgt.core.dto.StoreApplication;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementDAOUtil {

    private static final Log log = LogFactory.getLog(ApplicationManagementDAOUtil.class);

    public static StoreApplication loadApplication(ResultSet rs) throws SQLException, JSONException {
        ApplicationType applicationType = new ApplicationType();
        StoreApplication storeApplication = new StoreApplication();
        storeApplication.setUuid(rs.getString("UUID"));
        storeApplication.setIconName(rs.getString("ICON_NAME"));
        storeApplication.setBannerName(rs.getString("BANNER_NAME"));
        storeApplication.setScreenshotNames(jsonArrayStringToList(rs.getString("SCREENSHOTS")));
        return storeApplication;
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

    public static List<String> jsonArrayStringToList(String value) throws JSONException {
        JSONArray jsonArray = new JSONArray(value);
        List<String> list = new ArrayList<>();
        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                list.add(jsonArray.get(i).toString());
            }
        }
        return list;
    }
}
