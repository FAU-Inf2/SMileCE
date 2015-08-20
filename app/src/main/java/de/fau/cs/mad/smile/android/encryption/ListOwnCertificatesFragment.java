package de.fau.cs.mad.smile.android.encryption;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Enumeration;

import de.fau.cs.mad.smile_crypto.R;

public class ListOwnCertificatesFragment extends Fragment {
    public ListOwnCertificatesFragment() {
    }

    private ImageButton fab;

    private boolean expanded = false;

    private View fabAction1;
    private View fabAction2;

    private float offset1;
    private float offset2;

    private KeyAdapter keyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.keylist, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.keyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<KeyInfo> keylist = findCerts();
        keyAdapter = new KeyAdapter(keylist);
        recyclerView.setAdapter(keyAdapter);
        final ViewGroup fabContainer = (ViewGroup) rootView.findViewById(R.id.fab_container);
        fab = (ImageButton) rootView.findViewById(R.id.fab);
        fabAction1 = rootView.findViewById(R.id.fab_action_1);
        fabAction2 = rootView.findViewById(R.id.fab_action_2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                if (expanded) {
                    expandFab();
                } else {
                    collapseFab();
                }
            }
        });
        fabAction1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                collapseFab();
                expanded = false;
                Intent i = new Intent(getActivity(), ImportCertificateActivity.class);
                startActivity(i);
            }
        });

        fabAction2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                collapseFab();
                expanded = false;
                try {
                    new SelfSignedCertificateCreator().create();
                    keyAdapter.addKey(findCerts());
                } catch (OperatorCreationException | IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | InvalidKeySpecException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                    Log.e(SMileCrypto.LOG_TAG, "Error while importing certificate: " + e.getMessage());
                    Toast.makeText(getActivity(), R.string.error + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        fabContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = fab.getY() - fabAction1.getY();
                fabAction1.setTranslationY(offset1);
                offset2 = fab.getY() - fabAction2.getY();
                fabAction2.setTranslationY(offset2);
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    // FIXME: duplicate code to KeyManagement.getAllCertificates
    private ArrayList<KeyInfo> findCerts() {
        ArrayList<KeyInfo> keylist = new ArrayList<>();
        try {
            Log.d(SMileCrypto.LOG_TAG, "Find certs…");
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(SMileCrypto.LOG_TAG, "Found certificate with alias: " + alias);
                if(alias.equals(getString(R.string.smile_save_passphrases_certificate_alias)))
                    continue;
                Certificate c = ks.getCertificate(alias);
                KeyStore.Entry entry = ks.getEntry(alias, null);
                if (entry instanceof KeyStore.PrivateKeyEntry) {
                    KeyInfo ki = new KeyInfo();
                    //((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    ki.alias = alias;
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                    ki.type = c.getType();
                    ki.hash = Integer.toHexString(c.hashCode());
                    if(c.getType().equals("X.509")) {
                        X509Certificate cert = (X509Certificate) c;
                        ki.contact = cert.getSubjectX500Principal().getName();
                        //keyInfo.mail; TODO
                        ki.termination_date = new DateTime(cert.getNotAfter());
                        //keyInfo.trust; TODO
                    }
                    keylist.add(ki);
                } else {
                    //--> no private key available for this certificate
                    //currently there are no such entries because yet we cannot import the certs of
                    //others, e.g. by using their signature.
                    Log.d(SMileCrypto.LOG_TAG, "Not an instance of a PrivateKeyEntry");
                    Log.d(SMileCrypto.LOG_TAG, "· Type: " + c.getType());
                    Log.d(SMileCrypto.LOG_TAG, "· HashCode: " + c.hashCode());
                }
            }
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error while finding certificate: " + e.getMessage());
        }
        return keylist;
    }

    private void collapseFab() {
        fab.setImageResource(R.drawable.animated_minus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(fabAction1, offset1),
                createCollapseAnimator(fabAction2, offset2));
        animatorSet.start();
        animateFab();
    }

    private void expandFab() {
        fab.setImageResource(R.drawable.animated_plus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(fabAction1, offset1),
                createExpandAnimator(fabAction2, offset2));
        animatorSet.start();
        animateFab();
    }

    private static final String TRANSLATION_Y = "translationY";

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_Y, offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    private void animateFab() {
        Drawable drawable = fab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }
}