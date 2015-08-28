package de.fau.cs.mad.smile.android.encryption.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;

import de.fau.cs.mad.smile.android.encryption.App;
import de.fau.cs.mad.smile.android.encryption.R;

public class ValidityItem extends AbstractCertificateInfoItem {
    private HashMap<String, String> data;

    @Override
    public void build(HashMap<String, String> data) {
        if (data.keySet().contains("Startdate") && data.keySet().contains("Enddate")) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Data must contain \"Startdate\" and \"Enddate\".");
        }
    }

    @Override
    public View getView(ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = infalInflater.inflate(R.layout.validity_information, parent, false);
        TableRow thumbprint = (TableRow) convertView.findViewById(R.id.row01);
        TableRow serialNumber = (TableRow) convertView.findViewById(R.id.row02);
        ((TextView) thumbprint.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.start_date));
        ((TextView) thumbprint.findViewById(R.id.column_right)).setText(data.get("Startdate"));
        ((TextView) serialNumber.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.end_date));
        ((TextView) serialNumber.findViewById(R.id.column_right)).setText(data.get("Enddate"));
        return convertView;
    }
}
