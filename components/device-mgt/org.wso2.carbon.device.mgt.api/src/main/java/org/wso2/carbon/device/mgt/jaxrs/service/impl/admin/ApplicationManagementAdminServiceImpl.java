/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.jaxrs.exception.UnknownApplicationTypeException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.ApplicationManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.device.mgt.jaxrs.util.MDMAndroidOperationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.MDMIOSOperationUtil;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.MobileApp;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/applications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApplicationManagementAdminServiceImpl implements ApplicationManagementAdminService {

    private static final Log log = LogFactory.getLog(ApplicationManagementAdminServiceImpl.class);

    @POST
    @Path("/install-application")
    @Override
    public Response installApplication(ApplicationWrapper applicationWrapper) {
        ApplicationManager appManagerConnector;
        Operation operation = null;
        try {
            appManagerConnector = DeviceMgtAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    if (deviceIdentifier.getType().equals(Platform.ANDROID.toString())) {
                        operation = MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
                    } else if (deviceIdentifier.getType().equals(Platform.IOS.toString())) {
                        operation = MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
                    }
                }
                appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
            }
            return Response.status(Response.Status.ACCEPTED).entity("Application installation request has been sent " +
                    "to the device").build();
        } catch (ApplicationManagementException e) {
            String msg = "ErrorResponse occurred while processing application installation request";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnknownApplicationTypeException e) {
            String msg = "The type of application requested to be installed is not supported";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/uninstall-application")
    @Override
    public Response uninstallApplication(ApplicationWrapper applicationWrapper) {
        ApplicationManager appManagerConnector;
        org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
        try {
            appManagerConnector = DeviceMgtAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    if (deviceIdentifier.getType().equals(Platform.ANDROID.toString())) {
                        operation = MDMAndroidOperationUtil.createAppUninstallOperation(mobileApp);
                    } else if (deviceIdentifier.getType().equals(Platform.IOS.toString())) {
                        operation = MDMIOSOperationUtil.createAppUninstallOperation(mobileApp);
                    }
                }
                appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
            }
            return Response.status(Response.Status.ACCEPTED).entity("Application un-installation request has " +
                    "been sent to the device").build();
        } catch (ApplicationManagementException e) {
            String msg = "ErrorResponse occurred while processing application un-installation request";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UnknownApplicationTypeException e) {
            String msg = "The type of application requested to be un-installed is not supported";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
