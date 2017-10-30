package org.wso2.carbon.policy.mgt.core.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.DeviceGroupWrapper;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.policy.mgt.core.BasePolicyManagementDAOTest;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.PolicyManagerServiceImpl;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAO;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.impl.MonitoringDAOImpl;
import org.wso2.carbon.policy.mgt.core.enforcement.DelegationTask;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.mgt.FeatureManager;
import org.wso2.carbon.policy.mgt.core.mgt.MonitoringManager;
import org.wso2.carbon.policy.mgt.core.mgt.PolicyManager;
import org.wso2.carbon.policy.mgt.core.mock.TypeXDeviceManagementService;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MonitoringManagerImplTest extends BasePolicyManagementDAOTest{
    
    private static final Log log = LogFactory.getLog(MonitoringManagerImplTest.class);

    private static final String PROFILE5 = "profile5";
    private static final String DEVICE5 = "device5";
    private static final String POLICY5 = "policy5";
    private static final String GROUP5 = "group5";
    private static final String DEVICE_TYPE_E = "deviceTypeE";
    private static final String POLICY5_FEATURE1_CODE = "DISALLOW_ADJUST_VOLUME";

    private OperationManager operationManager;
    private FeatureManager featureManager;
    private MonitoringManager monitoringManager;
    private PolicyManagerService policyManagerService;

    private Device device5;
    private Policy policy5;
    private PolicyManager policyManager;

    @BeforeClass
    public void initialize() throws Exception {
        log.info("Initializing monitor manager tests");
        super.initializeServices();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        deviceMgtService.registerDeviceType(new TypeXDeviceManagementService(DEVICE_TYPE_E));
        operationManager = new OperationManagerImpl(DEVICE_TYPE_E);
        featureManager = new FeatureManagerImpl();
        monitoringManager = new MonitoringManagerImpl();
        policyManager = new PolicyManagerImpl();
        policyManagerService = new PolicyManagerServiceImpl();

        PolicyManagementDataHolder.getInstance().setPolicyManager(policyManager);

        enrollDevice(DEVICE5, DEVICE_TYPE_E);
        createDeviceGroup(GROUP5);
        addDeviceToGroup(new DeviceIdentifier(DEVICE5, DEVICE_TYPE_E), GROUP5);
        DeviceGroup group5 = groupMgtService.getGroup(GROUP5);

        device5 = deviceMgtService.getAllDevices().get(0);

        Profile profile = new Profile();
        profile.setProfileName(PROFILE5);
        profile.setTenantId(tenantId);
        profile.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        profile.setDeviceType(DEVICE_TYPE_E);

        DeviceGroupWrapper deviceGroupWrapper = new DeviceGroupWrapper();
        deviceGroupWrapper.setId(group5.getGroupId());
        deviceGroupWrapper.setName(GROUP5);
        deviceGroupWrapper.setOwner(ADMIN_USER);
        deviceGroupWrapper.setTenantId(tenantId);
        List<DeviceGroupWrapper> deviceGroupWrappers = new ArrayList<>();
        deviceGroupWrappers.add(deviceGroupWrapper);

        List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
        ProfileFeature profileFeature = new ProfileFeature();
        profileFeature.setContent("{'enable':'true'}");
        profileFeature.setDeviceType(DEVICE_TYPE_E);
        profileFeature.setFeatureCode(POLICY5_FEATURE1_CODE);
        profileFeatures.add(profileFeature);
        profile.setProfileFeaturesList(profileFeatures);
        profile.setProfileName("tp_profile1");
        profile.setUpdatedDate(new Timestamp(System.currentTimeMillis()));

        policy5 = new Policy();
        policy5.setPolicyName(POLICY5);
        policy5.setDescription(POLICY5);
        policy5.setProfile(profile);
        policy5.setOwnershipType("BYOD");
        policy5.setActive(false);
        policy5.setRoles(new ArrayList<>());
        policy5.setUsers(new ArrayList<>());
        policy5.setCompliance(PolicyManagementConstants.ENFORCE);
        policy5.setDeviceGroups(deviceGroupWrappers);
        List<Device> devices = new ArrayList<Device>();
        policy5.setDevices(devices);
        policy5.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        policy5 = policyManager.addPolicy(policy5);
        policyManagerService.getPAP().activatePolicy(policy5.getId());
        new DelegationTask().execute();
    }

    @Test(description = "This test case tests checking policy compliance")
    public void testCheckPolicyCompliance() throws Exception {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        monitoringManager.checkPolicyCompliance(deviceIdentifier, new ArrayList<ComplianceFeature>());
    }

    @Test(description = "This test case tests handling ProfileManagerDAOException when checking policy compliance",
          dependsOnMethods = "testCheckPolicyCompliance")
    public void testCheckPolicyComplianceThrowingProfileManagerDAOException() throws Exception {
        MonitoringDAO monitoringDAO = mock(MonitoringDAO.class);
        when(monitoringDAO.getCompliance(anyInt(),anyInt())).thenThrow(new MonitoringDAOException());

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        testThrowingException(monitoringManager,
                              null,
                              p -> monitoringManager.checkPolicyCompliance(deviceIdentifier, new ArrayList<ComplianceFeature>()),
                              "monitoringDAO", monitoringDAO,
                              MonitoringDAOException.class);
    }

    @Test(description = "This test case tests handling PolicyComplianceException when checking policy compliance",
          dependsOnMethods = "testCheckPolicyComplianceThrowingProfileManagerDAOException",
          expectedExceptions = PolicyComplianceException.class)
    public void testAddProfileThrowingPolicyComplianceException() throws Exception {
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doAnswer(new Answer<Connection>() {
            int callCounter = 0;
            @Override
            public Connection answer(InvocationOnMock invocationOnMock) throws Throwable {
                if(callCounter > 0){
                    Field currentConnectionField = PolicyManagementDAOFactory.class.getDeclaredField("currentConnection");
                    currentConnectionField.setAccessible(true);
                    ThreadLocal<Connection> threadLocal = new ThreadLocal<>();
                    threadLocal.set(pair.first());
                    currentConnectionField.set(null, threadLocal);
                    throw new SQLException();
                }
                callCounter++;
                return pair.second().first().getConnection();
            }
        }).when(pair.second().second()).getConnection();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        try {
            monitoringManager.checkPolicyCompliance(deviceIdentifier, new ArrayList<ComplianceFeature>());
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests handling PolicyComplianceException when checking policy compliance",
          dependsOnMethods = "testAddProfileThrowingPolicyComplianceException")
    public void testAddProfileThrowingMonitoringDAOException() throws Exception {
        MonitoringDAO monitoringDAO = mock(MonitoringDAO.class);
        when(monitoringDAO.getCompliance(anyInt(), anyInt())).thenThrow(
                new MonitoringDAOException());
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        testThrowingException(monitoringManager, deviceIdentifier, d -> monitoringManager.checkPolicyCompliance((DeviceIdentifier) d, new ArrayList<ComplianceFeature>()), "monitoringDAO",
                              monitoringDAO,
                              MonitoringDAOException.class);
    }

    @Test(description = "This test case tests handling PolicyComplianceException when checking policy compliance",
          dependsOnMethods = "testAddProfileThrowingMonitoringDAOException")
    public void testAddProfileThrowingMonitoringDAOException2() throws Exception {
        MonitoringDAO monitoringDAO = spy(MonitoringDAOImpl.class);
        doThrow(new MonitoringDAOException()).when(monitoringDAO).deleteNoneComplianceData(anyInt());
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        testThrowingException(monitoringManager, deviceIdentifier, d -> monitoringManager.checkPolicyCompliance((DeviceIdentifier) d, new ArrayList<ComplianceFeature>()), "monitoringDAO",
                              monitoringDAO,
                              MonitoringDAOException.class);
    }

    @Test(description = "This test case tests is compliant",
          dependsOnMethods = "testAddProfileThrowingMonitoringDAOException2")
    public void testIsCompliant() throws Exception {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        monitoringManager.isCompliant(deviceIdentifier);
    }

    @Test(description = "This test case tests is compliant",
          dependsOnMethods = "testIsCompliant")
    public void testIsCompliant2() throws Exception {
        MonitoringDAO monitoringDAO = spy(MonitoringDAOImpl.class);
        doReturn(null).when(monitoringDAO).getCompliance(anyInt(),anyInt());

        Object oldObj = changeFieldValue(monitoringManager, "monitoringDAO", monitoringDAO);
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setType(DEVICE_TYPE_E);
            deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
            boolean compliant = monitoringManager.isCompliant(deviceIdentifier);
            Assert.assertEquals(compliant,false);
        } finally {
            changeFieldValue(monitoringManager, "monitoringDAO", oldObj);
        }
    }

    @Test(description = "This test case tests handling DeviceManagementException when testing policy compliance",
          dependsOnMethods = "testIsCompliant2")
    public void testIsCompliantThrowingDeviceManagementException() throws Exception {
        DeviceManagementProviderService serviceMock = mock(DeviceManagementProviderService.class);
        doThrow(new DeviceManagementException()).when(serviceMock).getDevice(any(DeviceIdentifier.class), anyBoolean());

        PolicyManagementDataHolder instance = PolicyManagementDataHolder.getInstance();
        DeviceManagementProviderService service = instance.getDeviceManagementService();
        instance.setDeviceManagementService(serviceMock);

        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setType(DEVICE_TYPE_E);
            deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
            monitoringManager.isCompliant(deviceIdentifier);
        } catch (Exception e) {
            if (!(e.getCause() instanceof DeviceManagementException)) {
                throw e;
            }
        } finally {
            instance.setDeviceManagementService(service);
        }
    }

    @Test(description = "This test case tests handling MonitoringDAOException when checking policy compliance",
          dependsOnMethods = "testIsCompliantThrowingDeviceManagementException")
    public void testIsCompliantThrowingMonitoringDAOException() throws Exception {
        MonitoringDAO monitoringDAO = spy(MonitoringDAOImpl.class);
        doThrow(new MonitoringDAOException()).when(monitoringDAO).getCompliance(anyInt(),anyInt());
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        testThrowingException(monitoringManager, deviceIdentifier, d -> monitoringManager.isCompliant((DeviceIdentifier) d), "monitoringDAO",
                              monitoringDAO,
                              MonitoringDAOException.class);
    }

    @Test(description = "This test case tests handling SQLException when checking is compliant",
          dependsOnMethods = "testIsCompliantThrowingMonitoringDAOException",
          expectedExceptions = IllegalTransactionStateException.class)
    public void testIsCompliantThrowingIllegalTransactionStateException() throws Exception {
        Pair<Connection, Pair<DataSource, DataSource>> pair = mockConnection();
        PowerMockito.doThrow(new SQLException()).when(pair.second().second()).getConnection();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setType(DEVICE_TYPE_E);
            deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
            monitoringManager.isCompliant(deviceIdentifier);
        } finally {
            PolicyManagementDAOFactory.init(pair.second().first());
        }
    }

    @Test(description = "This test case tests is compliant",
          dependsOnMethods = "testIsCompliant2")
    public void testGetDevicePolicyCompliance() throws Exception {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(DEVICE_TYPE_E);
        deviceIdentifier.setId(String.valueOf(device5.getDeviceIdentifier()));
        monitoringManager.getDevicePolicyCompliance(deviceIdentifier);
    }

    @Test
    public void testAddMonitoringOperation() throws Exception {
        monitoringManager.addMonitoringOperation(deviceMgtService.getAllDevices());
    }

    @Test
    public void testGetDeviceTypes() throws Exception {
        monitoringManager.getDeviceTypes();
    }

}