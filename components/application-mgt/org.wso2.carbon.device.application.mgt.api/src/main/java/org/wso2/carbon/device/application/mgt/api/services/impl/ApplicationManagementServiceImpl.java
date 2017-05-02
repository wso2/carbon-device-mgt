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
package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.responses.ApplicationsListResponse;
import org.wso2.carbon.device.application.mgt.core.components.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagerException;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Produces({ "application/json"})
@Consumes({ "application/json"})
public class ApplicationManagementServiceImpl {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);


    @GET
    @Consumes("application/json")
    @Path("applications")
    public ApplicationsListResponse getApplications(@Context final HttpServletResponse servletResponse) {
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManager();
        try {
            ApplicationsListResponse applicationsListResponse =
                    new ApplicationsListResponse(applicationManager.getApplications());
            return applicationsListResponse;
        } catch (Exception e) {
            String msg = "Error occurred while getting the application list";
            log.error(msg, e);
            try {
                servletResponse.sendError(Response.Status.NOT_FOUND.getStatusCode());
            } catch (IOException e1) {
                log.error(msg, e1);
            }
            return null;
        }
    }
}
