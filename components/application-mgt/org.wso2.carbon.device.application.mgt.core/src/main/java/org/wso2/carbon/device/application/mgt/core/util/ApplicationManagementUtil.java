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
package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.InvalidConfigurationException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationReleaseManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.CategoryManager;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;
import org.wso2.carbon.device.application.mgt.common.services.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.common.services.VisibilityManager;
import org.wso2.carbon.device.application.mgt.common.services.VisibilityTypeManager;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.config.Extension;

import java.lang.reflect.Constructor;

/**
 * This Util class is responsible for making sure single instance of each Extension Manager is used throughout for
 * all the tasks.
 */
public class ApplicationManagementUtil {

    private static Log log = LogFactory.getLog(ApplicationManagementUtil.class);

    public static ApplicationManager getApplicationManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationManager);
        return getInstance(extension, ApplicationManager.class);
    }

    public static ApplicationReleaseManager getApplicationReleaseManagerInstance()
            throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationReleaseManager);
        return getInstance(extension, ApplicationReleaseManager.class);
    }

    public static CategoryManager getCategoryManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.CategoryManager);
        return getInstance(extension, CategoryManager.class);
    }

    public static CommentsManager getCommentsManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.CommentsManager);
        return getInstance(extension, CommentsManager.class);
    }

    public static LifecycleStateManager getLifecycleStateManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.LifecycleStateManager);
        return getInstance(extension, LifecycleStateManager.class);
    }

    public static PlatformManager getPlatformManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.PlatformManager);
        return getInstance(extension, PlatformManager.class);
    }

    public static VisibilityTypeManager getVisibilityTypeManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.VisibilityTypeManager);
        return getInstance(extension, VisibilityTypeManager.class);
    }

    public static VisibilityManager getVisibilityManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.VisibilityManager);
        return getInstance(extension, VisibilityManager.class);
    }

    public static SubscriptionManager getSubscriptionManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.SubscriptionManager);
        return getInstance(extension, SubscriptionManager.class);
    }

    public static ApplicationStorageManager getApplicationStorageManagerInstance() throws
            InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationStorageManager);
        return getInstance(extension, ApplicationStorageManager.class);
    }

    private static <T> T getInstance(Extension extension, Class<T> cls) throws InvalidConfigurationException {
        try {
            Class theClass = Class.forName(extension.getClassName());
            if (extension.getParameters() != null && extension.getParameters().size() > 0) {
                Class[] types = new Class[extension.getParameters().size()];
                Object[] paramValues = new String[extension.getParameters().size()];
                for (int i = 0; i < extension.getParameters().size(); i++) {
                    types[i] = String.class;
                    paramValues[i] = extension.getParameters().get(i).getValue();
                }
                Constructor<T> constructor = theClass.getConstructor(types);
                return constructor.newInstance(paramValues);
            } else {
                Constructor<T> constructor = theClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            throw new InvalidConfigurationException(
                    "Unable to get instance of extension - " + extension.getName() + " , for class - " + extension
                            .getClassName(), e);
        }
    }
}
