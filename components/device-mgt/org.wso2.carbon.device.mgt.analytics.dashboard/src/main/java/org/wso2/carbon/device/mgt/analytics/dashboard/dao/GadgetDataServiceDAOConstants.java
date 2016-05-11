/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

public final class GadgetDataServiceDAOConstants {

    public static class PotentialVulnerability {

        // These constants do not hold actual database values
        // These are just abstract values defined and used @ Gadget Data Service DAO Implementation layer
        public static final String NON_COMPLIANT = "NON_COMPLIANT";
        public static final String UNMONITORED = "UNMONITORED";

        private PotentialVulnerability() {
            throw new AssertionError();
        }

    }

    private GadgetDataServiceDAOConstants() { throw new AssertionError(); }

}
