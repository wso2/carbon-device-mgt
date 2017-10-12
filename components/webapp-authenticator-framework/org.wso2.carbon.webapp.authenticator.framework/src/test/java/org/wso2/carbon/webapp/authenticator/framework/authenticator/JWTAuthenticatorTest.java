package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.jwt.client.extension.dto.JWTConfig;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.util.JWTClientUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.util.TestTenantIndexingLoader;
import org.wso2.carbon.webapp.authenticator.framework.util.TestTenantRegistryLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JWTAuthenticatorTest {
    private JWTAuthenticator jwtAuthenticator;
    private Field headersField;
    private final String JWT_HEADER = "X-JWT-Assertion";
    private String jwtToken;
    private static final String SIGNED_JWT_AUTH_USERNAME = "http://wso2.org/claims/enduser";
    private static final String SIGNED_JWT_AUTH_TENANT_ID = "http://wso2.org/claims/enduserTenantId";
    private Properties properties;
    private final String ISSUER = "wso2.org/products/iot";
    private final String ALIAS = "wso2carbon";

    @BeforeClass
    public void setup() throws NoSuchFieldException, IOException, JWTClientException {
        jwtAuthenticator = new JWTAuthenticator();
        properties = new Properties();
        properties.setProperty(ISSUER, ALIAS);
        jwtAuthenticator.setProperties(properties);
        headersField = org.apache.coyote.Request.class.getDeclaredField("headers");
        headersField.setAccessible(true);
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource("jwt.properties");
        File jwtPropertyFile;
        JWTConfig jwtConfig = null;

        if (resourceUrl != null) {
            jwtPropertyFile = new File(resourceUrl.getFile());
            Properties jwtConfigProperties = new Properties();
            jwtConfigProperties.load(new FileInputStream(jwtPropertyFile));
            jwtConfig = new JWTConfig(jwtConfigProperties);
        }

        Map<String, String> customClaims = new HashMap<>();
        customClaims.put(SIGNED_JWT_AUTH_USERNAME, "admin");
        customClaims.put(SIGNED_JWT_AUTH_TENANT_ID, String.valueOf(MultitenantConstants.SUPER_TENANT_ID));
        jwtToken = JWTClientUtil.generateSignedJWTAssertion("admin", jwtConfig, false, customClaims);
    }

    @Test(description = "This method tests the get methods in the JWTAuthenticator")
    public void testGetMethods() {
        Assert.assertEquals(jwtAuthenticator.getName(), "JWT", "GetName method returns wrong value");
        Assert.assertNotNull(jwtAuthenticator.getProperties(), "Properties are not properly added to JWT "
                + "Authenticator");
        Assert.assertEquals(jwtAuthenticator.getProperties().size(), properties.size(),
                "Added properties do not match with retrieved properties");
        Assert.assertNull(jwtAuthenticator.getProperty("test"), "Retrieved a propety that was never added");
        Assert.assertNotNull(jwtAuthenticator.getProperty(ISSUER), ALIAS);
    }

    @Test(description = "This method tests the canHandle method under different conditions of request")
    public void testHandle() throws IllegalAccessException, NoSuchFieldException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertFalse(jwtAuthenticator.canHandle(request));
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(JWT_HEADER);
        bytes.setString("test");
        headersField.set(coyoteRequest, mimeHeaders);
        request.setCoyoteRequest(coyoteRequest);
        Assert.assertTrue(jwtAuthenticator.canHandle(request));
    }

    @Test(description = "This method tests authenticate method under the successful condition")
    public void testAuthenticate() throws IllegalAccessException, NoSuchFieldException {
        Request request = new Request();
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue(JWT_HEADER);
        bytes.setString(jwtToken);
        headersField.set(coyoteRequest, mimeHeaders);
        Field uriMB = org.apache.coyote.Request.class.getDeclaredField("uriMB");
        uriMB.setAccessible(true);
        bytes = MessageBytes.newInstance();
        bytes.setString("test");
        uriMB.set(coyoteRequest, bytes);
        request.setCoyoteRequest(coyoteRequest);

        AuthenticationInfo authenticationInfo = jwtAuthenticator.authenticate(request, null);
        Assert.assertNotNull(authenticationInfo.getUsername(), "Proper authentication request is not properly "
                + "authenticated by the JWTAuthenticator");
    }
}
