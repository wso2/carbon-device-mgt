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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.EnterpriseInstallationDetails;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.InstallationDetails;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/subscription")
public class SubscriptionManagementAPIImpl implements SubscriptionManagementAPI{

    private static Log log = LogFactory.getLog(SubscriptionManagementAPIImpl.class);

    @Override
    @POST
    @Path("/install-application")
    public Response installApplication(@ApiParam(name = "installationDetails", value = "Application ID and list of" +
            "devices", required = true) @Valid InstallationDetails installationDetails) {
        SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
        String applicationUUID = installationDetails.getApplicationUUID();

        if (applicationUUID.isEmpty() || installationDetails.getDeviceIdentifiers().isEmpty()) {
            String msg = "Some or all data in the incoming request is empty. Therefore unable to proceed with the "
                    + "installation.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            List<DeviceIdentifier> failedDevices = subscriptionManager.installApplicationForDevices(applicationUUID,
                    installationDetails.getDeviceIdentifiers());
            HashMap<String, Object> response = new HashMap<>();
            response.put("failedDevices", failedDevices);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (ApplicationManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error occurred while installing the application for devices" + ": " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response enterpriseInstallApplication(EnterpriseInstallationDetails enterpriseInstallationDetails) {
        SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
        String msg;
        String applicationUUID = enterpriseInstallationDetails.getApplicationUUID();
        EnterpriseInstallationDetails.EnterpriseEntity enterpriseEntity = enterpriseInstallationDetails.getEntityType();
        List<String> entityValueList = enterpriseInstallationDetails.getEntityValueList();
        List<DeviceIdentifier> failedDevices;

        if (applicationUUID.isEmpty()) {
            msg = "Application UUID is empty in the incoming request. Therefore unable to proceed with the "
                    + "installation.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (enterpriseEntity == null || entityValueList.isEmpty()) {
            msg = "Some or all details of the entity is empty in the incoming request. Therefore unable to proceed "
                    + "with the installation.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try{
            if (EnterpriseInstallationDetails.EnterpriseEntity.USER.equals(enterpriseEntity)) {
                failedDevices = subscriptionManager
                        .installApplicationForUsers(applicationUUID, entityValueList);
            } else if (EnterpriseInstallationDetails.EnterpriseEntity.ROLE.equals(enterpriseEntity)) {
                failedDevices = subscriptionManager
                        .installApplicationForRoles(applicationUUID, entityValueList);
            } else if (EnterpriseInstallationDetails.EnterpriseEntity.DEVICE_GROUP.equals(enterpriseEntity)) {
                failedDevices = subscriptionManager
                        .installApplicationForGroups(applicationUUID, entityValueList);
            } else {
                msg = "Entity type does not match either USER, ROLE or DEVICE_GROUP. Therefore unable to proceed with "
                        + "the installation";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }

            HashMap<String, Object> response = new HashMap<>();
            response.put("failedDevices", failedDevices);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (ApplicationManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error occurred while installing the application for devices" + ": " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response uninstallApplication(@ApiParam(name = "installationDetails", value = "The application ID and list" +
            " of devices/users/roles", required = true) @Valid InstallationDetails installationDetails) {
        return null;
    }

    @Override
    public Response enterpriseUninstallApplication(
            EnterpriseInstallationDetails enterpriseInstallationDetails) {
        return null;
    }

    @Override
    public Response getApplication(@ApiParam(name = "applicationUUID", value = "Application ID") String
                                               applicationUUID, @ApiParam(name = "deviceId", value = "The device ID")
            String deviceId) {
        return null;
    }
}
