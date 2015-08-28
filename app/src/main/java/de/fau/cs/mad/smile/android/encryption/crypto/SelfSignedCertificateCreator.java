package de.fau.cs.mad.smile.android.encryption.crypto;


import android.support.v4.util.Pair;
import android.util.Log;

import org.spongycastle.asn1.x500.X500Name;
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
import java.util.Date;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;

public class SelfSignedCertificateCreator {

    static {
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private X509v1CertificateBuilder v1CertGen;
    private PrivateKey key;

    public SelfSignedCertificateCreator() throws OperatorCreationException, IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        SecureRandom random = new SecureRandom();
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
        generator.initialize(spec, random);
        KeyPair pair = generator.generateKeyPair();
        key = pair.getPrivate();

        //
        // signers name
        //
        String issuer = "C=AU, O=SMile-crypto, OU=SMile Primary Certificate";

        //
        // subjects name - the same as we are self signed.
        //
        String subject = "C=AU, O=SMile-crypto, OU=SMile Primary Certificate, E=SMile@MAD.de, CN=SMile Group";

        //
        // create the certificate - version 1
        //
        v1CertGen = new JcaX509v1CertificateBuilder(new X500Name(issuer), BigInteger.valueOf(1),
                new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30), new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30 * 12 * 2)),
                new X500Name(subject), pair.getPublic());
    }

    public SelfSignedCertificateCreator(String name) throws OperatorCreationException, IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        SecureRandom random = new SecureRandom();
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "SC");
        generator.initialize(spec, random);
        KeyPair pair = generator.generateKeyPair();
        key = pair.getPrivate();

        //
        // signers name
        //
        String issuer = "C=AU, O=SMile-crypto, OU=SMile Primary Certificate";

        //
        // subjects name - the same as we are self signed.
        //
        String subject = "C=AU, O=SMile-crypto, OU=SMile Primary Certificate, E=SMile@MAD.de, CN=" + name;

        //
        // create the certificate - version 1
        //
        v1CertGen = new JcaX509v1CertificateBuilder(new X500Name(issuer), BigInteger.valueOf(1),
                new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30), new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 30 * 12 * 2)),
                new X500Name(subject), pair.getPublic());
    }

    public void create() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, OperatorCreationException, NoSuchProviderException {
        X509CertificateHolder ch = v1CertGen.build(new JcaContentSignerBuilder("SHA1WithRSA").setProvider("SC").build(key));
        Log.e(SMileCrypto.LOG_TAG, "Holder created");
        X509Certificate c = new JcaX509CertificateConverter().setProvider("SC").getCertificate(ch);
        Log.e(SMileCrypto.LOG_TAG, "Certificate created");
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        Log.e(SMileCrypto.LOG_TAG, "Got KeyStore instance");
        ks.load(null, null);
        Log.e(SMileCrypto.LOG_TAG, "Loaded");
        long importTime = System.currentTimeMillis();
        String alias = "SMile_crypto_selfsigned" + Long.toString(importTime); //TODO: other alias?
        Log.e(SMileCrypto.LOG_TAG, "Alias created: " + alias);
        ks.setKeyEntry(alias, key, null, new Certificate[]{c});
        Log.e(SMileCrypto.LOG_TAG, "KeyEntry set");
    }

    public Pair<PrivateKey, X509Certificate> createForTest() throws OperatorCreationException, CertificateException {
        X509CertificateHolder ch = v1CertGen.build(new JcaContentSignerBuilder("SHA1WithRSA").setProvider("SC").build(key));
        Log.e(SMileCrypto.LOG_TAG, "Holder created");
        X509Certificate c = new JcaX509CertificateConverter().setProvider("SC").getCertificate(ch);
        return Pair.create(key, c);
    }
}
