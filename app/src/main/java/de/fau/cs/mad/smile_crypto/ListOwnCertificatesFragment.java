package de.fau.cs.mad.smile_crypto;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class ListOwnCertificatesFragment extends Fragment {
    public ListOwnCertificatesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.task_layout);
        TextView myText = new TextView(getActivity());

        myText.setText(findCerts());
        linearLayout.addView(myText);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private String findCerts() {
        try {
            String result = "All Certificates: ";
            Log.d(SMileCrypto.LOG_TAG, "Find certs…");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                result += "\n Alias: " + alias;
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                Certificate c = ks.getCertificate(alias);
                Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                result += "\n\t · Type: " + c.getType();
                result += "\n\t · HashCode: " + c.hashCode();
            }
            return result;
        } catch (Exception e){
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
            return null;
        }
    }
}