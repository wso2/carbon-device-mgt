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

package org.wso2.carbon.device.mgt.core.config.email;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "NotificationMessage")
public class NotificationMessages {

    private String header;
    private String body;
    private String footerLine1;
    private String footerLine2;
    private String footerLine3;
    private String subject;
    private String url;

    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    @XmlElement(name = "Header", required = true)
    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @XmlElement(name = "Body", required = true)
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @XmlElement(name = "Subject", required = true)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @XmlElement(name = "Url", required = true)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement(name = "Footer1")
    public String getFooterLine1() {
        return footerLine1;
    }

    public void setFooterLine1(String footerLine1) {
        this.footerLine1 = footerLine1;
    }

    @XmlElement(name = "Footer2")
    public String getFooterLine2() {
        return footerLine2;
    }

    public void setFooterLine2(String footerLine2) {
        this.footerLine2 = footerLine2;
    }

    @XmlElement(name = "Footer3")
    public String getFooterLine3() {
        return footerLine3;
    }

    public void setFooterLine3(String footerLine3) {
        this.footerLine3 = footerLine3;
    }

}
