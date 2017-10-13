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
import org.apache.axis2.engine.AxisConfiguration;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.util.DeviceTypePluginConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/*
    Unit tests for deviceTypeCAppDeployer
 */
public class DeviceTypeCAppDeployerTest {
    private DeviceTypeCAppDeployer deviceTypeCAppDeployer;
    private CarbonApplication carbonApplication = null;
    private AxisConfiguration axisConfiguration = null;
    private ApplicationConfiguration applicationConfiguration = null;
    private CappFile cappFile = new CappFile();


    private void initializeActifact(String type) {
        Artifact tempArtifact = new Artifact();
        cappFile.setName("testCappFile");
        tempArtifact.setType(type);
        tempArtifact.addFile(cappFile);
        Artifact.Dependency dependency = new Artifact.Dependency();
        dependency.setArtifact(tempArtifact);
        tempArtifact.addDependency(dependency);
        Mockito.doReturn(tempArtifact).when(applicationConfiguration).getApplicationArtifact();
    }

    private void initializeErrorArtifact() {
        Artifact errArtifact = new Artifact();
        errArtifact.setType(DeviceTypePluginConstants.CDMF_PLUGIN_TYPE);
        Artifact.Dependency dependency = new Artifact.Dependency();
        dependency.setArtifact(errArtifact);
        errArtifact.addDependency(dependency);
        Mockito.doReturn(errArtifact).when(applicationConfiguration).getApplicationArtifact();
    }

    @BeforeClass
    public void init() throws NoSuchFieldException, IllegalAccessException, IOException, RegistryException {
        Field deviceTypePlugins;
        Field deviceTypeUIs;
        deviceTypeCAppDeployer = Mockito.mock(DeviceTypeCAppDeployer.class, Mockito.CALLS_REAL_METHODS);
        carbonApplication = Mockito.mock(CarbonApplication.class, Mockito.CALLS_REAL_METHODS);
        axisConfiguration = Mockito.mock(AxisConfiguration.class, Mockito.CALLS_REAL_METHODS);
        applicationConfiguration = Mockito.mock(ApplicationConfiguration.class, Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(applicationConfiguration).when(carbonApplication).getAppConfig();
        Mockito.doNothing().when(deviceTypeCAppDeployer).deployTypeSpecifiedArtifacts(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
        Mockito.doNothing().when(deviceTypeCAppDeployer).undeployTypeSpecifiedArtifacts(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any());
        this.initializeCarbonContext();
        deviceTypePlugins = DeviceTypeCAppDeployer.class.getDeclaredField("deviceTypePlugins");
        deviceTypePlugins.setAccessible(true);
        deviceTypePlugins.set(deviceTypeCAppDeployer, new ArrayList<Artifact>());
        deviceTypeUIs = DeviceTypeCAppDeployer.class.getDeclaredField("deviceTypeUIs");
        deviceTypeUIs.setAccessible(true);
        deviceTypeUIs.set(deviceTypeCAppDeployer, new ArrayList<Artifact>());
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

    @Test(description = "deploying a capp of plugin type")
    public void testDeployCarbonAppsPluginType() throws DeploymentException, IllegalAccessException {
        initializeActifact(DeviceTypePluginConstants.CDMF_PLUGIN_TYPE);
        deviceTypeCAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test(description = "deploying an erroneous car file")
    public void testDeployErrorArtifact() throws DeploymentException, IllegalAccessException {
        initializeErrorArtifact();
        deviceTypeCAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test(dependsOnMethods = {"testDeployCarbonAppsPluginType"}, description = "undeploying previously deployed capp")
    public void testUndeployCarbonAppsPluginType() throws DeploymentException {
        deviceTypeCAppDeployer.undeployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test(dependsOnMethods = {"testUndeployCarbonAppsPluginType"}, description = "deploying a capp of UI type")
    public void testDeployCarbonAppsUiType() throws DeploymentException, IllegalAccessException {
        initializeActifact(DeviceTypePluginConstants.CDMF_UI_TYPE);
        deviceTypeCAppDeployer.deployArtifacts(carbonApplication, axisConfiguration);
    }

    @Test(dependsOnMethods = {"testDeployCarbonAppsUiType"}, description = "Undeploy previously deployed capp")
    public void testUndeployCarbonAppsUiType() throws DeploymentException {
        deviceTypeCAppDeployer.undeployArtifacts(carbonApplication, axisConfiguration);
    }
}
