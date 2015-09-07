package de.fau.cs.mad.smile.android.encryption.utilities;

import android.support.v7.util.SortedList;

import de.fau.cs.mad.smile.android.encryption.KeyInfo;
import de.fau.cs.mad.smile.android.encryption.ui.adapter.KeyAdapter;

/**
 * Sorted list callback implementation for KeyInfo
 */
public class KeyListCallback extends SortedList.Callback<KeyInfo> {

    KeyAdapter adapter;

    public KeyListCallback(KeyAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int compare(KeyInfo o1, KeyInfo o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void onInserted(int position, int count) {
        adapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        adapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        adapter.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count) {
        adapter.notifyItemRangeChanged(position, count);
    }

    @Override
    public boolean areContentsTheSame(KeyInfo oldItem, KeyInfo newItem) {
        if(oldItem == null || newItem == null) {
            return false;
        }
        int contact = oldItem.compareName(newItem);
        int email = oldItem.compareMail(newItem);
        int date = oldItem.compareTermination(newItem);
        return contact == 0 && email == 0 && date == 0;
    }

    @Override
    public boolean areItemsTheSame(KeyInfo item1, KeyInfo item2) {
        return item1.equals(item2);
    }
}
