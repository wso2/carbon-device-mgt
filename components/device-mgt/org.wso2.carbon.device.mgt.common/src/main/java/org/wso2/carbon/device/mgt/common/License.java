/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean class for License
 */
@XmlRootElement(name = "License")
public class License implements Serializable {

    private static final long serialVersionUID = -7699923479237L;

    private String provider;
    private String name;
    private String version;
    private String language;
    private Date validFrom;
    private Date validTo;
    private String text;

    public License(String provider, String name, String version, String language, Date validFrom, Date validTo,
                   String text) {
        this.provider = provider;
        this.name = name;
        this.version = version;
        this.language = language;
        this.validFrom = new Date(validFrom.getTime());
        this.validTo = new Date(validTo.getTime());
        this.text = text;
    }

    @XmlElement(name = "Provider", required = true)
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @XmlElement(name = "Name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Version", required = true)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "Language", required = true)
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @XmlElement(name = "Text", required = true)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlElement(name = "ValidFrom")
    public Date getValidFrom() {
        return (Date) validFrom.clone();
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = new Date(validFrom.getTime());
    }

    @XmlElement(name = "ValidTo")
    public Date getValidTo() {
        return (Date) validTo.clone();
    }

    public void setValidTo(Date validTo) {
        this.validTo = new Date(validTo.getTime());
    }

}
