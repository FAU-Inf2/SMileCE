package de.fau.cs.mad.smile.android.encryption;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;

public class CardListUpdater {
    private KeyManagement keyManager;
    private Activity ac;
    private CardArrayRecyclerViewAdapter mCardArrayAdapter;
    private ArrayList<Card> cards;

    public CardListUpdater(KeyManagement keyManager, Activity ac, CardArrayRecyclerViewAdapter mCardArrayAdapter, ArrayList<Card> cards) {
        this.keyManager = keyManager;
        this.ac = ac;
        this.mCardArrayAdapter = mCardArrayAdapter;
        this.cards = cards;
    }

    public void updateCards(ArrayList<Card> cards) {
        this.cards = cards;
        mCardArrayAdapter.setCards(cards);
        mCardArrayAdapter.notifyDataSetChanged();
    }

    public void updateCards() {
        List<KeyInfo> kis =  keyManager.getAllCertificates();
        for(KeyInfo keyInfo : kis) {

            //Create a Card
            KeyCard card = new KeyCard(ac, keyInfo);

            //Create a CardHeader
            KeyCardHeader header;
            if(keyInfo.alias.contains("_own_")) {
                header = new KeyCardHeader(ac, true);
            } else {
                header = new KeyCardHeader(ac, false);
            }
            //Add Header to card
            if(keyInfo.contact != null) {
                header.setTitle(keyInfo.contact);
            } else {
                header.setTitle("No contact information available.");
            }

            card.addCardHeader(header);
            boolean contains = false;

            for(int i = 0; i < mCardArrayAdapter.getItemCount(); ++i) {
                KeyCard kc = (KeyCard) mCardArrayAdapter.getItem(i);
                if(kc.equals(card)) {
                    contains = true;
                }
            }

            if(!contains) {
                Log.e(SMileCrypto.LOG_TAG, "Items added");
                card.getCardHeader().setPopupMenu(R.menu.card_context, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                    @Override
                    public void onMenuItemClick(BaseCard card, MenuItem item) {
                        if(!(card instanceof KeyCard)) {
                            return;
                        }
                        KeyCard kc = (KeyCard) card;
                        int id = item.getItemId();
                        boolean own = kc.keyInfo.alias.startsWith("SMile_crypto_own");
                        if (id == R.id.delete) {
                            if(own) {
                                deleteOwnCertificate(kc);
                            }
                            else {
                                deleteOtherCertificate(kc);
                            }
                        } else if (id == R.id.export) {
                            if(own) {
                                exportOwnCertificate(kc.keyInfo);
                            } else {
                                exportOtherCertificate(kc.keyInfo);
                            }
                        }
                    }
                });

                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if (!(card instanceof KeyCard)) {
                            return;
                        }
                        final KeyCard kc = (KeyCard) card;
                        Intent i = new Intent(ac, DisplayCertificateInformationActivity.class);
                        i.putExtra("Alias", kc.keyInfo.alias);
                        ac.startActivity(i);
                    }
                });
                mCardArrayAdapter.add(card);
            }
        }
        ArrayList<Card> toDelete = new ArrayList<>();
        for(Card c : cards) {
            if (!(c instanceof KeyCard)) {
                continue;
            }
            final KeyCard kc = (KeyCard) c;
            if(!kis.contains(kc.keyInfo)) {
                toDelete.add(c);
            }
        }
        cards.removeAll(toDelete);
        mCardArrayAdapter.notifyDataSetChanged();
    }

    private void deleteOwnCertificate(final KeyCard kc) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                kc.getContext());

            alertDialogBuilder.setTitle(App.getContext().getString(R.string.alert_header_start) + kc.keyInfo.contact + App.getContext().getString(R.string.alert_header_end));

            alertDialogBuilder
                    .setMessage(App.getContext().getString(R.string.alert_content))
                    .setCancelable(false)
                    .setPositiveButton(App.getContext().getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            keyManager.deleteKey(kc.keyInfo.alias);
                            mCardArrayAdapter.remove(kc);
                            mCardArrayAdapter.notifyDataSetChanged();
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

    private void deleteOtherCertificate(final KeyCard kc) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                kc.getContext());

        alertDialogBuilder
                .setMessage(App.getContext().getString(R.string.alert_header_start) + kc.keyInfo.contact + App.getContext().getString(R.string.alert_header_end))
                .setCancelable(false)
                .setPositiveButton(App.getContext().getString(R.string.erase), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        keyManager.deleteKey(kc.keyInfo.alias);
                        mCardArrayAdapter.remove(kc);
                        mCardArrayAdapter.notifyDataSetChanged();
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ac);
        alertDialogBuilder.setTitle(ac.getString(R.string.alert_header_export));
        alertDialogBuilder
                .setMessage(ac.getString(R.string.alert_export))
                .setCancelable(false)
                .setPositiveButton(ac.getString(R.string.export), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String dst = KeyManagement.copyP12ToSDCard(keyInfo.alias);
                        if (dst == null) {
                            Toast.makeText(App.getContext(),
                                    ac.getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(App.getContext(),
                                    ac.getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(ac.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialogBuilder.create().show();
    }

    private void exportOtherCertificate(KeyInfo keyInfo) {
        String dst = KeyManagement.copyCertificateToSDCard(keyInfo.certificate, keyInfo.alias);
        if (dst == null) {
            Toast.makeText(App.getContext(),
                    ac.getString(R.string.certificate_export_fail), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(App.getContext(),
                    ac.getString(R.string.certificate_export_success) + dst, Toast.LENGTH_LONG).show();
        }
    }
}
