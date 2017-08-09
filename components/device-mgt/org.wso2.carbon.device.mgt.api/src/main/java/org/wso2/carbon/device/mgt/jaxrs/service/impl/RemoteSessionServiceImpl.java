/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.remote.session.RemoteSessionConfiguration;
import org.wso2.carbon.device.mgt.jaxrs.beans.RemoteSessionInfo;
import org.wso2.carbon.device.mgt.jaxrs.service.api.RemoteSessionService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * The api for
 */
public class RemoteSessionServiceImpl implements RemoteSessionService {

    private static Log log = LogFactory.getLog(RemoteSessionServiceImpl.class);

    @Path("connect/{deviceType}/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getRemoteSessionDeviceConnect(@PathParam("deviceId") String deviceId,
                                                  @PathParam("deviceType") String deviceType) {
        //First, check whether the remote session is enabled.
        RemoteSessionInfo sessionInfo = new RemoteSessionInfo();
        sessionInfo.setEnabled(false);
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                .getDeviceManagementConfig();
        if (deviceManagementConfig != null) {
            RemoteSessionConfiguration remoteSessionConfiguration = deviceManagementConfig.getRemoteSessionConfiguration();
            if (remoteSessionConfiguration != null) {
                boolean isEnabled = remoteSessionConfiguration.getIsEnabled();
                sessionInfo.setEnabled(isEnabled);
                if (isEnabled) {
                    sessionInfo.setServerUrl(remoteSessionConfiguration.getRemoteSessionServerUrl());
                }
                return Response.ok().entity(sessionInfo).build();
            }
        }

        return Response.ok().entity(sessionInfo).build();
    }
}
