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

public class PersonalInformationItem extends AbstractCertificateInfoItem {
    private HashMap<String, String> data;

    @Override
    public void build(HashMap<String, String> data) {
        this.data = data;
    }

    @Override
    public View getView(ViewGroup parent) {
        Context context = App.getContext();
        LayoutInflater infalInflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = infalInflater.inflate(R.layout.personal_information, parent, false);
        if (data.containsKey("Name")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row01);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.name));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("Name"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row01);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("Email")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row02);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.email));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("Email"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row02);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("title")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row03);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.title));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("title"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row03);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("INIT")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row04);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.INIT));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("INIT"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row04);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("gender")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row05);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.gender));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("gender"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row05);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("GN")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row06);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.GN));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("GN"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row06);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("NAB")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row07);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.NAB));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("NAB"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row07);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("pseudonym")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row08);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.pseudonym));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("pseudonym"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row08);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("surname")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row09);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.surname));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("surname"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row09);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("UID")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row10);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.UID));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("UID"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row10);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("DOB")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row11);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.DOB));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("DOB"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row11);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("TEL")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row12);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.TEL));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("TEL"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row12);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("gen")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row13);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.gen));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("gen"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row13);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("business")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row14);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.business));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("business"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row14);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("O")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row15);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.O));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("O"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row15);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("OU")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row16);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.OU));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("OU"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row16);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("COF")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row17);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.COF));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("COF"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row17);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("COR")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row18);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.COR));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("COR"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row18);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("POB")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row19);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.POB));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("POB"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row19);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("C")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row20);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.C));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("C"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row20);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("ST")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row21);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.ST));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("ST"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row21);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("L")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row22);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.L));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("L"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row22);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("street")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row23);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.street));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("street"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row23);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("POA")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row24);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.POA));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("POA"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row24);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("POC")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row25);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.POC));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("POC"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row25);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("DC")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row26);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.DC));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("DC"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row26);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("DMD")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row27);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.DMD));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("DMD"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row27);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("DNQ")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row28);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.DNQ));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("DNQ"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row28);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("SN")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row29);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.SN));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("SN"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row29);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        if (data.containsKey("UI")) {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row30);
            ((TextView) tr.findViewById(R.id.column_left)).setText(context.getString(R.string.UI));
            ((TextView) tr.findViewById(R.id.column_right)).setText(data.get("UI"));
        } else {
            TableRow tr = (TableRow) convertView.findViewById(R.id.row30);
            ((ViewGroup) tr.getParent()).removeView(tr);
        }
        return convertView;
    }
}
