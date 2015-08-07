package de.fau.cs.mad.smime_api;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SMimeApi {
    public static final String TAG = "SMIME API";
    public static final String SERVICE_INTENT = "de.fau.cs.mad.ISMimeService";
    public static final int API_VERSION = 1;
    public static final String ACTION_SIGN = "de.fau.cs.mad.action.SIGN";
    public static final String ACTION_ENCRYPT = "de.fau.cs.mad.action.ENCRYPT";
    public static final String ACTION_SIGN_AND_ENCRYPT = "de.fau.cs.mad.action.SIGN_AND_ENCRYPT";
    public static final String ACTION_DECRYPT_VERIFY = "de.fau.cs.mad.action.DECRYPT_VERIFY";
    public static final String EXTRA_INPUT = "de.fau.cs.mad.extra.EXTRA_INPUT";
    public static final String EXTRA_OUTPUT = "de.fau.cs.mad.extra.EXTRA_OUTPUT";
    public static final String EXTRA_SENDER = "de.fau.cs.mad.extra.EXTRA_SENDER";
    public static final String EXTRA_RECIPIENT = "de.fau.cs.mad.extra.EXTRA_RECIPIENT";
    public static final String EXTRA_API_VERSION = "de.fau.cs.mad.extra.API_VERSION";

    public static final int RESULT_CODE_ERROR = 0;
    public static final int RESULT_CODE_SUCCESS = 1;
    public static final String EXTRA_RESULT_ERROR = "de.fau.cs.mad.extra.ERROR";
    public static final String EXTRA_RESULT_CODE = "de.fau.cs.mad.extra.RESULT_COdE";

    public static final Intent signMessage(String senderAddress, ParcelFileDescriptor input, ParcelFileDescriptor output) {
        Intent intent = new Intent(ACTION_SIGN);
        intent.putExtra(EXTRA_SENDER, senderAddress);
        intent.putExtra(EXTRA_INPUT, input);
        intent.putExtra(EXTRA_OUTPUT, output);

        return intent;
    }

    public static final Intent signAndEncryptMessage(String senderAddress, String recipientAddress, ParcelFileDescriptor input, ParcelFileDescriptor output) {
        Intent intent = new Intent(ACTION_SIGN_AND_ENCRYPT);
        intent.putExtra(EXTRA_SENDER, senderAddress);
        intent.putExtra(EXTRA_RECIPIENT, recipientAddress);
        intent.putExtra(EXTRA_INPUT, input);
        intent.putExtra(EXTRA_OUTPUT, output);

        return intent;
    }

    ISMimeService mService;
    Context mContext;

    public SMimeApi(Context context, ISMimeService service) {
        this.mContext = context;
        this.mService = service;
    }

    public interface IOpenPgpCallback {
        void onReturn(final Intent result);
    }

    private class SMimeAsyncTask extends AsyncTask<Void, Integer, Intent> {
        Intent data;
        InputStream is;
        OutputStream os;
        IOpenPgpCallback callback;

        private SMimeAsyncTask(Intent data, InputStream is, OutputStream os, IOpenPgpCallback callback) {
            this.data = data;
            this.is = is;
            this.os = os;
            this.callback = callback;
        }

        @Override
        protected Intent doInBackground(Void... unused) {
            return executeApi(data, is, os);
        }

        protected void onPostExecute(Intent result) {
            callback.onReturn(result);
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void executeApiAsync(Intent data, InputStream is, OutputStream os, IOpenPgpCallback callback) {
        SMimeAsyncTask task = new SMimeAsyncTask(data, is, os, callback);

        // don't serialize async tasks!
        // http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        } else {
            task.execute((Void[]) null);
        }
    }

    /**
     * InputStream and OutputStreams are always closed after operating on them!
     *
     * @param data
     * @param is
     * @param os
     * @return
     */
    public Intent executeApi(Intent data, InputStream is, OutputStream os) {
        ParcelFileDescriptor input = null;
        ParcelFileDescriptor output = null;

        try {
            // always send version from client
            data.putExtra(EXTRA_API_VERSION, SMimeApi.API_VERSION);

            Intent result;

            // pipe the input and output
            if (is != null) {
                input = ParcelFileDescriptorUtil.pipeFrom(is,
                        new ParcelFileDescriptorUtil.IThreadListener() {

                            @Override
                            public void onThreadFinished(Thread thread) {
                                //Log.d(OpenPgpApi.TAG, "Copy to service finished");
                            }
                        }
                );
            }
            if (os != null) {
                output = ParcelFileDescriptorUtil.pipeTo(os,
                        new ParcelFileDescriptorUtil.IThreadListener() {

                            @Override
                            public void onThreadFinished(Thread thread) {
                                //Log.d(OpenPgpApi.TAG, "Service finished writing!");
                            }
                        }
                );
            }

            // blocks until result is ready
            result = mService.execute(data, input, output);

            // set class loader to current context to allow unparcelling
            // of OpenPgpError and OpenPgpSignatureResult
            // http://stackoverflow.com/a/3806769
            result.setExtrasClassLoader(mContext.getClassLoader());

            return result;
        } catch (Exception e) {
            Log.e(SMimeApi.TAG, "Exception in executeApi call", e);
            Intent result = new Intent();
            result.putExtra(EXTRA_RESULT_CODE, RESULT_CODE_ERROR);
            result.putExtra(EXTRA_RESULT_ERROR,
                    new SMimeError(SMimeError.CLIENT_SIDE_ERROR, e.getMessage()));
            return result;
        } finally {
            // close() is required to halt the TransferThread
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.e(SMimeApi.TAG, "IOException when closing ParcelFileDescriptor!", e);
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(SMimeApi.TAG, "IOException when closing ParcelFileDescriptor!", e);
                }
            }
        }
    }


}
