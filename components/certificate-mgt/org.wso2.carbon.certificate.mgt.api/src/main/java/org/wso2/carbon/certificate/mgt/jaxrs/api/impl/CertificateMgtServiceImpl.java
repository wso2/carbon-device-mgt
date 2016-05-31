package org.wso2.carbon.certificate.mgt.jaxrs.api.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.impl.CertificateGenerator;
import org.wso2.carbon.certificate.mgt.jaxrs.api.CertificateMgtService;
import org.wso2.carbon.certificate.mgt.jaxrs.exception.Message;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;


public class CertificateMgtServiceImpl implements CertificateMgtService {
    private static Log log = LogFactory.getLog(CertificateMgtServiceImpl.class);

    @POST
    @Path("signcsr")
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    @Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN})
    public Response getSignedCertFromCSR(String binarySecurityToken) {
        Message message = new Message();
        X509Certificate signedCert;
        String singedCertificate;
        Base64 base64 = new Base64();
        CertificateGenerator certificateGenerator = new CertificateGenerator();
        try {
            if (certificateGenerator.getSignedCertificateFromCSR(binarySecurityToken) == null) {
                message.setErrorMessage("Error occurred while signing the CSR.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(message).build();
            } else {
                signedCert = certificateGenerator.getSignedCertificateFromCSR(binarySecurityToken);
                singedCertificate = base64.encodeToString(signedCert.getEncoded());
                return Response.status(Response.Status.OK).entity(singedCertificate).build();
            }
        } catch (KeystoreException e) {
            String msg = "Error occurred while fetching certificate.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (CertificateEncodingException e) {
            String msg = "Error occurred while encoding the certificate.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
