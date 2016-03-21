/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.device.mgt.core.task;

import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.HashMap;

public class Utils {


    public static HashMap<String, Long> getTenantedTaskOperationMap(HashMap<Integer, HashMap<String, Long>> map) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (map.containsKey(tenantId)) {
            return map.get(tenantId);
        } else {
            HashMap<String, Long> mp = new HashMap<>();
            map.put(tenantId, mp);
            return mp;
        }
    }

}

