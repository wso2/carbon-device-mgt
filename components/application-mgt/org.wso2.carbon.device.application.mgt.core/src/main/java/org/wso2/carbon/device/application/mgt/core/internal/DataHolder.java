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

import org.wso2.carbon.device.application.mgt.common.services.*;
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

    private CategoryManager categoryManager;

    private CommentsManager commentsManager;

    private LifecycleStateManager lifecycleStateManager;

    private PlatformManager platformManager;

    private SubscriptionManager subscriptionManager;

    private VisibilityManager visibilityManager;

    private ApplicationStorageManager applicationStorageManager;

    private PlatformStorageManager platformStorageManager;

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

    public ApplicationReleaseManager getReleaseManager() {
        return releaseManager;
    }

    public void setReleaseManager(ApplicationReleaseManager releaseManager) {
        this.releaseManager = releaseManager;
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public void setCategoryManager(CategoryManager categoryManager) {
        this.categoryManager = categoryManager;
    }

    public CommentsManager getCommentsManager() {
        return commentsManager;
    }

    public void setCommentsManager(CommentsManager commentsManager) {
        this.commentsManager = commentsManager;
    }

    public LifecycleStateManager getLifecycleStateManager() {
        return lifecycleStateManager;
    }

    public void setLifecycleStateManager(LifecycleStateManager lifecycleStateManager) {
        this.lifecycleStateManager = lifecycleStateManager;
    }

    public PlatformManager getPlatformManager() {
        return platformManager;
    }

    public void setPlatformManager(PlatformManager platformManager) {
        this.platformManager = platformManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public VisibilityManager getVisibilityManager() {
        return visibilityManager;
    }

    public void setVisibilityManager(VisibilityManager visibilityManager) {
        this.visibilityManager = visibilityManager;
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

    public void setPlatformStorageManager(PlatformStorageManager platformStorageManager) {
        this.platformStorageManager = platformStorageManager;
    }
}
