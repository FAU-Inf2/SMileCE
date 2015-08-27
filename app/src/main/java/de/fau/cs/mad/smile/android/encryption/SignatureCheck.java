package de.fau.cs.mad.smile.android.encryption;

import android.support.annotation.NonNull;
import android.util.Log;

import org.spongycastle.asn1.ASN1Encoding;
import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.cms.Attribute;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.CMSAttributes;
import org.spongycastle.asn1.cms.Time;
import org.spongycastle.asn1.x509.AuthorityKeyIdentifier;
import org.spongycastle.asn1.x509.Extension;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.cert.jcajce.JcaCertStoreBuilder;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.SignerId;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.cms.jcajce.JcaX509CertSelectorConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.mail.smime.SMIMESigned;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;
import org.spongycastle.x509.PKIXCertPathReviewer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import korex.mail.MessagingException;
import korex.mail.internet.MimeBodyPart;
import korex.mail.internet.MimeMultipart;

import de.fau.cs.mad.smime_api.SMimeApi;

public class SignatureCheck {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final int SHORT_KEY_LENGTH = 512;

    private final KeyManagement keyManagement;

    public SignatureCheck() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        keyManagement = new KeyManagement();
    }

    public int verifySignature(final MimeBodyPart bodyPart, final String sender)
            throws MessagingException, CMSException, SMIMEException, IOException,
            GeneralSecurityException, OperatorCreationException, CertPathReviewerException {
        if (bodyPart == null) {
            throw new IllegalArgumentException("bodyPart should not be null");
        }

        boolean valid = true;
        SMIMESigned signed;

        if(bodyPart.isMimeType("multipart/signed")) {
            signed = new SMIMESigned((MimeMultipart) bodyPart.getContent());
        } else {
            return SMimeApi.RESULT_SIGNATURE_UNSIGNED;
        }

        JcaCertStoreBuilder jcaCertStoreBuilder = new JcaCertStoreBuilder();
        jcaCertStoreBuilder.addCertificates(signed.getCertificates());
        jcaCertStoreBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);

        CertStore certs = jcaCertStoreBuilder.build();
        SignerInformationStore signers = signed.getSignerInfos();

        KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
        keyStore.load(null);
        PKIXParameters pkixParameters = new PKIXParameters(keyStore);
        PKIXParameters usedParameters = (PKIXParameters) pkixParameters.clone();
        usedParameters.addCertStore(certs);
        usedParameters.setRevocationEnabled(false); // TODO: add crls?

        Collection signersCollection = signers.getSigners();
        Iterator iterator = signersCollection.iterator();

        int status = SMimeApi.RESULT_SIGNATURE_UNSIGNED;

        while (iterator.hasNext()) {
            SignerInformation signer = (SignerInformation) iterator.next();
            List<X509Certificate> certCollection = findCerts(usedParameters.getCertStores(), signer.getSID());
            for (X509Certificate cert : certCollection) {
                // check signature
                JcaSimpleSignerInfoVerifierBuilder verifierBuilder = new JcaSimpleSignerInfoVerifierBuilder();
                verifierBuilder.setProvider(BouncyCastleProvider.PROVIDER_NAME);
                valid &= signer.verify(verifierBuilder.build(cert.getPublicKey()));
                Log.d(SMileCrypto.LOG_TAG, "valid signature: " + valid);
                valid &= checkSigner(cert, sender);
                if (valid) {
                    //TODO: good place?
                    KeyManagement.addFriendsCertificate(cert);
                }

                Log.d(SMileCrypto.LOG_TAG, "valid signer: " + valid);
                Date signTime = checkSignatureTime(usedParameters, signer, cert);
                usedParameters.setDate(signTime);
                List<CertStore> userCertStores = new ArrayList<>();
                userCertStores.add(certs);
                final List<Boolean> userProvidedList = new ArrayList<>();
                CertPath certPath = createCertPath(cert, usedParameters.getTrustAnchors(), pkixParameters.getCertStores(), userCertStores, userProvidedList);

                PKIXCertPathReviewer review = new PKIXCertPathReviewer(certPath, usedParameters);

                if(review.isValidCertPath()) {
                    status = SMimeApi.RESULT_SIGNATURE_SIGNED;
                } else {
                    status = SMimeApi.RESULT_SIGNATURE_SIGNED_UNCOFIRMED;
                }

                Log.d(SMileCrypto.LOG_TAG, "valid certificate path: " + valid);
            }
        }

        return status;
    }

    @NonNull
    private Date checkSignatureTime(final PKIXParameters usedParameters,
                                    final SignerInformation signer, final X509Certificate cert)
            throws CertificateExpiredException, CertificateNotYetValidException {
        Date signTime = getSignatureTime(signer);

        if (signTime == null) {
            // TODO: notify no signing time
            signTime = usedParameters.getDate();
            if (signTime == null) {
                signTime = new Date();
            }
        } else {
            cert.checkValidity(signTime);
        }

        return signTime;
    }

    /**
     * Taken from bouncycastle SignedMailValidator
     * Returns an Object array containing a CertPath and a List of Booleans. The list contains the value <code>true</code>
     * if the corresponding certificate in the CertPath was taken from the user provided CertStores.
     *
     * @param signerCert       the end of the path
     * @param trustAnchors     trust anchors for the path
     * @param systemCertStores list of {@link CertStore} provided by the system
     * @param userCertStores   list of {@link CertStore} provided by the user
     * @return a CertPath and a List of booleans.
     * @throws GeneralSecurityException
     */
    public CertPath createCertPath(final X509Certificate signerCert,
                                   final Set<TrustAnchor> trustAnchors,
                                   final List<CertStore> systemCertStores,
                                   final List<CertStore> userCertStores,
                                   final List<Boolean> userProvidedList)
            throws GeneralSecurityException {
        final Set<X509Certificate> certSet = new LinkedHashSet<>();

        // add signer certificate

        X509Certificate cert = signerCert;
        certSet.add(cert);
        userProvidedList.add(true);

        FindTrustAnchorResult findTrustAnchorResult = findTrustAnchor(cert, trustAnchors, systemCertStores, userCertStores, certSet, userProvidedList);
        X509Certificate trustAnchorCert = findTrustAnchorResult.trustAnchorCert;

        // if a trustanchor was found - try to find a selfsigned certificate of
        // the trustanchor
        if (findTrustAnchorResult.trustAnchorFound) {
            if (trustAnchorCert != null && trustAnchorCert.getSubjectX500Principal().equals(trustAnchorCert.getIssuerX500Principal())) {
                certSet.add(trustAnchorCert);
                userProvidedList.add(false);
            } else {
                X509CertSelector select = new X509CertSelector();

                try {
                    select.setSubject(cert.getIssuerX500Principal().getEncoded());
                    select.setIssuer(cert.getIssuerX500Principal().getEncoded());
                } catch (IOException e) {
                    throw new IllegalStateException(e.toString());
                }

                boolean userProvided = false;

                trustAnchorCert = findNextCert(systemCertStores, select, certSet);
                if (trustAnchorCert == null && userCertStores != null) {
                    userProvided = true;
                    trustAnchorCert = findNextCert(userCertStores, select, certSet);
                }

                if (trustAnchorCert != null) {
                    try {
                        cert.verify(trustAnchorCert.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
                        certSet.add(trustAnchorCert);
                        userProvidedList.add(userProvided);
                    } catch (GeneralSecurityException gse) {
                        // wrong cert
                    }
                }
            }
        }

        CertPath certPath = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME).generateCertPath(new ArrayList<>(certSet));
        return certPath;
    }

    private FindTrustAnchorResult findTrustAnchor(final X509Certificate signerCertificate,
                                            final Set<TrustAnchor> trustAnchors,
                                            final List<CertStore> systemCertStores,
                                            final List<CertStore> userCertStores,
                                            final Set<X509Certificate> certSet,
                                            final List<Boolean> userProvidedList)
            throws CertStoreException {

        FindTrustAnchorResult result = new FindTrustAnchorResult();
        result.trustAnchorCert = null;
        result.trustAnchorFound = false;
        X509Certificate cert = signerCertificate;

        // add other certs to the cert path
        while (cert != null && !result.trustAnchorFound) {
            // check if cert Issuer is Trustanchor
            for (TrustAnchor anchor : trustAnchors) {
                X509Certificate anchorCert = anchor.getTrustedCert();
                if (anchorCert != null) {
                    if (anchorCert.getSubjectX500Principal().equals(
                            cert.getIssuerX500Principal())) {
                        try {
                            cert.verify(anchorCert.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
                            result.trustAnchorFound = true;
                            result.trustAnchorCert = anchorCert;
                            break;
                        } catch (Exception e) {
                            // trustanchor not found
                        }
                    }
                } else {
                    if (anchor.getCAName().equals(
                            cert.getIssuerX500Principal().getName())) {
                        try {
                            cert.verify(anchor.getCAPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
                            result.trustAnchorFound = true;
                            break;
                        } catch (Exception e) {
                            // trustanchor not found
                        }
                    }
                }
            }

            if (!result.trustAnchorFound) {
                // add next cert to path
                X509CertSelector select = new X509CertSelector();
                try {
                    select.setSubject(cert.getIssuerX500Principal().getEncoded());
                } catch (IOException e) {
                    throw new IllegalStateException(e.toString());
                }
                byte[] authKeyIdentBytes = cert.getExtensionValue(Extension.authorityKeyIdentifier.getId());
                if (authKeyIdentBytes != null) {
                    try {
                        AuthorityKeyIdentifier kid = AuthorityKeyIdentifier.getInstance(getObject(authKeyIdentBytes));
                        if (kid.getKeyIdentifier() != null) {
                            select.setSubjectKeyIdentifier(new DEROctetString(kid.getKeyIdentifier()).getEncoded(ASN1Encoding.DER));
                        }
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
                boolean userProvided = false;

                cert = findNextCert(systemCertStores, select, certSet);
                if (cert == null && userCertStores != null) {
                    userProvided = true;
                    cert = findNextCert(userCertStores, select, certSet);
                }

                if (cert != null) {
                    // cert found
                    certSet.add(cert);
                    userProvidedList.add(userProvided);
                }
            }
        }

        return result;
    }

    static class FindTrustAnchorResult {
        X509Certificate trustAnchorCert;
        boolean trustAnchorFound;
    }

    private X509Certificate findNextCert(List<CertStore> certStores, X509CertSelector selector, Set<X509Certificate> certSet)
            throws CertStoreException {
        List<X509Certificate> certificates = findCerts(certStores, selector);

        for (X509Certificate certificate : certificates) {
            if (!certSet.contains(certificate)) {
                return certificate;
            }
        }

        return null;
    }

    private boolean checkSigner(final X509Certificate cert, final String sender) throws CertificateParsingException, CertificateEncodingException, IOException {
        boolean valid = true;
        valid &= checkKeyLength(cert);
        valid &= checkKeyUsage(cert);
        valid &= checkExtendedKeyUsage(cert);
        valid &= checkMailAddresses(cert, sender);
        return valid;
    }

    private boolean checkMailAddresses(final X509Certificate cert, final String sender)
            throws CertificateParsingException, CertificateEncodingException {
        List<String> names = keyManagement.getAlternateNamesFromCert(cert);
        return names.contains(sender);
    }

    private boolean checkExtendedKeyUsage(final X509Certificate cert) throws CertificateParsingException {

        List<String> extendedKeyUsage = cert.getExtendedKeyUsage();
        return extendedKeyUsage.contains(KeyPurposeId.anyExtendedKeyUsage.getId()) ||
                extendedKeyUsage.contains(KeyPurposeId.id_kp_emailProtection.getId());
    }

    private boolean checkKeyLength(final X509Certificate cert) {
        final PublicKey key = cert.getPublicKey();
        int keyLength = -1;

        if (key instanceof RSAPublicKey) {
            keyLength = ((RSAPublicKey) key).getModulus().bitLength();
        } else if (key instanceof DSAPublicKey) {
            keyLength = ((DSAPublicKey) key).getParams().getP().bitLength();
        }

        return keyLength == -1 || keyLength > SHORT_KEY_LENGTH;
    }

    /**
     * See https://tools.ietf.org/html/rfc5280#section-4.2.1.3
     *
     * @param cert the certificate to check key usage for
     * @return true if key usage is set and either digitalSignature or nonRepudiation are present, otherwise false
     */
    private boolean checkKeyUsage(final X509Certificate cert) {
        final boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage == null) {
            return false;
        }

        return keyUsage[0] || keyUsage[1];
    }

    private Date getSignatureTime(final SignerInformation signer) {
        AttributeTable attributeTable = signer.getSignedAttributes();
        Date result = null;

        if (attributeTable != null) {
            Attribute attr = attributeTable.get(CMSAttributes.signingTime);
            if (attr != null) {
                Time t = Time.getInstance(attr.getAttrValues().getObjectAt(0)
                        .toASN1Primitive());
                result = t.getDate();
            }
        }

        return result;
    }

    private ASN1Primitive getObject(final byte[] ext)
            throws IOException {
        ASN1InputStream aIn = new ASN1InputStream(ext);
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();

        aIn = new ASN1InputStream(octs.getOctets());
        return aIn.readObject();
    }

    private List<X509Certificate> findCerts(final List<CertStore> certStores, final SignerId sid)
            throws CertStoreException {
        JcaX509CertSelectorConverter converter = new JcaX509CertSelectorConverter();
        return findCerts(certStores, converter.getCertSelector(sid));
    }

    private List<X509Certificate> findCerts(final List<CertStore> certStores,
                                            final X509CertSelector selector)
            throws CertStoreException {
        List<X509Certificate> certificates = new ArrayList<>();
        for (CertStore certStore : certStores) {
            Collection<? extends Certificate> storeCerts = certStore.getCertificates(selector);
            for (Certificate cert : storeCerts) {
                if (cert.getType().equals("X.509")) {
                    certificates.add((X509Certificate) cert);
                }
            }
        }

        return certificates;
    }
}