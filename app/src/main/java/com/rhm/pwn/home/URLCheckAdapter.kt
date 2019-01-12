package com.rhm.pwn.home

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import com.rhm.pwn.R
import com.rhm.pwn.model.URLCheck
import com.rhm.pwn.model.URLCheckSelectedAction

/**
 * Created by sambo on 8/29/2017.
 */

class URLCheckAdapter// Provide a suitable constructor (depends on the kind of dataset)
(private var values: MutableList<URLCheck>, private var action: URLCheckSelectedAction) : RecyclerView.Adapter<URLCheckAdapter.ViewHolder>() {
    private var defaultColors: ColorStateList? = null

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        // each data item is just a string in this case

        val baseUrlText: TextView = layout.findViewById(R.id.baseUrlText)
        val urlText: TextView = layout.findViewById(R.id.urlText)
        val lastUpdateText: TextView = layout.findViewById(R.id.lastCheckedDate)
        val notificationImage: ImageView = layout.findViewById(R.id.notificationImage)
        val lastUpdateDate: TextView = layout.findViewById(R.id.lastUpdateDate)
        val lastCheckedDate: TextView = layout.findViewById(R.id.lastCheckedDate)
        val viewAction: Button = layout.findViewById(R.id.view_button)
        val editAction: Button = layout.findViewById(R.id.edit_button)

    }

    fun add(position: Int, item: URLCheck) {
        values.add(position, item)
        notifyItemInserted(position)
    }

    fun setValues(list: MutableList<URLCheck>) {
        values = list
        this.notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): URLCheckAdapter.ViewHolder {
        // create a new view
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.url_check_row, parent, false)
        // set the view's size, margins, paddings and layout parameters
        val vh = ViewHolder(v)
        defaultColors = vh.lastUpdateText.textColors //save original colors
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        if (item.enableNotifications) {
            holder.baseUrlText.setTextColor(Color.BLACK)
        } else {
            holder.baseUrlText.setTextColor(defaultColors)
        }
        holder.baseUrlText.text = item.displayTitle
        holder.urlText.text = item.displayBody

        if (item.lastRunCode == URLCheck.CODE_RUN_FAILURE) {
            holder.lastUpdateText.text = item.lastRunMessage
            holder.lastUpdateText.setTextColor(Color.RED)
        } else {
            holder.lastUpdateText.text = item.lastUpdated
            holder.lastUpdateText.setTextColor(defaultColors)
        }
        holder.lastUpdateText.visibility = if (TextUtils.isEmpty(holder.lastUpdateText.text)) View.GONE else View.VISIBLE
        holder.notificationImage.visibility = if (item.hasBeenUpdated) View.VISIBLE else View.GONE
        holder.layout.setOnClickListener { action.onSelectedURLCheck(item) }
        holder.layout.setOnLongClickListener { action.onEditURLCheck(item) }

        if (TextUtils.isEmpty(item.lastUpdated)) {
            holder.lastUpdateDate.visibility = View.GONE
            holder.lastUpdateDate.text = ""
        } else {
            holder.lastUpdateDate.visibility = View.VISIBLE
            holder.lastUpdateDate.text = "Updated: " + item.lastUpdated
        }
        //Don't show checked date if it is missing or the same as updated
        if (TextUtils.isEmpty(item.lastChecked) || item.lastChecked.equals(item.lastUpdated)) {
            holder.lastCheckedDate.visibility = View.GONE
            holder.lastCheckedDate.text = ""
        } else {
            holder.lastCheckedDate.visibility = View.VISIBLE
            holder.lastCheckedDate.text = "Checked: " + item.lastChecked
        }

        //setup action buttons
        holder.viewAction.setOnClickListener { action.onSelectedURLCheck(item) }
        holder.editAction.setOnClickListener { action.onEditURLCheck(item) }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return values.size
    }

}
