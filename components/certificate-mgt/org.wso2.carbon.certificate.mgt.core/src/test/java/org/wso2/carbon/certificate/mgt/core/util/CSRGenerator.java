/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.certificate.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
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
import java.security.NoSuchAlgorithmException;

/**
 * Test class for generating a mock CSR request.
 */
public class CSRGenerator {
    private static Log log = LogFactory.getLog(CSRGenerator.class);

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
        } catch (OperatorCreationException ex) {
            log.error("Error while Key generation operation", ex);
        } catch (IOException ex) {
            log.error("Error while generating CSR,ex");
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
        } catch (NoSuchAlgorithmException e) {
            log.error("The provided algorithm is not found ",e);
        }
        return null;
    }
}
