package de.fau.cs.mad.smile.android.encryption.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import de.fau.cs.mad.smile.android.encryption.PathConverter;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.crypto.KeyManagement;

public class ImportCertificateActivity extends ActionBarActivity {
    private Toolbar toolbar;
    protected final int FILE_CHOOSER_REQUEST_CODE = 1;
    private KeyManagement keyManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_certificate);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_import_certificate);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(SMileCrypto.DEBUG) {
            Log.d(SMileCrypto.LOG_TAG, "Started ImportCertificateActivity.");
        }
        final Intent intent = getIntent();
        final String action = intent.getAction();

        try {
            keyManagement = KeyManagement.getInstance();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException e) {
            Toast.makeText(this, R.string.open_keystore_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Intent.ACTION_VIEW.equals(action)) {
            if(SMileCrypto.DEBUG) {
                Log.d(SMileCrypto.LOG_TAG, "Intent contains path to file.");
            }
            Uri uri = intent.getData();
            String path = PathConverter.getPath(this, uri);
            TextView textView = (TextView) findViewById(R.id.import_text_view);
            textView.setText(getString(R.string.import_certificate_show_path) + path);
            if(SMileCrypto.DEBUG) {
                Log.d(SMileCrypto.LOG_TAG, "Path to file is " + path);
            }
            handleFile(path);
        } else {
            if(SMileCrypto.DEBUG) {
                Log.d(SMileCrypto.LOG_TAG, "Intent was something else: " + action);
                Log.d(SMileCrypto.LOG_TAG, "Show file chooser.");
            }
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
        String[] mimeTypes = {"application/x-x509-user-cert", "application/x-x509-ca-cert", "application/x-pkcs12"};
        //TODO: more file types: pem? der?
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        try {
            if(SMileCrypto.DEBUG) {
                Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose certificate file.");
            }
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.fab_select_import_certificate)),
                    FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            }
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
                    if(SMileCrypto.DEBUG) {
                        Log.d(SMileCrypto.LOG_TAG, "Path to selected certificate: " + path);
                    }
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
        if(pathToFile == null) {
            return;
        }

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(pathToFile);
        if (mimeType == null) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "MimeType was null.");
                Log.e(SMileCrypto.LOG_TAG, "Filename was: " + pathToFile);
            }
            importError(getResources().getString(R.string.unknown_filetype));
            return;
        }

        switch (mimeType) {
            case "application/x-pkcs12":
                if(SMileCrypto.DEBUG) {
                    Log.d(SMileCrypto.LOG_TAG, "File is a .p12-file, show passphrase prompt.");
                }
                showPassphrasePrompt(pathToFile);

                break;
            case "application/x-x509-ca-cert":
            case "application/x-x509-user-cert":
                if(SMileCrypto.DEBUG) {
                    Log.d(SMileCrypto.LOG_TAG, "File is a .crt/.cer-file, get certificate.");
                }
                X509Certificate certificate = getCertificate(pathToFile);

                if (certificate == null) {
                    importError(getResources().getString(R.string.error_reading_certificate));
                }

                if (keyManagement.addFriendsCertificate(certificate)) {
                    importSuccessful();
                } else {
                    importError(getResources().getString(R.string.internal_error));
                }
                break;
            default:
                if(SMileCrypto.DEBUG) {
                    Log.e(SMileCrypto.LOG_TAG, "Unknown mime type: " + mimeType);
                    Log.e(SMileCrypto.LOG_TAG, "Filename was: " + pathToFile);
                }
                importError(getResources().getString(R.string.unknown_filetype));
                break;
        }
    }

    private void showPassphrasePrompt(final String pathToFile) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View passphrasePromptView = layoutInflater.inflate(R.layout.passphrase_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(passphrasePromptView);

        final EditText passphraseUserInput = (EditText) passphrasePromptView.
                findViewById(R.id.passphraseUserInput);
        passphraseUserInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passphraseUserInput.setTransformationMethod(new PasswordTransformationMethod());

        alertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.go),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String passphrase = passphraseUserInput.getText().toString();
                        if (!keyManagement.addPrivateKeyFromP12ToKeyStore(pathToFile, passphrase)) {
                            wrongPassphrase(pathToFile);
                        } else {
                            importSuccessful();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //dialog.dismiss();
                                finish();
                            }
                        }
                );
        alertDialogBuilder.create().show();
    }

    private void wrongPassphrase(final String pathToFile) {
        if(SMileCrypto.DEBUG) {
            Log.d(SMileCrypto.LOG_TAG, "Wrong passphrase. Show passphrase prompt again.");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(getResources().getString(R.string.enter_passphrase_wrong) +
                "\n" + getResources().getString(R.string.try_again));

        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showPassphrasePrompt(pathToFile);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        builder.create().show();
    }

    private X509Certificate getCertificate(String pathToFile) {
        try {
            File certificateFile = new File(pathToFile);
            int size = (int) certificateFile.length();
            byte[] certificateBytes = new byte[size];
            X509Certificate x509cert;
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(certificateFile));
                buf.read(certificateBytes, 0, certificateBytes.length);
                buf.close();
            } catch (Exception e) {
                if(SMileCrypto.DEBUG) {
                    Log.e(SMileCrypto.LOG_TAG, "Error reading certificate file.");
                }
                return null;
            }
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bias = new ByteArrayInputStream(certificateBytes);
            x509cert = (X509Certificate) factory.generateCertificate(bias);
            return x509cert;
        } catch (CertificateException e) {
            if(SMileCrypto.DEBUG) {
                Log.e(SMileCrypto.LOG_TAG, "CertificateException: " + e.getMessage());
            }
            return null;
        }
    }

    private void importSuccessful() {
        if(SMileCrypto.DEBUG) {
            Log.d(SMileCrypto.LOG_TAG, "Exit-Status: " + SMileCrypto.EXIT_STATUS);
        }
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

    private void importError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(message);

        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                showFileChooser();
            }
        });
        builder.setNegativeButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}
