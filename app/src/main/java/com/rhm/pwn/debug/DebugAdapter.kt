package com.rhm.pwn.debug

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rhm.pwn.R


class DebugAdapter
(private var values: MutableList<DebugItem>) : RecyclerView.Adapter<DebugAdapter.ViewHolder>() {
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class ViewHolder(var layout: View) : RecyclerView.ViewHolder(layout) {
        // each data item is just a string in this case

        val dateText: TextView = layout.findViewById(R.id.debugDateTextView)
        val msgText: TextView = layout.findViewById(R.id.debugMsgTextView)
    }

    fun add(position: Int, item: DebugItem) {
        values.add(position, item)
        notifyItemInserted(position)
    }

    fun setValues(list: MutableList<DebugItem>) {
        values = list
        this.notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DebugAdapter.ViewHolder {
        // create a new view
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.debug_row, parent, false)
        // set the view's size, margins, paddings and layout parameters
        val vh = ViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.dateText.text = item.dateStr
        holder.msgText.text = item.getMessage()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return values.size
    }

}
