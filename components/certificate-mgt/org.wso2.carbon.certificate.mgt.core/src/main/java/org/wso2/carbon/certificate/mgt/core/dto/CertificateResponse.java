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

package org.wso2.carbon.certificate.mgt.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigInteger;

@ApiModel(value = "CertificateResponse", description = "This class carries all information related to certificates")
public class CertificateResponse {

    @ApiModelProperty(name = "certificate", value = "The certificate in bytes", required = true)
    byte[] certificate;

    @ApiModelProperty(name = "serialNumber", value = "It is the unique ID that is used to identify a certificate", required = true)
    String serialNumber;

    @ApiModelProperty(name = "tenantId", value = "The ID of the tenant who adds the certificate", required = true)
    int tenantId;

    @ApiModelProperty(name = "commonName", value = "In mutual SSL the common name refers to the serial number of the Android device.", required = true)
    String commonName;

    @ApiModelProperty(name = "notAfter", value = "The expiration date of the certificate that is inherent to the certificate", required = true)
    long notAfter;

    @ApiModelProperty(name = "notBefore", value = "The date from when the certificate is valid", required = true)
    long notBefore;

    @ApiModelProperty(name = "certificateserial", value = "The serial number of the certificate", required = true)
    BigInteger certificateserial;

    @ApiModelProperty(name = "issuer", value = "The identity of the authority that signs the SSL certificate", required = true)
    String issuer;

    @ApiModelProperty(name = "subject", value = "The identity of the certificate", required = true)
    String subject;

    @ApiModelProperty(name = "certificateVersion", value = "The version of the certificate", required = true)
    int certificateVersion;

    @ApiModelProperty(name ="username", value="username of the logged user", required = true)
    String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(long notAfter) {
        this.notAfter = notAfter;
    }

    public long getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(long notBefore) {
        this.notBefore = notBefore;
    }

    public BigInteger getCertificateserial() {
        return certificateserial;
    }

    public void setCertificateserial(BigInteger certificateserial) {
        this.certificateserial = certificateserial;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getCertificateVersion() {
        return certificateVersion;
    }

    public void setCertificateVersion(int certificateVersion) {
        this.certificateVersion = certificateVersion;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
