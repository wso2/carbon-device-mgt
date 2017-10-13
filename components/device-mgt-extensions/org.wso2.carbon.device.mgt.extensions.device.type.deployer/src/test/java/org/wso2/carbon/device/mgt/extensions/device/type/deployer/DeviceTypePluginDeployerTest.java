/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/*
    Unit tests for DeviceTypePluginDeployer
 */
public class DeviceTypePluginDeployerTest {
    private DeviceTypePluginDeployer deviceTypePluginDeployer;
    private DeploymentFileData deploymentFileData;
    private DeploymentFileData invalidDeploymentFileData;
    private Field deviceTypeServiceRegistrations = null;
    private Field deviceTypeConfigurationDataMap = null;
    private ServiceRegistration serviceRegistration = null;
    private File file = new File("src/test/resources/android.xml");
    private File invalidFile = new File("src/test/resources/invalidAndroid.xml");

    @BeforeClass
    public void init() throws NoSuchFieldException, IllegalAccessException, IOException, RegistryException {
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
        if (file.exists()) {
            deploymentFileData = new DeploymentFileData(file);
        }
        if (invalidFile.exists()) {
            invalidDeploymentFileData = new DeploymentFileData(invalidFile);
        }
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

    @Test(description = "Testing exception for invalid xml files", expectedExceptions = {org.apache.axis2.deployment
            .DeploymentException.class})
    public void deployInvalidXml() throws DeploymentException, IllegalAccessException {
        deviceTypePluginDeployer.deploy(invalidDeploymentFileData);
    }

    @Test(description = "Testing exception for non existing xml file", expectedExceptions = {org.apache.axis2.deployment
            .DeploymentException.class})
    public void unDeployInvalidXml() throws DeploymentException, IllegalAccessException {
        deviceTypePluginDeployer.deploy(new DeploymentFileData(new File("src/test/resources/notExist.xml")));
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"deploy"} , description = "Testing deviceType undeploy method by un-deploying Android " +
            "device type")
    public void unDeploy() throws DeploymentException, IllegalAccessException {
        deviceTypePluginDeployer.undeploy(deploymentFileData.getAbsolutePath());
        Map<String, ServiceRegistration> tempServiceRegistration = (Map<String, ServiceRegistration>)
                deviceTypeServiceRegistrations.get(deviceTypePluginDeployer);
        Assert.assertNull(tempServiceRegistration.get(deploymentFileData.getAbsolutePath()));
        Map<String, DeviceTypeConfigIdentifier> tempDeviceTypeConfig = (Map<String, DeviceTypeConfigIdentifier>)
                deviceTypeConfigurationDataMap.get(deviceTypePluginDeployer);
        Assert.assertNull(tempDeviceTypeConfig.get(deploymentFileData.getAbsolutePath()));
    }
}
