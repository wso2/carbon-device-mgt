/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.analytics.dashboard;

import java.util.Map;

/**
 * To be updated...
 */
public interface GadgetDataService {

    @SuppressWarnings("unused")
    int getTotalDeviceCount(Map<String, Object> filters);

    @SuppressWarnings("unused")
    int getActiveDeviceCount();

    @SuppressWarnings("unused")
    int getInactiveDeviceCount();

    @SuppressWarnings("unused")
    int getRemovedDeviceCount();

    @SuppressWarnings("unused")
    int getNonCompliantDeviceCount();

    @SuppressWarnings("unused")
    int getUnmonitoredDeviceCount();
}
