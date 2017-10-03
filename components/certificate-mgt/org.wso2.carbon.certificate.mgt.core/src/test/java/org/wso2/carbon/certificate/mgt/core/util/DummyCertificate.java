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

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.security.InvalidKeyException;
import java.util.Date;
import java.util.Set;

/**
 * A Mock Certificate class with an extension provided.(Used only to test the extension)
 */
public class DummyCertificate extends X509Certificate {

    public static final String EXT = "Dummy extension";
    public static final String DN = "O=WSO2,OU=Mobile,C=LK,CN=123456789";

    @Override
    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public BigInteger getSerialNumber() {
        return new BigInteger("123456789");
    }

    @Override
    public Principal getIssuerDN() {
        return null;
    }

    @Override
    public Principal getSubjectDN() {
        return new Principal() {
            @Override
            public String getName() {
                return DN;
            }
        };
    }

    @Override
    public Date getNotBefore() {
        return null;
    }

    @Override
    public Date getNotAfter() {
        return null;
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public byte[] getSignature() {
        return new byte[0];
    }

    @Override
    public String getSigAlgName() {
        return null;
    }

    @Override
    public String getSigAlgOID() {
        return null;
    }

    @Override
    public byte[] getSigAlgParams() {
        return new byte[0];
    }

    @Override
    public boolean[] getIssuerUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getSubjectUniqueID() {
        return new boolean[0];
    }

    @Override
    public boolean[] getKeyUsage() {
        return new boolean[0];
    }

    @Override
    public int getBasicConstraints() {
        return 0;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        return new byte[0];
    }

    @Override
    public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    }

    @Override
    public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public PublicKey getPublicKey() {
        return null;
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
        return false;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
        return null;
    }

    @Override
    public byte[] getExtensionValue(String oid) {
        if (oid.equals(PKCSObjectIdentifiers.pkcs_9_at_challengePassword.toString())) {
            return EXT.getBytes();
        } else {
            return new byte[0];
        }
    }
}
