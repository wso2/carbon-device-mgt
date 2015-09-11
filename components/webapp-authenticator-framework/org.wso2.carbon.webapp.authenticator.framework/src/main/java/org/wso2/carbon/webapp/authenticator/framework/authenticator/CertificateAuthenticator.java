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
    private static final String CERTIFICATE_VERIFICATION_HEADER = "certificate-verification-header";

    @Override
    public boolean canHandle(Request request) {
        String certVerificationHeader = request.getContext().findParameter(CERTIFICATE_VERIFICATION_HEADER);

        if (certVerificationHeader != null && !certVerificationHeader.isEmpty()) {

            String certHeader = request.getHeader(certVerificationHeader);

            return certHeader != null;
        }

        return false;
    }

    @Override
    public Status authenticate(Request request, Response response) {

        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isEmpty()) {
            return Status.CONTINUE;
        }

        String certVerificationHeader = request.getContext().findParameter(CERTIFICATE_VERIFICATION_HEADER);

        try {
            if (certVerificationHeader != null && !certVerificationHeader.isEmpty()) {

                String certHeader = request.getHeader(certVerificationHeader);

                if (certHeader != null && DataHolder.getInstance().getCertificateManagementService().
                        verifySignature(certHeader)) {
                    return Status.SUCCESS;
                }
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
}
