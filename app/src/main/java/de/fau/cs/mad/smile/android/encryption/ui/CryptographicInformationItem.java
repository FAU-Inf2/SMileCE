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

public class CryptographicInformationItem extends AbstractCertificateInfoItem {
    private HashMap<String, String> data;

    @Override
    public void build(HashMap<String, String> data) {
        if(data.keySet().contains("Public Key") && data.keySet().contains("Signature Algorithm")
                && data.keySet().contains("Signature")) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Data must contain \"Public Key\", \"Signature Algorithm\", " +
                    "and \"Signature\".");
        }
    }

    @Override
    public View getView(ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = infalInflater.inflate(R.layout.certificate_information, parent, false);

        if(data.keySet().contains("Modulus") && data.keySet().contains("Exponent")) {
            TableRow publicKey = (TableRow) convertView.findViewById(R.id.row01);
            TableRow exponent = (TableRow) convertView.findViewById(R.id.row02);
            TableRow modulus = (TableRow) convertView.findViewById(R.id.row03);
            TableRow signatureAlgorithm = (TableRow) convertView.findViewById(R.id.row04);
            signatureAlgorithm.setVisibility(View.VISIBLE);
            TableRow signature = (TableRow) convertView.findViewById(R.id.row05);
            signature.setVisibility(View.VISIBLE);

            ((TextView) publicKey.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.pub_key));
            ((TextView) publicKey.findViewById(R.id.column_right)).setText(data.get("Public Key"));

            ((TextView) modulus.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.exponent));
            ((TextView) modulus.findViewById(R.id.column_right)).setText(data.get("Exponent"));

            ((TextView) exponent.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.modulus));
            ((TextView) exponent.findViewById(R.id.column_right)).setText(data.get("Modulus"));

            ((TextView) signatureAlgorithm.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.signature_algorithm));
            ((TextView) signatureAlgorithm.findViewById(R.id.column_right)).setText(data.get("Signature Algorithm"));

            ((TextView) signature.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.signature));
            ((TextView) signature.findViewById(R.id.column_right)).setText(data.get("Signature"));
        } else {
            TableRow publicKey = (TableRow) convertView.findViewById(R.id.row01);
            TableRow signatureAlgorithm = (TableRow) convertView.findViewById(R.id.row02);
            TableRow signature = (TableRow) convertView.findViewById(R.id.row03);

            ((TextView) publicKey.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.pub_key));
            ((TextView) publicKey.findViewById(R.id.column_right)).setText(data.get("Public Key"));

            ((TextView) signatureAlgorithm.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.signature_algorithm));
            ((TextView) signatureAlgorithm.findViewById(R.id.column_right)).setText(data.get("Signature Algorithm"));

            ((TextView) signature.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.signature));
            ((TextView) signature.findViewById(R.id.column_right)).setText(data.get("Signature"));
        }
        return convertView;
    }
}
