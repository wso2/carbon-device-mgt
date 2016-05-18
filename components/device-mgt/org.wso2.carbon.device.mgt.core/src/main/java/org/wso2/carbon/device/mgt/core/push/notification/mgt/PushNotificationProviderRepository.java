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
package org.wso2.carbon.device.mgt.core.push.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PushNotificationProviderRepository {

    private Map<String, PushNotificationProvider> providers;
    private static final Log log = LogFactory.getLog(PushNotificationProviderRepository.class);

    public PushNotificationProviderRepository() {
        this.providers = new ConcurrentHashMap<>();
    }

    public void addProvider(PushNotificationProvider provider) {
        providers.put(provider.getType(), provider);
    }

    public void addProvider(String className) {
        try {
            Class<?> clz = Class.forName(className);
            PushNotificationProvider provider = (PushNotificationProvider) clz.newInstance();
            providers.put(provider.getType(), provider);
        } catch (ClassNotFoundException e) {
            log.error("Provided push notification provider implementation '" + className + "' cannot be found", e);
        } catch (InstantiationException e) {
            log.error("Error occurred while instantiating push notification provider implementation '" +
                    className + "'", e);
        } catch (IllegalAccessException e) {
            log.error("Error occurred while adding push notification provider implementation '" + className + "'", e);
        }
    }

    public PushNotificationProvider getProvider(String type) {
        return providers.get(type);
    }

}
