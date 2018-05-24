package com.greyogproducts.greyog.fts

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import com.greyogproducts.greyog.fts.SummaryFragment.OnListFragmentInteractionListener
import com.greyogproducts.greyog.fts.dummy.DummyContent.DummyItem

import kotlinx.android.synthetic.main.fragment_pair.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPairRecyclerViewAdapter(
        private val mValues: ArrayList<SummaryListItem>?,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyPairRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as SummaryListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_pair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues == null) {
            return
        }
        val item = mValues[position]
        holder.tvPid.text = item.pid
        holder.tvName.text = item.name
        holder.tvSymbol.text = item.symbol
        for (s in item.sums) {
            val tv = TextView(holder.itemView.context)
            tv.text = s
            holder.llSummary.addView(tv)
        }

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val tvPid: TextView = mView.tvPid
        val tvName: TextView = mView.tvName
        val tvSymbol : TextView = mView.tvSymbol
        val llSummary = mView.llSummary
        val llMa = mView.llMa
        val llInd = mView.llInd

        override fun toString(): String {
            return super.toString() + " '" + tvPid.text + " : "+tvName.text + "'"
        }
    }
}
