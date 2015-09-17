package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.scep.SCEPException;
import org.wso2.carbon.device.mgt.core.scep.SCEPManager;
import org.wso2.carbon.device.mgt.core.scep.TenantedDeviceWrapper;
import org.wso2.carbon.webapp.authenticator.framework.DataHolder;

import java.security.cert.X509Certificate;

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

                    X509Certificate certificate = DataHolder.getInstance().getCertificateManagementService().
                            extractCertificateFromSignature(certHeader);
                    String challengeToken = DataHolder.getInstance().getCertificateManagementService().
                            extractChallengeToken(certificate);

                    if(challengeToken != null) {

                        challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();

                        SCEPManager scepManager = DataHolder.getInstance().getScepManager();
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        deviceIdentifier.setId(challengeToken);
                        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);

                        TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);

                        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        ctx.setTenantId(tenantedDeviceWrapper.getTenantId());
                        ctx.setTenantDomain(tenantedDeviceWrapper.getTenantDomain());

                        return Status.SUCCESS;
                    }
                }
            }
        } catch (KeystoreException e) {
            log.error("KeystoreException occurred ", e);
        } catch (SCEPException e) {
            log.error("SCEPException occurred ", e);
        }

        return Status.FAILURE;
    }

    @Override
    public String getName() {
        return CERTIFICATE_AUTHENTICATOR;
    }
}
