package com.rhm.pwn.home;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.rhm.pwn.R;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckSelectedAction;
import com.rhm.pwn.utils.PWNUtils;

/**
 * Created by sambo on 8/29/2017.
 */

public class URLCheckAdapter extends RecyclerView.Adapter<URLCheckAdapter.ViewHolder> {

    private List<URLCheck> values;
    public URLCheckSelectedAction action;
    private ColorStateList defaultColors;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView baseUrlText;
        public TextView urlText;
        public TextView lastUpdateText;
        public ImageView notificationImage;
        public TextView lastUpdateDate;
        public Button viewAction;
        public Button editAction;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            baseUrlText = v.findViewById(R.id.baseUrlText);
            urlText = v.findViewById(R.id.urlText);
            lastUpdateText = v.findViewById(R.id.lastUpdateText);
            notificationImage = v.findViewById(R.id.notificationImage);
            lastUpdateDate = v.findViewById(R.id.lastUpdateDate);
            viewAction = v.findViewById(R.id.view_button);
            editAction = v.findViewById(R.id.edit_button);
        }
    }

    public void add(int position, URLCheck item) {
        values.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        values.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public URLCheckAdapter(List<URLCheck> myDataset, URLCheckSelectedAction callback) {
        values = myDataset;
        action = callback;
    }

    public void setValues(List<URLCheck> list) {
        values = list;
        this.notifyDataSetChanged();
    }

    public List<URLCheck> getValues() {
        return values;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public URLCheckAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.url_check_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        defaultColors = vh.lastUpdateText.getTextColors(); //save original colors
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final URLCheck item = values.get(position);
        if (item.isEnableNotifications()) {
            holder.baseUrlText.setTextColor(Color.BLACK);
        } else {
            holder.baseUrlText.setTextColor(defaultColors);
        }
        holder.baseUrlText.setText(item.getDisplayTitle());
        holder.urlText.setText(item.getDisplayBody());

        if (item.getLastRunCode() == URLCheck.CODE_RUN_FAILURE) {
            holder.lastUpdateText.setText(item.getLastRunMessage());
            holder.lastUpdateText.setTextColor(Color.RED);
        } else {
            holder.lastUpdateText.setText(item.getLastUpdated());
            holder.lastUpdateText.setTextColor(defaultColors);
        }
        holder.lastUpdateText.setVisibility(TextUtils.isEmpty(holder.lastUpdateText.getText()) ? View.GONE : View.VISIBLE);
        holder.notificationImage.setVisibility(item.isHasBeenUpdated() ? View.VISIBLE : View.GONE);
        holder.layout.setOnClickListener(v -> action.onSelectedURLCheck(item));
        holder.layout.setOnLongClickListener(view -> action.onEditURLCheck(item));
        if (TextUtils.isEmpty(item.getLastChecked())) {
            holder.lastUpdateDate.setVisibility(View.GONE);
            holder.lastUpdateDate.setText("");
        } else {
            holder.lastUpdateDate.setVisibility(View.VISIBLE);
            holder.lastUpdateDate.setText("Checked: "+ item.getLastChecked());
        }

        //setup action buttons
        holder.viewAction.setOnClickListener(v -> action.onSelectedURLCheck(item));
        holder.editAction.setOnClickListener(v -> action.onEditURLCheck(item));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}
