package de.fau.cs.mad.smile_crypto;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.mail.internet.MimeBodyPart;

public class DecryptLocalMailFragment extends Fragment {
    public DecryptLocalMailFragment() {
    }

    public static DecryptLocalMailFragment newInstance(String pathToFile) {
        DecryptLocalMailFragment f = new DecryptLocalMailFragment();

        Bundle b = new Bundle();
        b.putString("pathToFile", pathToFile);
        f.setArguments(b);
        return f;
    }

    public static DecryptLocalMailFragment newInstanceDecryptedContent(String decryptedContent) {
        DecryptLocalMailFragment f = new DecryptLocalMailFragment();

        Bundle b = new Bundle();
        b.putString("decryptedContent", decryptedContent);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.task_layout);
        TextView myText = new TextView(getActivity());

        try {
            if(getArguments().getString("pathToFile").equals(getResources().getString(R.string.not_eml)))
                myText.setText(getArguments().getString("pathToFile"));
            else
                myText.setText(getString(R.string.decrypt_file_show_path) +
                        getArguments().getString("pathToFile"));
        } catch (NullPointerException n) {
            try {
                myText.setText(getArguments().getString("decryptedContent"));
            } catch (Exception e) {
                myText.setText("");
            }
        }
        myText.computeScroll();
        linearLayout.addView(myText);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}