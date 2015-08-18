package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class ImportCertificateActivity extends ActionBarActivity {
    private Toolbar toolbar;
    protected final int FILE_CHOOSER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_certificate);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_import_certificate); //TODO
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(SMileCrypto.LOG_TAG, "Started ImportCertificateActivity.");
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if(Intent.ACTION_VIEW.equals(action)){
            Log.d(SMileCrypto.LOG_TAG, "Intent contains path to file.");
            Uri uri = intent.getData();
            String path = PathConverter.getPath(this, uri);
            TextView textView = (TextView)findViewById(R.id.import_text_view);
            textView.setText(getString(R.string.import_certificate_show_path) + path);
            Log.d(SMileCrypto.LOG_TAG, "Path to file is " + path);
            handleFile(path);
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Intent was something else: " + action);
            Log.d(SMileCrypto.LOG_TAG, "Show file chooser.");
            showFileChooser();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        finish();
        return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/x-x509-user-cert", "application/x-x509-ca-cert"};
        //TODO: more file types: pem? der?
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose certificate file.");
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.fab_select_import_certificate)),
                    FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
            builder.setTitle(getResources().getString(R.string.error));
            builder.setMessage(getResources().getString(R.string.no_file_manager));

            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_CHOOSER_REQUEST_CODE:
                //receive result from file manager (--> uri of certificate)
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = PathConverter.getPath(this, uri);
                    Log.d(SMileCrypto.LOG_TAG, "Path to selected certificate: " + path);
                    TextView textView = (TextView) this.findViewById(R.id.import_text_view);
                    textView.setText(getString(R.string.import_certificate_show_path) + path);
                    handleFile(path);
                } else {
                    noFileSelected();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void noFileSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(getResources().getString(R.string.import_certificate_no_file));

        builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showFileChooser();
            }
        });
        builder.create().show();
    }

    private void handleFile(String pathToFile) {
        Log.d(SMileCrypto.LOG_TAG, "Handle.");

        X509Certificate certificate = getCertificate(pathToFile);
        Boolean success = KeyManagement.addFriendsCertificate(certificate);
        if(success) {
            importSuccessful();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
            builder.setTitle(getResources().getString(R.string.error));
            builder.setMessage(getResources().getString(R.string.internal_error));

            builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    showFileChooser();
                }
            });
            builder.create().show();
        }
    }

    private X509Certificate getCertificate(String pathToFile) {
        try {
            File file = new File(pathToFile);
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            X509Certificate x509cert;
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bias = new ByteArrayInputStream(bytes);
            x509cert = (X509Certificate) factory.generateCertificate(bias);
            return x509cert;
        } catch (CertificateException e) {
            Log.e(SMileCrypto.LOG_TAG, e.getMessage());
            return null;
        }
    }

    private void importSuccessful() {
        Log.d(SMileCrypto.LOG_TAG, "Exit-Status: " + SMileCrypto.EXIT_STATUS);
        if (SMileCrypto.EXIT_STATUS == SMileCrypto.STATUS_CERTIFICATE_ALREADY_IMPORTED) {
            AlertDialog.Builder builderImported = new AlertDialog.Builder(ImportCertificateActivity.this);
            builderImported.setTitle(getResources().getString(R.string.info));
            builderImported.setMessage(getResources().getString(R.string.certificate_already_imported));

            builderImported.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builderImported.create().show();
        } else {
            AlertDialog.Builder builderImported = new AlertDialog.Builder(ImportCertificateActivity.this);
            builderImported.setTitle(getResources().getString(R.string.info));
            builderImported.setMessage(getResources().getString(R.string.import_certificate_successful));

            builderImported.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builderImported.create().show();
        }
    }

}
