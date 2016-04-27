package com.rhm.pwn.fragments;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rhm.pwn.R;
import com.rhm.pwn.models.URLItem;

import java.util.Date;
import java.util.List;

/**
 * Created by sambo on 4/25/2016.
 */
public class URLItemRecyclerViewAdapter extends RecyclerView.Adapter<URLItemRecyclerViewAdapter.ViewHolder> {

    private final List<URLItem> mValues;
    private final URLListFragment.OnListFragmentInteractionListener mListener;

    public URLItemRecyclerViewAdapter(List<URLItem> items, URLListFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_urlitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.tvUrl_name.setText(mValues.get(position).name);
        Date lastdate = mValues.get(position).lastUpdateDatetime;
        String lastdate_str = "New";
        if (lastdate != null) {
            lastdate_str = lastdate.toString();
        }
        //show it in a more apparent view if this is a new item to review
        if (mValues.get(position).markedRead) {
            holder.tvUrl_name.setTypeface(null, Typeface.NORMAL);
            holder.tvUrl_lastvalue_date.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.tvUrl_name.setTypeface(null, Typeface.BOLD);
            holder.tvUrl_lastvalue_date.setTypeface(null, Typeface.BOLD);
        }
        holder.tvUrl_lastvalue_date.setText(lastdate_str);
        holder.tvUrl_lastvalue.setText(mValues.get(position).lastValue);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvUrl_name;
        public final TextView tvUrl_lastvalue_date;
        public final TextView tvUrl_lastvalue;
        public URLItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvUrl_name = (TextView) view.findViewById(R.id.url_name);
            tvUrl_lastvalue_date = (TextView) view.findViewById(R.id.url_lastvalue_date);
            tvUrl_lastvalue = (TextView) view.findViewById(R.id.url_lastvalue);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvUrl_lastvalue_date.getText() + "'";
        }
    }
}

