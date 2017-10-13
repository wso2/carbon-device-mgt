/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.h2.jdbcx.JdbcDataSource;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.core.impl.KeyStoreReader;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPException;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManager;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManagerImpl;
import org.wso2.carbon.certificate.mgt.core.scep.TenantedDeviceWrapper;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.util.TestCertificateGenerator;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is a test case for {@link CertificateAuthenticator}.
 */
public class CertificateAuthenticatorTest {
    private CertificateAuthenticator certificateAuthenticator;
    private Request certificationVerificationRequest;
    private Request mutalAuthHeaderRequest;
    private Request proxyMutalAuthHeaderRequest;
    private Field headersField;
    private static final String MUTUAL_AUTH_HEADER = "mutual-auth-header";
    private static final String PROXY_MUTUAL_AUTH_HEADER = "proxy-mutual-auth-header";
    private static final String CERTIFICATE_VERIFICATION_HEADER = "Mdm-Signature";
    private static final String CLIENT_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";
    private X509Certificate X509certificate;

    @BeforeClass
    public void setup() throws KeystoreException, NoSuchFieldException, IllegalAccessException, SQLException,
            DeviceManagementException, CertificateEncodingException, CMSException, IOException, SCEPException {
        certificateAuthenticator = new CertificateAuthenticator();
        CertificateManagementService certificateManagementService = Mockito
                .mock(CertificateManagementServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);

        Field certificateManagementServiceImpl = CertificateManagementServiceImpl.class.getDeclaredField
                ("certificateManagementServiceImpl");
        certificateManagementServiceImpl.setAccessible(true);
        Field keyStoreReaderField = CertificateManagementServiceImpl.class.getDeclaredField("keyStoreReader");
        keyStoreReaderField.setAccessible(true);
        Field certificateGeneratorField = CertificateManagementServiceImpl.class.getDeclaredField
                ("certificateGenerator");
        certificateGeneratorField.setAccessible(true);
        certificateManagementServiceImpl.set(null, certificateManagementService);

        // Create KeyStore Reader
        Field dataSource = CertificateManagementDAOFactory.class.getDeclaredField("dataSource");
        dataSource.setAccessible(true);
        dataSource.set(null, createDatabase());
        Field databaseEngine = CertificateManagementDAOFactory.class.getDeclaredField("databaseEngine");
        databaseEngine.setAccessible(true);
        databaseEngine.set(null, "H2");
        KeyStoreReader keyStoreReader = new KeyStoreReader();
        keyStoreReaderField.set(null, keyStoreReader);

        CertificateGenerator certificateGenerator = new TestCertificateGenerator();
        certificateGeneratorField.set(null, certificateGenerator);

        AuthenticatorFrameworkDataHolder.getInstance().
                setCertificateManagementService(certificateManagementService);
        X509certificate = certificateManagementService.generateX509Certificate();

        proxyMutalAuthHeaderRequest = createRequest(PROXY_MUTUAL_AUTH_HEADER, String.valueOf(X509certificate));
        System.setProperty("carbon.config.dir.path",
                System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "conf");
        DeviceConfigurationManager.getInstance().initConfig();
        certificationVerificationRequest = createRequest(CERTIFICATE_VERIFICATION_HEADER,
                createEncodedSignature(X509certificate));

        mutalAuthHeaderRequest = createRequest(MUTUAL_AUTH_HEADER, "test");

        SCEPManager scepManager = Mockito.mock(SCEPManagerImpl.class, Mockito.CALLS_REAL_METHODS);
        TenantedDeviceWrapper tenantedDeviceWrapper = new TenantedDeviceWrapper();
        tenantedDeviceWrapper.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        tenantedDeviceWrapper.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        Device device = new Device();
        device.setEnrolmentInfo(new EnrolmentInfo("admin", null, null));
        tenantedDeviceWrapper.setDevice(device);
        Mockito.doReturn(tenantedDeviceWrapper).when(scepManager).getValidatedDevice(Mockito.any());
        AuthenticatorFrameworkDataHolder.getInstance().setScepManager(scepManager);
    }

    @Test(description = "This test case tests the behaviour of the CertificateAuthenticator for Proxy mutal Auth "
            + "Header requests")
    public void testRequestsWithProxyMutalAuthHeader()
            throws KeystoreException, NoSuchFieldException, IllegalAccessException {
        Assert.assertTrue(certificateAuthenticator.canHandle(proxyMutalAuthHeaderRequest), "canHandle method "
                + "returned false for a request with all the required header");
        AuthenticationInfo authenticationInfo = certificateAuthenticator
                .authenticate(proxyMutalAuthHeaderRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertNull(authenticationInfo.getTenantDomain(),
                "Authentication got succeeded without proper certificate");

        proxyMutalAuthHeaderRequest = createRequest(PROXY_MUTUAL_AUTH_HEADER,
                String.valueOf(X509certificate.getIssuerDN()));
        authenticationInfo = certificateAuthenticator.authenticate(proxyMutalAuthHeaderRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertNotNull(authenticationInfo.getTenantDomain(),
                "Authentication got failed for a proper certificate");

        CertificateGenerator tempCertificateGenerator = new CertificateGenerator();
        X509Certificate certificateWithOutCN = tempCertificateGenerator.generateX509Certificate();
        proxyMutalAuthHeaderRequest = createRequest(PROXY_MUTUAL_AUTH_HEADER,
                String.valueOf(certificateWithOutCN.getIssuerDN()));
        authenticationInfo = certificateAuthenticator.authenticate(proxyMutalAuthHeaderRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertEquals(authenticationInfo.getStatus(), WebappAuthenticator.Status.FAILURE,
                "Authentication got passed with a certificate without CN");


    }

    @Test(description = "This test case tests the behaviour of the CertificateAuthenticator for Certification "
            + "Verification Header requests")
    public void testRequestCertificateVerificationHeader()
            throws CertificateEncodingException, IOException, CMSException, NoSuchFieldException,
            IllegalAccessException {
        Assert.assertTrue(certificateAuthenticator.canHandle(certificationVerificationRequest),
                "canHandle method returned false for a request with all the required header");
        AuthenticationInfo authenticationInfo = certificateAuthenticator
                .authenticate(certificationVerificationRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertNull(authenticationInfo.getTenantDomain(), "Authentication got passed without proper certificate");
        authenticationInfo = certificateAuthenticator.authenticate(certificationVerificationRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertEquals(authenticationInfo.getTenantDomain(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                "Authentication failed for a valid request with " + CERTIFICATE_VERIFICATION_HEADER + " header");
    }

    @Test(description = "This test case tests the behaviour of the Certificate Authenticator for the requests with "
            + "Mutal Auth Header")
    public void testMutalAuthHeaderRequest() {
        Assert.assertTrue(certificateAuthenticator.canHandle(mutalAuthHeaderRequest),
                "canHandle method returned false for a request with all the required header");

        AuthenticationInfo authenticationInfo = certificateAuthenticator.authenticate(mutalAuthHeaderRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertEquals(authenticationInfo.getMessage(), "No client certificate is present",
                "Authentication got passed without proper certificate");

        X509Certificate[] x509Certificates = new X509Certificate[1];
        x509Certificates[0] = X509certificate;
        mutalAuthHeaderRequest.setAttribute(CLIENT_CERTIFICATE_ATTRIBUTE, x509Certificates);
        authenticationInfo = certificateAuthenticator.authenticate(mutalAuthHeaderRequest, null);
        Assert.assertNotNull(authenticationInfo, "Authentication Info from Certificate Authenticator is null");
        Assert.assertEquals(authenticationInfo.getTenantDomain(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                "Authentication failed even with proper certificate");
    }
    /**
     * To create a request that can be understandable by Certificate Authenticator.
     *
     * @param headerName Name of the header
     * @param value      Value for the header
     * @return Request that is created.
     * @throws IllegalAccessException Illegal Access Exception.
     * @throws NoSuchFieldException   No Such Field Exception.
     */
    private Request createRequest(String headerName, String value) throws IllegalAccessException, NoSuchFieldException {
        Request request = new Request();
        Context context = new StandardContext();
        request.setContext(context);
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(headerName);
        bytes.setString(value);
        headersField.set(coyoteRequest, mimeHeaders);

        request.setCoyoteRequest(coyoteRequest);
        return request;
    }

    private DataSource createDatabase() throws SQLException {
        URL resourceURL = ClassLoader.getSystemResource("sql-scripts" + File.separator + "h2.sql");
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:cert;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        final String LOAD_DATA_QUERY = "RUNSCRIPT FROM '" + resourceURL.getPath() + "'";
        Connection conn = null;
        Statement statement = null;
        try {
            conn = dataSource.getConnection();
            statement = conn.createStatement();
            statement.execute(LOAD_DATA_QUERY);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {

                }
            }
            if (statement != null) {
                statement.close();
            }
        }
        return dataSource;
    }

    private String createEncodedSignature(X509Certificate x509Certificate)
            throws CertificateEncodingException, CMSException, IOException {
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        List<X509Certificate> list = new ArrayList<>();
        list.add(x509Certificate);
        JcaCertStore store = new JcaCertStore(list);
        generator.addCertificates(store);
        AtomicReference<CMSSignedData> degenerateSd = new AtomicReference<>(generator.generate(new CMSAbsentContent()));
        byte[] signature = degenerateSd.get().getEncoded();
        return Base64.getEncoder().encodeToString(signature);
    }
}
