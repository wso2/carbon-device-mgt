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
package org.wso2.carbon.device.mgt.extensions.pull.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.pull.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationSubscriber;
import org.wso2.carbon.device.mgt.extensions.pull.notification.internal.PullNotificationDataHolder;

import java.util.Map;

public class PullNotificationSubscriberImpl implements PullNotificationSubscriber {

    private static final Log log = LogFactory.getLog(PullNotificationSubscriberImpl.class);

    public void init(Map<String, String> properties) {

    }

    public void execute(NotificationContext ctx) throws PullNotificationExecutionFailedException {
        Operation operation = new Operation();
        operation.setId(ctx.getNotificationPayload().getOperationId());
        operation.setPayLoad(ctx.getNotificationPayload().getPayload());
        try {
            PullNotificationDataHolder.getInstance().getDeviceManagementProviderService().updateOperation(
                    ctx.getDeviceId(), operation);
        } catch (OperationManagementException e) {
            throw new PullNotificationExecutionFailedException(e);
        }
    }

    public void clean() {

    }
}
