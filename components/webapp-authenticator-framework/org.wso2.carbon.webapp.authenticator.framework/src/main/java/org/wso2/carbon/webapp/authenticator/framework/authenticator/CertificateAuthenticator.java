package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.webapp.authenticator.framework.DataHolder;

/**
 * This authenticator authenticates HTTP requests using certificates.
 */
public class CertificateAuthenticator implements WebappAuthenticator {

    private static final Log log = LogFactory.getLog(CertificateAuthenticator.class);
    private static final String CERTIFICATE_AUTHENTICATOR = "CertificateAuth";
    private static final String HEADER_MDM_SIGNATURE = "Mdm-Signature";
    private String[] skippedURIs;

    public CertificateAuthenticator() {
        skippedURIs = new String[]{
                "/ios-enrollment/ca",
                "/ios-enrollment/authenticate",
                "/ios-enrollment/profile",
                "/ios-enrollment/scep",
                "/ios-enrollment/enroll",
                "/ios-enrollment/enrolled"};
    }

    @Override
    public boolean canHandle(Request request) {
        return true;
    }

    @Override
    public Status authenticate(Request request, Response response) {

        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isEmpty()) {
            return Status.CONTINUE;
        }

        if(isURISkipped(requestUri)) {
            return Status.CONTINUE;
        }

        String headerMDMSignature = request.getHeader(HEADER_MDM_SIGNATURE);

        try {
            if (headerMDMSignature != null && !headerMDMSignature.isEmpty() &&
                    DataHolder.getInstance().getCertificateManagementService().verifySignature(headerMDMSignature)) {
                return Status.SUCCESS;
            }
        } catch (KeystoreException e) {
            log.error("KeystoreException occurred ", e);
            return Status.FAILURE;
        }

        return Status.FAILURE;
    }

    @Override
    public String getName() {
        return CERTIFICATE_AUTHENTICATOR;
    }

    private boolean isURISkipped(String requestUri) {

        for (String element : skippedURIs) {
            if (element.equals(requestUri)) {
                return true;
            }
        }

        return false;
    }

}
