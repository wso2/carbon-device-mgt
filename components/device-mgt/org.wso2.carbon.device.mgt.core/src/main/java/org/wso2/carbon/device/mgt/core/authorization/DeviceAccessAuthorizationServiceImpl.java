/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of DeviceAccessAuthorization service.
 */
public class DeviceAccessAuthorizationServiceImpl implements DeviceAccessAuthorizationService {

    private final static String EMM_ADMIN_PERMISSION = "/device-mgt/emm-admin";
    private final static String EMM_ADMIN = "Device Management Administrator";
    private final static String DEVICE_MGT_PERMISSION = "/device-mgt";
    private final static String DEVICE_MGT = "Device Management";
    private static Log log = LogFactory.getLog(DeviceAccessAuthorizationServiceImpl.class);

    public static final class PermissionMethod {
        private PermissionMethod() {
            throw new AssertionError();
        }

        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String DELETE = "delete";
        public static final String ACTION = "action";
        public static final String UI_EXECUTE = "ui.execute";
    }

    public DeviceAccessAuthorizationServiceImpl() {
        try {
            this.addAdminPermissionToRegistry();
        } catch (PermissionManagementException e) {
            log.error("Unable to add the emm-admin permission to the registry.", e);
        }
    }

    @Override
    public boolean isUserAuthorized(DeviceIdentifier deviceIdentifier) throws DeviceAccessAuthorizationException {
        boolean status;
        String username = this.getUserName();
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
             return !DeviceManagementDataHolder.getInstance().requireDeviceAuthorization(deviceIdentifier.getType());
        }
        try {
            //Check for admin users. If the user is an admin user we authorize the access to that device.
            status = isAdminUser(username, tenantId);
        } catch (UserStoreException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                         deviceIdentifier.getId() + " for the user : " + username, e);
        }
        //Check for device ownership. If the user is the owner of the device we allow the access.
        if (!status) {
            try {
                Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                                                                                           getDevice(deviceIdentifier);
                EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
                if (enrolmentInfo != null && username.equalsIgnoreCase(enrolmentInfo.getOwner())) {
                    status = true;
                }
            } catch (DeviceManagementException e) {
                throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                             deviceIdentifier.getId() + " for the user : " + username, e);
            }
        }
        return status;
    }

    @Override
    public DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers) throws
                                                                                   DeviceAccessAuthorizationException {
        boolean status;
        DeviceAuthorizationResult deviceAuthorizationResult = new DeviceAuthorizationResult();
        String username = this.getUserName();
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
            return deviceAuthorizationResult;
        }
        try {
            //Check for admin users. If the user is an admin user we authorize the access to that device.
            status = isAdminUser(username, tenantId);
        } catch (UserStoreException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access to devices for the user : " +
                                                         username, e);
        }
        //Check for device ownership. If the user is the owner of the device we allow the access.
        if (!status) {
            try {
                //Get the list of devices of the user
                List<Device> devicesOfUser =  DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevicesOfUser(username);
                //Convert device-list to a Map
                Map<String, String> ownershipData = this.getOwnershipOfDevices(devicesOfUser);
                for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                    if (ownershipData.containsKey(deviceIdentifier.getId())) {
                        deviceAuthorizationResult.addAuthorizedDevice(deviceIdentifier);
                    } else {
                        deviceAuthorizationResult.addUnauthorizedDevice(deviceIdentifier);
                    }
                }
            } catch (DeviceManagementException e) {
                throw new DeviceAccessAuthorizationException("Unable to authorize the access to devices for the user : "
                                                             + username, e);
            }
        } else {
            deviceAuthorizationResult.setAuthorizedDevices(deviceIdentifiers);
        }
        return deviceAuthorizationResult;
    }

    @Override
    public boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String username)
            throws DeviceAccessAuthorizationException {
        boolean status;
        int tenantId = this.getTenantId();
        if (username == null || username.isEmpty()) {
            return false;
        }
        try {
            //Check for admin users. If the user is an admin user we authorize the access to that device.
            status = isAdminUser(username, tenantId);
        } catch (UserStoreException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                         deviceIdentifier.getId() + " for the user : " + username, e);
        }
        //Check for device ownership. If the user is the owner of the device we allow the access.
        if (!status) {
            try {
                Device device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevice(deviceIdentifier);
                EnrolmentInfo enrolmentInfo = device.getEnrolmentInfo();
                if (enrolmentInfo != null && username.equalsIgnoreCase(enrolmentInfo.getOwner())) {
                    status = true;
                }
            } catch (DeviceManagementException e) {
                throw new DeviceAccessAuthorizationException("Unable to authorize the access to device : " +
                                                             deviceIdentifier.getId() + " for the user : " + username, e);
            }
        }
        return status;
    }

    @Override
    public DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String username)
                                                                            throws DeviceAccessAuthorizationException {
        boolean status;
        int tenantId = this.getTenantId();
        DeviceAuthorizationResult deviceAuthorizationResult = new DeviceAuthorizationResult();
        if (username == null || username.isEmpty()) {
            return null;
        }
        try {
            //Check for admin users. If the user is an admin user we authorize the access to that device.
            status = isAdminUser(username, tenantId);
        } catch (UserStoreException e) {
            throw new DeviceAccessAuthorizationException("Unable to authorize the access to devices for the user : " +
                                                         username, e);
        }
        //Check for device ownership. If the user is the owner of the device we allow the access.
        if (!status) {
            try {
                Device device;
                EnrolmentInfo enrolmentInfo;
                for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                    device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                            getDevice(deviceIdentifier);
                    enrolmentInfo = device.getEnrolmentInfo();
                    if (enrolmentInfo != null && username.equalsIgnoreCase(enrolmentInfo.getOwner())) {
                        deviceAuthorizationResult.addAuthorizedDevice(deviceIdentifier);
                    } else {
                        deviceAuthorizationResult.addUnauthorizedDevice(deviceIdentifier);
                    }
                }
            } catch (DeviceManagementException e) {
                throw new DeviceAccessAuthorizationException("Unable to authorize the access to devices for the user : "
                                                             + username, e);
            }
        } else {
            deviceAuthorizationResult.setAuthorizedDevices(deviceIdentifiers);
        }
        return deviceAuthorizationResult;
    }

    private boolean isAdminUser(String username, int tenantId) throws UserStoreException {
        UserRealm userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
        if (userRealm != null && userRealm.getAuthorizationManager() != null) {
            return userRealm.getAuthorizationManager()
                            .isUserAuthorized(removeTenantDomain(username), PermissionUtils.getAbsolutePermissionPath(EMM_ADMIN_PERMISSION),
                                              PermissionMethod.UI_EXECUTE);
        }
        return false;
    }

    private String getUserName() {
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (username != null && !username.isEmpty()) {
            return removeTenantDomain(username);
        }
        return null;
    }

    private String removeTenantDomain(String username) {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private boolean addAdminPermissionToRegistry() throws PermissionManagementException {
        Permission permission = new Permission();
        permission.setName(DEVICE_MGT);
        permission.setPath(PermissionUtils.getAbsolutePermissionPath(DEVICE_MGT_PERMISSION));
        PermissionUtils.putPermission(permission);
        permission = new Permission();
        permission.setName(EMM_ADMIN);
        permission.setPath(PermissionUtils.getAbsolutePermissionPath(EMM_ADMIN_PERMISSION));
        return PermissionUtils.putPermission(permission);
    }

    private Map<String, String> getOwnershipOfDevices(List<Device> devices) {
        Map<String, String> ownershipData = new HashMap<>();
        EnrolmentInfo enrolmentInfo;
        String owner;
        for (Device device : devices) {
            enrolmentInfo = device.getEnrolmentInfo();
            if (enrolmentInfo != null) {
                owner = enrolmentInfo.getOwner();
                if (owner != null && !owner.isEmpty()) {
                    ownershipData.put(device.getDeviceIdentifier(), owner);
                }
            }
        }
        return ownershipData;
    }
}