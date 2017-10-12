package org.wso2.carbon.device.mgt.extensions.device.type.deployer;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.junit.Assert;
import org.mockito.Mockito;
import org.osgi.framework.ServiceRegistration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.device.type.template.DeviceTypeConfigIdentifier;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceTypePluginDeployerTest {

    private DeviceTypePluginDeployer deviceTypePluginDeployer;
    private DeploymentFileData deploymentFileData;

    private Field deviceTypeServiceRegistrations = null;
    private Field deviceTypeConfigurationDataMap = null;
    private ServiceRegistration serviceRegistration = null;

    @BeforeClass
    public void init() throws Exception {
        deviceTypePluginDeployer = Mockito.mock(DeviceTypePluginDeployer.class, Mockito.CALLS_REAL_METHODS);
        serviceRegistration = Mockito.mock(ServiceRegistration.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(serviceRegistration).when(deviceTypePluginDeployer).registerDeviceType(Mockito.any(),
                Mockito.any());

        deviceTypeServiceRegistrations = DeviceTypePluginDeployer.class.getDeclaredField
                ("deviceTypeServiceRegistrations");
        deviceTypeServiceRegistrations.setAccessible(true);
        deviceTypeServiceRegistrations.set(deviceTypePluginDeployer, new ConcurrentHashMap());

        deviceTypeConfigurationDataMap = DeviceTypePluginDeployer.class.getDeclaredField
                ("deviceTypeConfigurationDataMap");
        deviceTypeConfigurationDataMap.setAccessible(true);
        deviceTypeConfigurationDataMap.set(deviceTypePluginDeployer, new ConcurrentHashMap());

        this.initializeCarbonContext();
    }

    private void initializeCarbonContext() throws IOException, RegistryException {

        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());

            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Testing deviceType deploy method by deploying Android device type")
    public void deploy() throws DeploymentException, IllegalAccessException {
        File file = new File("src/test/resources/android.xml");
        if (file.exists()) {
            deploymentFileData = new DeploymentFileData(file);
        }
        deviceTypePluginDeployer.deploy(deploymentFileData);
        Map<String, ServiceRegistration> tempServiceRegistration = (Map<String, ServiceRegistration>)
                deviceTypeServiceRegistrations.get(deviceTypePluginDeployer);
        Assert.assertEquals(tempServiceRegistration.get(deploymentFileData.getAbsolutePath()), serviceRegistration);
        Map<String, DeviceTypeConfigIdentifier> tempDeviceTypeConfig = (Map<String, DeviceTypeConfigIdentifier>)
                deviceTypeConfigurationDataMap.get(deviceTypePluginDeployer);
        DeviceTypeConfigIdentifier deviceTypeConfigIdentifier = tempDeviceTypeConfig.get(deploymentFileData
                .getAbsolutePath());
        Assert.assertEquals(deviceTypeConfigIdentifier.getDeviceType(), "android");
    }
}
