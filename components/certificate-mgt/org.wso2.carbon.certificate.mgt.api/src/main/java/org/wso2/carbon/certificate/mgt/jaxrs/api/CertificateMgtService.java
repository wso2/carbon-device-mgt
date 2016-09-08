package org.wso2.carbon.certificate.mgt.jaxrs.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.certificate.mgt.jaxrs.beans.ErrorResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/scep")
public interface CertificateMgtService {

    /**
     * Sign the client's certificate signing request and save it in the database.
     *
     * @param binarySecurityToken Base64 encoded Certificate signing request.
     * @return X509Certificate type sign certificate.
     */
    @POST
    @Path("/sign-csr")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(
            consumes = MediaType.TEXT_PLAIN,
            produces = MediaType.TEXT_PLAIN,
            httpMethod = "POST",
            value = "Process a given CSR and return signed certificates.",
            notes = "This will return a signed certificate upon a given CSR.",
            tags = "Device Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the device location.",
                            response = String.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while retrieving signed certificate.",
                            response = ErrorResponse.class)
            })
    @Scope(key = "certificate:sign-csr", name = "Sign CSR", description = "")
    @Permission(name = "Sign CSR", permission = "/device-mgt/certificates/manage")
    Response getSignedCertFromCSR(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            String binarySecurityToken);
}
