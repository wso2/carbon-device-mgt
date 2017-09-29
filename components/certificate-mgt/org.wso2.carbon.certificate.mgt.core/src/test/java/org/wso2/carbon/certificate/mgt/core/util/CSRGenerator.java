package org.wso2.carbon.certificate.mgt.core.util;

import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;


public class CSRGenerator {

    /**
     * Generate the desired CSR for signing
     *
     * @param sigAlg
     * @param keyPair
     * @return
     */
    public byte[] generateCSR(String sigAlg, KeyPair keyPair) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outStream);

        try {

            PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
                    new X500Principal("CN=Requested Test Certificate"), keyPair.getPublic());
            JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            ContentSigner signer = csBuilder.build(keyPair.getPrivate());
            PKCS10CertificationRequest csr = p10Builder.build(signer);

            return csr.getEncoded();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != outStream) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != printStream) {
                printStream.close();
            }
        }

        return new byte[0];
    }

    /**
     * Generate the desired keypair
     *
     * @param alg
     * @param keySize
     * @return
     */
    public KeyPair generateKeyPair(String alg, int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = null;
            keyPairGenerator = KeyPairGenerator.getInstance(alg);

            keyPairGenerator.initialize(keySize);

            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
