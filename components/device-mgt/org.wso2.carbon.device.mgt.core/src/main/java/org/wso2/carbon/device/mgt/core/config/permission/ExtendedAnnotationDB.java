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
package org.wso2.carbon.device.mgt.core.config.permission;

import org.scannotation.AnnotationDB;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.StreamIterator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ExtendedAnnotationDB extends AnnotationDB {

    public ExtendedAnnotationDB() {
        super();
    }

    public void scanArchives(URL... urls) throws IOException {
        URL[] arr = urls;
        int len = urls.length;

        for(int i = 0; i < len; ++i) {
            URL url = arr[i];
            Filter filter = new Filter() {
                public boolean accepts(String filename) {
                    if(filename.endsWith(".class")) {
                        if(filename.startsWith("/") || filename.startsWith("\\")) {
                            filename = filename.substring(1);
                        }

                        if(!ExtendedAnnotationDB.this.ignoreScan(filename.replace('/', '.'))) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            StreamIterator it = ExtendedIteratorFactory.create(url, filter);

            InputStream stream;
            while((stream = it.next()) != null) {
                this.scanClass(stream);
            }
        }

    }

    private boolean ignoreScan(String intf) {
        String[] arr;
        int len;
        int i;
        String ignored;
        if(this.scanPackages != null) {
            arr = this.scanPackages;
            len = arr.length;

            for(i = 0; i < len; ++i) {
                ignored = arr[i];
                if(intf.startsWith(ignored + ".")) {
                    return false;
                }
            }

            return true;
        } else {
            arr = this.ignoredPackages;
            len = arr.length;

            for(i = 0; i < len; ++i) {
                ignored = arr[i];
                if(intf.startsWith(ignored + ".")) {
                    return true;
                }
            }
            return false;
        }
    }
}
