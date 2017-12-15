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
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.exception.UnknownApplicationTypeException;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.ApplicationManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
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
        Activity activity = null;

        RequestValidationUtil.validateApplicationInstallationContext(applicationWrapper);
        try {
            appManagerConnector = DeviceMgtAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    String deviceType = deviceIdentifier.getType().toUpperCase();
                    if (Platform.ANDROID.toString().equals(deviceType)) {
                        operation = MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
                    } else if (Platform.IOS.toString().equals(deviceType)) {
                        operation = MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
                    }
                }
                if (applicationWrapper.getRoleNameList() != null && applicationWrapper.getRoleNameList().size() > 0) {
                    activity = appManagerConnector.installApplicationForUserRoles(operation, applicationWrapper.getRoleNameList());
                } else if (applicationWrapper.getUserNameList() != null &&
                        applicationWrapper.getUserNameList().size() > 0) {
                    activity = appManagerConnector.installApplicationForUsers(operation, applicationWrapper.getUserNameList());
                } else if (applicationWrapper.getDeviceIdentifiers() != null &&
                        applicationWrapper.getDeviceIdentifiers().size() > 0) {
                    activity = appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(
                                    "No application installation criteria i.e. user/role/device is given").build()).build();
                }
            }
            return Response.status(Response.Status.ACCEPTED).entity(activity).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while processing application installation request";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnknownApplicationTypeException e) {
            String msg = "The type of application requested to be installed is not supported";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/uninstall-application")
    @Override
    public Response uninstallApplication(ApplicationWrapper applicationWrapper) {
        ApplicationManager appManagerConnector;
        org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
        Activity activity = null;

        RequestValidationUtil.validateApplicationInstallationContext(applicationWrapper);
        try {
            appManagerConnector = DeviceMgtAPIUtils.getAppManagementService();
            MobileApp mobileApp = applicationWrapper.getApplication();

            if (applicationWrapper.getDeviceIdentifiers() != null) {
                for (DeviceIdentifier deviceIdentifier : applicationWrapper.getDeviceIdentifiers()) {
                    String deviceType = deviceIdentifier.getType().toUpperCase();
                    if (Platform.ANDROID.toString().equals(deviceType)) {
                        operation = MDMAndroidOperationUtil.createAppUninstallOperation(mobileApp);
                    } else if (deviceType.equals(Platform.IOS.toString())) {
                        operation = MDMIOSOperationUtil.createAppUninstallOperation(mobileApp);
                    }
                }
                if (applicationWrapper.getRoleNameList() != null && applicationWrapper.getRoleNameList().size() > 0) {
                    activity = appManagerConnector.installApplicationForUserRoles(operation, applicationWrapper.getRoleNameList());
                } else if (applicationWrapper.getUserNameList() != null &&
                        applicationWrapper.getUserNameList().size() > 0) {
                    activity = appManagerConnector.installApplicationForUsers(operation, applicationWrapper.getUserNameList());
                } else if (applicationWrapper.getDeviceIdentifiers() != null &&
                        applicationWrapper.getDeviceIdentifiers().size() > 0) {
                    activity = appManagerConnector.installApplicationForDevices(operation, applicationWrapper.getDeviceIdentifiers());
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(
                                    "No application un-installation criteria i.e. user/role/device is given").build()).build();
                }
            }
            return Response.status(Response.Status.ACCEPTED).entity(activity).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while processing application un-installation request";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (UnknownApplicationTypeException e) {
            String msg = "The type of application requested to be un-installed is not supported";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

}
