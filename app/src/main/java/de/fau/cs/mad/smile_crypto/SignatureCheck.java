package de.fau.cs.mad.smile_crypto;

import android.util.Base64;
import android.util.Log;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cms.CMSProcessable;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataParser;
import org.spongycastle.cms.CMSTypedStream;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;
import org.spongycastle.util.Store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class SignatureCheck {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public SignatureCheck() {
    }

    public Boolean checkSignature(String pathToFile) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Check signature for file: " + pathToFile);
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            File file = new File(pathToFile);
            MimeMessage mimeMessage = new MimeMessage(session, new FileInputStream(file));
            //Log.d(SMileCrypto.LOG_TAG, mimeMessage.getContent().toString());
            return checkSignature(mimeMessage);

        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while converting file to MimeMessage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Boolean checkSignature(MimeMessage mimeMessage) {
        SMIMEToolkit toolkit = new SMIMEToolkit(new BcDigestCalculatorProvider());
        try {
            if (toolkit.isSigned(mimeMessage))
                Log.d(SMileCrypto.LOG_TAG, "MimeMessage is signed!");
            else {
                Log.d(SMileCrypto.LOG_TAG, "MimeMessage is NOT signed!");
                return false;
            }

            Log.e(SMileCrypto.LOG_TAG, "----------------\ntry working example");
            workingExample();
            Log.e(SMileCrypto.LOG_TAG, "finished working example\n----------------");

            byte[] signatureWithCert = ("MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIAwggPNMIIC" +
                    "taADAgECAgkAq46Vk5EJgW4wDQYJKoZIhvcNAQELBQAwfTELMAkGA1UEBhMCREUxCzAJBgNVBAgM" +
                    "AkJZMREwDwYDVQQHDAhFcmxhbmdlbjESMBAGA1UECgwJTUFELUZpeG1lMRQwEgYDVQQDDAtGaXgg" +
                    "TXkgTWFpbDEkMCIGCSqGSIb3DQEJARYVZml4bXltYWlsQHQtb25saW5lLmRlMB4XDTE1MDQyODA4" +
                    "MjkyNloXDTE2MDQyNzA4MjkyNlowfTELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJZMREwDwYDVQQH" +
                    "DAhFcmxhbmdlbjESMBAGA1UECgwJTUFELUZpeG1lMRQwEgYDVQQDDAtGaXggTXkgTWFpbDEkMCIG" +
                    "CSqGSIb3DQEJARYVZml4bXltYWlsQHQtb25saW5lLmRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A" +
                    "MIIBCgKCAQEAwrN63G5RzZXerqI/V+Qvapu6yBA4TuiGOhMPCqYm7//5/J6+jiLrvEJizsNCiiEG" +
                    "h9bn8Nqw94F/o7YOqDJCkInZyRh0Jw8hBMEtDXmiEcD2W2D2r9/g+0pa6G683WpQQPnhbSIc52yg" +
                    "Pxipd3NaQ8LD/RHGIj+AH2eF6+evabc167yi/NYvcKunWFVQciN1L5fCF5NsVxNstkPPGQgeuzPX" +
                    "mcDjAATrltFT2Re3FWgzZmERYuVEasj2/PdwX4RvwS009d2s/8e7EGSOnK2o5FDo7CO6yAKWPX4m" +
                    "p/W4PCj4861fWsOx4rZic6NP4VAge2/lwVYNeOlAdmgJMOzR7QIDAQABo1AwTjAdBgNVHQ4EFgQU" +
                    "/v99mI/RcXeNUfFdtOrUAQIRYJIwHwYDVR0jBBgwFoAU/v99mI/RcXeNUfFdtOrUAQIRYJIwDAYD" +
                    "VR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAbP3/IPTDfHAh42QAtQkbb2ed2BAHjEb2xTUD" +
                    "QZMnre08d/raXH74zvf5lEFGru98ggUrQsS/zMLkqAqEO7xNkjUyyjKS2y7uFMUv80EVD9tr4E2Z" +
                    "PiP/NqQ4nLIEGhJExZipzRauTdDgjUrV8O8YN+uy3rtrOYcRIcadzWhdnTwU1Q2KhvBTtURSue+v" +
                    "Mkjos8gzwEnTaxoseWsoj68Z6hL65BJU2cg5bYblvLehHlrtznwCt3fzV8vMyA9HkdXVjlpiVfih" +
                    "4mwgWR2sUtobinpMIPaVRuqbM56DFxQfw8MLe/xQ/6QxXUeya0Py9kfU4q26nWGzViQjdeQPSM8S" +
                    "DwAAMYICETCCAg0CAQEwgYowfTELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJZMREwDwYDVQQHDAhF" +
                    "cmxhbmdlbjESMBAGA1UECgwJTUFELUZpeG1lMRQwEgYDVQQDDAtGaXggTXkgTWFpbDEkMCIGCSqG" +
                    "SIb3DQEJARYVZml4bXltYWlsQHQtb25saW5lLmRlAgkAq46Vk5EJgW4wCQYFKw4DAhoFAKBdMBgG" +
                    "CSqGSIb3DQEJAzELBgkqhkiG9w0BBwEwHAYJKoZIhvcNAQkFMQ8XDTE1MDgxMTE5MzY0OFowIwYJ" +
                    "KoZIhvcNAQkEMRYEFMAgEOsfsAuBZ5HAovClnUx0CxOMMA0GCSqGSIb3DQEBAQUABIIBAF+0GPnX" +
                    "WI3t70Ur0olEzUE3o3xwMhd2iM+eR/y8DYWbQWTsUXRk9UOiuDa023A8ovAcqcESwQb8HFRFZ/g5" +
                    "IPEt3xGbvBpODKi4okkxXB2L7Vfavt61tSL8ehO0L63UdoT2+w7wYzlqPUa+N+amZnYDDf8GdmSE" +
                    "eGOXzdJwYkF2IQUhUR+5i5Bl6GQ9Xlz+ckYb+plaSQ6w2WeZR/ASxB1lsF8HwK06KI/q3ATP6uDE" +
                    "F8MiPJFilbOSeJN/Y+IdvFKdD0XeBL1ddiYXYd24zh0JaWdkx3RdKCQmm2fUw5AycRV6l1vqDjM/" +
                    "UxG+BmJ32O4M45bbocfIbBYbU+mXiTUAAAAAAAA=").getBytes();
            byte[] bPlainText = ("ClRoaXMgbWFpbCAgaGFzIGEgc2lnbmF0dXJlLCBwbGVhc2UgdmVyaWZ5Li4uLgoKU2VudCBmcm9t" +
                    "IG15IGFuZHJvaWQgZGV2aWNlLg==").getBytes(); //TODO.. find correct input...

            CMSProcessable cmsProcessableContent = new CMSProcessableByteArray(Base64.decode(bPlainText, 0));
            CMSSignedData signedData = new CMSSignedData(cmsProcessableContent, Base64.decode(signatureWithCert, 0));

            // Extract Certificate + Verify signature
            Store store = signedData.getCertificates();
            SignerInformationStore signers = signedData.getSignerInfos();
            Collection c = signers.getSigners();
            Iterator it = c.iterator();
            Boolean hasValidSigner = false;
            while (it.hasNext()) {
                SignerInformation signer = (SignerInformation) it.next();
                Collection certCollection = store.getMatches(signer.getSID());
                Iterator certIt = certCollection.iterator();
                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
                X509Certificate certFromSignedData = new JcaX509CertificateConverter().setProvider("SC").getCertificate(certHolder);
                Log.d(SMileCrypto.LOG_TAG, "Info from extracted cert: " + certFromSignedData.getSubjectDN().getName());
                KeyManagement.addFriendsCertificate(certFromSignedData);
                Log.d(SMileCrypto.LOG_TAG, "Check signature now…");
                try {
                    if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("SC").build(certFromSignedData))) {
                        Log.d(SMileCrypto.LOG_TAG, "Signature verified!");
                        hasValidSigner = true;
                    } else {
                        Log.d(SMileCrypto.LOG_TAG, "Signature verification failed!");
                    }
                } catch (Exception e) {
                    Log.e(SMileCrypto.LOG_TAG, "Error: " + e.getMessage());
                    Log.e(SMileCrypto.LOG_TAG, "Signature verification failed!");
                }
            }
            return hasValidSigner;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Exception while checking signature: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Boolean workingExample() throws Exception{
        Log.e(SMileCrypto.LOG_TAG, "try hardcoded part......");
            /*
            * example from https://stackoverflow.com/questions/16662408/correct-way-to-sign-and-verify-signature-using-bouncycastle
            * */

        // envelopedData == signature (containing cert etc.)
        String envelopedData = "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAQAAoIAwggLQMIIC" +
                "OQIEQ479uzANBgkqhkiG9w0BAQUFADCBrjEmMCQGCSqGSIb3DQEJARYXcm9zZXR0YW5ldEBtZW5k" +
                "ZWxzb24uZGUxCzAJBgNVBAYTAkRFMQ8wDQYDVQQIEwZCZXJsaW4xDzANBgNVBAcTBkJlcmxpbjEi" +
                "MCAGA1UEChMZbWVuZGVsc29uLWUtY29tbWVyY2UgR21iSDEiMCAGA1UECxMZbWVuZGVsc29uLWUt" +
                "Y29tbWVyY2UgR21iSDENMAsGA1UEAxMEbWVuZDAeFw0wNTEyMDExMzQyMTlaFw0xOTA4MTAxMzQy" +
                "MTlaMIGuMSYwJAYJKoZIhvcNAQkBFhdyb3NldHRhbmV0QG1lbmRlbHNvbi5kZTELMAkGA1UEBhMC" +
                "REUxDzANBgNVBAgTBkJlcmxpbjEPMA0GA1UEBxMGQmVybGluMSIwIAYDVQQKExltZW5kZWxzb24t" +
                "ZS1jb21tZXJjZSBHbWJIMSIwIAYDVQQLExltZW5kZWxzb24tZS1jb21tZXJjZSBHbWJIMQ0wCwYD" +
                "VQQDEwRtZW5kMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+X1g6JvbdwJI6mQMNT41GcycH" +
                "UbwCFWKJ4qHDaHffz3n4h+uQJJoQvc8yLTCfnl109GB0yL2Y5YQtTohOS9IwyyMWBhh77WJtCN8r" +
                "dOfD2DW17877te+NlpugRvg6eOH6np9Vn3RZODVxxTyyJ8pI8VMnn13YeyMMw7VVaEO5hQIDAQAB" +
                "MA0GCSqGSIb3DQEBBQUAA4GBALwOIc/rWMAANdEh/GgO/DSkVMwxM5UBr3TkYbLU/5jg0Lwj3Y++" +
                "KhumYSrxnYewSLqK+JXA4Os9NJ+b3eZRZnnYQ9eKeUZgdE/QP9XE04y8WL6ZHLB4sDnmsgVaTU+p" +
                "0lFyH0Te9NyPBG0J88109CXKdXCTSN5gq0S1CfYn0staAAAxggG9MIIBuQIBATCBtzCBrjEmMCQG" +
                "CSqGSIb3DQEJARYXcm9zZXR0YW5ldEBtZW5kZWxzb24uZGUxCzAJBgNVBAYTAkRFMQ8wDQYDVQQI" +
                "EwZCZXJsaW4xDzANBgNVBAcTBkJlcmxpbjEiMCAGA1UEChMZbWVuZGVsc29uLWUtY29tbWVyY2Ug" +
                "R21iSDEiMCAGA1UECxMZbWVuZGVsc29uLWUtY29tbWVyY2UgR21iSDENMAsGA1UEAxMEbWVuZAIE" +
                "Q479uzAJBgUrDgMCGgUAoF0wGAYJKoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUx" +
                "DxcNMTMwNTIxMDE1MDUzWjAjBgkqhkiG9w0BCQQxFgQU8mE6gw6iudxLUc9379lWK0lUSWcwDQYJ" +
                "KoZIhvcNAQEBBQAEgYB5mVhqJu1iX9nUqfqk7hTYJb1lR/hQiCaxruEuInkuVTglYuyzivZjAR54" +
                "zx7Cfm5lkcRyyxQ35ztqoq/V5JzBa+dYkisKcHGptJX3CbmmDIa1s65mEye4eLS4MTBvXCNCUTb9" +
                "STYSWvr4VPenN80mbpqSS6JpVxjM0gF3QTAhHwAAAAAAAA==";
        String Sig_Bytes = "YduK22AlMLSXV3ajX5r/pX5OQ0xjj58uhGT9I9MvOrz912xNHo+9OiOKeMOD+Ys2/LUW3XaN6T+/" +
                "tuRM5bi4RK7yjaqaJCZWtr/O4I968BQGgt0cyNvK8u0Jagbr9MYk6G7nnejbRXYHyAOaunqD05lW" +
                "U/+g92i18dl0OMc50m4=";

        //Log.e(SMileCrypto.LOG_TAG, "Sig_Bytes = " + new String(Base64.decode(Sig_Bytes, 0)));
        //Log.e(SMileCrypto.LOG_TAG, "envelopedData = " + new String(Base64.decode(envelopedData, 0)));

        CMSProcessable cmsProcessableContent = new CMSProcessableByteArray(Base64.decode(Sig_Bytes.getBytes(), 0));
        CMSSignedData signedData = new CMSSignedData(cmsProcessableContent, Base64.decode(envelopedData.getBytes(), 0));

        // Extract Certificate + Verify signature
        Store store = signedData.getCertificates();
        SignerInformationStore signers = signedData.getSignerInfos();
        Collection c = signers.getSigners();
        Iterator it = c.iterator();
        Boolean hasValidSigner = false;
        while (it.hasNext()) {
            SignerInformation signer = (SignerInformation) it.next();
            Collection certCollection = store.getMatches(signer.getSID());
            Iterator certIt = certCollection.iterator();
            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
            X509Certificate certFromSignedData = new JcaX509CertificateConverter().setProvider("SC").getCertificate(certHolder);
            KeyManagement.addFriendsCertificate(certFromSignedData);
            Log.d(SMileCrypto.LOG_TAG, "Info from extracted cert: " + certFromSignedData.getSubjectDN().getName());
            Log.d(SMileCrypto.LOG_TAG, "Check signature now…");
            try {
                if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("SC").build(certFromSignedData))) {
                    Log.d(SMileCrypto.LOG_TAG, "Signature verified!");
                    hasValidSigner = true;
                } else {
                    Log.d(SMileCrypto.LOG_TAG, "Signature verification failed!");
                }
            } catch (Exception e) {
                Log.e(SMileCrypto.LOG_TAG, "Error: " + e.getMessage());
                Log.e(SMileCrypto.LOG_TAG, "Signature verification failed!");
            }
        }
        //verifySignature(Base64.decode(envelopedData, 0), Base64.decode(Sig_Bytes, 0)); //works!

        return hasValidSigner;
    }

    public void verifySignature(byte[] signedData, byte[] bPlainText) throws Exception {
        InputStream is = new ByteArrayInputStream(bPlainText);
        CMSSignedDataParser sp = new CMSSignedDataParser(new BcDigestCalculatorProvider(), new CMSTypedStream(is), signedData);
        CMSTypedStream signedContent = sp.getSignedContent();

        signedContent.drain();

        //CMSSignedData s = new CMSSignedData(signedData);
        Store certStore = sp.getCertificates();

        SignerInformationStore signers = sp.getSignerInfos();
        Collection c = signers.getSigners();
        Iterator it = c.iterator();
        while (it.hasNext()) {
            SignerInformation signer = (SignerInformation) it.next();
            Collection certCollection = certStore.getMatches(signer.getSID());
            Iterator certIt = certCollection.iterator();
            X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();

            if (!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("SC").build(certHolder))) {
                Log.e(SMileCrypto.LOG_TAG, "FAIL!");
            } else {
                Log.e(SMileCrypto.LOG_TAG, "SUCCESS!");
            }
        }
    }
}