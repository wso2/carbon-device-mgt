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
package org.wso2.carbon.device.mgt.core.push.notification.mgt.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationMappingDAO;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.LinkedList;
import java.util.List;

/**
 * ${{@link PushNotificationJob}} is for sending push notifications for given device batch.
 */
public class PushNotificationJob implements Runnable {

    private static Log log = LogFactory.getLog(PushNotificationJob.class);
    private final OperationDAO operationDAO = OperationManagementDAOFactory.getOperationDAO();
    private final OperationMappingDAO operationMappingDAO = OperationManagementDAOFactory.getOperationMappingDAO();
    private final DeviceManagementProviderService provider = DeviceManagementDataHolder.getInstance()
            .getDeviceManagementProvider();
    private final TenantManager tenantManager = DeviceManagementDataHolder.getInstance().getRealmService()
            .getTenantManager();

    @Override
    public void run() {
        List<OperationMapping> operationsCompletedList = new LinkedList<>();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Push notification job started");
            }
            //Get next available operation list per device batch
            List<OperationMapping> operationMappings = operationDAO.getOperationMappingsByStatus(Operation.Status
                    .PENDING, Operation.PushStatus.SCHEDULED, DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig().getPushNotificationConfiguration().getSchedulerBatchSize());
            // Sending push notification to each device
            for (OperationMapping operationMapping : operationMappings) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Sending push notification for operationId :" + operationMapping.getOperationId() +
                                "to deviceId : " + operationMapping.getDeviceIdentifier().getId());
                    }
                    // Set tenant id and domain
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(operationMapping.getTenantId());
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantManager.getDomain
                            (operationMapping.getTenantId()));
                    // Get notification strategy for given device type
                    NotificationStrategy notificationStrategy = provider.getNotificationStrategyByDeviceType
                            (operationMapping.getDeviceIdentifier().getType());
                    // Send the push notification on given strategy
                    notificationStrategy.execute(new NotificationContext(operationMapping.getDeviceIdentifier(),
                            provider.getOperation(operationMapping.getDeviceIdentifier().getType(), operationMapping
                                    .getOperationId())));
                    operationMapping.setPushStatus(Operation.PushStatus.COMPLETED);
                    operationsCompletedList.add(operationMapping);
                } catch (DeviceManagementException e) {
                    log.error("Error occurred while getting notification strategy for operation mapping " +
                            operationMapping.getDeviceIdentifier().getType(), e);
                } catch (OperationManagementException e) {
                    log.error("Unable to get the operation for operation " + operationMapping.getOperationId(), e);
                } catch (PushNotificationExecutionFailedException e) {
                    log.error("Error occurred while sending push notification to operation:  " + operationMapping
                            .getOperationId(), e);
                } catch (UserStoreException e) {
                    log.error("Tenant domain cannot be found for given tenant id:  " + operationMapping.getTenantId()
                            , e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Push notification job running completed.");
            }
        } catch (OperationManagementDAOException e) {
            log.error("Unable to retrieve scheduled pending operations for task. ", e);
        } finally {
            // Update push notification status to competed for operations which already sent
            try {
                OperationManagementDAOFactory.beginTransaction();
                operationMappingDAO.updateOperationMapping(operationsCompletedList);
                OperationManagementDAOFactory.commitTransaction();
            } catch (TransactionManagementException | OperationManagementDAOException e) {
                OperationManagementDAOFactory.rollbackTransaction();
                log.error("Error occurred while updating operation mappings for sent notifications ", e);
            } finally {
                OperationManagementDAOFactory.closeConnection();
            }
        }
    }
}
