package org.wso2.carbon.certificate.mgt.cert.jaxrs.api;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.CertificateList;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.EnrollmentCertificate;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.ErrorResponse;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "Certificate Management"),
                                @ExtensionProperty(name = "context", value = "/api/certificate-mgt/v1.0/admin/certificates"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Api(value = "Certificate Management", description = "This API includes all the certificate management related operations")
@Path("/admin/certificates")
@Scopes(scopes = {
        @Scope(
                name = "Adding a new SSL certificate",
                description = "Adding a new SSL certificate",
                key = "perm:admin:certificates:add",
                permissions = {"/device-mgt/admin/certificates/add"}
        ),
        @Scope(
                name = "Getting Details of an SSL Certificate",
                description = "Getting Details of an SSL Certificate",
                key = "perm:admin:certificates:details",
                permissions = {"/device-mgt/admin/certificates/details"}
        ),
        @Scope(
                name = "Getting Details of Certificates",
                description = "Getting Details of Certificates",
                key = "perm:admin:certificates:view",
                permissions = {"/device-mgt/admin/certificates/view"}
        ),
        @Scope(
                name = "Deleting an SSL Certificate",
                description = "Deleting an SSL Certificate",
                key = "perm:admin:certificates:delete",
                permissions = {"/device-mgt/admin/certificates/delete"}
        ),
        @Scope(
                name = "Verify SSL certificate",
                description = "Verify SSL certificate",
                key = "perm:admin:certificates:verify",
                permissions = {"/device-mgt/admin/certificates/verify"}
        )
}
)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CertificateManagementAdminService {

    String SCOPE = "scope";

    /**
     * Save a list of certificates and relevant information in the database.
     *
     * @param enrollmentCertificates List of all the certificates which includes the tenant id, certificate as
     *                               a pem and a serial number.
     * @return Status of the data persist operation.
     */
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a new SSL certificate",
            notes = "Add a new SSL certificate to the client end database.\n",
            tags = "Certificate Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:certificates:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Successfully added the certificate.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added certificates."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported Media Type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while adding certificates.",
                            response = ErrorResponse.class)
            })
    Response addCertificate(
            @ApiParam(
                    name = "enrollmentCertificates",
                    value = "The properties to add a new certificate. It includes the following: \n" +
                            "serial: The unique ID of the certificate. \n" +
                            "pem: Convert the OpenSSL certificate to the .pem format and base 64 encode the file. \n" +
                            "INFO: Upload the .pem file and base 64 encode it using a tool, such as the base64encode.in tool.",
                    required = true) EnrollmentCertificate[] enrollmentCertificates);

    /**
     * Get a certificate when the serial number is given.
     *
     * @param serialNumber serial of the certificate needed.
     * @return certificate response.
     */
    @GET
    @Path("/{serialNumber}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of an SSL Certificate",
            notes = "Get the client side SSL certificate details.",
            tags = "Certificate Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:certificates:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the certificate details.",
                    response = CertificateResponse.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n " +
                            "Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified certificate does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while retrieving the requested certificate information.",
                    response = ErrorResponse.class)
    })
    Response getCertificate(
            @ApiParam(name = "serialNumber",
                    value = "The serial number of the certificate.",
                    required = true,
                    defaultValue = "124380353155528759302")
            @PathParam("serialNumber") String serialNumber,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince
    );

    /**
     * Get all certificates in a paginated manner.
     *
     * @return paginated result of certificate.
     */
    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Certificates",
            notes = "Get all the details of the certificates you have used for mutual SSL. In a situation where you wish to "
                    + "view all the certificate details, it is not feasible to show all the details on one "
                    + "page. Therefore, the details are paginated.",
            tags = "Certificate Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:certificates:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of certificates.",
                    response = CertificateList.class,
                    responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n " +
                            "The source can be retrieved from the URL specified in the location header.\n",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n " +
                            "Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable. \n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while retrieving the certificate details.",
                    response = ErrorResponse.class)
    })
    Response getAllCertificates(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many certificate details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @DELETE
    @Path("/{serialNumber}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting an SSL Certificate",
            notes = "Delete an SSL certificate that's on the client end.",
            tags = "Certificate Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:certificates:delete")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully removed the certificate."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while removing the certificate.",
                    response = ErrorResponse.class)})
    Response removeCertificate(
            @ApiParam(
                    name = "serialNumber",
                    value = "The serial number of the certificate.\n" +
                            "NOTE: Make sure that a certificate with the serial number you provide exists in the server. If not, first add a certificate.",
                    required = true,
                    defaultValue = "12438035315552875930")
            @PathParam("serialNumber") String serialNumber);

    /**
     * Verify Certificate for the API security filter
     *
     * @param certificate to be verified as a String
     * @return Status of the certificate verification.
     */
    @POST
    @Path("/verify/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Verify SSL certificate",
            notes = "Verify Certificate for the API security filter.\n",
            tags = "Certificate Management",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = SCOPE, value = "perm:admin:certificates:verify")
            })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "Return the status of the certificate verification.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class)
            })
    Response verifyCertificate(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true,
                    allowableValues = "android, ios, windows")
            @PathParam("type")
            @Size(max = 45)
            String type,
            @ApiParam(
                    name = "certificate",
                    value = "The properties to verify certificate. It includes the following: \n" +
                            "serial: The unique ID of the certificate. (optional) \n" +
                            "pem: pem String of the certificate",
                    required = true) EnrollmentCertificate certificate);
}

