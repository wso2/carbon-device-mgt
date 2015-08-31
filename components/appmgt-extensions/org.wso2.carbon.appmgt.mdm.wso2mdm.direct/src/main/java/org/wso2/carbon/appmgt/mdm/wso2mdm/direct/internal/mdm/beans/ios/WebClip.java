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

package org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm.beans.ios;

import com.google.gson.Gson;
import org.wso2.carbon.appmgt.api.AppManagementException;

/**
 * Copied from MDM. Need to refactor the MDM code base and remove this
 */
public class WebClip {

    private String URL;
    private String label;
    private String icon;
    private String isRemovable;
    private String UUID;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIsRemovable() {
        return isRemovable;
    }

    public void setIsRemovable(String isRemovable) {
        this.isRemovable = isRemovable;
    }

    public String toJSON() throws AppManagementException {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
