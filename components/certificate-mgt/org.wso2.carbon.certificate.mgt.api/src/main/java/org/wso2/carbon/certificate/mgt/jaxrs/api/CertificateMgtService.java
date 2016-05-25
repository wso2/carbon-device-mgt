package org.wso2.carbon.certificate.mgt.jaxrs.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface CertificateMgtService {

    /**
     * Sign the client's certificate signing request and save it in the database.
     *
     * @param binarySecurityToken Base64 encoded Certificate signing request.
     * @return X509Certificate type sign certificate.
     */
    @POST
    @Path("csr-sign")
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    @Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    Response getSignedCertFromCSR(String binarySecurityToken);
}
