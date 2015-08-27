/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.appmgt.mdm.wso2mdm.direct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.api.AppManagementException;
import org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.MDMAndroidOperationUtil;
import org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.MDMAppConstants;
import org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.MDMIOSOperationUtil;
import org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.beans.MobileAppBean;
import org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.beans.MobileAppTypes;
import org.wso2.carbon.appmgt.mobile.interfaces.MDMOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.mdm.Property;
import org.wso2.carbon.appmgt.mobile.utils.User;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Platform;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * MDM operations implementation with internal OSGI java call
 */
public class MDMOperationsImpl implements MDMOperations {

    private static final Log log = LogFactory.getLog(MDMOperationsImpl.class);

    private static final String APPM_APP_TYPE = "type";
    private static final String APPM_APP_NAME = "name";
    private static final String APPM_APP_IDENTIFIER = "identifier";
    private static final String APPM_APP_IMAGE_URL = "ImageURL";
    private static final String APPM_APP_TYPE_ENTERPRISE = "enterprise";
    private static final String APPM_APP_TYPE_PUBLIC = "public";
    private static final String APPM_APP_TYPE_WEBAPP = "webapp";
    private static final String APPM_APP_TYPE_MOBILE_DEVICE = "mobileDevice";
    private static final String APPM_APP_PLATFORM = "platform";
    private static final String APPM_APP_PLATFORM_IOS = "ios";
    private static final String APPM_APP_PLATFORM_WEBAPP = "webapp";
    private static final String APPM_DEVICE_ID_CONCAT_SEPARATOR = "---";
    private static final String APPM_APP_CALL_TYPE_USER = "user";
    private static final String APPM_APP_CALL_TYPE_ROLE = "role";
    private static final String APPM_APP_CALL_TYPE_STATUS = "status";

    @Override
    public void performAction(User currentUser, String action, App app, int tenantId, String type, String[] params,
            HashMap<String, String> configParams) {
        try {
            //            DeviceManagementProviderService deviceManagementProviderService = getDeviceManagerService(
            //                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            ApplicationManager appManagerConnector = getAppManagementService(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            MobileAppBean mobileApp = createMobileApp(currentUser, action, app, tenantId, type, params, configParams);
            List<DeviceIdentifier> deviceIdentifiers = convertDeviceIdentifiers(type, params);
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation = null;
                ArrayList<DeviceIdentifier> deviceIdentifiersToInstall = new ArrayList<>(1);
                if (deviceIdentifier.getType().equals(Platform.android.toString())) {
                    operation = MDMAndroidOperationUtil.createInstallAppOperation(mobileApp);
                } else if (deviceIdentifier.getType().equals(Platform.ios.toString())) {
                    operation = MDMIOSOperationUtil.createInstallAppOperation(mobileApp);
                } else {
                    log.error("Could not infer the operation on mobile device. User [" + currentUser + "] Application ["
                            + mobileApp + "]");
                }
                if (operation != null) {
                    appManagerConnector.installApplication(operation, deviceIdentifiersToInstall);
                }
            }

        } catch (AppManagementException | ApplicationManagementException e) {
            log.error("Could not retrieve the 'DeviceManagementProviderService' by OSGI lookup. User [" + currentUser
                    + "], tenant [" + tenantId + "] config params [" + configParams + "]", e);
        }

    }

    private List<DeviceIdentifier> convertDeviceIdentifiers(String type, String[] params) {
        List<DeviceIdentifier> result = new ArrayList<>();
        if ("user".equals(type) || "role".equals(type)) {
            for (String param : params) {
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(param, type);
                result.add(deviceIdentifier);
            }
        } else {
            for (String param : params) {
                String[] paramDevices = param.split(APPM_DEVICE_ID_CONCAT_SEPARATOR);
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(paramDevices[0], paramDevices[1]);
                result.add(deviceIdentifier);
            }
        }

        return result;
    }

    private MobileAppBean createMobileApp(User currentUser, String action, App app, int tenantId, String type,
            String[] params, HashMap<String, String> configParams) {

        MobileAppBean mobileApp = new MobileAppBean();
        Class mobileAppClazz = MobileAppBean.class;

        Method[] methods = app.getClass().getMethods();

        //Copies the AppM side mobile app properties to MDM mobile App. Uses reflection and annotation to perform data transfer
        for (Method method : methods) {
            if (method.isAnnotationPresent(Property.class)) {
                try {
                    Object value = method.invoke(app);
                    if (value != null) {
                        String propName = method.getAnnotation(Property.class).name();
                        propName = propName.substring(0, 1).toUpperCase() + propName.substring(1, propName.length());
                        if (propName.equals("Type")) {
                            value = MobileAppTypes.valueOf(String.valueOf(value).toUpperCase());
                        }
                        Method m2 = mobileAppClazz.getMethod("set" + propName, value.getClass());
                        m2.invoke(mobileApp, value);
                    }
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    String errorMessage = "Illegal Action";
                    if (log.isDebugEnabled()) {
                        log.error(errorMessage, e);
                    } else {
                        log.error(errorMessage);
                    }
                }
            }

        }

        Properties requestApp = new Properties();
        requestApp.put(APPM_APP_TYPE, mobileApp.getType().name());

        Properties properties = new Properties();
        if (APPM_APP_PLATFORM_IOS.equals(requestApp.get(APPM_APP_PLATFORM))) {
            if (APPM_APP_TYPE_ENTERPRISE.equals(requestApp.get(APPM_APP_TYPE))) {
                properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
                properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
            } else if (APPM_APP_TYPE_PUBLIC.equals(requestApp.get(APPM_APP_TYPE))) {
                properties.put(MDMAppConstants.IOSConstants.I_TUNES_ID,
                        Integer.parseInt(String.valueOf(requestApp.get(APPM_APP_IDENTIFIER))));
                properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
                properties.put(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP, true);
            } else if (APPM_APP_TYPE_WEBAPP.equals(requestApp.get(APPM_APP_TYPE))) {
                properties.put(MDMAppConstants.IOSConstants.LABEL, requestApp.get(APPM_APP_NAME));
                properties.put(MDMAppConstants.IOSConstants.IS_REMOVE_APP, true);
            }
        } else if (APPM_APP_PLATFORM_WEBAPP.equals(requestApp.get(APPM_APP_PLATFORM))) {
            properties.put(MDMAppConstants.WebappConstants.LABEL, requestApp.get(APPM_APP_NAME));
            properties.put(MDMAppConstants.WebappConstants.IS_REMOVE_APP, true);
        }
        mobileApp.setProperties(properties);

        return mobileApp;
    }

    @Override
    public List<Device> getDevices(User currentUser, int tenantId, String type, String[] params, String platform,
            String platformVersion, boolean isSampleDevicesEnabled, HashMap<String, String> configParams) {
        DeviceManagementProviderService deviceManagementProviderService = null;
        try {
            deviceManagementProviderService = getDeviceManagerService(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (AppManagementException e) {
            log.error("Could not retrieve the 'DeviceManagementProviderService' by OSGI lookup. User [" + currentUser
                    + "], tenant [" + tenantId + "] config params [" + configParams + "]", e);
            return Collections.emptyList();
        }

        List<Device> result = new ArrayList<>();
        try {
            List<org.wso2.carbon.device.mgt.common.Device> mdmDevices = null;
            switch (type) {

            case APPM_APP_CALL_TYPE_USER:
                if (params.length > 0) {
                    String userName = params[0];
                    mdmDevices = deviceManagementProviderService.getDevicesOfUser(userName);
                } else {
                    log.error("User name is not found in parameter [0]");
                }
                break;
            case APPM_APP_CALL_TYPE_ROLE:
                if (params.length > 0) {
                    String roleName = params[0];
                    mdmDevices = deviceManagementProviderService.getAllDevicesOfRole(roleName);
                } else {
                    log.error("Role name is not found in parameter [0]");
                }
                break;
            case APPM_APP_CALL_TYPE_STATUS:
                //                mdmDevices = deviceManagementProviderService.getDevicesByStatus(currentUser);
                log.error("getDevices with type [status] is not supported on this version");
                break;
            default:
                mdmDevices = deviceManagementProviderService.getAllDevices();
            }

            for (org.wso2.carbon.device.mgt.common.Device mdmDevice : mdmDevices) {
                Device device = convertDevice(mdmDevice, configParams);
                result.add(device);
            }
        } catch (DeviceManagementException e) {
            log.error("Error occurred getting the device list for user " + currentUser);
        }
        return result;
    }

    private Device convertDevice(org.wso2.carbon.device.mgt.common.Device mdmDevice,
            HashMap<String, String> configParams) {
        Device device = new Device();
        Map<String, String> props = new HashMap<>();
        for (org.wso2.carbon.device.mgt.common.Device.Property property : mdmDevice.getProperties()) {
            props.put(property.getName(), property.getValue());
        }
        device.setId(mdmDevice.getDeviceIdentifier() + APPM_DEVICE_ID_CONCAT_SEPARATOR + mdmDevice.getType());
        device.setName(props.get(MDMAppConstants.MDMAppPropertyKeys.DEVICE_NAME));
        device.setModel(props.get(MDMAppConstants.MDMAppPropertyKeys.DEVICE_MODEL));
        device.setPlatform(mdmDevice.getType());
        device.setImage(String.format(configParams.get(APPM_APP_IMAGE_URL), device.getModel()));
        device.setType(APPM_APP_TYPE_MOBILE_DEVICE);
        device.setPlatformVersion(props.get(MDMAppConstants.MDMAppPropertyKeys.OS_VERSION));

        return device;
    }

    private DeviceManagementProviderService getDeviceManagerService(String tenantDomain) throws AppManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            int tenantId;
            DeviceManagementProviderService deviceManagementProviderService;
            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = getTenantId(tenantDomain);
            }
            ctx.setTenantDomain(tenantDomain);
            ctx.setTenantId(tenantId);
            deviceManagementProviderService = (DeviceManagementProviderService) ctx
                    .getOSGiService(DeviceManagementProviderService.class, new Hashtable<String, String>());
            return deviceManagementProviderService;
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private int getTenantId(String tenantDomain) throws AppManagementException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        try {
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AppManagementException("Error obtaining tenant id from tenant domain " + tenantDomain);
        }
    }

    private ApplicationManager getAppManagementService(String tenantDomain) throws AppManagementException {
        ApplicationManager appService = null;
        // until complete login this is use to load super tenant context
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            int tenantId;

            if (tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = getTenantId(tenantDomain);
            }
            ctx.setTenantDomain(tenantDomain);
            ctx.setTenantId(tenantId);

            appService = (ApplicationManager) ctx
                    .getOSGiService(ApplicationManagementProviderService.class, new Hashtable<String, String>());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return appService;
    }
}
