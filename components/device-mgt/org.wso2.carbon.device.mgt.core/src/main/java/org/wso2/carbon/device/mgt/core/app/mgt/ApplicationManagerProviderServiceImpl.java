/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.device.mgt.core.app.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.app.mgt.oauth.ServiceAuthenticator;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.identity.IdentityConfigurations;
import org.wso2.carbon.device.mgt.core.dao.*;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements Application Manager interface
 */
public class ApplicationManagerProviderServiceImpl implements ApplicationManagementProviderService {

    private DeviceDAO deviceDAO;
    private ApplicationDAO applicationDAO;
    private ApplicationMappingDAO applicationMappingDAO;

    private static final String GET_APP_LIST_URL = "store/apis/assets/mobileapp?domain=carbon.super&page=1";
    private static final Log log = LogFactory.getLog(ApplicationManagerProviderServiceImpl.class);

    public ApplicationManagerProviderServiceImpl(AppManagementConfig appManagementConfig) {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        this.applicationMappingDAO = DeviceManagementDAOFactory.getApplicationMappingDAO();
    }

    ApplicationManagerProviderServiceImpl() {
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        this.applicationMappingDAO = DeviceManagementDAOFactory.getApplicationMappingDAO();
    }

    @Override
    public Application[] getApplications(String domain, int pageNumber, int size)
            throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceId, Application application,
                                        String status) throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceId,
                                       Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public Activity installApplicationForDevices(Operation operation, List<DeviceIdentifier> deviceIds)
            throws ApplicationManagementException {
        try {
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIds.size() > 0) {
                type = deviceIds.get(0).getType();
            }
            Activity activity =  DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                   addOperation(type, operation, deviceIds);
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().notifyOperationToDevices
                    (operation, deviceIds);
            return activity;
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in notify operation at app installation", e);
        }
    }

    @Override
    public Activity installApplicationForUsers(Operation operation, List<String> userNameList)
            throws ApplicationManagementException {

        String userName = null;
        try {
            List<Device> deviceList;
            List<DeviceIdentifier> deviceIdentifierList = new ArrayList<>();
            DeviceIdentifier deviceIdentifier;


            for (String user : userNameList) {
                userName = user;
                deviceList = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevicesOfUser
                        (user);
                for (Device device : deviceList) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(Integer.toString(device.getId()));
                    deviceIdentifier.setType(device.getType());

                    deviceIdentifierList.add(deviceIdentifier);
                }
            }
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIdentifierList.size() > 0) {
                type = deviceIdentifierList.get(0).getType();
            }

            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .addOperation(type, operation, deviceIdentifierList);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in get devices for user: " + userName +
                    " in app installation", e);

        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);

        }
    }

    @Override
    public Activity installApplicationForUserRoles(Operation operation, List<String> userRoleList)
            throws ApplicationManagementException {

        String userRole = null;
        try {
            List<Device> deviceList;
            List<DeviceIdentifier> deviceIdentifierList = new ArrayList<>();
            DeviceIdentifier deviceIdentifier;

            for (String role : userRoleList) {
                userRole = role;
                deviceList = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                        .getAllDevicesOfRole(userRole);
                for (Device device : deviceList) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(Integer.toString(device.getId()));
                    deviceIdentifier.setType(device.getType());

                    deviceIdentifierList.add(deviceIdentifier);
                }
            }
            //TODO: Fix this properly later adding device type to be passed in when the task manage executes "addOperations()"
            String type = null;
            if (deviceIdentifierList.size() > 0) {
                type = deviceIdentifierList.get(0).getType();
            }
            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().addOperation(type, operation,
                    deviceIdentifierList);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in get devices for user role " + userRole +
                    " in app installation", e);

        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);

        }
    }

    @Override
    public void updateApplicationListInstalledInDevice(
            DeviceIdentifier deviceIdentifier,
            List<Application> applications) throws ApplicationManagementException {
        List<Application> installedAppList = getApplicationListForDevice(deviceIdentifier);
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            Device device = deviceDAO.getDevice(deviceIdentifier, tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Device:" + device.getId() + ":identifier:" + deviceIdentifier.getId());
            }

            if (log.isDebugEnabled()) {
                log.debug("num of apps installed:" + installedAppList.size());
            }
            List<Application> appsToAdd = new ArrayList<>();
            List<Integer> appIdsToRemove = new ArrayList<>(installedAppList.size());

            for (Application installedApp : installedAppList) {
                if (!applications.contains(installedApp)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Remove app Id:" + installedApp.getId());
                    }
                    appIdsToRemove.add(installedApp.getId());
                }
            }

            Application installedApp;
            List<Integer> applicationIds = new ArrayList<>();

            for (Application application : applications) {
                if (!installedAppList.contains(application)) {
                    installedApp = applicationDAO.getApplication(application.getApplicationIdentifier(), tenantId);
                    if (installedApp == null) {
                        appsToAdd.add(application);
                    } else {
                        applicationIds.add(installedApp.getId());
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("num of apps add:" + appsToAdd.size());
            }
            applicationIds.addAll(applicationDAO.addApplications(appsToAdd, tenantId));

            if (log.isDebugEnabled()) {
                log.debug("num of app Ids:" + applicationIds.size());
            }
            applicationMappingDAO.addApplicationMappings(device.getId(), applicationIds, tenantId);

            if (log.isDebugEnabled()) {
                log.debug("num of remove app Ids:" + appIdsToRemove.size());
            }
            applicationMappingDAO.removeApplicationMapping(device.getId(), appIdsToRemove, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new ApplicationManagementException("Error occurred saving application list to the device", e);
        } catch (TransactionManagementException e) {
            throw new ApplicationManagementException("Error occurred while initializing transaction", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Application> getApplicationListForDevice(
            DeviceIdentifier deviceId) throws ApplicationManagementException {
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            device = deviceDAO.getDevice(deviceId, tenantId);
            return applicationDAO.getInstalledApplications(device.getId());
        } catch (DeviceManagementDAOException e) {
            throw new ApplicationManagementException("Error occurred while fetching the Application List of '" +
                    deviceId.getType() + "' device carrying the identifier'" + deviceId.getId(), e);
        } catch (SQLException e) {
            throw new ApplicationManagementException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

}
