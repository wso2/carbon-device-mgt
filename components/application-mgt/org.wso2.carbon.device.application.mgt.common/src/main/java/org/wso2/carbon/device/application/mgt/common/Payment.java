/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.common;

public class Payment {
    private boolean freeApp;

    private String paymentCurrency;

    private float paymentPrice;

    public boolean isFreeApp() {
        return freeApp;
    }

    public void setFreeApp(boolean freeApp) {
        this.freeApp = freeApp;
    }

    public String getPaymentCurrency() {
        return paymentCurrency;
    }

    public void setPaymentCurrency(String paymentCurrency) {
        this.paymentCurrency = paymentCurrency;
    }

    public float getPaymentPrice() {
        return paymentPrice;
    }

    public void setPaymentPrice(float paymentPrice) {
        this.paymentPrice = paymentPrice;
    }
}
