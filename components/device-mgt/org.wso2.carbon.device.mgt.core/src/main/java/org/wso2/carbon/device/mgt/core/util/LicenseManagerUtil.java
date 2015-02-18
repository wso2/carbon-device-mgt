/*
 * *
 *  *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.util;

import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.LicenseManagementException;
import org.wso2.carbon.device.mgt.core.config.license.License;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.license.mgt.GenericArtifactManagerFactory;
import org.wso2.carbon.governance.api.common.GovernanceArtifactFilter;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import javax.xml.namespace.QName;
import java.util.Date;

public class LicenseManagerUtil {

    public static void addDefaultLicenses(LicenseConfig licenseConfig) throws LicenseManagementException {
        try {
            GenericArtifactManager artifactManager =
                    GenericArtifactManagerFactory.getTenantAwareGovernanceArtifactManager();
            for (License license : licenseConfig.getLicenses()) {
                /* TODO: This method call can be expensive as it appears to do a complete table scan to check the existence
                 * of an artifact for each license configuration. Need to find the optimal way of doing this */
                if (LicenseManagerUtil.isArtifactExists(artifactManager, license)) {
                    continue;
                }
                GenericArtifact artifact =
                        artifactManager.newGovernanceArtifact(new QName("http://www.wso2.com",
                                DeviceManagementConstants.LicenseProperties.LICENSE_REGISTRY_KEY));
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.OVERVIEW_NAME, license.getName());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.OVERVIEW_VERSION,
                        license.getVersion());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.OVERVIEW_LANGUAGE,
                        license.getLanguage());
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.OVERVIEW_PROVIDER,
                        license.getProvider());
                Date validTo = license.getValidTo();
                if (validTo != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_TO, validTo.toString());
                }
                Date validFrom = license.getValidFrom();
                if (validFrom != null) {
                    artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_FROM, validFrom.toString());
                }
                artifact.setAttribute(DeviceManagementConstants.LicenseProperties.LICENSE, license.getText());
                artifactManager.addGenericArtifact(artifact);
            }
        } catch (GovernanceException e) {
            throw new LicenseManagementException("Error occurred while initializing default licences", e);
        }
    }

    private static boolean isArtifactExists(final GenericArtifactManager artifactManager,
                                            final License license) throws GovernanceException {
        GovernanceArtifact[] artifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
            @Override
            public boolean matches(GenericArtifact artifact) throws GovernanceException {
                return artifact.getAttribute(DeviceManagementConstants.LicenseProperties.OVERVIEW_NAME).equals(
                        license.getName());
            }
        });
        return (artifacts != null && artifacts.length > 0);
    }

}
