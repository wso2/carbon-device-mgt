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

package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;
import org.wso2.carbon.device.mgt.extensions.utils.Utils;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.governance.api.util.GovernanceUtils.getGovernanceArtifactConfiguration;

/**
 * This class handles all the setup that need to be done before starting to run the test cases.
 */
public class BaseExtensionsTest {

    @BeforeSuite
    public void init() throws RegistryException, IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("device-types/license.rxt");
        String rxt = null;
        File carbonHome;
        if (resourceUrl != null) {
            rxt = FileUtil.readFileToString(resourceUrl.getFile());
        }
        resourceUrl = classLoader.getResource("carbon-home");

        if (resourceUrl != null) {
            carbonHome = new File(resourceUrl.getFile());
            System.setProperty("carbon.home", carbonHome.getAbsolutePath());
        }

        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        RegistryService registryService = Utils.getRegistryService();
        OSGiDataHolder.getInstance().setRegistryService(registryService);
        UserRegistry systemRegistry =
                registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);

        GovernanceArtifactConfiguration configuration =  getGovernanceArtifactConfiguration(rxt);
        List<GovernanceArtifactConfiguration> configurations = new ArrayList<>();
        configurations.add(configuration);
        GovernanceUtils.loadGovernanceArtifacts(systemRegistry, configurations);
        Registry governanceSystemRegistry = registryService.getConfigSystemRegistry();
        DeviceTypeExtensionDataHolder.getInstance().setRegistryService(registryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.SYSTEM_CONFIGURATION, governanceSystemRegistry);
    }
}
