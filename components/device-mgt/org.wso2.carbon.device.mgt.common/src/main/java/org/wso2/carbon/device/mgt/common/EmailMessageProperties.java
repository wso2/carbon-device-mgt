/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.common;

import java.util.Arrays;

public class EmailMessageProperties {

    private String messageBody;
    private String[] mailTo;
    private String[] ccList;
    private String[] bccList;
    private String subject;
    private String firstName;
    private String enrolmentUrl;
    private String title;
    private String password;
    private String userName;
    private String domainName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String[] getMailTo() {
        return mailTo;
    }

    public void setMailTo(String[] mailTo) {
        this.mailTo = mailTo;
    }

    public String[] getCcList() {
        return ccList;
    }

    public void setCcList(String[] ccList) {
        this.ccList = ccList;
    }

    public String[] getBccList() {
        return bccList;
    }

    public void setBccList(String[] bccList) {
        this.bccList = bccList;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEnrolmentUrl() {
        return enrolmentUrl;
    }

    public void setEnrolmentUrl(String enrolmentUrl) {
        this.enrolmentUrl = enrolmentUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "EmailMessageProperties{" +
                "messageBody='" + messageBody + '\'' +
                ", mailTo=" + Arrays.toString(mailTo) +
                ", ccList=" + Arrays.toString(ccList) +
                ", bccList=" + Arrays.toString(bccList) +
                ", subject='" + subject + '\'' +
                ", firstName='" + firstName + '\'' +
                ", enrolmentUrl='" + enrolmentUrl + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
