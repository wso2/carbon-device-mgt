/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.api;

import io.swagger.annotations.Api;
import org.wso2.carbon.device.mgt.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.device.mgt.jaxrs.beans.EnrollmentCertificate;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * All the certificate related tasks such as saving certificates, can be done through this endpoint.
 */
@Api(value = "Certificate", description = "certificate related tasks such as saving certificates")
@SuppressWarnings("NonJaxWsWebServices")
@Produces({ "application/json", "application/xml" })
@Consumes({ "application/json", "application/xml" })
public interface Certificate {

    /**
     * Save a list of certificates and relevant information in the database.
     *
     * @param enrollmentCertificates List of all the certificates which includes the tenant id, certificate as
     *                               a pem and a serial number.
     * @return Status of the data persist operation.
     */
    @POST
    @Path("saveCertificate")
    Response saveCertificate(@HeaderParam("Accept") String acceptHeader,
                             EnrollmentCertificate[] enrollmentCertificates);

    /**
     * Get a certificate when the serial number is given.
     *
     * @param serialNumber serial of the certificate needed.
     * @return certificate response.
     */
    @GET
    @Path("{serialNumber}")
    Response getCertificate(@HeaderParam("Accept") String acceptHeader,
                            @PathParam("serialNumber") String serialNumber);

    /**
     * Get all certificates in a paginated manner.
     *
     * @param startIndex index of the first record to be fetched
     * @param length     number of records to be fetched starting from the start index.
     * @return paginated result of certificate.
     * @throws MDMAPIException
     */
    @GET
    @Path("paginate")
    Response getAllCertificates(@HeaderParam("Accept") String acceptHeader,
                                @QueryParam("start") int startIndex, @QueryParam("length") int length)
            throws MDMAPIException;

    @DELETE
    @Path("{serialNumber}")
    Response removeCertificate(@HeaderParam("Accept") String acceptHeader,
                               @PathParam("serialNumber") String serialNumber) throws MDMAPIException;

}
