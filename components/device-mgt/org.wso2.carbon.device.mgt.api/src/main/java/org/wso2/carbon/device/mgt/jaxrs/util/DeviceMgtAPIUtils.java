/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.jaxrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * MDMAPIUtils class provides utility function used by CDM REST-API classes.
 */
public class DeviceMgtAPIUtils {

    public static final MediaType DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_TYPE;
    private static final String NOTIFIER_FREQUENCY = "notifierFrequency";
    private static Log log = LogFactory.getLog(DeviceMgtAPIUtils.class);

    public static int getNotifierFrequency(PlatformConfiguration tenantConfiguration) {
        List<ConfigurationEntry> configEntryList = tenantConfiguration.getConfiguration();
        if (configEntryList != null && !configEntryList.isEmpty()) {
            for (ConfigurationEntry entry : configEntryList) {
                if (NOTIFIER_FREQUENCY.equals(entry.getName())) {
                    if (entry.getValue() == null) {
                        throw new InputValidationException(
                                new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(
                                        "Notifier frequency cannot be null. Please specify a valid non-negative " +
                                                "integer value to successfully set up notification frequency. " +
                                                "Should the service be stopped, use '0' as the notification " +
                                                "frequency.").build()
                        );
                    }
                    return (int) (Double.parseDouble(entry.getValue().toString()) + 0.5d);
                }
            }
        }
        return 0;
    }

    public static void scheduleTaskService(int notifierFrequency) {
        TaskScheduleService taskScheduleService;
        try {
            taskScheduleService = getPolicyManagementService().getTaskScheduleService();
            if (taskScheduleService.isTaskScheduled()) {
                taskScheduleService.updateTask(notifierFrequency);
            } else {
                taskScheduleService.startTask(notifierFrequency);
            }
        } catch (PolicyMonitoringTaskException e) {
            log.error("Exception occurred while starting the Task service.", e);
        }
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "DeviceImpl Management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                (DeviceAccessAuthorizationService) ctx.getOSGiService(DeviceAccessAuthorizationService.class, null);
        if (deviceAccessAuthorizationService == null) {
            String msg = "DeviceAccessAuthorization service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceAccessAuthorizationService;
    }

    public static GroupManagementProviderService getGroupManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        GroupManagementProviderService groupManagementProviderService =
                (GroupManagementProviderService) ctx.getOSGiService(GroupManagementProviderService.class, null);
        if (groupManagementProviderService == null) {
            String msg = "GroupImpl Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return groupManagementProviderService;
    }

    public static UserStoreManager getUserStoreManager() throws UserStoreException {
        RealmService realmService;
        UserStoreManager userStoreManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        int tenantId = ctx.getTenantId();
        userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        return userStoreManager;
    }

    public static RealmService getRealmService() throws UserStoreException {
        RealmService realmService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    /**
     * Getting the current tenant's user realm
     */
    public static UserRealm getUserRealm() throws UserStoreException {
        RealmService realmService;
        UserRealm realm;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);

        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        realm = realmService.getTenantUserRealm(tenantId);
        return realm;
    }

    public static AuthorizationManager getAuthorizationManager() throws UserStoreException {
        RealmService realmService;
        AuthorizationManager authorizationManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized.");
        }
        int tenantId = ctx.getTenantId();
        authorizationManager = realmService.getTenantUserRealm(tenantId).getAuthorizationManager();

        return authorizationManager;
    }

    public static ApplicationManagementProviderService getAppManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ApplicationManagementProviderService applicationManagementProviderService =
                (ApplicationManagementProviderService) ctx.getOSGiService(ApplicationManagementProviderService.class, null);
        if (applicationManagementProviderService == null) {
            throw new IllegalStateException("AuthenticationImpl management service has not initialized.");
        }
        return applicationManagementProviderService;
    }

    public static PolicyManagerService getPolicyManagementService() {
        PolicyManagerService policyManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        policyManagementService =
                (PolicyManagerService) ctx.getOSGiService(PolicyManagerService.class, null);
        if (policyManagementService == null) {
            String msg = "PolicyImpl Management service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return policyManagementService;
    }

    public static PlatformConfigurationManagementService getPlatformConfigurationManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PlatformConfigurationManagementService tenantConfigurationManagementService =
                (PlatformConfigurationManagementService) ctx.getOSGiService(
                        PlatformConfigurationManagementService.class, null);
        if (tenantConfigurationManagementService == null) {
            throw new IllegalStateException("Tenant configuration Management service not initialized.");
        }
        return tenantConfigurationManagementService;
    }

    public static NotificationManagementService getNotificationManagementService() {
        NotificationManagementService notificationManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        notificationManagementService = (NotificationManagementService) ctx.getOSGiService(
                NotificationManagementService.class, null);
        if (notificationManagementService == null) {
            throw new IllegalStateException("Notification Management service not initialized.");
        }
        return notificationManagementService;
    }

    public static DeviceInformationManager getDeviceInformationManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager deviceInformationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);
        if (deviceInformationManager == null) {
            throw new IllegalStateException("DeviceImpl information Manager service has not initialized.");
        }
        return deviceInformationManager;
    }


    public static SearchManagerService getSearchManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        SearchManagerService searchManagerService =
                (SearchManagerService) ctx.getOSGiService(SearchManagerService.class, null);
        if (searchManagerService == null) {
            throw new IllegalStateException("DeviceImpl search manager service is not initialized.");
        }
        return searchManagerService;
    }

    public static GadgetDataService getGadgetDataService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        GadgetDataService gadgetDataService = (GadgetDataService) ctx.getOSGiService(GadgetDataService.class, null);
        if (gadgetDataService == null) {
            throw new IllegalStateException("Gadget Data Service has not been initialized.");
        }
        return gadgetDataService;
    }

    public static ScopeManagementService getScopeManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ScopeManagementService scopeManagementService =
                (ScopeManagementService) ctx.getOSGiService(ScopeManagementService.class, null);
        if (scopeManagementService == null) {
            throw new IllegalStateException("Scope Management Service has not been initialized.");
        }
        return scopeManagementService;
    }

    public static int getTenantId(String tenantDomain) throws DeviceManagementException {
        RealmService realmService =
                (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new IllegalStateException("Realm service has not been initialized.");
        }
        try {
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new DeviceManagementException("Error occured while trying to " +
                "obtain tenant id of currently logged in user");
        }
    }

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username != null && username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

}
