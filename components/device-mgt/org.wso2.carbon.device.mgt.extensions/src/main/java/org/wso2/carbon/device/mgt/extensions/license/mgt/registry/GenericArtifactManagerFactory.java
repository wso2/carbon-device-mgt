/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.license.mgt.registry;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.HashMap;
import java.util.Map;

public class GenericArtifactManagerFactory {

    private static Map<Integer, GenericArtifactManager> tenantArtifactManagers =
            new HashMap<Integer, GenericArtifactManager>();
    private static final Object LOCK = new Object();

    public static GenericArtifactManager getTenantAwareGovernanceArtifactManager(
            Registry registry) throws LicenseManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GenericArtifactManager artifactManager;
            synchronized (LOCK) {
                artifactManager =
                        tenantArtifactManagers.get(tenantId);
                if (artifactManager == null) {
                    /* Hack, to fix https://wso2.org/jira/browse/REGISTRY-2427 */
                    //GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                    artifactManager =
                            new GenericArtifactManager((org.wso2.carbon.registry.core.Registry) registry,
                                    DeviceManagementConstants.LicenseProperties.LICENSE_REGISTRY_KEY);
                    tenantArtifactManagers.put(tenantId, artifactManager);
                }
            }
            return artifactManager;
        } catch (RegistryException e) {
            throw new LicenseManagementException("Error occurred while initializing GovernanceArtifactManager " +
                    "associated with tenant '" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + "'", e);
        }
    }

}
