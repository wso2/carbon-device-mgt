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
