/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.email;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;

import java.io.*;

public class RegistryBasedResourceLoader extends ResourceLoader {

    private static final String EMAIL_CONFIG_BASE_LOCATION = "email-templates";

    @Override
    public void init(ExtendedProperties extendedProperties) {

    }

    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException {
        try {
            Registry registry =
                    CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_CONFIGURATION);
            if (registry == null) {
                throw new IllegalStateException("No valid registry instance is attached to the current carbon context");
            }
            if (!registry.resourceExists(EMAIL_CONFIG_BASE_LOCATION + "/" + name + ".vm")) {
                throw new ResourceNotFoundException("Resource '" + name + "' does not exist");
            }
            org.wso2.carbon.registry.api.Resource resource =
                    registry.get(EMAIL_CONFIG_BASE_LOCATION + "/" + name + ".vm");

            return resource.getContentStream();
        } catch (RegistryException e) {
            throw new ResourceNotFoundException("Error occurred while retrieving resource", e);
        }
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }

}
