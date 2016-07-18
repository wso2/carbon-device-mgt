package org.wso2.carbon.certificate.mgt.cert.jaxrs.api;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.CertificateList;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.EnrollmentCertificate;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.ErrorResponse;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "Certificate Management", version = "1.0.0",
        context = "api/certificate-mgt/v1.0/admin/certificates",
        tags = {"devicemgt_admin"})

@Api(value = "Certificate Management", description = "This API carries all certificate management related operations " +
        "such as get all the available devices, etc.")
@Path("/admin/certificates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CertificateManagementAdminService {

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
            value = "Add a SSL certificate",
            notes = "Add a new SSL certificate",
            tags = "Certificate Management")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Certificates have successfully been added",
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
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
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while adding certificates.",
                            response = ErrorResponse.class)
            })
    @Permission(scope = "certificate:add", roles = {"admin"})
    Response addCertificate(
            @ApiParam(
                    name = "enrollmentCertificates",
                    value = "certificate with serial, "
                            + "pem and tenant id",
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
            value = "Getting Details of an SSL CertificateManagementAdminService",
            notes = "Get the client side SSL certificate details",
            tags = "Certificate Management")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched information of the device.",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
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
                    message = "Not Found. \n No device is found under the provided type and id."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while retrieving information requested certificate.",
                    response = ErrorResponse.class)
    })
    @Permission(scope = "certificate:view", roles = {"admin"})
    Response getCertificate(
            @ApiParam(name = "serialNumber",
                    value = "Provide the serial number of the certificate that you wish to get the details of",
                    required = true)
            @PathParam("serialNumber") String serialNumber,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
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
            value = "Get certificates",
            notes = "You will have many certificates used for mutual SSL. In a situation where you wish to "
                    + "view all the certificate details, it is not feasible to show all the details on one "
                    + "page therefore the details are paginated",
            tags = "Certificate Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n List of certificates enrolled in the system",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n " +
                            "Source can be retrieved from the URL specified at the Location header.",
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
                            "Server error occurred while retrieving all certificates enrolled in the system.",
                    response = ErrorResponse.class)
    })
    @Permission(scope = "certificate:view", roles = {"admin"})
    Response getAllCertificates(
            @ApiParam(
                    name = "offset",
                    value = "Starting point within the complete list of items qualified.",
                    required = false)
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Maximum size of resource array to return.",
                    required = false)
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @DELETE
    @Path("/{serialNumber}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete an SSL certificate",
            notes = "Delete an SSL certificate that's on the client end",
            tags = "Certificate Management")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Certificate has successfully been removed"),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while removing the certificate.",
                    response = ErrorResponse.class)})
    @Permission(scope = "certificate:modify", roles = {"admin"})
    Response removeCertificate(
            @ApiParam(
                    name = "serialNumber",
                    value = "Provide the serial number of the "
                            + "certificate that you wish to delete",
                    required = true)
            @PathParam("serialNumber") String serialNumber);

}
