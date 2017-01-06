package org.wso2.carbon.certificate.mgt.jaxrs.api;

import io.swagger.annotations.*;

import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.certificate.mgt.jaxrs.beans.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SCEP Management"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/scep"),
                        })
                }
        ),
        tags = {
                @Tag(name = "scep_management", description = "SCEP management related REST-API. " +
                                                                    "This can be used to manipulated device " +
                                                                    "certificate related details.")
        }
)
@Path("/scep")
@Api(value = "SCEP Management", description = "This API carries all device Certificate management " +
                                                            "related operations.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(scopes = {
        @Scope(
                name = "Sign CSR",
                description = "Sign CSR",
                key = "perm:sign-csr",
                permissions = {"/device-mgt/certificates/manage"}
        )
}
)
public interface CertificateMgtService {

     String SCOPE = "scope";

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
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:sign-csr")
                    })
            }
    )
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
    Response getSignedCertFromCSR(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            String binarySecurityToken);
}
