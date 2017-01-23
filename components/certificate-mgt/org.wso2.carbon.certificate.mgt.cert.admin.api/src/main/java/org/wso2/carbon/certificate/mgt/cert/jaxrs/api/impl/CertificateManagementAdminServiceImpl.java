package org.wso2.carbon.certificate.mgt.cert.jaxrs.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.CertificateManagementAdminService;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.CertificateList;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.EnrollmentCertificate;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.ErrorResponse;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.beans.ValidationResponce;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.util.CertificateMgtAPIUtils;
import org.wso2.carbon.certificate.mgt.cert.jaxrs.api.util.RequestValidationUtil;
import org.wso2.carbon.certificate.mgt.core.dto.CertificateResponse;
import org.wso2.carbon.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPException;
import org.wso2.carbon.certificate.mgt.core.scep.SCEPManager;
import org.wso2.carbon.certificate.mgt.core.scep.TenantedDeviceWrapper;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.certificate.mgt.core.service.PaginationResult;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin/certificates")
public class CertificateManagementAdminServiceImpl implements CertificateManagementAdminService {

    private static Log log = LogFactory.getLog(CertificateManagementAdminServiceImpl.class);
    private static final String PROXY_AUTH_MUTUAL_HEADER = "proxy-mutual-auth-header";

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
        certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        try {
            for (EnrollmentCertificate enrollmentCertificate : enrollmentCertificates) {
                certificate = new org.wso2.carbon.certificate.mgt.core.bean.Certificate();
                certificate.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
                X509Certificate x509Certificate = certificateService
                        .pemToX509Certificate(enrollmentCertificate.getPem());
                certificate.setSerial(x509Certificate.getSerialNumber().toString());
                certificate.setCertificate(x509Certificate);
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

        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
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
        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
        try {
            PaginationResult result = certificateService.getAllCertificates(offset, limit);
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

        CertificateManagementService certificateService = CertificateMgtAPIUtils.getCertificateManagementService();
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

//    @POST
//    @Path("/verify/ios")
//    public Response verifyIOSCertificate(@ApiParam(name = "certificate", value = "Mdm-Signature of the " +
//            "certificate that needs to be verified", required = true) EnrollmentCertificate certificate) {
//        try {
//            CertificateManagementService certMgtService = CertificateMgtAPIUtils.getCertificateManagementService();
//            X509Certificate cert = certMgtService.extractCertificateFromSignature(certificate.getPem());
//            String challengeToken = certMgtService.extractChallengeToken(cert);
//
//            if (challengeToken != null) {
//                challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();
//
//                SCEPManager scepManager = CertificateMgtAPIUtils.getSCEPManagerService();
//                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
//                deviceIdentifier.setId(challengeToken);
//                deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
//                TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);
//
//                if (tenantedDeviceWrapper != null) {
//                    return Response.status(Response.Status.OK).entity("valid").build();
//                }
//            }
//        } catch (SCEPException e) {
//            String msg = "Error occurred while extracting information from certificate.";
//            log.error(msg, e);
//            return Response.serverError().entity(
//                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
//        } catch (KeystoreException e) {
//            String msg = "Error occurred while converting PEM file to X509Certificate.";
//            log.error(msg, e);
//            return Response.serverError().entity(
//                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
//        }
//        return Response.status(Response.Status.OK).entity("invalid").build();
//    }
//
//    @POST
//    @Path("/verify/android")
//    public Response verifyAndroidCertificate(@ApiParam(name = "certificate", value = "Base64 encoded .pem file of the " +
//            "certificate that needs to be verified", required = true) EnrollmentCertificate certificate) {
//        CertificateResponse certificateResponse = null;
//        try {
//            CertificateManagementService certMgtService = CertificateMgtAPIUtils.getCertificateManagementService();
//            if (certificate.getSerial().toLowerCase().contains(PROXY_AUTH_MUTUAL_HEADER)) {
//                certificateResponse = certMgtService.verifySubjectDN(certificate.getPem());
//            } else {
//                X509Certificate clientCertificate = certMgtService.pemToX509Certificate(certificate.getPem());
//                if (clientCertificate != null) {
//                    certificateResponse = certMgtService.verifyPEMSignature(clientCertificate);
//                }
//            }
//
//            if (certificateResponse != null && certificateResponse.getCommonName() != null && !certificateResponse
//                    .getCommonName().isEmpty()) {
//                return Response.status(Response.Status.OK).entity("valid").build();
//            }
//        } catch (KeystoreException e) {
//            String msg = "Error occurred while converting PEM file to X509Certificate.";
//            log.error(msg, e);
//            return Response.serverError().entity(
//                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
//        }
//        return Response.status(Response.Status.OK).entity("invalid").build();
//    }

    @POST
    @Path("/verify/{type}")
    public Response verifyCertificate(@PathParam("type") String type, EnrollmentCertificate certificate) {
        try {
            CertificateManagementService certMgtService = CertificateMgtAPIUtils.getCertificateManagementService();

            if (DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS.equalsIgnoreCase(type)) {
                X509Certificate cert = certMgtService.extractCertificateFromSignature(certificate.getPem());
                String challengeToken = certMgtService.extractChallengeToken(cert);

                if (challengeToken != null) {
                    challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();

                    SCEPManager scepManager = CertificateMgtAPIUtils.getSCEPManagerService();
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(challengeToken);
                    deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                    TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);
//
//                    var claims = {"http://wso2.org/claims/enduserTenantId": adminUserTenantId,
//                            "http://wso2.org/claims/enduser": adminUsername};

                    Map<String, String> claims = new HashMap<>();

                    claims.put("http://wso2.org/claims/enduserTenantId", String.valueOf(tenantedDeviceWrapper.getTenantId()));
                    claims.put("http://wso2.org/claims/enduser", tenantedDeviceWrapper.getDevice().getEnrolmentInfo().getOwner());
                    claims.put("http://wso2.org/claims/deviceIdentifier", tenantedDeviceWrapper.getDevice().getDeviceIdentifier());
                    claims.put("http://wso2.org/claims/deviceIdType", tenantedDeviceWrapper.getDevice().getType());

                    JWTClientManagerService jwtClientManagerService = CertificateMgtAPIUtils.getJwtClientManagerService();
                    String jwdToken = jwtClientManagerService.getJWTClient().getJwtToken(
                            tenantedDeviceWrapper.getDevice().getEnrolmentInfo().getOwner(), claims);

                    ValidationResponce validationResponce = new ValidationResponce();
                    validationResponce.setDeviceId(challengeToken);
                    validationResponce.setDeviceType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                    validationResponce.setJWTToken(jwdToken);
                    validationResponce.setTenantId(tenantedDeviceWrapper.getTenantId());

                    if (tenantedDeviceWrapper != null) {
                        return Response.status(Response.Status.OK).entity(validationResponce).build();
                    }
                }
            }

            if (DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID.equalsIgnoreCase(type)) {
                CertificateResponse certificateResponse = null;
                if (certificate.getSerial().toLowerCase().contains(PROXY_AUTH_MUTUAL_HEADER)) {
                    certificateResponse = certMgtService.verifySubjectDN(certificate.getPem());
                } else {
                    X509Certificate clientCertificate = certMgtService.pemToX509Certificate(certificate.getPem());
                    if (clientCertificate != null) {
                        certificateResponse = certMgtService.verifyPEMSignature(clientCertificate);
                    }
                }

                if (certificateResponse != null && certificateResponse.getCommonName() != null && !certificateResponse
                        .getCommonName().isEmpty()) {
                    return Response.status(Response.Status.OK).entity("valid").build();
                }
            }
        } catch (SCEPException e) {
            String msg = "Error occurred while extracting information from certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (KeystoreException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        } catch (JWTClientException e) {
            String msg = "Error occurred while converting PEM file to X509Certificate.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity("invalid").build();
    }
}