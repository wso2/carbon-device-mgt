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

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * This is a test class for {@link NotificationManagementServiceImpl}.
 */
@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({"org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils",
        "org.wso2.carbon.context.CarbonContext"})
@PrepareForTest({DeviceMgtAPIUtils.class, MultitenantUtils.class, CarbonContext.class})
public class NotificationManagementServiceImplTest {
    private NotificationManagementService notificationManagementService;
    private org.wso2.carbon.device.mgt.jaxrs.service.api.NotificationManagementService notificationManagement;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    public void setup() throws UserStoreException, NotificationManagementException {
        initMocks(this);
        notificationManagementService = Mockito.mock(NotificationManagementService.class);
        PaginationResult paginationResult = new PaginationResult();
        List<Notification> notifications = new ArrayList<>();
        notifications.add(new Notification());
        paginationResult.setData(notifications);
        paginationResult.setRecordsTotal(1);
        Mockito.doReturn(paginationResult).when(notificationManagementService).getAllNotifications(Mockito.any());
        Mockito.doReturn(paginationResult).when(notificationManagementService)
                .getNotificationsByStatus(Mockito.any(), Mockito.any());
        notificationManagement = new NotificationManagementServiceImpl();
    }

    @Test(description = "This method tests the behaviour of getNotifications method under different conditions")
    public void testGetNotifications() throws NotificationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getNotificationManagementService"))
                .toReturn(this.notificationManagementService);
        Response response = notificationManagement.getNotifications("NEW", "test", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "Notification retrieval failed");
        response = notificationManagement.getNotifications(null, "test", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "Notification retrieval failed");
        Mockito.reset(this.notificationManagementService);
        Mockito.doThrow(new NotificationManagementException()).when(notificationManagementService)
                .getAllNotifications(Mockito.any());
        response = notificationManagement.getNotifications(null, "test", 0, 10);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Notification retrieval succeeded with issues in NotificationManagement OSGI service");
        Mockito.reset(this.notificationManagementService);
    }

    @Test(description = "This method tests the behaviour of updateNotificationStatus method under different conditions")
    public void testUpdateNotificationStatus() throws NotificationManagementException {
        PowerMockito.stub(PowerMockito.method(DeviceMgtAPIUtils.class, "getNotificationManagementService"))
                .toReturn(this.notificationManagementService);
        Mockito.doReturn(true).when(notificationManagementService)
                .updateNotificationStatus(1, Notification.Status.CHECKED);
        Mockito.doThrow(NotificationManagementException.class).when(notificationManagementService)
                .updateNotificationStatus(2, Notification.Status.CHECKED);
        Mockito.doReturn(true).when(notificationManagementService)
                .updateNotificationStatus(3, Notification.Status.CHECKED);
        Mockito.doReturn(new Notification()).when(notificationManagementService).getNotification(1);
        Mockito.doThrow(new NotificationManagementException()).when(notificationManagementService).getNotification(3);
        Response response = notificationManagement.updateNotificationStatus(1);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Notification status update failed under correct conditions");
        response = notificationManagement.updateNotificationStatus(2);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Notification status update succeeded under erroneous conditions");
        response = notificationManagement.updateNotificationStatus(3);
        Assert.assertEquals(response.getEntity(),
                "Notification updated successfully. But the retrial of the updated " + "notification failed",
                "Notification status update succeeded under erroneous conditions");
    }
}
