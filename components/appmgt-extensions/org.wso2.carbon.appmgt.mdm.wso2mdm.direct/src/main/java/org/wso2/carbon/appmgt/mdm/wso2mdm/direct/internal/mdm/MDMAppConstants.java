/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.appmgt.mdm.wso2mdm.direct.internal.mdm;

/**
 * Forked from product-mdm
 */
public class MDMAppConstants {

    public class AppConstants {

        public static final String LABEL = "label";
    }

    public class IOSConstants extends WebappConstants {

        private IOSConstants() {
            throw new AssertionError();
        }

        public static final String IS_PREVENT_BACKUP = "isPreventBackup";
        public static final String I_TUNES_ID = "iTunesId";

        public static final String OPCODE_INSTALL_ENTERPRISE_APPLICATION = "INSTALL_ENTERPRISE_APPLICATION";
        public static final String OPCODE_INSTALL_STORE_APPLICATION = "INSTALL_STORE_APPLICATION";
        public static final String OPCODE_INSTALL_WEB_APPLICATION = "WEB_CLIP";
        public static final String OPCODE_REMOVE_APPLICATION = "REMOVE_APPLICATION";
    }

    public class AndroidConstants extends AppConstants {

        private AndroidConstants() {
            throw new AssertionError();
        }

        public static final String OPCODE_INSTALL_APPLICATION = "INSTALL_APPLICATION";
        public static final String OPCODE_UNINSTALL_APPLICATION = "UNINSTALL_APPLICATION";
    }

    public class WebappConstants extends AppConstants {

        private WebappConstants() {
            throw new AssertionError();
        }

        public static final String IS_REMOVE_APP = "isRemoveApp";
    }

    public class MDMAppPropertyKeys {
        public static final String DEVICE_NAME = "DEVICE_NAME";
        public static final String DEVICE_MODEL = "DEVICE_MODEL";
        public static final String OS_VERSION = "OS_VERSION";
    }
}
