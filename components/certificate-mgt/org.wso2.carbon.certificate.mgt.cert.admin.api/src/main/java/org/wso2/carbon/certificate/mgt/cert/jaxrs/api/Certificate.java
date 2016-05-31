package org.wso2.carbon.certificate.mgt.cert.jaxrs.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.EnrollmentCertificate;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.common.MDMAPIException;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface Certificate {

    /**
     * Save a list of certificates and relevant information in the database.
     *
     * @param enrollmentCertificates List of all the certificates which includes the tenant id, certificate as
     *                               a pem and a serial number.
     * @return Status of the data persist operation.
     */
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "POST",
            value = "Adding an SSL Certificate",
            notes = "Add a new SSL certificate to the client end database")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Added successfully"),
            @ApiResponse(code = 500, message = "Error occurred while saving the certificate")
    })
    @Permission(scope = "certificate-modify", permissions = {"/permission/admin/device-mgt/certificate/save"})
    Response saveCertificate(@HeaderParam("Accept") String acceptHeader,
                             @ApiParam(name = "enrollmentCertificates", value = "certificate with serial, "
                                     + "pem and tenant id", required = true) EnrollmentCertificate[]
                                     enrollmentCertificates);

    /**
     * Get a certificate when the serial number is given.
     *
     * @param serialNumber serial of the certificate needed.
     * @return certificate response.
     */
    @GET
    @Path("{serialNumber}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting Details of an SSL Certificate",
            notes = "Get the client side SSL certificate details",
            response = CertificateResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CertificateResponse.class),
            @ApiResponse(code = 400, message = "Notification status updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while converting PEM file to X509Certificate")
    })
    @Permission(scope = "certificate-view", permissions = {"/permission/admin/device-mgt/certificate/view"})
    Response getCertificate(@HeaderParam("Accept") String acceptHeader,
                            @ApiParam(name = "serialNumber", value = "Provide the serial number of the "
                                    + "certificate that you wish to get the details of", required = true)
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
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "GET",
            value = "Getting the Certificate Details in a Paginated Manner",
            notes = "You will have many certificates used for mutual SSL. In a situation where you wish to "
                    + "view all the certificate details, it is not feasible to show all the details on one "
                    + "page therefore the details are paginated",
            response = PaginationResult.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaginationResult.class),
            @ApiResponse(code = 400, message = "Invalid start index"),
            @ApiResponse(code = 400, message = "Invalid length value"),
            @ApiResponse(code = 500, message = "Error occurred while fetching all certificates")
    })
    @Permission(scope = "certificate-view", permissions = {"/permission/admin/device-mgt/certificate/view"})
    Response getAllCertificates(@HeaderParam("Accept") String acceptHeader,
                                @ApiParam(name = "start",
                                        value = "Provide the starting pagination index as the value", required = true)
                                @QueryParam("start") int startIndex,
                                @ApiParam(name = "length", value = "Provide how many certificate details you"
                                        + " require from the starting pagination index as the value",
                                        required = true) @QueryParam("length") int length) throws MDMAPIException;

    @DELETE
    @Path("{serialNumber}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            produces = MediaType.APPLICATION_JSON + ", " + MediaType.APPLICATION_XML,
            httpMethod = "DELETE",
            value = "Deleting an SSL Certificate",
            notes = "Delete an SSL certificate that's on the client end",
            response = boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Invalid start index"),
            @ApiResponse(code = 500, message = "Error when deleting the certificate"
            ) })
    @Permission(scope = "certificate-modify", permissions = {"/permission/admin/device-mgt/certificate/remove"})
    Response removeCertificate(@HeaderParam("Accept") String acceptHeader,
                               @ApiParam(name = "serialNumber", value = "Provide the serial number of the "
                                       + "certificate that you wish to delete", required = true)
                               @PathParam("serialNumber") String serialNumber) throws MDMAPIException;

}
