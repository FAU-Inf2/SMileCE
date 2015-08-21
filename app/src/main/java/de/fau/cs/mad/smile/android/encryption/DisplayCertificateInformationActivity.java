package de.fau.cs.mad.smile.android.encryption;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x500.style.IETFUtils;
import org.spongycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayCertificateInformationActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private String name;
    private String alias;
    private KeyInfo keyInfo;
    private HashMap<String, List<AbstractCertificateInfoItem>> listDataChild;
    private List<String> listDataHeader;
    ExpandableCertificateListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(SMileCrypto.LOG_TAG, "Started DisplayCertificateInformationActivity.");
        Bundle extras = getIntent().getExtras();
        this.alias = extras.getString("Alias");
        if(this.alias == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called without alias.");
            finish();
        }
        Log.d(SMileCrypto.LOG_TAG, "Called with alias: " + alias);
        this.name = extras.getString("Name");

        setContentView(R.layout.activity_display_certificate_information);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.name); //if (name == null) --> set later
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        getKeyInfo();

        //TODO: workaround to prevent crash
        if(listDataHeader == null || listDataChild == null) {
            Log.e(SMileCrypto.LOG_TAG, "ListDataHeader/ListDataChild was null.");
            finish();
        } else {
            listAdapter = new ExpandableCertificateListAdapter(this, listDataHeader, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display_certificate_information, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.e(SMileCrypto.LOG_TAG, "item: " + id);
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if(id == R.id.action_delete) {
            deleteKey(this.keyInfo);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getKeyInfo() {
        try {
            KeyManagement keyManagement = new KeyManagement();
            KeyInfo keyInfo = keyManagement.getKeyInfo(this.alias);
            this.keyInfo = keyInfo;
            extractCertificateInformation(keyInfo);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error: " + e.getMessage());
            showErrorPrompt();
        }
    }

    private void extractCertificateInformation(KeyInfo keyInfo) {
        if(this.name == null) {
            this.name = keyInfo.contact;
            Log.d(SMileCrypto.LOG_TAG, "Name was null, set name to: " + this.name);
            toolbar.setTitle(this.name);
            setSupportActionBar(toolbar);
        }
        generatePersonalInformation(keyInfo);
    }

    private void generatePersonalInformation(KeyInfo keyInfo) {
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.personal));
        listDataChild = new HashMap<>();
        HashMap<String, String> data = new HashMap<>();
        data.put("Name", keyInfo.contact);
        data.put("Email", keyInfo.mail);
        X509Certificate certificate = keyInfo.certificate;
        if(certificate == null) {
            Log.e(SMileCrypto.LOG_TAG, "Certificate was null -- abort.");
            return;
        }
        X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(certificate).getSubject();
            RDN[] L = x500name.getRDNs(BCStyle.L);
            if (L.length > 0) {
                 data.put("L", IETFUtils.valueToString(L[0].getFirst().getValue()));
            }
            RDN[] DC = x500name.getRDNs(BCStyle.DC);
            if (DC.length > 0) {
                data.put("DC", IETFUtils.valueToString(DC[0].getFirst().getValue()));
            }
            RDN[] O = x500name.getRDNs(BCStyle.O);
            if (O.length > 0) {
                data.put("O", IETFUtils.valueToString(O[0].getFirst().getValue()));
            }
            RDN[] OU = x500name.getRDNs(BCStyle.DC);
            if (OU.length > 0) {
                data.put("OU", IETFUtils.valueToString(OU[0].getFirst().getValue()));
            }
            ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
            PersonalInformationItem persI = new PersonalInformationItem();
            persI.build(data);
            pers.add(persI);
            listDataChild.put(listDataHeader.get(0), pers);
        } catch (CertificateEncodingException e) {
            Log.d(SMileCrypto.LOG_TAG, "Error with certificate encoding: " + e.getMessage());
            Toast.makeText(App.getContext(), "Failed to extract personal information.", Toast.LENGTH_SHORT).show();
        }

        listDataHeader.add(getString(R.string.validity));
        HashMap<String, String> validity = new HashMap<>();
        validity.put("Startdate", keyInfo.valid_after.toString());
        validity.put("Enddate", keyInfo.termination_date.toString());
        ArrayList<AbstractCertificateInfoItem> val = new ArrayList<>();
        ValidityItem validityItem  = new ValidityItem();
        validityItem.build(validity);
        val.add(validityItem);
        listDataChild.put(listDataHeader.get(1), val);

        listDataHeader.add(getString(R.string.certificate));
        HashMap<String, String> certificateInfo = new HashMap<>();
        certificateInfo.put("Thumbprint", keyInfo.thumbprint);
        BigInteger serialNumber = keyInfo.certificate.getSerialNumber();
        certificateInfo.put("Serial number", serialNumber.toString(16));
        certificateInfo.put("Version", Integer.toString(keyInfo.certificate.getVersion()));
        ArrayList<AbstractCertificateInfoItem> cert = new ArrayList<>();
        CertificateInformationItem certificateInformationItem = new CertificateInformationItem();
        certificateInformationItem.build(certificateInfo);
        cert.add(certificateInformationItem);
        listDataChild.put(listDataHeader.get(2), cert);

        listDataHeader.add(getString(R.string.cryptographic));
        HashMap<String, String> cryptographicInfo = new HashMap<>();
        PublicKey publicKey = keyInfo.certificate.getPublicKey();
        if(publicKey instanceof  RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            String modulus = rsaPublicKey.getModulus().toString(16);
            String exponent = rsaPublicKey.getPublicExponent().toString(16);
            cryptographicInfo.put("Public Key", "RSAPublicKey");
            cryptographicInfo.put("Modulus", modulus);
            cryptographicInfo.put("Exponent", exponent);
            cryptographicInfo.put("Signature Algorithm", keyInfo.certificate.getSigAlgName());
            cryptographicInfo.put("Signature", new BigInteger(keyInfo.certificate.getSignature()).toString(16));
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Not an instance of RSAPublicKey.");
            cryptographicInfo.put("Public Key", keyInfo.certificate.getPublicKey().toString());
            cryptographicInfo.put("Signature Algorithm", keyInfo.certificate.getSigAlgName());
            cryptographicInfo.put("Signature", new BigInteger(keyInfo.certificate.getSignature()).toString(16));
        }
        ArrayList<AbstractCertificateInfoItem> crypto = new ArrayList<>();
        CryptographicInformationItem cryptographicInformationItem = new CryptographicInformationItem();
        cryptographicInformationItem.build(cryptographicInfo);
        crypto.add(cryptographicInformationItem);
        listDataChild.put(listDataHeader.get(3), crypto);

        /*
        * · Personal: Name, Email, SubjectDN
        · Validity: start date, end date
        · Certificate: Version, Serial Number, Thumbprint
        · Issuer: IssuerDN (splitted)
        · Cryptographic: Public Key, Signature algo, Signature */
    }

    private void deleteKey(final KeyInfo keyInfo) {
        final KeyManagement keyManagement;
        try {
            keyManagement = new KeyManagement();
        } catch (Exception e) {
            showErrorPrompt();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (keyInfo.alias.startsWith("SMile_crypto_own")) {
            alertDialogBuilder.setTitle(getString(R.string.alert_header_start) + keyInfo.contact + getString(R.string.alert_header_end));
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_content))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean success = keyManagement.deleteKey(keyInfo.alias);
                            if(success)
                                Toast.makeText(App.getContext(),
                                        R.string.certificate_deleted, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

           alertDialogBuilder.create().show();
        } else {
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_header_start) + keyInfo.contact + getString(R.string.alert_header_end))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean success = keyManagement.deleteKey(keyInfo.alias);
                            if(success)
                                Toast.makeText(App.getContext(),
                                        R.string.certificate_deleted, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    }

    private void showErrorPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCertificateInformationActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        Log.e(SMileCrypto.LOG_TAG, "EXIT_STATUS: " + SMileCrypto.EXIT_STATUS);
        builder.setMessage(getResources().getString(R.string.internal_error));
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}