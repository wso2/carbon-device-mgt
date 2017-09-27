/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.core.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EntityDoesNotExistException;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to test NotificationManagementServiceImpl.
 */
public class NotificationManagementServiceImplTests {

    private static final Log log = LogFactory.getLog(NotificationManagementServiceImplTests.class);
    private static final String DEVICE_TYPE = "NOTIFICATION_TEST_DEVICE";
    private static final String DEVICE_ID_PREFIX = "NOTIFICATION-TEST-DEVICE-ID-";
    private static final int NO_OF_DEVICES = 10;
    private static final int NO_OF_NOTIFICATIONS = 10;
    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private NotificationManagementServiceImpl notificationManagementService;
    private static final String TEST_NOTIFICATION_DESCRIPTION = "test notification";
    private static final int NOTIFICATION_OPERATION_ID = 1;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing");
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);

        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        notificationManagementService = new NotificationManagementServiceImpl();
    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test(description = "Add notifications using addNotification method and check whether it returns true.")
    public void addNotification() throws Exception {
        Notification notification = null;
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE);
            notification = TestDataHolder.getNotification(i, Notification.Status.NEW.toString(),
                    testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + i,
                    NOTIFICATION_OPERATION_ID, DEVICE_TYPE);
            Assert.assertTrue(notificationManagementService.addNotification(testDeviceIdentifier, notification));
        }
        try {
            notificationManagementService.addNotification(new DeviceIdentifier(DEVICE_ID_PREFIX + 123,
                    DEVICE_TYPE), notification);
            Assert.fail();
        } catch (EntityDoesNotExistException ignored) {
        }
    }

    @Test(dependsOnMethods = "addNotification", description = "This tests the updateNotification Method" +
            " and check whether it returns true ( got updated )")
    public void updateNotification() throws Exception {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE);
            Notification notification = TestDataHolder.getNotification(i, Notification.Status.CHECKED.toString(),
                    testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + i, NOTIFICATION_OPERATION_ID,
                    DEVICE_TYPE);
            Assert.assertTrue(notificationManagementService.updateNotification(notification));
        }
    }

    @Test(dependsOnMethods = "updateNotification", description = "This method update notification status " +
            "and check whether it got updated")
    public void updateNotificationStatus() throws Exception {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            Assert.assertTrue(notificationManagementService.updateNotificationStatus(i, Notification.Status.CHECKED));
        }
    }

    @Test(dependsOnMethods = "addNotification", description = "this tests getAllNotifications" +
            " method by listing down all the notifications.")
    public void getAllNotifications() throws Exception {
        List<Notification> returnedNotifications = notificationManagementService.getAllNotifications();
        Assert.assertEquals(returnedNotifications.size(), NO_OF_DEVICES);
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this method retries notification by id" +
            " and checks it")
    public void getNotification() throws Exception {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            Notification returnedNotification = notificationManagementService.getNotification(i);
            Assert.assertEquals(returnedNotification.getNotificationId(), i);
            Assert.assertEquals(returnedNotification.getStatus(), Notification.Status.CHECKED);
            Assert.assertEquals(returnedNotification.getDescription(), TEST_NOTIFICATION_DESCRIPTION);
            Assert.assertEquals(returnedNotification.getOperationId(), NOTIFICATION_OPERATION_ID);
        }
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this method gets all notification by status checked")
    public void getNotificationsByStatus() throws Exception {
        List<Notification> returnedNotifications = notificationManagementService.getNotificationsByStatus(Notification.Status.CHECKED);
        Assert.assertEquals(returnedNotifications.size(), NO_OF_NOTIFICATIONS);
    }

}

