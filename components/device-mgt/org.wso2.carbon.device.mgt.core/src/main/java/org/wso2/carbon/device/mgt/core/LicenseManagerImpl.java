/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.device.mgt.common.License;
import org.wso2.carbon.device.mgt.common.LicenseManagementException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LicenseManagerImpl implements LicenseManager {

    private static Log log = LogFactory.getLog(DeviceManagerImpl.class);

    @Override
    public License getLicense(final String deviceType,
            final String languageCodes) throws LicenseManagementException {

        if (log.isDebugEnabled()){
            log.debug("entered get License in license manager impl");
        }
        // TODO: After completes JAX-RX user login, this need to be change to CarbonContext
        PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

        Registry registry = (UserRegistry) PrivilegedCarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE);

        GenericArtifact[] filteredArtifacts;
        License license = new License();

        try {
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            GenericArtifactManager artifactManager = new GenericArtifactManager(registry, "license");

            filteredArtifacts = artifactManager.findGenericArtifacts(
                    new GenericArtifactFilter() {
                        public boolean matches(GenericArtifact artifact) throws GovernanceException {
                            String attributeNameVal = artifact.getAttribute("overview_name");
                            String attributeLangVal = artifact.getAttribute("overview_language");
                            return (attributeNameVal != null && attributeLangVal != null && attributeNameVal.equals
                                    (deviceType) && attributeLangVal.equals(languageCodes));
                        }
                    });
            String validFrom;
            String validTo;
            DateFormat format;
            Date fromDate;
            Date toDate;

            for (GenericArtifact artifact : filteredArtifacts) {
                if (log.isDebugEnabled()){
                    log.debug("Overview name:"+artifact.getAttribute("overview_name"));
                    log.debug("Overview provider:"+artifact.getAttribute("overview_provider"));
                    log.debug("Overview Language:"+artifact.getAttribute("overview_language"));
                    log.debug("overview_validityFrom:"+artifact.getAttribute("overview_validityFrom"));
                    log.debug("overview_validityTo:"+artifact.getAttribute("overview_validityTo"));
                }
                validFrom = artifact.getAttribute("overview_validityFrom");
                validTo = artifact.getAttribute("overview_validityTo");
                format = new SimpleDateFormat("dd-mm-yyyy", Locale.ENGLISH);
                try {
                    fromDate = format.parse(validFrom);
                    toDate = format.parse(validTo);
                    if (fromDate.getTime()<= new Date().getTime() && new Date().getTime() <= toDate.getTime()){
                        license.setLicenseText(artifact.getAttribute("overview_license"));
                    }
                } catch (ParseException e) {
                    log.error("validFrom:"+ validFrom);
                    log.error("validTo:"+validTo);
                    log.error("Valid date parse error:",e);
                }
            }
        } catch (RegistryException regEx) {
            log.error("registry exception:",regEx);
            throw new LicenseManagementException();
        }finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return license;
    }

}
