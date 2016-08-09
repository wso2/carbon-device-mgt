package org.wso2.carbon.certificate.mgt.cert.jaxrs.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.CertificateManagementAdminService;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.CertificateList;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.EnrollmentCertificate;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.ErrorResponse;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.util.DeviceMgtAPIUtils;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.util.RequestValidationUtil;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/admin/certificates")
public class CertificateManagementAdminServiceImpl implements CertificateManagementAdminService {

    private static Log log = LogFactory.getLog(CertificateManagementAdminServiceImpl.class);

    /**
     * Save a list of certificates and relevant information in the database.
     *
     * @param enrollmentCertificates List of all the certificates which includes the tenant id, certificate as
     *                               a pem and a serial number.
     * @return Status of the data persist operation.
     */
    @POST
    public Response addCertificate(EnrollmentCertificate[] enrollmentCertificates) {
        CertificateManagementService certificateService;
        List<org.wso2.carbon.certificate.mgt.core.bean.Certificate> certificates = new ArrayList<>();
        org.wso2.carbon.certificate.mgt.core.bean.Certificate certificate;
        certificateService = DeviceMgtAPIUtils.getCertificateManagementService();
        try {
            for (EnrollmentCertificate enrollmentCertificate : enrollmentCertificates) {
                certificate = new org.wso2.carbon.certificate.mgt.core.bean.Certificate();
                certificate.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                certificate.setSerial(enrollmentCertificate.getSerial());
                certificate.setCertificate(certificateService.pemToX509Certificate(enrollmentCertificate.getPem()));
                certificates.add(certificate);
            }
            certificateService.saveCertificate(certificates);
            return Response.status(Response.Status.CREATED).entity("Added successfully.").build();
        } catch (KeystoreException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
    }

    /**
     * Get a certificate when the serial number is given.
     *
     * @param serialNumber serial of the certificate needed.
     * @return certificate response.
     */
    @GET
    @Path("/{serialNumber}")
    public Response getCertificate(
            @PathParam("serialNumber") String serialNumber,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        RequestValidationUtil.validateSerialNumber(serialNumber);

        CertificateManagementService certificateService = DeviceMgtAPIUtils.getCertificateManagementService();
        List<CertificateResponse> certificateResponse;
        try {
            certificateResponse = certificateService.searchCertificates(serialNumber);
            return Response.status(Response.Status.OK).entity(certificateResponse).build();
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
    }

    /**
     * Get all certificates in a paginated manner.
     *
     * @param offset index of the first record to be fetched
     * @param limit  number of records to be fetched starting from the start index.
     * @return paginated result of certificate.
     */
    @GET
    public Response getAllCertificates(
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        RequestValidationUtil.validatePaginationInfo(offset, limit);

        CertificateManagementService certificateService = DeviceMgtAPIUtils.getCertificateManagementService();
        PaginationRequest paginationRequest = new PaginationRequest(offset, limit);
        try {
            PaginationResult result = certificateService.getAllCertificates(paginationRequest);
            CertificateList certificates = new CertificateList();
            certificates.setCount(result.getRecordsTotal());
            certificates.setList((List<CertificateResponse>) result.getData());
            return Response.status(Response.Status.OK).entity(certificates).build();
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while fetching all certificates.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Path("/{serialNumber}")
    public Response removeCertificate(@PathParam("serialNumber") String serialNumber) {
        RequestValidationUtil.validateSerialNumber(serialNumber);

        CertificateManagementService certificateService = DeviceMgtAPIUtils.getCertificateManagementService();
        try {
            boolean status = certificateService.removeCertificate(serialNumber);
            if (!status) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        "No certificate is found with the given " +
                                "serial number '" + serialNumber + "'").build();
            } else {
                return Response.status(Response.Status.OK).entity(
                        "Certificate that carries the serial number '" +
                                serialNumber + "' has been removed").build();
            }
        } catch (CertificateManagementException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

}

//return Response.status(Response.Status.NOT_FOUND).entity("No certificate is found with the given " +
//        "serial number '" + serialNumber + "'");