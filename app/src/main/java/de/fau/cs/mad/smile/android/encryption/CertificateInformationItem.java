package de.fau.cs.mad.smile.android.encryption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;

public class CertificateInformationItem extends AbstractCertificateInfoItem {
    private HashMap<String, String> data;

    @Override
    public void build(HashMap<String, String> data) {
        if(data.keySet().contains("Thumbprint") && data.keySet().contains("Serial number")
                && data.keySet().contains("Version")) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Data must contain \"Thumbprint\", \"Serial number\", " +
            "and \"Version\".");
        }
    }

    @Override
    public View getView(ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = infalInflater.inflate(R.layout.certificate_information, parent, false);
        TableRow thumbprint = (TableRow) convertView.findViewById(R.id.row01);
        TableRow serialNumber = (TableRow) convertView.findViewById(R.id.row02);
        TableRow version = (TableRow) convertView.findViewById(R.id.row03);
        ((TextView) thumbprint.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.thumbprint));
        ((TextView) thumbprint.findViewById(R.id.column_right)).setText(data.get("Thumbprint"));
        ((TextView) serialNumber.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.serial_nr));
        ((TextView) serialNumber.findViewById(R.id.column_right)).setText(data.get("Serial number"));
        ((TextView) version.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.version));
        ((TextView) version.findViewById(R.id.column_right)).setText(data.get("Version"));
        return convertView;
    }
}
