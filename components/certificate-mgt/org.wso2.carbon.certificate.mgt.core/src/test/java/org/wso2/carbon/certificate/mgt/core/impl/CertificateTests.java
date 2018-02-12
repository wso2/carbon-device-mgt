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

package org.wso2.carbon.certificate.mgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.certificate.mgt.core.bean.Certificate;
import org.wso2.carbon.certificate.mgt.core.util.DummyCertificate;

/**
 * This class tests the DTO for certificates
 */
public class CertificateTests {

    private static String SERIAL = "1234";
    private static String TENANT_DOMAIN = "tenant_domain";
    private static int TENANT_ID = 1234;

    @Test(description = "This test case tests the Certificate object getters and setters")
    public void certificateCreationTest() {

        Certificate certificate = new Certificate();
        certificate.setSerial(SERIAL);
        certificate.setCertificate(new DummyCertificate());
        certificate.setTenantDomain(TENANT_DOMAIN);
        certificate.setTenantId(TENANT_ID);

        Assert.assertEquals(certificate.getCertificate(), new DummyCertificate());
        Assert.assertEquals(certificate.getSerial(), SERIAL);
        Assert.assertEquals(certificate.getTenantDomain(), TENANT_DOMAIN);
        Assert.assertEquals(certificate.getTenantId(), TENANT_ID);
    }
}
