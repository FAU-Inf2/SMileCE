package de.fau.cs.mad.smile.android.encryption.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
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
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.crypto.KeyManagement;
import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.ui.activity.DisplayCertificateInformationActivity;


public class KeyAdapter extends RecyclerSwipeAdapter<KeyAdapter.KeyViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener{
    private SortedList<KeyInfo> keylist;
    private Activity activity;
    private static SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());
    private static List<Integer> materialColors = Arrays.asList(
            0xffe57373,
            0xfff06292,
            0xffba68c8,
            0xff9575cd,
            0xff7986cb,
            0xff64b5f6,
            0xff4fc3f7,
            0xff4dd0e1,
            0xff4db6ac,
            0xff81c784,
            0xffaed581,
            0xffff8a65,
            0xffd4e157,
            0xffffd54f,
            0xffffb74d,
            0xffa1887f,
            0xff90a4ae
    );

    public void switchCards(ArrayList<KeyInfo> cardsFiltered) {
        keylist.clear();
        addKey(cardsFiltered);
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(SMileCrypto.LOG_TAG, "Settings changed: Key " + key);
        if(key.equals("pref_key_direction")) {
            ArrayList<KeyInfo> kis = new ArrayList<KeyInfo>(keylist.size());
            for(int i = 0; i < getItemCount(); ++i) {
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

    private class oncClickCreatePopup implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

        private int position;

        public oncClickCreatePopup(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(App.getContext(), v);

            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.card_context);
            popup.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            KeyInfo keyInfo = keylist.get(position);
            int id = item.getItemId();
            boolean own = keyInfo.getAlias().startsWith("SMile_crypto_own");
            if (id == R.id.delete) {
                if (own) {
                    deleteOwnCertificate(keyInfo, position);
                } else {
                    deleteOtherCertificate(keyInfo, position);
                }
            } else if (id == R.id.export) {
                if (own) {
                    exportOwnCertificate(keyInfo);
                } else {
                    exportOtherCertificate(keyInfo);
                }
            }
            return true;
        }
    }

    public KeyAdapter(Activity activity) {
        this(activity, null);
    }

    public KeyAdapter(Activity activity, List<KeyInfo> keyInfoList) {
        this.activity = activity;
        this.keylist = new SortedList<KeyInfo>(KeyInfo.class, new SortedList.Callback<KeyInfo>() {
            @Override
            public int compare(KeyInfo o1, KeyInfo o2) {
                int erg = 0;
                if(o1.getContact().equals("") && o2.getContact().equals("")) {
                    erg = o1.getMail().compareTo(o2.getMail());
                } else if(o1.getContact().equals("")) {
                    erg = o1.getMail().compareTo(o2.getContact());
                } else if(o2.getContact().equals("")) {
                    erg = o1.getContact().compareTo(o2.getMail());
                } else {
                    erg = o1.getContact().compareTo(o2.getContact());
                }
                Log.d(SMileCrypto.LOG_TAG, "Order: " + Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1")));
                erg *= Integer.valueOf(sharedPreferences.getString("pref_key_direction", "1"));
                return erg;
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(KeyInfo oldItem, KeyInfo newItem) {
                return oldItem.getContact().equals(newItem.getContact()) && oldItem.getMail().equals(newItem.getMail()) && oldItem.getTerminationDate().equals(newItem.getTerminationDate());
            }

            @Override
            public boolean areItemsTheSame(KeyInfo item1, KeyInfo item2) {
                return item1.equals(item2);
            }
        });
        if(keyInfoList != null) {
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
        if(keyInfo.getContact().equals("")) {
            holder.header.setText(keyInfo.getMail());
        } else {
            holder.header.setText(keyInfo.getContact());
        }
        holder.mail.setText(keyInfo.getMail());

        if(keyInfo.getHasPrivateKey()) {
            holder.privateKey.setVisibility(View.VISIBLE);
        }

        if(keyInfo.getTerminationDate() != null && keyInfo.getValidAfter() != null) {
            DateTime valid = keyInfo.getTerminationDate();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");
            String displayDate= valid.toString(fmt);
            holder.termination_date.setText(displayDate);
            DateTime today = new DateTime();
            Years years = Years.yearsBetween(today, valid);
            Months months = Months.monthsBetween(today, valid);

            if(valid.getMillis() <= today.getMillis()) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#ff0000"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if (keyInfo.getValidAfter().getMillis() > today.getMillis()) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#0000ff"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#0000ff"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#0000ff"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#0000ff"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#0000ff"));
            } else if(years.getYears() > 1 || (years.getYears() == 1 && months.getMonths() > 0)) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#00ff00"));
            } else if(months.getMonths() > 5) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#00ff00"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if (months.getMonths() > 2) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if(months.getMonths() > 0) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
            } else if(valid.getMillis() > today.getMillis()) {
                holder.validCircle0.getBackground().setColorFilter(getColorFilter("#FFA500"));
                holder.validCircle1.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle2.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle3.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
                holder.validCircle4.getBackground().setColorFilter(getColorFilter("#d3d3d3"));
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
        holder.contextButton.setOnClickListener(new oncClickCreatePopup(position));
        holder.swipe.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                layout.setDragDistance(0);
                boolean own = keyInfo.getAlias().startsWith("SMile_crypto_own");
                if(holder.delete_icon.isShown()) {
                    holder.delete_icon.setVisibility(View.INVISIBLE);
                    if(own) {
                        deleteOwnCertificate(keyInfo, position);
                    } else {
                        deleteOtherCertificate(keyInfo, position);
                    }
                }

                if(holder.share_icon.isShown()) {
                    holder.share_icon.setVisibility(View.INVISIBLE);
                    if(own) {
                        shareOwnCertificate(keyInfo);
                    } else {
                        shareOtherCertificate(keyInfo);
                    }
                }
                super.onHandRelease(layout, xvel, yvel);
            }
        });
        holder.swipe.addRevealListener(R.id.delete, new SwipeLayout.OnRevealListener() {
            @Override
            public void onReveal(View view, SwipeLayout.DragEdge dragEdge, float v, int i) {
                if (dragEdge != SwipeLayout.DragEdge.Right) {
                    return;
                }
                if (v <= 0.3 && holder.delete_icon.isShown()) {
                    holder.delete_icon.setVisibility(View.INVISIBLE);
                } else if (v > 0.3 && !holder.delete_icon.isShown()) {
                    holder.delete_icon.setVisibility(View.VISIBLE);
                }
            }
        });
        holder.swipe.addRevealListener(R.id.share, new SwipeLayout.OnRevealListener() {
            @Override
            public void onReveal(View view, SwipeLayout.DragEdge dragEdge, float v, int i) {
                if (dragEdge != SwipeLayout.DragEdge.Left) {
                    return;
                }
                if (v <= 0.2 && holder.share_icon.isShown()) {
                    holder.share_icon.setVisibility(View.INVISIBLE);
                } else if (v > 0.2 && !holder.share_icon.isShown()) {
                    holder.share_icon.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private ColorFilter getColorFilter(String color) {
        int iColor = Color.parseColor(color);

        int red = (iColor & 0xFF0000) / 0xFFFF;
        int green = (iColor & 0xFF00) / 0xFF;
        int blue = iColor & 0xFF;

        float[] matrix = { 0, 0, 0, 0, red
                , 0, 0, 0, 0, green
                , 0, 0, 0, 0, blue
                , 0, 0, 0, 1, 0 };

        return new ColorMatrixColorFilter(matrix);
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
        if(position >= 0 && position < getItemCount()) {
            keylist.removeItemAt(position);
        }
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private Bitmap generateCircleBitmap(int circleColor, float diameterDP, String text){
        /**
         *
         * http://stackoverflow.com/questions/31168636/rounded-quickcontactbadge-with-text
         */
        final int textColor = 0xffffffff;

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float diameterPixels = diameterDP * (metrics.densityDpi / 160f);
        float radiusPixels = diameterPixels/2;

        // Create the bitmap
        Bitmap output = Bitmap.createBitmap((int) diameterPixels, (int) diameterPixels, Bitmap.Config.ARGB_8888);

        // Create the canvas to draw on
        Canvas canvas = new Canvas(output);
        canvas.drawARGB(0, 0, 0, 0);

        // Draw the circle
        final Paint paintC = new Paint();
        paintC.setAntiAlias(true);
        paintC.setColor(circleColor);
        canvas.drawCircle(radiusPixels, radiusPixels, radiusPixels, paintC);

        // Draw the text
        if (text != null && text.length() > 0) {
            final Paint paintT = new Paint();
            paintT.setColor(textColor);
            paintT.setAntiAlias(true);
            paintT.setTextSize(radiusPixels * 2);
            paintT.setTypeface(Typeface.SANS_SERIF);
            final Rect textBounds = new Rect();
            paintT.getTextBounds(text, 0, text.length(), textBounds);
            canvas.drawText(text, radiusPixels - textBounds.exactCenterX(), radiusPixels - textBounds.exactCenterY(), paintT);
        }

        return output;
    }

    private int getMaterialColor(Object key) {
        return materialColors.get(Math.abs(key.hashCode()) % materialColors.size());
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
        Log.d(SMileCrypto.LOG_TAG, "mail: " + email);
        Log.d(SMileCrypto.LOG_TAG, "thumb: " + thumb);
        Log.d(SMileCrypto.LOG_TAG, "name: " + name);
        Log.d(SMileCrypto.LOG_TAG, "key: " + lookUpKey);

        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUpKey);

        if(thumb != null && thumb.length() > 0) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(App.getContext().getContentResolver(), lookupUri, false);
            Bitmap bmp = getCroppedBitmap(BitmapFactory.decodeStream(input));
            BitmapDrawable bdrawable = new BitmapDrawable(App.getContext().getResources(), bmp);
            holder.contactimage.setBackground(bdrawable);
            Log.d(SMileCrypto.LOG_TAG, "Thumbnail found.");
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Thumbnail not found.");

            String initial = "A";
            if (name.length() > 0) {
                initial = name.substring(0, 1);
            } else {
                if(keyInfo != null && keyInfo.getContact() != null && keyInfo.getContact().length() > 0){
                    initial = keyInfo.getContact().substring(0, 1);
                } else if(keyInfo != null && keyInfo.getMail() != null && keyInfo.getMail().length() > 0) {
                    initial = keyInfo.getMail().substring(0, 1);
                } else {
                    initial = " ";
                }
            }

            BitmapDrawable bdrawable = new BitmapDrawable(App.getContext().getResources(), generateCircleBitmap(getMaterialColor(initial), 42, initial));
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

    private void deleteOwnCertificate(final KeyInfo keyInfo, final int position) {
        final KeyManagement keyManagement;
        try {
            keyManagement = new KeyManagement();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException  e) {
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

    private void printError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(App.getContext().getResources().getString(R.string.error));
        Log.e(SMileCrypto.LOG_TAG, "EXIT_STATUS: " + SMileCrypto.EXIT_STATUS);
        builder.setMessage(App.getContext().getResources().getString(R.string.internal_error));
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        builder.create().show();
    }

    private void deleteOtherCertificate(final KeyInfo keyInfo, final int position) {
        final KeyManagement keyManagement;
        try {
            keyManagement = new KeyManagement();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException  e) {
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
