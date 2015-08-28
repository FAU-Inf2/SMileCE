package de.fau.cs.mad.smile.android.encryption.remote;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.spongycastle.cms.CMSException;
import org.spongycastle.mail.smime.SMIMEException;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.x509.CertPathReviewerException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.remote.operation.CryptoOperation;
import de.fau.cs.mad.smime_api.ISMimeService;
import de.fau.cs.mad.smime_api.SMimeApi;
import korex.mail.MessagingException;

public class SMimeService extends Service {

    private final ISMimeService.Stub mBinder = new ISMimeService.Stub() {
        @Override
        public Intent execute(Intent data, ParcelFileDescriptor input, ParcelFileDescriptor output) throws RemoteException {
            return processRequest(data, input, output);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Intent processRequest(final Intent data, final ParcelFileDescriptor input, final ParcelFileDescriptor output) {
        Intent result = new Intent();
        result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
        CryptoOperation operation = null;
        String action = data.getAction();

        try {
            CryptoOperationBuilder cryptoOperationBuilder = new CryptoOperationBuilder();
            cryptoOperationBuilder.setData(data);
            cryptoOperationBuilder.setInput(input);
            cryptoOperationBuilder.setOutput(output);

            switch (action) {
                case SMimeApi.ACTION_SIGN:
                    operation = cryptoOperationBuilder.createSignOperation();
                    break;
                case SMimeApi.ACTION_ENCRYPT:
                    operation = cryptoOperationBuilder.createEncryptOperation();
                    break;
                case SMimeApi.ACTION_ENCRYPT_AND_SIGN:
                    operation = cryptoOperationBuilder.createSignAndEncryptOperation();
                    break;
                case SMimeApi.ACTION_DECRYPT_VERIFY:
                    operation = cryptoOperationBuilder.createDecryptAndVerifyOperation();
                    break;
                case SMimeApi.ACTION_VERIFY:
                    operation = cryptoOperationBuilder.createVerifyOperation();
                    break;
            }

            if (operation != null) {
                operation.execute();
                result = operation.getResult();
            }
        } catch (IOException | GeneralSecurityException | CertPathReviewerException | CMSException |
                OperatorCreationException | SMIMEException | ExecutionException | InterruptedException | MessagingException e) {
            result.putExtra(SMimeApi.EXTRA_RESULT_CODE, SMimeApi.RESULT_CODE_ERROR);
            Log.e(SMileCrypto.LOG_TAG, "Exception while doing crypto stuff", e);
        } finally {
            if (operation != null) {
                try {
                    operation.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

}
