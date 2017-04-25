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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.api.services.ApplicationManagementService;
import org.wso2.carbon.device.application.mgt.core.components.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagerException;
import org.wso2.carbon.device.application.mgt.core.util.ApplicationManagementUtil;

import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

public class ApplicationManagementServiceImpl implements ApplicationManagementService {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);

    @POST
    @Override
    public Response createApplication(String ifModifiedSince, String name) {
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManager();
        return null;
    }

    @Override
    public Response getApplications(String ifModifiedSince) {
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManager();
        try {
            return Response.ok().entity(applicationManager.getApplications()).build();
        } catch (ApplicationManagerException e) {
            String msg = "Error occured while getting the application list";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }
}
