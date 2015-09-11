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
package org.wso2.carbon.certificate.mgt.core.util;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {

    public Date getValidityStartDate() {
        Date targetDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.add(Calendar.DATE, -2);

        return calendar.getTime();
    }

    public Date getValidityEndDate() {
        Date targetDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(targetDate);
        calendar.add(Calendar.YEAR, 100);

        return calendar.getTime();
    }

    public static synchronized BigInteger generateSerialNumber() {
        return BigInteger.valueOf(System.currentTimeMillis());
    }

}
