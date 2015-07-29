package de.fau.cs.mad.smile_crypto;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImportCertificateFragment extends Fragment {
    public ImportCertificateFragment() {
    }

    public static ImportCertificateFragment newInstance(String pathToFile) {
        ImportCertificateFragment f = new ImportCertificateFragment();

        Bundle b = new Bundle();
        b.putString("pathToFile", pathToFile);
        f.setArguments(b);
        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.task_layout);
        TextView myText = new TextView(getActivity());

        try {
            myText.setText(getString(R.string.import_certificate_show_path) +
                    getArguments().getString("pathToFile"));
        } catch (NullPointerException n) {
            myText.setText(getString(R.string.import_certificate_todo));
        }
        linearLayout.addView(myText);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
}