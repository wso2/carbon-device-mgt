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

package org.wso2.carbon.policy.mgt.core.dao;

public class ProfileManagerDAOException extends Exception{

    private String profileDAOErrorMessage;

    public String getProfileDAOErrorMessage() {
        return profileDAOErrorMessage;
    }

    public void setProfileDAOErrorMessage(String profileDAOErrorMessage) {
        this.profileDAOErrorMessage = profileDAOErrorMessage;
    }

    public ProfileManagerDAOException(String message) {
        super(message);
        setProfileDAOErrorMessage(message);
    }

    public ProfileManagerDAOException(String message, Exception ex) {
        super(message, ex);
        setProfileDAOErrorMessage(message);
    }

    public ProfileManagerDAOException(String message, Throwable cause) {
        super(message, cause);
        setProfileDAOErrorMessage(message);
    }

    public ProfileManagerDAOException() {
        super();
    }

    public ProfileManagerDAOException(Throwable cause) {
        super(cause);
    }
}
