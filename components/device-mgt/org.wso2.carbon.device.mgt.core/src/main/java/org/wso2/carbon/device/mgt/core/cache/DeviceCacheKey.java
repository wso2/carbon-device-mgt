/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.cache;

import java.util.Objects;

/**
 * This represents a Key object used in DeviceCache.
 */
public class DeviceCacheKey {

    private String deviceId;
    private String deviceType;
    private int tenantId;
    private volatile int hashCode;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!DeviceCacheKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final DeviceCacheKey other = (DeviceCacheKey) obj;
        String thisId = this.deviceId + "-" + this.deviceType + "_" + this.tenantId;
        String otherId = other.deviceId + "-" + other.deviceType + "_" + other.tenantId;
        if (!thisId.equals(otherId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(deviceId, deviceType, tenantId);
        }
        return hashCode;
    }
}
