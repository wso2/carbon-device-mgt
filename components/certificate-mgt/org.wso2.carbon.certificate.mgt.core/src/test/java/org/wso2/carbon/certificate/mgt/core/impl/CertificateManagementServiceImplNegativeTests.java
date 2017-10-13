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

import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.exception.TransactionManagementException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.sql.DataSource;
import java.io.File;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;

/**
 * This class covers the negative tests for CertificateManagementServiceImpl class
 */
@PowerMockIgnore({"java.net.ssl", "javax.security.auth.x500.X500Principal"})
@PrepareForTest({CertificateManagementServiceImpl.class, JcaX509CertificateConverter.class, CertificateGenerator.class,
        CertificateManagementDAOFactory.class})
public class CertificateManagementServiceImplNegativeTests extends PowerMockTestCase{

    private CertificateManagementServiceImpl instance;
    private DataSource daoExceptionDatasource;
    private static final String MOCK_SERIAL="1234";
    private static final String MOCK_DATASOURCE="H2";

    @BeforeClass
    public void init() throws SQLException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
            file = new File("../../../resources/carbon-home");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants
                .SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        DataSource normalDatasource = Mockito.mock(DataSource.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(normalDatasource.getConnection().getMetaData().getDatabaseProductName()).thenReturn(MOCK_DATASOURCE);
        CertificateManagementDAOFactory.init(normalDatasource);

        //configure datasource to throw dao exception
        daoExceptionDatasource = Mockito.mock(DataSource.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(daoExceptionDatasource.getConnection().getMetaData().getDatabaseProductName()).thenReturn(MOCK_DATASOURCE);
        Mockito.when(daoExceptionDatasource.getConnection().prepareStatement(Mockito.anyString())).thenThrow(new SQLException());

        //save as class variable
        instance = CertificateManagementServiceImpl.getInstance();
    }


    @Test(description = "This test case tests behaviour when an error occurs when opening the data source"
            ,expectedExceptions = CertificateManagementException.class)
    public void negativeTestretrieveCertificate2() throws Exception {
        PowerMockito.mockStatic(CertificateManagementDAOFactory.class);
        PowerMockito.doThrow(new SQLException()).when(CertificateManagementDAOFactory.class,"openConnection");
        instance.retrieveCertificate(MOCK_SERIAL);
    }

    @Test(description = "This test case tests behaviour when an error occurs when looking for a certificate with " +
            "a serial number",expectedExceptions = CertificateManagementException.class)
    public void negativeTestretrieveCertificate() throws Exception {
        CertificateManagementDAOFactory.init(daoExceptionDatasource);
        CertificateManagementServiceImpl instance1 = CertificateManagementServiceImpl.getInstance();
        instance1.retrieveCertificate(MOCK_SERIAL);
    }

    @Test(description = "This test case tests behaviour when an error occurs when opening the data source",
            expectedExceptions = CertificateManagementException.class)
    public void negativeTestgetAllCertificates() throws Exception {
        PowerMockito.mockStatic(CertificateManagementDAOFactory.class);
        PowerMockito.doThrow(new SQLException()).when(CertificateManagementDAOFactory.class,"openConnection");
        instance.getAllCertificates(1,2);
    }

    @Test(description = "This test case tests behaviour when an error occurs getting the list of certificates from repository"
            ,expectedExceptions = CertificateManagementException.class)
    public void negativeTestgetAllCertificates2() throws Exception {
        CertificateManagementDAOFactory.init(daoExceptionDatasource);
        CertificateManagementServiceImpl instance1 = CertificateManagementServiceImpl.getInstance();
        instance1.getAllCertificates(1,2);
    }

    @Test(description = "This test case tests behaviour when data source transaction error occurs when removing the certificate"
            ,expectedExceptions = CertificateManagementException.class)
    public void negativeTestRemoveCertificate() throws Exception {
        PowerMockito.mockStatic(CertificateManagementDAOFactory.class);
        PowerMockito.doThrow(new TransactionManagementException()).when(CertificateManagementDAOFactory.class,"beginTransaction");
        instance.removeCertificate(MOCK_SERIAL);
    }

    @Test(description = "This test case tests behaviour when an error occurs while removing the certificate from the certificate " +
            "repository",expectedExceptions = CertificateManagementException.class)
    public void negativeTestRemoveCertificate2() throws Exception {
        CertificateManagementDAOFactory.init(daoExceptionDatasource);
        CertificateManagementServiceImpl instance1 = CertificateManagementServiceImpl.getInstance();
        instance1.removeCertificate(MOCK_SERIAL);
    }

    @Test(description = "This test case tests behaviour when an error occurs when opening the data source",
            expectedExceptions = CertificateManagementException.class)
    public void negativeTestGetCertificates() throws Exception {
        PowerMockito.mockStatic(CertificateManagementDAOFactory.class);
        PowerMockito.doThrow(new SQLException()).when(CertificateManagementDAOFactory.class,"openConnection");
        instance.getCertificates();
    }

    @Test(description = "This test case tests behaviour when an error occurs while looking up for the list of certificates"
            ,expectedExceptions = CertificateManagementException.class)
    public void negativeTestGetCertificates2() throws CertificateManagementException {
        CertificateManagementDAOFactory.init(daoExceptionDatasource);
        CertificateManagementServiceImpl instance1 = CertificateManagementServiceImpl.getInstance();
        instance1.getCertificates();
    }

    @Test(description = "This test case tests behaviour when an error occurs when opening the data source",
            expectedExceptions = CertificateManagementException.class)
    public void negativeTestSearchCertificates() throws Exception {
        PowerMockito.mockStatic(CertificateManagementDAOFactory.class);
        PowerMockito.doThrow(new SQLException()).when(CertificateManagementDAOFactory.class,"openConnection");
        instance.searchCertificates(MOCK_SERIAL);
    }

    @Test(description = "This test case tests behaviour when an error occurs while searching for the certificate by the serial",expectedExceptions = CertificateManagementException.class)
    public void negativeTestSearchCertificates2() throws CertificateManagementException {
        CertificateManagementDAOFactory.init(daoExceptionDatasource);
        CertificateManagementServiceImpl instance1 = CertificateManagementServiceImpl.getInstance();
        instance1.searchCertificates(MOCK_SERIAL);
    }

    //Powermockito requirement
    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}
