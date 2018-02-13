/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class EnterpriseInstallationDetails {

    /**
     * This enum represents the type of entities which an application can be installed on.
     *
     * e.g: An application can be installed on all the devices belong to a user or a specific device group.
     */
    @ApiModel
    public enum EnterpriseEntity {
        USER, ROLE, DEVICE_GROUP
    }

    @ApiModelProperty(
            name = "applicationUUID",
            value = "Application ID",
            required = true,
            example = "4354c752-109f-11e8-b642-0ed5f89f718b"
    )
    private String applicationUUID;

    @ApiModelProperty(
            name = "entityType",
            value = "Enterprise entity type",
            required = true,
            example = "USER"
    )
    private EnterpriseEntity entityType;

    @ApiModelProperty(
            name = "entityValueList",
            value = "List of users/roles or device groups.",
            required = true,
            example = "user1,user2, user3"
    )
    private List<String> entityValueList;

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public EnterpriseEntity getEntityType() {
        return entityType;
    }

    public void setEntityType(EnterpriseEntity entityType) {
        this.entityType = entityType;
    }

    public List<String> getEntityValueList() {
        return entityValueList;
    }

    public void setEntityValueList(List<String> entityValueList) {
        this.entityValueList = entityValueList;
    }
}
