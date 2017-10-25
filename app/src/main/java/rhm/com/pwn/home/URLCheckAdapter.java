package rhm.com.pwn.home;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import rhm.com.pwn.R;
import rhm.com.pwn.model.URLCheck;
import rhm.com.pwn.model.URLCheckSelectedAction;
import rhm.com.pwn.view_url.WebViewActivity;

/**
 * Created by sambo on 8/29/2017.
 */

public class URLCheckAdapter extends RecyclerView.Adapter<URLCheckAdapter.ViewHolder> {

    private List<URLCheck> values;
    public URLCheckSelectedAction action;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView baseUrlText;
        public TextView urlText;
        public TextView lastUpdateText;
        public ImageView notificationImage;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            baseUrlText = (TextView) v.findViewById(R.id.baseUrlText);
            urlText = (TextView) v.findViewById(R.id.urlText);
            lastUpdateText = (TextView) v.findViewById(R.id.lastUpdateText);
            notificationImage = (ImageView) v.findViewById(R.id.notificationImage);
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
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.url_check_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final URLCheck item = values.get(position);
        holder.baseUrlText.setText(item.getDisplayTitle());
        holder.urlText.setText(item.getDisplayBody());
        holder.lastUpdateText.setText(item.getLastUpdated());
        holder.lastUpdateText.setVisibility(TextUtils.isEmpty(item.getLastUpdated())?View.GONE : View.VISIBLE);
        boolean updated = item.isHasBeenUpdated();
        holder.notificationImage.setVisibility(item.isHasBeenUpdated()?View.VISIBLE : View.GONE);
        holder.layout.setOnClickListener(v -> action.onSelectedURLCheck(item));
        holder.layout.setOnLongClickListener(view -> action.onEditURLCheck(item));

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}
