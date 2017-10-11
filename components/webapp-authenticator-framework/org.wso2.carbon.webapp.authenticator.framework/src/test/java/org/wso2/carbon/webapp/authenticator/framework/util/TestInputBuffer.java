package org.wso2.carbon.webapp.authenticator.framework.util;

import org.apache.catalina.connector.InputBuffer;
import org.apache.coyote.Request;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;

import java.io.IOException;
import java.lang.reflect.Field;

public class TestInputBuffer implements org.apache.coyote.InputBuffer {
    @Override
    public int doRead(ByteChunk byteChunk, Request request) throws IOException {
        String string = request.getHeader("custom");
        MimeHeaders mimeHeaders = new MimeHeaders();
        Field byteC = null;
        try {
            byteC = MessageBytes.class.getDeclaredField("byteC");
            byteC.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        MessageBytes bytes = mimeHeaders.addValue("content-type");
        try {
            byteC.set(bytes, byteChunk);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        bytes.setString(string);
        bytes.toBytes();
        return byteChunk.getLength();
    }
}
