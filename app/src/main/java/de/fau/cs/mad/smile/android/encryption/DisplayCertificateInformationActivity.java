package de.fau.cs.mad.smile.android.encryption;

import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.FileOutputStream;
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
import java.util.Locale;

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
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if(id == R.id.action_delete) {
            deleteKey(this.keyInfo);
        } else if (id == R.id.action_export) {
            exportCertificate();
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
        Log.d(SMileCrypto.LOG_TAG, "Setting personal information");
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.personal));
        listDataChild = new HashMap<>();
        HashMap<String, String> data = new HashMap<>();
        X509Certificate certificate = keyInfo.certificate;
        if(certificate == null) {
            Log.e(SMileCrypto.LOG_TAG, "Certificate was null -- abort.");
            return;
        }
        X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(certificate).getSubject();
            parseX500Name(data, x500name);
            ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
            PersonalInformationItem persI = new PersonalInformationItem();
            persI.build(data);
            pers.add(persI);
            listDataChild.put(listDataHeader.get(0), pers);
        } catch (CertificateEncodingException e) {
            Log.d(SMileCrypto.LOG_TAG, "Error with certificate encoding: " + e.getMessage());
            Toast.makeText(App.getContext(), "Failed to extract personal information.", Toast.LENGTH_SHORT).show();
        }
        listDataHeader.add(getString(R.string.CA));
        HashMap<String, String> cadata = new HashMap<>();

        try {
            x500name = new JcaX509CertificateHolder(certificate).getIssuer();
            parseX500Name(cadata, x500name);
            ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
            PersonalInformationItem persI = new PersonalInformationItem();
            persI.build(cadata);
            pers.add(persI);
            listDataChild.put(listDataHeader.get(1), pers);
        } catch (CertificateEncodingException e) {
            Log.d(SMileCrypto.LOG_TAG, "Error with certificate encoding: " + e.getMessage());
            Toast.makeText(App.getContext(), "Failed to extract personal information.", Toast.LENGTH_SHORT).show();
        }

        Log.d(SMileCrypto.LOG_TAG, "Setting validity information");
        listDataHeader.add(getString(R.string.validity));
        HashMap<String, String> validity = new HashMap<>();
        validity.put("Startdate", keyInfo.valid_after.toString());
        validity.put("Enddate", keyInfo.termination_date.toString());
        ArrayList<AbstractCertificateInfoItem> val = new ArrayList<>();
        ValidityItem validityItem  = new ValidityItem();
        validityItem.build(validity);
        val.add(validityItem);
        listDataChild.put(listDataHeader.get(2), val);


        Log.d(SMileCrypto.LOG_TAG, "Setting certificate information");
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
        listDataChild.put(listDataHeader.get(3), cert);

        Log.d(SMileCrypto.LOG_TAG, "Setting cryptographic information");
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
        listDataChild.put(listDataHeader.get(4), crypto);

        /*
        * · Personal: Name, Email, SubjectDN
        · Validity: start date, end date
        · Certificate: Version, Serial Number, Thumbprint
        · Issuer: IssuerDN (splitted)
        · Cryptographic: Public Key, Signature algo, Signature */
    }

    private void parseX500Name(HashMap<String, String> data, X500Name x500name) {
        RDN[] Name = x500name.getRDNs(BCStyle.CN);
        if (Name.length > 0) {
            data.put("Name", IETFUtils.valueToString(Name[0].getFirst().getValue()));
        }
        RDN[] mail = x500name.getRDNs(BCStyle.E);
        if (mail.length > 0) {
            data.put("Email", IETFUtils.valueToString(mail[0].getFirst().getValue()));
        }
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
        RDN[] OU = x500name.getRDNs(BCStyle.OU);
        if (OU.length > 0) {
            data.put("OU", IETFUtils.valueToString(OU[0].getFirst().getValue()));
        }
        RDN[] business = x500name.getRDNs(BCStyle.BUSINESS_CATEGORY);
        if (business.length > 0) {
            data.put("business", IETFUtils.valueToString(business[0].getFirst().getValue()));
        }
        RDN[] C = x500name.getRDNs(BCStyle.C);
        if (C.length > 0) {
            String iso2country = IETFUtils.valueToString(C[0].getFirst().getValue());
            data.put("C", new Locale("en", iso2country).getDisplayCountry());
        }
        RDN[] COF = x500name.getRDNs(BCStyle.COUNTRY_OF_CITIZENSHIP);
        if (COF.length > 0) {
            data.put("COF", IETFUtils.valueToString(COF[0].getFirst().getValue()));
        }
        RDN[] COR = x500name.getRDNs(BCStyle.COUNTRY_OF_RESIDENCE);
        if (COR.length > 0) {
            data.put("COR", IETFUtils.valueToString(COR[0].getFirst().getValue()));
        }
        RDN[] DOB = x500name.getRDNs(BCStyle.DATE_OF_BIRTH);
        if (DOB.length > 0) {
            data.put("DOB", IETFUtils.valueToString(DOB[0].getFirst().getValue()));
        }
        RDN[] DMD = x500name.getRDNs(BCStyle.DMD_NAME);
        if (DMD.length > 0) {
            data.put("DMD", IETFUtils.valueToString(DMD[0].getFirst().getValue()));
        }
        RDN[] DNQ = x500name.getRDNs(BCStyle.DN_QUALIFIER);
        if (DNQ.length > 0) {
            data.put("DNQ", IETFUtils.valueToString(DNQ[0].getFirst().getValue()));
        }
        RDN[] gender = x500name.getRDNs(BCStyle.GENDER);
        if (gender.length > 0) {
            data.put("gender", IETFUtils.valueToString(gender[0].getFirst().getValue()));
        }
        RDN[] gen = x500name.getRDNs(BCStyle.GENERATION);
        if (gen.length > 0) {
            data.put("gen", IETFUtils.valueToString(gen[0].getFirst().getValue()));
        }
        RDN[] GN = x500name.getRDNs(BCStyle.GIVENNAME);
        if (GN.length > 0) {
            data.put("GN", IETFUtils.valueToString(GN[0].getFirst().getValue()));
        }
        RDN[] INIT = x500name.getRDNs(BCStyle.INITIALS);
        if (INIT.length > 0) {
            data.put("INIT", IETFUtils.valueToString(INIT[0].getFirst().getValue()));
        }
        RDN[] name = x500name.getRDNs(BCStyle.NAME);
        if (name.length > 0) {
            data.put("name", IETFUtils.valueToString(name[0].getFirst().getValue()));
        }
        RDN[] NAB = x500name.getRDNs(BCStyle.NAME_AT_BIRTH);
        if (NAB.length > 0) {
            data.put("NAB", IETFUtils.valueToString(NAB[0].getFirst().getValue()));
        }
        RDN[] POB = x500name.getRDNs(BCStyle.PLACE_OF_BIRTH);
        if (POB.length > 0) {
            data.put("POB", IETFUtils.valueToString(POB[0].getFirst().getValue()));
        }
        RDN[] POA = x500name.getRDNs(BCStyle.POSTAL_ADDRESS);
        if (POA.length > 0) {
            data.put("POA", IETFUtils.valueToString(POA[0].getFirst().getValue()));
        }
        RDN[] POC = x500name.getRDNs(BCStyle.POSTAL_CODE);
        if (POC.length > 0) {
            data.put("POC", IETFUtils.valueToString(POC[0].getFirst().getValue()));
        }
        RDN[] pseudonym = x500name.getRDNs(BCStyle.PSEUDONYM);
        if (pseudonym.length > 0) {
            data.put("pseudonym", IETFUtils.valueToString(pseudonym[0].getFirst().getValue()));
        }
        RDN[] SN = x500name.getRDNs(BCStyle.SN);
        if (SN.length > 0) {
            data.put("SN", IETFUtils.valueToString(SN[0].getFirst().getValue()));
        }
        RDN[] ST = x500name.getRDNs(BCStyle.ST);
        if (ST.length > 0) {
            data.put("ST", IETFUtils.valueToString(ST[0].getFirst().getValue()));
        }
        RDN[] street = x500name.getRDNs(BCStyle.STREET);
        if (street.length > 0) {
            data.put("street", IETFUtils.valueToString(street[0].getFirst().getValue()));
        }
        RDN[] surname = x500name.getRDNs(BCStyle.SURNAME);
        if (surname.length > 0) {
            data.put("surname", IETFUtils.valueToString(surname[0].getFirst().getValue()));
        }
        RDN[] title = x500name.getRDNs(BCStyle.T);
        if (title.length > 0) {
            data.put("title", IETFUtils.valueToString(title[0].getFirst().getValue()));
        }
        RDN[] TEL = x500name.getRDNs(BCStyle.TELEPHONE_NUMBER);
        if (TEL.length > 0) {
            data.put("TEL", IETFUtils.valueToString(TEL[0].getFirst().getValue()));
        }
        RDN[] UID = x500name.getRDNs(BCStyle.UID);
        if (UID.length > 0) {
            data.put("UID", IETFUtils.valueToString(UID[0].getFirst().getValue()));
        }
        RDN[] UI = x500name.getRDNs(BCStyle.UNIQUE_IDENTIFIER);
        if (UI.length > 0) {
            data.put("UI", IETFUtils.valueToString(UI[0].getFirst().getValue()));
        }
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

    private void exportCertificate() {
        Log.d(SMileCrypto.LOG_TAG, "Try to export certificate.");
        if(this.alias.contains("_own_")) {
            exportOwnCertificate();
        } else if(this.alias.contains("_other_")) {
            exportOtherCertificate();
        } else {
            //this should not happen
            Log.e(SMileCrypto.LOG_TAG, "Tried to export certificate with invalid alias: " + alias);
        }
    }

    private void exportOwnCertificate() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.alert_header_export));
        alertDialogBuilder
                    .setMessage(getString(R.string.alert_export))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.export), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String dst = KeyManagement.copyP12ToSDCard(alias);
                            if (dst == null) {
                                Toast.makeText(App.getContext(),
                                        getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(App.getContext(),
                                        getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alertDialogBuilder.create().show();
        }

    private void exportOtherCertificate() {
        String dst = KeyManagement.copyCertificateToSDCard(keyInfo.certificate, alias);
        if (dst == null) {
            Toast.makeText(App.getContext(),
                    getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(App.getContext(),
                    getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
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