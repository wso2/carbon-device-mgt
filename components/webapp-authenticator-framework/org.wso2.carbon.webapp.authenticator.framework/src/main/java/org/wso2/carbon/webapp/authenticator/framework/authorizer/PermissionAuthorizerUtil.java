/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.authenticator.framework.authorizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.webapp.authenticator.framework.authorizer.config.Permission;

public class PermissionAuthorizerUtil {

    private static Registry registry = CarbonContext.getThreadLocalCarbonContext().
            getRegistry(RegistryType.SYSTEM_GOVERNANCE);

    private static final String PROPERTY_NAME = "name";
    private static final String PATH_PERMISSION = "/permission";
    private static final Log log = LogFactory.getLog(PermissionAuthorizerUtil.class);

    public static void addPermission(Permission permission) {

        if (registry == null) {
            throw new IllegalArgumentException("Registry instance retrieved is null");
        }

        if (permission == null) {
            throw new IllegalArgumentException("Permission argument is null");
        }
        try {
            Collection collection = registry.newCollection();
            collection.setProperty(PROPERTY_NAME, permission.getName());
            registry.put(PATH_PERMISSION + permission.getPath(), collection);

        } catch (RegistryException e) {
            String errorMsg = "Error occured while adding permission '" + permission.getName() +
                              "' to registry. ";
            log.error(errorMsg + e.getMessage());
        }
    }
}
