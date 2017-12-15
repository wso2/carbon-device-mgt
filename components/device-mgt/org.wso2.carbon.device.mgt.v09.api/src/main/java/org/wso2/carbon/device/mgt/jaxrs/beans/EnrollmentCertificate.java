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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "EnrollmentCertificate", description = "Details of certificates used in enrollment.")
public class EnrollmentCertificate {

    @ApiModelProperty(name = "serial", value = "The unique ID used to identify a certificate. This is the devices " +
                                               "serial number in case of mutual SSL is used for enrollment.",
                      required = true )
    String serial;
    @ApiModelProperty(name = "pem", value = "Case 64 encode .pem file content.", required = true )
    String pem;
    @ApiModelProperty(name = "tenantId", value = "The ID of the tenant who adds the certificate.", required = true )
    int tenantId;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getPem() {
        return pem;
    }

    public void setPem(String pem) {
        this.pem = pem;
    }

}
