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

package org.wso2.carbon.certificate.mgt.core.config;

import org.wso2.carbon.certificate.mgt.core.util.CertificateManagementConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for holding CertificateKeystore data.
 */
@XmlRootElement(name = "CertificateKeystore")
public class CertificateKeystoreConfig {

    private String certificateKeystoreLocation;
    private String certificateKeystoreType;
    private String certificateKeystorePassword;
    private String caCertAlias;
    private String caPrivateKeyPassword;
    private String raCertAlias;
    private String raPrivateKeyPassword;

    @XmlElement(name = "CertificateKeystoreLocation", required = true)
    public String getCertificateKeystoreLocation() {
        return certificateKeystoreLocation;
    }

    public void setCertificateKeystoreLocation(String certificateKeystoreLocation) {
        if (certificateKeystoreLocation != null && certificateKeystoreLocation.toLowerCase().
                contains(CertificateManagementConstants.CARBON_HOME_ENTRY)) {
            certificateKeystoreLocation = certificateKeystoreLocation.replace(CertificateManagementConstants.CARBON_HOME_ENTRY,
                                                                                   System.getProperty(CertificateManagementConstants.CARBON_HOME));
        }
        this.certificateKeystoreLocation = certificateKeystoreLocation;
    }

    @XmlElement(name = "CertificateKeystoreType", required = true)
    public String getCertificateKeystoreType() {
        return certificateKeystoreType;
    }

    public void setCertificateKeystoreType(String certificateKeystoreType) {
        this.certificateKeystoreType = certificateKeystoreType;
    }

    @XmlElement(name = "CertificateKeystorePassword", required = true)
    public String getCertificateKeystorePassword() {
        return certificateKeystorePassword;
    }

    public void setCertificateKeystorePassword(String certificateKeystorePassword) {
        this.certificateKeystorePassword = certificateKeystorePassword;
    }

    @XmlElement(name = "CACertAlias", required = true)
    public String getCACertAlias() {
        return caCertAlias;
    }

    public void setCACertAlias(String caCertAlias) {
        this.caCertAlias = caCertAlias;
    }

    @XmlElement(name = "CAPrivateKeyPassword", required = true)
    public String getCAPrivateKeyPassword() {
        return caPrivateKeyPassword;
    }

    public void setCAPrivateKeyPassword(String caPrivateKeyPassword) {
        this.caPrivateKeyPassword = caPrivateKeyPassword;
    }

    @XmlElement(name = "RACertAlias", required = true)
    public String getRACertAlias() {
        return raCertAlias;
    }

    public void setRACertAlias(String raCertAlias) {
        this.raCertAlias = raCertAlias;
    }

    @XmlElement(name = "RAPrivateKeyPassword", required = true)
    public String getRAPrivateKeyPassword() {
        return raPrivateKeyPassword;
    }

    public void setRAPrivateKeyPassword(String raPrivateKeyPassword) {
        this.raPrivateKeyPassword = raPrivateKeyPassword;
    }
}
