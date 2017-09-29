package org.wso2.carbon.device.mgt.core.push.notification.mgt.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.TestNotificationStrategy;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMapping;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationDAO;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PushNotificationSchedulerTaskTest extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(PushNotificationSchedulerTask.class);
    private DeviceManagementProviderService deviceMgtProviderService;
    private PushNotificationSchedulerTask pushNotificationSchedulerTask;
    private OperationDAO operationDAO;

    @BeforeClass
    public void init() throws DeviceManagementException, RegistryException {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Push Notification Scheduler Test Class");
        DeviceManagementServiceComponent.notifyStartupListeners();
        this.deviceMgtProviderService = Mockito.mock(DeviceManagementProviderServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(this.deviceMgtProviderService);
        this.operationDAO = OperationManagementDAOFactory.getOperationDAO();
        this.pushNotificationSchedulerTask = new PushNotificationSchedulerTask();
    }

    @Test
    public void testPushNotificationScheduler() {
        try {
            log.debug("Attempting to execute push notification task scheduler");
            Mockito.doReturn(new TestNotificationStrategy()).when(this.deviceMgtProviderService)
                    .getNotificationStrategyByDeviceType(Mockito.anyString());
            Mockito.doReturn(new org.wso2.carbon.device.mgt.common.operation.mgt.Operation())
                    .when(this.deviceMgtProviderService).getOperation(Mockito.anyString(), Mockito.anyInt());
            this.pushNotificationSchedulerTask.run();
            OperationManagementDAOFactory.openConnection();
            Map<Integer, List<OperationMapping>> operationMappingsTenantMap = operationDAO
                    .getOperationMappingsByStatus(Operation.Status.PENDING, Operation.PushNotificationStatus.SCHEDULED,
                            DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                                    .getPushNotificationConfiguration().getSchedulerBatchSize());
            Assert.assertEquals(operationMappingsTenantMap.size(), 0);
            log.debug("Push notification task execution complete.");
        } catch (DeviceManagementException e) {
            Assert.fail("Unexpected exception occurred when getting the push notification strategy.", e);
        } catch (SQLException | OperationManagementDAOException e) {
            Assert.fail("Unexpected exception occurred retrieving the operation mapping list.", e);
        } catch (OperationManagementException e) {
            Assert.fail("Unexpected exception occurred when retrieving an operation.", e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }
}
