package de.fau.cs.mad.smile.android.encryption.crypto;


import android.util.Log;

import org.joda.time.DateTime;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class SelfSignedCertificateCreator {

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public static int validateName(String name) {
        if(name != null) {
            if(name.length() > 0) {
                if(name.contains("=") ||name.contains(",")) {
                    return SMileCrypto.STATUS_NAME_INVALID_CHARACTER;
                } else {
                    return SMileCrypto.STATUS_NAME_OK;
                }
            } else {
                return SMileCrypto.STATUS_NAME_EMPTY;
            }
        } else {
            return SMileCrypto.STATUS_NO_NAME;
        }
    }

    public static int validateEmail(String email) {
        if(email != null) {
            if(email.length() > 0) {
                if(email.contains("=") || email.contains(",")) {
                    return SMileCrypto.STATUS_EMAIL_INVALID_CHARACTER;
                } else {
                    if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        return SMileCrypto.STATUS_EMAIL_OK;
                    } else {
                        return SMileCrypto.STATUS_EMAIL_INVALID;
                    }
                }
            } else {
                return SMileCrypto.STATUS_EMAIL_EMPTY;
            }
        } else {
            return SMileCrypto.STATUS_NO_EMAIL;
        }
    }

    public static int createCert(String name, String email, String expert, DateTime end , String passphrase) {
        if(passphrase == null || passphrase.length() == 0) {
            return SMileCrypto.STATUS_NO_PASSPHRASE;
        }
        X500Name x500Name;
        if(expert != null && expert.length() > 0) {
            x500Name = new X500Name(expert);
        } else {
            int vname = validateName(name);
            if (vname != SMileCrypto.STATUS_NAME_OK) {
                return vname;
            }

            int vemail = validateEmail(email);
            if (vemail != SMileCrypto.STATUS_EMAIL_OK) {
                return vemail;
            }

            X500NameBuilder nameBuilder = new X500NameBuilder();
            nameBuilder.addRDN(BCStyle.CN, name);
            nameBuilder.addRDN(BCStyle.E, email);
            x500Name = nameBuilder.build();
        }
        SecureRandom random = new SecureRandom();
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA", "SC");
            generator.initialize(spec, random);
            KeyPair pair = generator.generateKeyPair();

            JcaX509v1CertificateBuilder v1CertGen = new JcaX509v1CertificateBuilder(x500Name,
                    BigInteger.valueOf(1), DateTime.now().toDate(), end.toDate(), x500Name,
                    pair.getPublic());
            X509CertificateHolder ch = v1CertGen.build(new JcaContentSignerBuilder("SHA1WithRSA").setProvider("SC").build(pair.getPrivate()));
            X509Certificate c = new JcaX509CertificateConverter().setProvider("SC").getCertificate(ch);
            KeyManagement km = new KeyManagement();
            if(km.addPrivateKeyFromCert(c, pair.getPrivate(), passphrase)) {
                return SMileCrypto.STATUS_SAVED_CERT;
            } else {
                return SMileCrypto.STATUS_FAILED_SAVE_CERT;
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | OperatorCreationException | InvalidAlgorithmParameterException | CertificateException | KeyStoreException | IOException e) {
            Log.d(SMileCrypto.LOG_TAG, "Error creating self signed certificate. " + e.getMessage());
            return SMileCrypto.STATUS_FAILED_SAVE_CERT;
        }
    }
}
