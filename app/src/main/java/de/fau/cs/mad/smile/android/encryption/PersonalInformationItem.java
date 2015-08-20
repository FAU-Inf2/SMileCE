package de.fau.cs.mad.smile.android.encryption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;

public class PersonalInformationItem extends AbstractCertificateInfoItem {
    private HashMap<String, String> data;

    @Override
    public void build(HashMap<String, String> data) {
        if(data.keySet().contains("Name") && data.keySet().contains("Email")) {
            this.data = data;
        } else {
            throw new IllegalArgumentException("Data must contain \"Name\" nad \"Email\"");
        }
    }

    @Override
    public View getView(ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = infalInflater.inflate(R.layout.personal_information, parent, false);
        TableRow name = (TableRow) convertView.findViewById(R.id.row01);
        TableRow mail = (TableRow) convertView.findViewById(R.id.row02);
        ((TextView)name.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.name));
        ((TextView)name.findViewById(R.id.column_right)).setText(data.get("Name"));
        ((TextView)mail.findViewById(R.id.column_left)).setText(App.getContext().getString(R.string.email));
        ((TextView)mail.findViewById(R.id.column_right)).setText(data.get("Email"));
        if(data.containsKey("L")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row03);
            ((TextView)tr.findViewById(R.id.column_left)).setText("L");
            ((TextView)tr.findViewById(R.id.column_right)).setText(data.get("L"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row03);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if(data.containsKey("DC")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row04);
            ((TextView)tr.findViewById(R.id.column_left)).setText("DC");
            ((TextView)tr.findViewById(R.id.column_right)).setText(data.get("DC"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row04);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if(data.containsKey("O")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row05);
            ((TextView)tr.findViewById(R.id.column_left)).setText("O");
            ((TextView)tr.findViewById(R.id.column_right)).setText(data.get("O"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row05);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if(data.containsKey("OU")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row06);
            ((TextView)tr.findViewById(R.id.column_left)).setText("OU");
            ((TextView)tr.findViewById(R.id.column_right)).setText(data.get("OU"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row06);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        return convertView;
    }
}
