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
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.api.Registry;

import javax.xml.namespace.QName;
import java.lang.String;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@SuppressWarnings("unused")
public class RegistryBasedLicenseManager implements LicenseManager {

    private Registry registry;
    private GenericArtifactManager artifactManager;

    public RegistryBasedLicenseManager() {
        Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        if (registry == null) {
            throw new IllegalArgumentException("Registry instance retrieved is null. Hence, " +
                    "'Registry based license manager cannot be initialized'");
        }
        this.registry = registry;
        try {
            this.artifactManager = GenericArtifactManagerFactory.getTenantAwareGovernanceArtifactManager(registry);
        } catch (LicenseManagementException e) {
            throw new IllegalStateException("Failed to initialize generic artifact manager bound to " +
                    "Registry based license manager", e);
        }
    }

    @Override
    public License getLicense(final String deviceType, final String languageCode) throws LicenseManagementException {
        try {
            GenericArtifact[] artifacts = artifactManager.findGenericArtifacts(new GenericArtifactFilter() {
                @Override
                public boolean matches(GenericArtifact artifact) throws GovernanceException {
                    String attributeNameVal = artifact.getAttribute(
                            DeviceManagementConstants.LicenseProperties.NAME);
                    String attributeLangVal = artifact.getAttribute(
                            DeviceManagementConstants.LicenseProperties.LANGUAGE);
                    return (attributeNameVal != null && attributeLangVal != null && attributeNameVal.equals
                            (deviceType) && attributeLangVal.equals(languageCode));
                }
            });
            if (artifacts == null || artifacts.length <= 0) {
                return null;
            }
            return this.populateLicense(artifacts[0]);
        } catch (GovernanceException e) {
            throw new LicenseManagementException("Error occurred while retrieving license corresponding to " +
                    "device type '" + deviceType + "'");
        } catch (ParseException e) {
            throw new LicenseManagementException("Error occurred while parsing the ToDate/FromDate date string " +
                    "of the license configured upon the device type '" + deviceType + "'");
        }
    }

    private License populateLicense(GenericArtifact artifact) throws GovernanceException, ParseException {
        License license = new License();
        license.setName(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.NAME));
        license.setProvider(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.PROVIDER));
        license.setVersion(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.VERSION));
        license.setLanguage(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.LANGUAGE));
        license.setText(artifact.getAttribute(DeviceManagementConstants.LicenseProperties.TEXT));

        DateFormat format = new SimpleDateFormat("dd-mm-yyyy", Locale.ENGLISH);
        license.setValidFrom(format.parse(artifact.getAttribute(
                DeviceManagementConstants.LicenseProperties.VALID_FROM)));
        license.setValidTo(format.parse(artifact.getAttribute(
                DeviceManagementConstants.LicenseProperties.VALID_TO)));
        return license;
    }

    @Override
    public void addLicense(final String deviceType, final License license) throws LicenseManagementException {
        GenericArtifactManager artifactManager =
                GenericArtifactManagerFactory.getTenantAwareGovernanceArtifactManager(registry);
        try {
            GenericArtifact artifact =
                    artifactManager.newGovernanceArtifact(new QName("http://www.wso2.com",
                            DeviceManagementConstants.LicenseProperties.LICENSE_REGISTRY_KEY));
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.NAME, license.getName());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VERSION, license.getVersion());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.PROVIDER, license.getProvider());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.LANGUAGE, license.getLanguage());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.TEXT, license.getText());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_TO,
                    license.getValidTo().toString());
            artifact.setAttribute(DeviceManagementConstants.LicenseProperties.VALID_FROM,
                    license.getValidFrom().toString());
            artifactManager.addGenericArtifact(artifact);
        } catch (GovernanceException e) {
            throw new LicenseManagementException("Error occurred while adding license for device type " +
                    deviceType + "'", e);
        }
    }

}
