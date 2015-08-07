package de.fau.cs.mad.smile_crypto.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.io.OutputStream;

import de.fau.cs.mad.smime_api.ISMimeService;
import de.fau.cs.mad.smime_api.SMimeApi;

public class SMimeService extends Service {

    private final ISMimeService.Stub mBinder = new ISMimeService.Stub() {
        @Override
        public Intent execute(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws RemoteException {
            String action = data.getAction();
            switch (action) {
                case SMimeApi.ACTION_SIGN:
                    sign(data, input, output);
                    break;
                case SMimeApi.ACTION_SIGN_AND_ENCRYPT:
                    signAndEncrypt(data, input, output);
                    break;
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    decryptAndVerify(data, input, output);
                    break;
                default:
                    return null;
            }
            return null;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final Intent decryptAndVerify(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        inputStream = new ParcelFileDescriptor.AutoCloseInputStream(input);
        outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(output);

        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_SUCCESS);
        return result;
    }

    private final Intent sign(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }

    private final Intent signAndEncrypt(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        return null;
    }
}
