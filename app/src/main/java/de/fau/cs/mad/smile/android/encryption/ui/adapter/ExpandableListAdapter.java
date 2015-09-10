package de.fau.cs.mad.smile.android.encryption.ui.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.SMileCrypto;
import de.fau.cs.mad.smile.android.encryption.utilities.Utils;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Pair<Integer, String[]>>> listDataChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<Pair<Integer, String[]>>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int size = getGroupCount();
        if(groupPosition < 0 || size < groupPosition) {
            throw new IllegalArgumentException("Position not available." );
        }
        String header = this.listDataHeader.get(groupPosition);
        if(this.listDataChild.containsKey(header)) {
            return this.listDataChild.get(header).size();
        } else {
            throw new IllegalArgumentException(header + " has no children.");
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        int size = getGroupCount();
        if(groupPosition < 0 || size < groupPosition) {
            throw new IllegalArgumentException("Position not available." );
        }
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        int groupSize = getGroupCount();
        if(groupPosition < 0 || groupSize < groupPosition) {
            throw new IllegalArgumentException("Position not available." );
        }
        int childSize = getChildrenCount(groupPosition);
        if(childPosition < 0 || childSize < childPosition) {
            throw new IllegalArgumentException("Position not available." );
        }
        String header = this.listDataHeader.get(groupPosition);
        if(this.listDataChild.containsKey(header)) {
            List<Pair<Integer, String[]>> child = this.listDataChild.get(header);
            return child.get(childPosition);
        } else {
            throw new IllegalArgumentException(header + " has no children.");
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.activity_help_list_group, parent, false);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.listHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Pair<Integer, String[]> item = (Pair<Integer, String[]>) getChild(groupPosition, childPosition);
        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String[] data;
        switch (item.first) {
            case 0:
                convertView = layoutInflater.inflate(R.layout.activity_help_list_item, parent, false);

                TextView listChild = (TextView) convertView.findViewById(R.id.listItem);
                listChild.setText(item.second[0]);
                break;
            case 1:
                convertView = layoutInflater.inflate(R.layout.validity_circles, parent, false);
                data = item.second;
                if (data.length == 6) {
                    ImageView validCircle0 = (ImageView) convertView.findViewById(R.id.valid_circle_0);
                    ImageView validCircle1 = (ImageView) convertView.findViewById(R.id.valid_circle_1);
                    ImageView validCircle2 = (ImageView) convertView.findViewById(R.id.valid_circle_2);
                    ImageView validCircle3 = (ImageView) convertView.findViewById(R.id.valid_circle_3);
                    ImageView validCircle4 = (ImageView) convertView.findViewById(R.id.valid_circle_4);
                    TextView text = (TextView) convertView.findViewById(R.id.valid_text);
                    validCircle0.getBackground().setColorFilter(Utils.getColorFilter(data[0]));
                    validCircle1.getBackground().setColorFilter(Utils.getColorFilter(data[1]));
                    validCircle2.getBackground().setColorFilter(Utils.getColorFilter(data[2]));
                    validCircle3.getBackground().setColorFilter(Utils.getColorFilter(data[3]));
                    validCircle4.getBackground().setColorFilter(Utils.getColorFilter(data[4]));
                    text.setText(data[5]);
                }
                break;
            case 2:
                convertView = layoutInflater.inflate(R.layout.image_text, parent, false);
                data = item.second;
                ImageView imageView = (ImageView) convertView.findViewById(R.id.image_part);
                imageView.setImageDrawable(context.getResources().getDrawable(Integer.parseInt(data[0])));
                TextView text = (TextView) convertView.findViewById(R.id.text_part);
                text.setText(data[1]);
                break;
            default:
                if(SMileCrypto.isDEBUG()) {
                    Log.d(SMileCrypto.LOG_TAG, "This case shouldn't occur. Check if you forgot to add a new case.");
                }
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
