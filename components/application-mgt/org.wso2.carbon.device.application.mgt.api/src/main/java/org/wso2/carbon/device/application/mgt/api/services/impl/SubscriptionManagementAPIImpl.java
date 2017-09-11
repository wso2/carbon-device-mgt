/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.InstallationDetails;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;

import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.util.HashMap;
import java.util.List;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/subscription")
public class SubscriptionManagementAPIImpl implements SubscriptionManagementAPI{

    private static Log log = LogFactory.getLog(SubscriptionManagementAPIImpl.class);

    @Override
    public Response installApplication(@ApiParam(name = "installationDetails", value = "The application ID and list" +
            " the devices/users/roles", required = true) @Valid InstallationDetails installationDetails) {
        Object result;
        SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
        try {
            String applicationUUID = installationDetails.getApplicationUUID();
            if (!installationDetails.getDeviceIdentifiers().isEmpty()) {
                List<DeviceIdentifier> deviceList = installationDetails.getDeviceIdentifiers();
                result = subscriptionManager.installApplicationForDevices(applicationUUID, deviceList);
            } else if (!installationDetails.getUserNameList().isEmpty()) {
                List<String> userList = installationDetails.getUserNameList();
                result = subscriptionManager.installApplicationForUsers(applicationUUID, userList);
            } else if (!installationDetails.getRoleNameList().isEmpty()) {
                List<String> roleList = installationDetails.getRoleNameList();
                result = subscriptionManager.installApplicationForRoles(applicationUUID, roleList);
            } else {
                result = "Missing request data!";
                return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
            }
            HashMap<String, Object> response = new HashMap<>();
            response.put("failedDevices", result);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    public Response uninstallApplication(@ApiParam(name = "installationDetails", value = "The application ID and list" +
            " of devices/users/roles", required = true) @Valid InstallationDetails installationDetails) {
        return null;
    }

    @Override
    public Response getApplication(@ApiParam(name = "applicationUUID", value = "Application ID") String
                                               applicationUUID, @ApiParam(name = "deviceId", value = "The device ID")
            String deviceId) {
        return null;
    }
}
