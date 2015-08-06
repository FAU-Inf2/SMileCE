package de.fau.cs.mad.smile_crypto;


import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.crypto.params.RSAKeyParameters;
import org.spongycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.spongycastle.jce.X509KeyUsage;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;

public class SelfSignedCertificateCreator {

    private X509v1CertificateBuilder v1CertGen;
    private ContentSigner sigGen;
    private PrivateKey key;

    public SelfSignedCertificateCreator() throws OperatorCreationException, IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        RSAKeyParameters lwPubKey = new RSAKeyParameters(
                false,
                new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
                new BigInteger("11", 16));


        RSAPrivateCrtKeyParameters lwPrivKey = new RSAPrivateCrtKeyParameters(
                new BigInteger("b4a7e46170574f16a97082b22be58b6a2a629798419be12872a4bdba626cfae9900f76abfb12139dce5de56564fab2b6543165a040c606887420e33d91ed7ed7", 16),
                new BigInteger("11", 16),
                new BigInteger("9f66f6b05410cd503b2709e88115d55daced94d1a34d4e32bf824d0dde6028ae79c5f07b580f5dce240d7111f7ddb130a7945cd7d957d1920994da389f490c89", 16),
                new BigInteger("c0a0758cdf14256f78d4708c86becdead1b50ad4ad6c5c703e2168fbf37884cb", 16),
                new BigInteger("f01734d7960ea60070f1b06f2bb81bfac48ff192ae18451d5e56c734a5aab8a5", 16),
                new BigInteger("b54bb9edff22051d9ee60f9351a48591b6500a319429c069a3e335a1d6171391", 16),
                new BigInteger("d3d83daf2a0cecd3367ae6f8ae1aeb82e9ac2f816c6fc483533d8297dd7884cd", 16),
                new BigInteger("b8f52fc6f38593dabb661d3f50f8897f8106eee68b1bce78a95b132b4e5b5d19", 16));
        KeyFactory kf = KeyFactory.getInstance("RSA", "SC");
        key = kf.generatePrivate((KeySpec) lwPrivKey);
        sigGen= new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(lwPrivKey);
        byte[] publickeyb = sigAlgId.getEncoded();
        ASN1Sequence seq = (ASN1Sequence) ASN1Sequence.fromByteArray(publickeyb);
        SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(seq);
        Date startDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000);
        v1CertGen = new X509v1CertificateBuilder(
                new X500Name("CN=Test"),
                BigInteger.ONE,
                startDate, endDate,
                new X500Name("CN=Test"),
                subPubKeyInfo);
    }

    public void create() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        X509CertificateHolder ch = v1CertGen.build(sigGen);
        X509Certificate c = new JcaX509CertificateConverter().setProvider( "SC" ).getCertificate(ch);
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        long importTime = System.currentTimeMillis();
        String alias = "SMile_crypto_selfsigned" + Long.toString(importTime); //TODO: other alias?
        ks.setKeyEntry(alias, key, null, new Certificate[] { c });
    }
}
