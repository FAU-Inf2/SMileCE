package de.fau.cs.mad.smile_crypto;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.security.KeyChain;
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
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class ImportCertificateActivity extends ActionBarActivity {

    private Toolbar toolbar;
    protected final int FAB_FILE_CHOOSER_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_certificate);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.navigation_drawer_import_certificate);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.currentFragment, new ImportCertificateFragment()).commit();

        Log.d(SMileCrypto.LOG_TAG, "Started ImportCertificateActivity.");
        showFileChooser();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FAB_FILE_CHOOSER_REQUEST_CODE:
                //receive result from file manager (--> uri of certificate)
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = getPath(this, uri);
                    Log.d(SMileCrypto.LOG_TAG, "Path to selected certificate: " + path);
                    getSupportFragmentManager().beginTransaction().replace(R.id.currentFragment,
                            ImportCertificateFragment.newInstance(path)).commitAllowingStateLoss();
                    //addCertificateToKeyChain(path);
                    showPassphrasePrompt(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/x-pkcs12"); //TODO: more file types?
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            Log.d(SMileCrypto.LOG_TAG, "Call file manager to choose certificate file.");
            startActivityForResult(Intent.createChooser(intent,
                            getResources().getString(R.string.fab_select_import_certificate)),
                    FAB_FILE_CHOOSER_REQUEST_CODE);

        } catch (android.content.ActivityNotFoundException anfe) {
            Log.e(SMileCrypto.LOG_TAG, "No file manager installed. " + anfe.getMessage());
            Toast.makeText(this,
                    getResources().getString(R.string.no_file_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showPassphrasePrompt(final String pathToFile) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View passphrasePromptView = layoutInflater.inflate(R.layout.passphrase_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(passphrasePromptView);

        final EditText passphraseUserInput = (EditText) passphrasePromptView.
                findViewById(R.id.passphraseUserInput);
        passphraseUserInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passphraseUserInput.setTransformationMethod(new PasswordTransformationMethod());

        alertDialogBuilder.setCancelable(false).setNegativeButton(getResources().getString(R.string.go),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String passphrase = passphraseUserInput.getText().toString();
                        if (!checkPassphraseAddCertificates(pathToFile, passphrase)) {
                            Log.d(SMileCrypto.LOG_TAG, "Wrong passphrase. Show passphrase prompt again.");

                            AlertDialog.Builder builder = new AlertDialog.Builder(ImportCertificateActivity.this);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage(getResources().getString(R.string.enter_passphrase_wrong) +
                                    "\n" + getResources().getString(R.string.try_again));

                            builder.setPositiveButton(R.string.cancel, null);
                            builder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    showPassphrasePrompt(pathToFile);
                                }
                            });
                            builder.create().show();
                        }
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        alertDialogBuilder.create().show();
    }

    private Boolean checkPassphraseAddCertificates(String pathToFile, String passphrase) {
        try {
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(new FileInputStream(pathToFile), passphrase.toCharArray());
            Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                Log.d(SMileCrypto.LOG_TAG, "· SubjectDN: " + c.getSubjectDN().getName());
                Log.d(SMileCrypto.LOG_TAG, "· IssuerDN: " + c.getIssuerDN().getName());
                addCertificateToKeyStore(c);
            }
            return true;
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while loading keyStore: " + e.getMessage());
            return false;
        }
    }

    private void addCertificateToKeyStore(X509Certificate c) {
        try {
            Log.d(SMileCrypto.LOG_TAG, "Import certificate to keyStore.");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ks.setCertificateEntry("myCertAlias", c); //TODO: set alias

            Toast.makeText(this, R.string.import_certificate_successful, Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(this, R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addCertificateToKeyChain(String pathToFile) {
        try {
            File file = new File(pathToFile);
            byte[] p12 = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(p12);
            fileInputStream.close();

            Log.d(SMileCrypto.LOG_TAG, "Import certificate to keychain.");
            Intent importKey = KeyChain.createInstallIntent();
            importKey.putExtra(KeyChain.EXTRA_PKCS12, p12);
            startActivity(importKey);

        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
            Toast.makeText(this, R.string.error + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /*The following methods are from FileUtils.java at https://github.com/iPaulPro/aFileChooser
    *
    * Project is under Apache License 2.0
    *
    * found on http://stackoverflow.com/a/20559175/2319481
    *
    * TODO: remove unused parts
    * */

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}