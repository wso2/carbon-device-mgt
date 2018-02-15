/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.internal;

import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.common.services.UnrestrictedRoleManager;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * DataHolder is responsible for holding the references to OSGI Services.
 */
public class DataHolder {

    private DeviceManagementProviderService deviceManagementService;

    private RealmService realmService;

    private ApplicationManager applicationManager;

    private ApplicationReleaseManager releaseManager;

    private CommentsManager commentsManager;

    private SubscriptionManager subscriptionManager;

    private UnrestrictedRoleManager unrestrictedRoleManager;

    private ApplicationStorageManager applicationStorageManager;

    private static final DataHolder applicationMgtDataHolder = new DataHolder();

    private DataHolder() {

    }

    public static DataHolder getInstance() {
        return applicationMgtDataHolder;
    }

    public DeviceManagementProviderService getDeviceManagementService() {
        return deviceManagementService;
    }

    public void setDeviceManagementService(DeviceManagementProviderService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public ApplicationReleaseManager getApplicationReleaseManager() {
        return releaseManager;
    }

    public void setApplicationReleaseManager(ApplicationReleaseManager releaseManager) {
        this.releaseManager = releaseManager;
    }

    public CommentsManager getCommentsManager() {
        return commentsManager;
    }

    public void setCommentsManager(CommentsManager commentsManager) {
        this.commentsManager = commentsManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public UnrestrictedRoleManager getVisibilityManager() {
        return unrestrictedRoleManager;
    }

    public void setVisibilityManager(UnrestrictedRoleManager unrestrictedRoleManager) {
        this.unrestrictedRoleManager = unrestrictedRoleManager;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public void setApplicationStorageManager(ApplicationStorageManager applicationStorageManager) {
        this.applicationStorageManager = applicationStorageManager;
    }

    public ApplicationStorageManager getApplicationStorageManager() {
        return applicationStorageManager;
    }
}
