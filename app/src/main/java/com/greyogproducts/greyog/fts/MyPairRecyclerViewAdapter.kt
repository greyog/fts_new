package com.greyogproducts.greyog.fts


import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.greyogproducts.greyog.fts.SummaryFragment.OnListFragmentInteractionListener
import com.greyogproducts.greyog.fts.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.fragment_pair.view.*
import kotlinx.android.synthetic.main.simple_text_view.view.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPairRecyclerViewAdapter(
        private val mValues: ArrayList<SummaryListItem>?,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyPairRecyclerViewAdapter.ViewHolder>() {

    private var expandedPos = -1

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
//        if (holder.llSummary.childCount > 0)
            holder.llSummary.removeAllViews()
            for (s in item.sums) {
                val view = LayoutInflater.from(holder.itemView.context)
                        .inflate(R.layout.simple_text_view, holder.llSummary, false)
                setViewTextAndColor(view.tvSimple,s)
                holder.llSummary.addView(view)
            }
//        if (holder.llInd.childCount > 0)
            holder.llInd.removeAllViews()
            for (s in item.inds) {
                val view = LayoutInflater.from(holder.itemView.context)
                        .inflate(R.layout.simple_text_view, holder.llInd, false)
                setViewTextAndColor(view.tvSimple,s)
                holder.llInd.addView(view)
            }
//        if (holder.llMa.childCount > 0)
            holder.llMa.removeAllViews()
            for (s in item.mas) {
                val view = LayoutInflater.from(holder.itemView.context)
                        .inflate(R.layout.simple_text_view, holder.llMa, false)
                setViewTextAndColor(view.tvSimple,s)
                holder.llMa.addView(view)
            }

        holder.mView.setOnClickListener {
            val h = it.tag as ViewHolder?
            if (expandedPos >= 0) notifyItemChanged(expandedPos)
            expandedPos = if (h != null) {
                if (expandedPos == h.adapterPosition) -1 else h.adapterPosition
            } else -1
            notifyItemChanged(expandedPos)
        }

        if (position == expandedPos) {
            holder.setExpand(true)
        }
        else {
            holder.setExpand(false)
        }
    }

    private fun setViewTextAndColor(textView: TextView?, s: String) {
        textView?.text = s
        when {
            s.contains("Buy") -> textView?.setTextColor(Color.GREEN)
            s.contains("Sell") -> textView?.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int = mValues?.size ?: 0

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val tvPid: TextView = mView.tvPid
        val tvName: TextView = mView.tvName
        val tvSymbol: TextView = mView.tvSymbol
        val llSummary: LinearLayout = mView.llSummary
        val llMa: LinearLayout = mView.llMa
        val llInd: LinearLayout = mView.llInd
        private val tvSumTit : TextView = mView.tvSummaryTitle
        private val tvMaTit : TextView = mView.tvMaTitle
        private val tvIndTit : TextView = mView.tvIndTitle
        private val arrExpandables = arrayOf(llInd,llMa,tvIndTit,tvMaTit,tvSumTit)

        init {
            mView.tag = this
        }

        fun setExpand(isExp : Boolean) {
            if (isExp) {
                arrExpandables.forEach {
                    it.visibility = View.VISIBLE
                }
            } else {
                arrExpandables.forEach {
                    it.visibility = View.GONE
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + tvPid.text + " : " + tvName.text + "'"
        }
    }
}
