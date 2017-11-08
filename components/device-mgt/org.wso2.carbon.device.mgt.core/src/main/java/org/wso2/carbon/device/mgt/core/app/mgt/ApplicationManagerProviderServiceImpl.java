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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.DeviceApplicationMapping;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.ApplicationMappingDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

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
                type = deviceIds.get(0).getType().toLowerCase();
            }
            Activity activity = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    addOperation(type, operation, deviceIds);
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().notifyOperationToDevices
                    (operation, deviceIds);
            return activity;
        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in notify operation at app installation", e);
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
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
                        (user, false);
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
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
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
                        .getAllDevicesOfRole(userRole, false);
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
        } catch (InvalidDeviceException e) {
            throw new ApplicationManagementException("Invalid DeviceIdentifiers found.", e);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error in get devices for user role " + userRole +
                    " in app installation", e);

        } catch (OperationManagementException e) {
            throw new ApplicationManagementException("Error in add operation at app installation", e);

        }
    }

    private boolean contains(DeviceApplicationMapping deviceApp, List<Application> installedApps) {
        boolean installed = false;
        for (Application app : installedApps) {
            if (app.getApplicationIdentifier().equals(deviceApp.getApplicationUUID()) && app.getVersion().equals(deviceApp.getVersionName())) {
                installed = true;
                break;
            }
        }
        return installed;
    }

    @Override
    public void updateApplicationListInstalledInDevice(
            DeviceIdentifier deviceIdentifier,
            List<Application> applications) throws ApplicationManagementException {
<<<<<<< HEAD

        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<DeviceApplicationMapping> installedDeviceApps = new ArrayList<>();
            List<DeviceApplicationMapping> uninstalledDeviceApps = new ArrayList<>();
            List<DeviceApplicationMapping> deviceApps = applicationMappingDAO.getApplicationsOfDevice(deviceIdentifier.getId(), false);
            for (DeviceApplicationMapping deviceApp : deviceApps) {
                if (contains(deviceApp, applications)) {
                    if (!deviceApp.isInstalled()) {
                        // device app mapping is recorded as not installed (i.e. install app operation has been sent to device)
                        // as the app list sent from the device contains this, app is now installed in the device
                        // so we can mark the device app mapping entry as installed.
                        deviceApp.setInstalled(true);
                        applicationMappingDAO.updateDeviceApplicationMapping(deviceApp);
                    }
                } else {
                    if (deviceApp.isInstalled()) {
                        // we have a device-app mapping in the installed state in the db. but app is not installed in the device.
                        // which implies that a previously installed app has been uninstalled from the device. so we have to remove the device app mapping
                        applicationMappingDAO.removeApplicationMapping(deviceApp);
                    }
                }
            }
            DeviceManagementDAOFactory.commitTransaction();

        } catch (DeviceManagementDAOException | TransactionManagementException e) {
            String msg = "Failed to update application list of the device " + deviceIdentifier;
            throw new ApplicationManagementException(msg, e);

        }
    }

    public void updateApplicationListInstalledInDeviceDep(
            DeviceIdentifier deviceIdentifier,
            List<Application> applications) throws ApplicationManagementException {

        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<DeviceApplicationMapping> installedDeviceApps = new ArrayList<>();
            List<DeviceApplicationMapping> uninstalledDeviceApps = new ArrayList<>();
            List<DeviceApplicationMapping> deviceApps = applicationMappingDAO.getApplicationsOfDevice(deviceIdentifier.getId(), false);
            for (DeviceApplicationMapping deviceApp : deviceApps) {
                if (contains(deviceApp, applications)) {
                    // if device app is pending mark device app as installed
                    if (!deviceApp.isInstalled()) {
                        deviceApp.setInstalled(true);
                        applicationMappingDAO.updateDeviceApplicationMapping(deviceApp);
                    }
                } else {
                    if (deviceApp.isInstalled()) {
                        // this means we have a device-app mapping in the installed state in the db. but app is not installed in the device.
                        // which implies that a previously installed app has been uninstalled from the device. so we have to remove the device app mapping
                        applicationMappingDAO.removeApplicationMapping(deviceApp);
                    }
                }
            }
            DeviceManagementDAOFactory.commitTransaction();

        } catch (DeviceManagementDAOException | TransactionManagementException e) {
            String msg = "Failed to update application list of the device " + deviceIdentifier;
            throw new ApplicationManagementException(msg, e);

        }

=======
        if (log.isDebugEnabled()) {
            log.debug("Updating application list for device: " + deviceIdentifier.toString());
        }
>>>>>>> 9cbc4a5da3dbc45ea572d94212b7577655aa365e
        List<Application> installedAppList = getApplicationListForDevice(deviceIdentifier);
        try {
            Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier,
                    false);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

            if (log.isDebugEnabled()) {
                log.debug("Number of apps installed:" + installedAppList.size());
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
            DeviceManagementDAOFactory.beginTransaction();
            applicationMappingDAO.removeApplicationMapping(device.getId(), appIdsToRemove, tenantId);
            Application installedApp;
            List<Integer> applicationIds = new ArrayList<>();

            for (Application application : applications) {
                // Adding N/A if application doesn't have a version. Also truncating the application version,
                // if length of the version is greater than maximum allowed length.
                if (application.getVersion() == null) {
                    application.setVersion("N/A");
                } else if (application.getVersion().length() >
                           DeviceManagementConstants.OperationAttributes.APPLIST_VERSION_MAX_LENGTH) {
                    application.setVersion(StringUtils.abbreviate(application.getVersion(),
                            DeviceManagementConstants.OperationAttributes.APPLIST_VERSION_MAX_LENGTH));
                }
                if (!installedAppList.contains(application)) {
                    installedApp = applicationDAO.getApplication(application.getApplicationIdentifier(),
                            application.getVersion(), tenantId);
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
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred saving application list of the device " + deviceIdentifier.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initializing transaction for saving application list to the device "
                         + deviceIdentifier.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred obtaining the device object for device " + deviceIdentifier.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Exception occurred saving application list of the device " + deviceIdentifier.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Application> getApplicationListForDevice(DeviceIdentifier deviceId)
            throws ApplicationManagementException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId,
                    false);
        } catch (DeviceManagementException e) {
            throw new ApplicationManagementException("Error occurred while fetching the device of '" +
                    deviceId.getType() + "' carrying the identifier'" + deviceId.getId(), e);
        }
        if (device == null) {
            if (log.isDebugEnabled()) {
                log.debug("No device is found upon the device identifier '" + deviceId.getId() +
                        "' and type '" + deviceId.getType() + "'. Therefore returning empty app list");
            }
            return new ArrayList<>();
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            return applicationDAO.getInstalledApplications(device.getId());
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while fetching the Application List of device " + deviceId.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to get application " +
                         "list of the device " + deviceId.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Exception occurred getting application list of the device " + deviceId.toString();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }  finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}