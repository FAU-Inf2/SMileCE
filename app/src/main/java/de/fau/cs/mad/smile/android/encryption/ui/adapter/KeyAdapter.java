package de.fau.cs.mad.smile.android.encryption.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.crypto.KeyManagement;
import de.fau.cs.mad.smile.android.encryption.ui.activity.DisplayCertificateInformationActivity;
import de.fau.cs.mad.smile.android.encryption.ui.listener.DeleteRevealListener;
import de.fau.cs.mad.smile.android.encryption.ui.listener.ExecuteSwipe;
import de.fau.cs.mad.smile.android.encryption.ui.listener.ShareRevealListener;
import de.fau.cs.mad.smile.android.encryption.ui.listener.onClickCreatePopup;
import de.fau.cs.mad.smile.android.encryption.utilities.KeyListCallback;
import de.fau.cs.mad.smile.android.encryption.utilities.Utils;


public class KeyAdapter extends RecyclerSwipeAdapter<KeyAdapter.KeyViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SortedList<KeyInfo> keylist;
    private Activity activity;
    private static SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

    public void switchCards(List<KeyInfo> cardsFiltered) {
        keylist.clear();
        addKey(cardsFiltered);
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "Settings changed: Key " + key);
        }
        if (key.equals("pref_key_direction") || key.equals("pref_key_type")) {
            ArrayList<KeyInfo> kis = new ArrayList<KeyInfo>(keylist.size());
            for (int i = 0; i < getItemCount(); ++i) {
                kis.add(keylist.get(i));
            }
            keylist.clear();
            addKey(kis);
        }
    }

    public static class KeyViewHolder extends RecyclerView.ViewHolder {
        protected TextView mail;
        protected TextView termination_date;
        protected Button contactimage;
        protected TextView header;
        protected ImageView publicKey;
        protected ImageView privateKey;
        protected ImageButton contextButton;
        protected ImageView validCircle0;
        protected ImageView validCircle1;
        protected ImageView validCircle2;
        protected ImageView validCircle3;
        protected ImageView validCircle4;
        protected RelativeLayout view;
        protected SwipeLayout swipe;
        protected RelativeLayout delete;
        protected ImageView delete_icon;
        protected RelativeLayout share;
        protected ImageView share_icon;
        private SwipeLayout.SwipeListener swipeListener;


        public KeyViewHolder(final View itemView) {
            super(itemView);
            mail = (TextView) itemView.findViewById(R.id.email);
            termination_date = (TextView) itemView.findViewById(R.id.valid_until);
            contactimage = (Button) itemView.findViewById(R.id.badge);
            header = (TextView) itemView.findViewById(R.id.card_header);
            publicKey = (ImageView) itemView.findViewById(R.id.publicKey);
            privateKey = (ImageView) itemView.findViewById(R.id.privateKey);
            contextButton = (ImageButton) itemView.findViewById(R.id.context_button);
            validCircle0 = (ImageView) itemView.findViewById(R.id.valid_circle_0);
            validCircle1 = (ImageView) itemView.findViewById(R.id.valid_circle_1);
            validCircle2 = (ImageView) itemView.findViewById(R.id.valid_circle_2);
            validCircle3 = (ImageView) itemView.findViewById(R.id.valid_circle_3);
            validCircle4 = (ImageView) itemView.findViewById(R.id.valid_circle_4);
            view = (RelativeLayout) itemView.findViewById(R.id.card_layout);
            swipe = (SwipeLayout) itemView.findViewById(R.id.swipe);
            delete = (RelativeLayout) itemView.findViewById(R.id.delete);
            share = (RelativeLayout) itemView.findViewById(R.id.share);
            delete_icon = (ImageView) itemView.findViewById(R.id.delete_icon);
            share_icon = (ImageView) itemView.findViewById(R.id.share_icon);
        }
    }

    public KeyAdapter(Activity activity) {
        this(activity, null);
    }

    public KeyAdapter(Activity activity, List<KeyInfo> keyInfoList) {
        this.activity = activity;
        this.keylist = new SortedList<KeyInfo>(KeyInfo.class, new KeyListCallback(this));
        if (keyInfoList != null) {
            addKey(keyInfoList);
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public KeyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.keycard, parent, false);

        return new KeyViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final KeyViewHolder holder, final int position) {
        final KeyInfo keyInfo = keylist.get(position);
        if (keyInfo.getContact().equals("")) {
            holder.header.setText(keyInfo.getMail());
        } else {
            holder.header.setText(keyInfo.getContact());
        }
        holder.mail.setText(keyInfo.getMail());

        if (keyInfo.getHasPrivateKey()) {
            holder.privateKey.setVisibility(View.VISIBLE);
        } else {
            holder.privateKey.setVisibility(View.INVISIBLE);
        }

        if (keyInfo.getTerminationDate() != null && keyInfo.getValidAfter() != null) {
            DateTime valid = keyInfo.getTerminationDate();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
            String displayDate = valid.toString(fmt);
            holder.termination_date.setText(displayDate);
            DateTime today = new DateTime();
            Years years = Years.yearsBetween(today, valid);
            Months months = Months.monthsBetween(today, valid);

            int config = 0;

            if (valid.getMillis() <= today.getMillis()) {
                config = 7;
            } else if (keyInfo.getValidAfter().getMillis() > today.getMillis()) {
                config = 1;
            } else if (years.getYears() > 1 || (years.getYears() == 1 && months.getMonths() > 0)) {
                config = 2;
            } else if (months.getMonths() > 5) {
                config = 3;
            } else if (months.getMonths() > 2) {
                config = 4;
            } else if (months.getMonths() > 0) {
                config = 5;
            } else if (valid.getMillis() > today.getMillis()) {
                config = 6;
            }
            if(config != 0) {
                List<TypedArray> colours = Utils.getMultiTypedArray(App.getContext(), "meaning_colour");
                TypedArray colour = colours.get(config);
                ColorFilter circleFilter1 = Utils.getColorFilter(colour.getString(1));
                ColorFilter circleFilter2 = Utils.getColorFilter(colour.getString(2));
                ColorFilter circleFilter3 = Utils.getColorFilter(colour.getString(3));
                ColorFilter circleFilter4 = Utils.getColorFilter(colour.getString(4));
                ColorFilter circleFilter5 = Utils.getColorFilter(colour.getString(5));
                holder.validCircle0.getBackground().setColorFilter(circleFilter1);
                holder.validCircle1.getBackground().setColorFilter(circleFilter2);
                holder.validCircle2.getBackground().setColorFilter(circleFilter3);
                holder.validCircle3.getBackground().setColorFilter(circleFilter4);
                holder.validCircle4.getBackground().setColorFilter(circleFilter5);
            }
        }

        setContactBadge(keyInfo.getMail(), holder, keyInfo);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, DisplayCertificateInformationActivity.class);
                i.putExtra("Alias", keyInfo.getAlias());
                activity.startActivity(i);
            }
        });

        holder.contextButton.setOnClickListener(new onClickCreatePopup(this, keyInfo));

        holder.swipe.removeSwipeListener(holder.swipeListener);


        holder.swipeListener = new ExecuteSwipe(this, keyInfo);
        holder.swipe.addSwipeListener(holder.swipeListener);
        holder.swipe.addRevealListener(R.id.delete, new DeleteRevealListener());
        holder.swipe.addRevealListener(R.id.share, new ShareRevealListener());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns items position in the list.
     * @param item The Key info to search.
     * @return The index in the list.
     */
    private int getPosition(KeyInfo item) {
        return keylist.indexOf(item);
    }

    @Override
    public int getItemCount() {
        return keylist.size();
    }

    public void addKey(KeyInfo key) {
        if (key != null) {
            keylist.add(key);
        }
    }

    public void addKey(List<KeyInfo> keys) {
        if (keys == null) {
            return;
        }
        keylist.beginBatchedUpdates();
        for (KeyInfo ki : keys) {
            keylist.add(ki);
        }
        keylist.endBatchedUpdates();
    }

    public void removeKey(int position) {
        if (position >= 0 && position < getItemCount()) {
            keylist.removeItemAt(position);
        }
    }

    private void setContactBadge(final String email, KeyViewHolder holder, KeyInfo keyInfo) {
        Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI, Uri.encode(email));
        String name = "";
        String lookUpKey = "";
        String thumb = "";

        ContentResolver contentResolver = App.getContext().getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[]{ContactsContract.Data.LOOKUP_KEY, ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.PHOTO_THUMBNAIL_URI}, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                lookUpKey = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                thumb = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }
        if(SMileCrypto.isDEBUG()) {
            Log.d(SMileCrypto.LOG_TAG, "mail: " + email);
            Log.d(SMileCrypto.LOG_TAG, "thumb: " + thumb);
            Log.d(SMileCrypto.LOG_TAG, "name: " + name);
            Log.d(SMileCrypto.LOG_TAG, "key: " + lookUpKey);
        }

        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUpKey);

        if (thumb != null && thumb.length() > 0) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(App.getContext().getContentResolver(), lookupUri, false);
            Bitmap bmp = Utils.getCroppedBitmap(BitmapFactory.decodeStream(input));
            BitmapDrawable bdrawable = new BitmapDrawable(App.getContext().getResources(), bmp);
            holder.contactimage.setBackground(bdrawable);
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Thumbnail found.");
            }
        } else {
            if(SMileCrypto.isDEBUG()) {
                Log.d(SMileCrypto.LOG_TAG, "Thumbnail not found.");
            }

            String initial = "A";
            if (name.length() > 0) {
                initial = name.substring(0, 1);
            } else {
                if (keyInfo != null && keyInfo.getContact() != null && keyInfo.getContact().length() > 0) {
                    initial = keyInfo.getContact().substring(0, 1);
                } else if (keyInfo != null && keyInfo.getMail() != null && keyInfo.getMail().length() > 0) {
                    initial = keyInfo.getMail().substring(0, 1);
                } else {
                    initial = " ";
                }
            }

            BitmapDrawable bdrawable = new BitmapDrawable(App.getContext().getResources(), Utils.generateCircleBitmap(Utils.getMaterialColor(initial), 42, initial));
            holder.contactimage.setBackground(bdrawable);
        }

        holder.contactimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, Uri.parse("mailto:" + email));
                activity.startActivity(intent);
            }
        });
    }


    private void printError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(App.getContext().getResources().getString(R.string.error));
        if(SMileCrypto.isDEBUG()) {
            Log.e(SMileCrypto.LOG_TAG, "EXIT_STATUS: " + SMileCrypto.EXIT_STATUS);
        }
        builder.setMessage(App.getContext().getResources().getString(R.string.internal_error));
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        builder.create().show();
    }

    public void deleteCertificate(KeyInfo keyInfo) {
        if(keyInfo != null) {
            if (keyInfo.getHasPrivateKey()) {
                deleteOwnCertificate(keyInfo);
            } else {
                deleteOtherCertificate(keyInfo);
            }
        }
    }

    private void deleteOwnCertificate(final KeyInfo keyInfo) {
        final int position = getPosition(keyInfo);
        final KeyManagement keyManagement;
        try {
            keyManagement = KeyManagement.getInstance();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException e) {
            printError();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        alertDialogBuilder.setTitle(App.getContext().getString(R.string.alert_header_start) + keyInfo.getContact() + App.getContext().getString(R.string.alert_header_end));

        alertDialogBuilder
                .setMessage(App.getContext().getString(R.string.alert_content))
                .setCancelable(false)
                .setPositiveButton(App.getContext().getString(R.string.erase), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        keyManagement.deleteKey(keyInfo.getAlias());
                        removeKey(position);
                    }
                })
                .setNegativeButton(App.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void deleteOtherCertificate(final KeyInfo keyInfo) {
        final int position = getPosition(keyInfo);
        final KeyManagement keyManagement;
        try {
            keyManagement = KeyManagement.getInstance();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | NoSuchProviderException | CertificateException e) {
            printError();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);

        alertDialogBuilder
                .setMessage(App.getContext().getString(R.string.alert_header_start) + keyInfo.getContact() + App.getContext().getString(R.string.alert_header_end))
                .setCancelable(false)
                .setPositiveButton(App.getContext().getString(R.string.erase), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        keyManagement.deleteKey(keyInfo.getAlias());
                        removeKey(position);
                    }
                })
                .setNegativeButton(App.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }

    public void exportCertificate(KeyInfo keyInfo) {
        if(keyInfo != null) {
            if (keyInfo.getHasPrivateKey()) {
                exportOwnCertificate(keyInfo);
            } else {
                exportOtherCertificate(keyInfo);
            }
        }
    }

    private void exportOwnCertificate(final KeyInfo keyInfo) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(App.getContext().getString(R.string.alert_header_export));
        alertDialogBuilder
                .setMessage(App.getContext().getString(R.string.alert_export))
                .setCancelable(false)
                .setPositiveButton(App.getContext().getString(R.string.export), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String dst = KeyManagement.copyP12ToSDCard(keyInfo.getAlias());
                        if (dst == null) {
                            Toast.makeText(App.getContext(),
                                    App.getContext().getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(App.getContext(),
                                    App.getContext().getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(App.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void exportOtherCertificate(KeyInfo keyInfo) {
        String dst = KeyManagement.copyCertificateToSDCard(keyInfo.getCertificate(), keyInfo.getAlias());
        if (dst == null) {
            Toast.makeText(activity,
                    App.getContext().getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity,
                    App.getContext().getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
        }
    }

    public void shareCertificate(KeyInfo keyInfo) {
        if(keyInfo != null) {
            if (keyInfo.getHasPrivateKey()) {
                shareOwnCertificate(keyInfo);
            } else {
                shareOtherCertificate(keyInfo);
            }
        }
    }


    private void shareOwnCertificate(final KeyInfo keyInfo) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(App.getContext().getString(R.string.alert_share_own));
        alertDialogBuilder
                .setMessage(App.getContext().getString(R.string.dialog_share_own))
                .setCancelable(false)
                .setPositiveButton(App.getContext().getString(R.string.share), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String dst = KeyManagement.copyP12ToSDCard(keyInfo.getAlias());
                        if (dst == null) {
                            Toast.makeText(App.getContext(),
                                    App.getContext().getString(R.string.certificate_share_fail), Toast.LENGTH_LONG).show();
                        } else {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("application/x-pkcs12");

                            Uri uri = Uri.fromFile(new File((dst)));
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            activity.startActivity(shareIntent);
                        }
                    }
                })
                .setNegativeButton(App.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void shareOtherCertificate(KeyInfo keyInfo) {
        String dst = KeyManagement.copyCertificateToSDCard(keyInfo.getCertificate(), keyInfo.getAlias());
        if (dst == null) {
            Toast.makeText(activity,
                    App.getContext().getString(R.string.certificate_share_fail), Toast.LENGTH_LONG).show();
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/x-pkcs12");

            Uri uri = Uri.fromFile(new File((dst)));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(shareIntent);
        }
    }
}
