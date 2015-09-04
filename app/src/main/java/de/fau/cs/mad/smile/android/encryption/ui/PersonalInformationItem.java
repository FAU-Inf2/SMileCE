package de.fau.cs.mad.smile.android.encryption.ui;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;

public class PersonalInformationItem extends AbstractCertificateInfoItem {
    private LinkedHashMap<String, String[]> data;

    public void buildComplex(LinkedHashMap<String, String[]> data) {
        for(String[] arr : data.values()) {
            if(arr.length != 2) {
                throw new IllegalArgumentException("Each value of the hash map must have a size of two.");
            }
        }
        this.data = data;
    }

    @Override
    public View getView(ViewGroup parent) {
        Context context = App.getContext();
        LayoutInflater layoutInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.personal_information, parent, false);
        TableLayout table = (TableLayout) convertView.findViewById(R.id.table01);
        for(String key : data.keySet()) {
            LinearLayout row = (LinearLayout) layoutInflater.inflate(R.layout.table_row, table, false);
            TextView name = (TextView) row.findViewById(R.id.column_left);
            TextView entry = (TextView) row.findViewById(R.id.column_right);
            String[] values = data.get(key);
            name.setText(values[0]);
            entry.setText(values[1]);
            table.addView(row);
        }
        return convertView;
    }
}
