/*
*  Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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
*/

package org.wso2.carbon.policy.mgt.common;

public class PolicyEvaluationException extends Exception {

    private String policyEvaluationErrorMessage;

    public String getPolicyEvaluationErrorMessage() {
        return policyEvaluationErrorMessage;
    }

    public void setPolicyEvaluationErrorMessage(String policyEvaluationErrorMessage) {
        this.policyEvaluationErrorMessage = policyEvaluationErrorMessage;
    }

    public PolicyEvaluationException(String message) {
        super(message);
        setPolicyEvaluationErrorMessage(message);
    }

    public PolicyEvaluationException(String message, Exception ex) {
        super(message, ex);
        setPolicyEvaluationErrorMessage(message);
    }

    public PolicyEvaluationException(String message, Throwable cause) {
        super(message, cause);
        setPolicyEvaluationErrorMessage(message);
    }

    public PolicyEvaluationException() {
        super();
    }

    public PolicyEvaluationException(Throwable cause) {
        super(cause);
    }
}
