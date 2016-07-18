/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util;

import org.scannotation.archiveiterator.*;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendedIteratorFactory {

    private static final ConcurrentHashMap<String, DirectoryIteratorFactory> registry = new ConcurrentHashMap();

    public static StreamIterator create(URL url, Filter filter) throws IOException {
        String urlString = url.toString();
        if(urlString.endsWith("!/")) {
            urlString = urlString.substring(4);
            urlString = urlString.substring(0, urlString.length() - 2);
            url = new URL(urlString);
        }

        if(!urlString.endsWith("/")) {
            return new JarIterator(url.openStream(), filter);
        } else {
            DirectoryIteratorFactory factory = registry.get(url.getProtocol());
            if(factory == null) {
                throw new IOException("Unable to scan directory of protocol: " + url.getProtocol());
            } else {
                return factory.create(url, filter);
            }
        }
    }

    static {
        registry.put("file", new ExtendedFileProtocolIteratorFactory());
    }
}
