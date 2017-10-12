package org.wso2.carbon.webapp.authenticator.framework.util;

import org.apache.catalina.connector.InputBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * This is a dummy implementation of {@link InputBuffer} for the test cases.
 */
public class TestInputBuffer implements org.apache.coyote.InputBuffer {
    private Log log = LogFactory.getLog(TestInputBuffer.class);

    @Override
    public int doRead(ByteChunk byteChunk, Request request) throws IOException {
        String string = request.getHeader("custom");
        MimeHeaders mimeHeaders = new MimeHeaders();
        Field byteC = null;
        try {
            byteC = MessageBytes.class.getDeclaredField("byteC");
            byteC.setAccessible(true);
        } catch (NoSuchFieldException e) {
            log.error("Cannot get the byteC field", e);
        }
        MessageBytes bytes = mimeHeaders.addValue("content-type");
        try {
            if (byteC != null) {
                byteC.set(bytes, byteChunk);
            }
        } catch (IllegalAccessException e) {
            log.error("Cannot set byteC field", e);
        }
        bytes.setString(string);
        bytes.toBytes();
        return byteChunk.getLength();
    }
}
