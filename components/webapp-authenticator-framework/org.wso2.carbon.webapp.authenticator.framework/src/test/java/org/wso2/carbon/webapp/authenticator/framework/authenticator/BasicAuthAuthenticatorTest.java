package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class BasicAuthAuthenticatorTest {
    @Test
    public void testCanHandle()
            throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException {
        Request request = new Request();
        Context context = new StandardContext();
        context.addParameter("basicAuth", "true");
        request.setContext(context);
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        Field headers = org.apache.coyote.Request.class.getDeclaredField("headers");
        headers.setAccessible(true);

        Field mimeHeaderField = MimeHeaders.class.getDeclaredField("headers");
        mimeHeaderField.setAccessible(true);


        MimeHeaders mimeHeaders = new MimeHeaders();
        MessageBytes bytes = mimeHeaders.addValue("Authorization");
        bytes.setString("test");
//        mimeHeaders.setValue()
        headers.set(coyoteRequest, mimeHeaders);

        request.setCoyoteRequest(coyoteRequest);
        BasicAuthAuthenticator basicAuthAuthenticator = new BasicAuthAuthenticator();
        basicAuthAuthenticator.canHandle(request);
    }
}
