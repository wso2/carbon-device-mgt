/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.certificate.mgt.jaxrs.exception;

import org.wso2.carbon.certificate.mgt.jaxrs.beans.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class UnexpectedServerErrorException extends WebApplicationException {

    private static final long serialVersionUID = 147943679458906890L;

    public UnexpectedServerErrorException(ErrorResponse error) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build());
    }

}
