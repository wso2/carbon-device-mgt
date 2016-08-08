/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.webapp.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.scope.mgt.ScopeManagementService;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * This class contains the util methods which are needed
 * to web app publisher related functions.
 */
public class WebappPublisherUtil {

    private static Log log = LogFactory.getLog(WebappPublisherUtil.class);
    private static final int CARBON_SUPER = -1234;


    public static Document convertToDocument(File file) throws WebappPublisherConfigurationFailedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new WebappPublisherConfigurationFailedException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    public static ScopeManagementService getScopeManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ScopeManagementService scopeManagementService =
                (ScopeManagementService) ctx.getOSGiService(ScopeManagementService.class, null);
        if (scopeManagementService == null) {
            String msg = "Scope Management Service has not been initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return scopeManagementService;
    }

    /**
     * Getting the current tenant's user realm
     */
    public static UserRealm getUserRealm() throws UserStoreException {
        RealmService realmService;
        UserRealm realm;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);

        if (realmService == null) {
            throw new IllegalStateException("Realm service not initialized");
        }
        realm = realmService.getTenantUserRealm(CARBON_SUPER);
        return realm;
    }

}
