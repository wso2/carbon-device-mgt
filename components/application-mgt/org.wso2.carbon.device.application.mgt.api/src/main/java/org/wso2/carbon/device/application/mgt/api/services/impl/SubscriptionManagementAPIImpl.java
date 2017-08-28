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
        Object response = null;
        SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
        try {
            String applicationUUTD = installationDetails.getApplicationUUID();
            if (!installationDetails.getDeviceIdentifiers().isEmpty()) {
                List<DeviceIdentifier> deviceList = installationDetails.getDeviceIdentifiers();
                response = subscriptionManager.installApplicationForDevices(applicationUUTD, deviceList);
            }
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
