package de.fau.cs.mad.smile_crypto;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


public class KeyAdapter extends RecyclerView.Adapter<KeyAdapter.KeyViewHolder> {
    private List<KeyInfo> keylist;

    public static class KeyViewHolder extends RecyclerView.ViewHolder {
        protected TextView alias;
        //protected QuickContactBadge contact_badge;
        protected TextView contact;
        protected TextView mail;
        protected TextView type;
        protected TextView hash;
        protected TextView trust;
        protected TextView termination_date;

        public KeyViewHolder(View itemView) {
            super(itemView);
            alias = (TextView) itemView.findViewById(R.id.alias);
            //contact_badge = (QuickContactBadge) itemView.findViewById(R.id.contact_badge);
            contact = (TextView) itemView.findViewById(R.id.contact);
            mail = (TextView) itemView.findViewById(R.id.mail);
            type = (TextView) itemView.findViewById(R.id.type);
            hash = (TextView) itemView.findViewById(R.id.hash);
            trust = (TextView) itemView.findViewById(R.id.trust);
            termination_date = (TextView) itemView.findViewById(R.id.termination_date);
        }
    }

    public KeyAdapter() {
        this.keylist = new ArrayList<>();
    }

    public KeyAdapter(List<KeyInfo> keylistist) {
        this.keylist = keylistist;
    }

    @Override
    public KeyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.key, parent, false);

        return new KeyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(KeyViewHolder holder, int position) {
        KeyInfo ki = keylist.get(position);
        holder.alias.setText(ki.alias);
        holder.contact.setText(ki.contact);
        //holder.contact_badge.assignContactFromEmail(ki.mail, true);
        holder.hash.setText(ki.hash);
        holder.mail.setText(ki.mail);
        holder.type.setText(ki.type);
        holder.trust.setText(ki.trust);
        if(ki.termination_date != null)
            holder.termination_date.setText(ki.termination_date.toString());
    }

    @Override
    public int getItemCount() {
        return keylist.size();
    }

    public void addKey(KeyInfo key) {
        if(key != null && !keylist.contains(key)) {
            int pos = keylist.size();
            keylist.add(key);
            Log.e(SMileCrypto.LOG_TAG, "Added KeyInfo: " + key + " at position: " + pos);
            notifyItemInserted(pos);
        }
    }

    public  void addKey(List<KeyInfo> keys) {
        if(keys != null)
            for(KeyInfo ki : keys)
                addKey(ki);
    }
}
