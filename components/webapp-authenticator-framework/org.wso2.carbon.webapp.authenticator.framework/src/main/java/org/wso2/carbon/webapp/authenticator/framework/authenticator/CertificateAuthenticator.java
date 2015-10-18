package org.wso2.carbon.webapp.authenticator.framework.authenticator;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.scep.SCEPException;
import org.wso2.carbon.device.mgt.core.scep.SCEPManager;
import org.wso2.carbon.device.mgt.core.scep.TenantedDeviceWrapper;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticatorFrameworkDataHolder;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;

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
    public AuthenticationInfo authenticate(Request request, Response response) {

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.isEmpty()) {
            authenticationInfo.setStatus(Status.CONTINUE);
        }

        String certVerificationHeader = request.getContext().findParameter(CERTIFICATE_VERIFICATION_HEADER);
        try {
            if (certVerificationHeader != null && !certVerificationHeader.isEmpty()) {

                String certHeader = request.getHeader(certVerificationHeader);
                if (certHeader != null &&
                    AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                            verifySignature(certHeader)) {

                    X509Certificate certificate =
                            AuthenticatorFrameworkDataHolder.getInstance().getCertificateManagementService().
                                    extractCertificateFromSignature(certHeader);
                    String challengeToken = AuthenticatorFrameworkDataHolder.getInstance().
                            getCertificateManagementService().extractChallengeToken(certificate);

                    if (challengeToken != null) {
                        challengeToken = challengeToken.substring(challengeToken.indexOf("(") + 1).trim();
                        SCEPManager scepManager = AuthenticatorFrameworkDataHolder.getInstance().getScepManager();
                        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                        deviceIdentifier.setId(challengeToken);
                        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
                        TenantedDeviceWrapper tenantedDeviceWrapper = scepManager.getValidatedDevice(deviceIdentifier);
                        authenticationInfo.setTenantDomain(tenantedDeviceWrapper.getTenantDomain());
                        authenticationInfo.setTenantId(tenantedDeviceWrapper.getTenantId());
                        authenticationInfo.setStatus(Status.CONTINUE);
                    }
                }
            }
        } catch (KeystoreException e) {
            log.error("KeystoreException occurred ", e);
        } catch (SCEPException e) {
            log.error("SCEPException occurred ", e);
        }
        return authenticationInfo;
    }

    @Override
    public String getName() {
        return CERTIFICATE_AUTHENTICATOR;
    }
}
