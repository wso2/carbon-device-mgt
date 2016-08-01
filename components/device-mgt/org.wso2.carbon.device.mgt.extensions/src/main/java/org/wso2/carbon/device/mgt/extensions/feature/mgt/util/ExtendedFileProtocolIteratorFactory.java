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
package org.wso2.carbon.device.mgt.extensions.feature.mgt.util;

import org.scannotation.archiveiterator.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ExtendedFileProtocolIteratorFactory implements DirectoryIteratorFactory {

    private static final String ENCODING_SCHEME = "UTF-8";

    @Override
    public StreamIterator create(URL url, Filter filter) throws IOException {
        File f = new File(java.net.URLDecoder.decode(url.getPath(), ENCODING_SCHEME));
        return f.isDirectory()?new FileIterator(f, filter):new JarIterator(url.openStream(), filter);
    }

}
