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
import org.wso2.carbon.device.application.mgt.common.*;

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
     * @return List of Applications that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static List<Application> loadApplications(ResultSet rs) throws SQLException, JSONException {

        List<Application> applications = new ArrayList<>();
        Application application = null ;
        int applicatioId = -1;

        while (rs.next()){
            if (applicatioId != rs.getInt("APP_ID")){

                if( application != null){
                    applications.add(application);
                }
                applicatioId = rs.getInt("APP_ID");
                application = new Application();
                application.setId(applicatioId);
                application.setName(rs.getString("APP_NAME"));
                application.setType(rs.getString("APP_TYPE"));
                application.setAppCategory(rs.getString("APP_CATEGORY"));
                application.setIsFree(rs.getInt("IS_FREE"));
                application.setIsRestricted(rs.getInt("RESTRICTED"));

                List<Tag> tags = new ArrayList<>();
                Tag tag = new Tag();
                tag.setTagName(rs.getString("APP_TAG"));
                tags.add(tag);
                application.setTags(tags);

                List<UnrestrictedRole> unrestrictedRoles = new ArrayList<>();
                UnrestrictedRole unrestrictedRole = new UnrestrictedRole();
                unrestrictedRole.setRole(rs.getString("ROLE"));
                unrestrictedRoles.add(unrestrictedRole);
                application.setUnrestrictedRoles(unrestrictedRoles);
            }else{
                Tag tag = new Tag();
                tag.setTagName(rs.getString("APP_TAG"));
                UnrestrictedRole unrestrictedRole = new UnrestrictedRole();
                unrestrictedRole.setRole(rs.getString("ROLE"));
                if (application != null && application.getTags().contains(tag)){
                    application.getTags().add(tag);
                }
                if (application != null && application.getUnrestrictedRoles().contains(unrestrictedRole)){
                    application.getUnrestrictedRoles().add(unrestrictedRole);
                }

            }
            if(rs.last()){
                applications.add(application);
            }
        }

        return applications;

    }


    /**
     * To create application object from the result set retrieved from the Database.
     *
     * @param rs           ResultSet
     * @return Application that is retrieved from the Database.
     * @throws SQLException  SQL Exception
     * @throws JSONException JSONException.
     */
    public static Application loadApplication(ResultSet rs) throws SQLException, JSONException {

        Application application = new Application();
        int applicatioId = -1;
        int iteration = 0;

        while (rs.next()){
            if (iteration == 0){
                applicatioId = rs.getInt("APP_ID");
                application.setId(applicatioId);
                application.setName(rs.getString("APP_NAME"));
                application.setType(rs.getString("APP_TYPE"));
                application.setAppCategory(rs.getString("APP_CATEGORY"));
                application.setIsFree(rs.getInt("IS_FREE"));
                application.setIsRestricted(rs.getInt("RESTRICTED"));
            }

            Tag tag = new Tag();
            tag.setTagName(rs.getString("APP_TAG"));
            UnrestrictedRole unrestrictedRole = new UnrestrictedRole();
            unrestrictedRole.setRole(rs.getString("ROLE"));
            if (application.getTags().contains(tag)){
                application.getTags().add(tag);
            }
            if (application.getUnrestrictedRoles().contains(unrestrictedRole)){
                application.getUnrestrictedRoles().add(unrestrictedRole);
            }
            iteration++;
        }
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
