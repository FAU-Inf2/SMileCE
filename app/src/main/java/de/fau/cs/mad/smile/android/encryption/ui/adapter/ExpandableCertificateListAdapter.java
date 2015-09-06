package de.fau.cs.mad.smile.android.encryption.ui.adapter;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import de.fau.cs.mad.smile.android.encryption.R;
import de.fau.cs.mad.smile.android.encryption.ui.activity.items.AbstractCertificateInfoItem;

public class ExpandableCertificateListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<AbstractCertificateInfoItem>> listDataChild;

    public ExpandableCertificateListAdapter(Context context, List<String> listDataHeader,
                                            HashMap<String, List<AbstractCertificateInfoItem>> listChildData) {
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
            List<AbstractCertificateInfoItem> children = this.listDataChild.get(header);
            return children.get(childPosition);
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
        final AbstractCertificateInfoItem item = (AbstractCertificateInfoItem) getChild(groupPosition, childPosition);
        return item.getView(parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
