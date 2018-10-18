package com.greyogproducts.greyog.fts3


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.greyogproducts.greyog.fts3.SummaryFragment.OnListFragmentInteractionListener
import com.greyogproducts.greyog.fts3.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.fragment_pair.view.*
import kotlinx.android.synthetic.main.simple_text_view.view.*
import java.util.*
import kotlin.Comparator
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class MyPairRecyclerViewAdapter(private val mPrefs: SharedPreferences,
                                items: ArrayList<SummaryListItem>?,
                                private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyPairRecyclerViewAdapter.ViewHolder>() {

    private var expandedPos = -1

    private val mOnClickListener: View.OnClickListener
    private val sort : Int
        get() = mPrefs.getInt("sort", 1)
    private var mValues: MutableList<SummaryListItem>

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as SummaryListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
        mValues = MutableList(0){SummaryListItem()}
        if (items != null) {
            mValues.addAll(items)
        }
        sortBy(sort)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_pair, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        if (mValues == null) {
//            return
//        }
        val item = mValues[position]
        holder.tvPid.text = item.pid
        holder.tvPrice.text = item.price
        holder.tvSymbol.text = item.symbol
        holder.tvPDesc.text = item.name
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

        holder.mView.setOnLongClickListener { it ->
            val builder = AlertDialog.Builder(this.mListener as Context)
            val listener = DialogInterface.OnClickListener { dialogInterface, i ->
//                println("mValues[i] = "+mValues[i])
                val h = it.tag as ViewHolder?
                if (h != null) {
                    val itemId = h.tvPid.text
//                    println("itemId = $itemId")
                    when (i) {
                        1 -> { //delete action
                            val ind = mValues.indexOfFirst { it.pid == itemId }
//                    println("mValues[ind] = "+mValues[ind])
                            mValues.removeAt(ind)
                            notifyItemRemoved(ind)
                            val idSet = List(mValues.size) { mValues[it].pid }.toMutableSet()
                            println("idset= $idSet")
                            mPrefs.edit().putStringSet("pairs", idSet).apply()
                        }
                        0 -> { //view details action
                            val itemName = h.tvPDesc.text
                            val intent = Intent(mListener as Context, DetailsActivity::class.java)
                            intent.putExtra("pair", itemId)
                            intent.putExtra("name", itemName)
                            (mListener as Context).startActivity(intent)
                        }
                    }

                }
                dialogInterface.dismiss()
            }
            builder.setItems(R.array.list_item_long_menu, listener)
            builder.create().show()
            return@setOnLongClickListener false
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

    override fun getItemCount(): Int = mValues.size

    fun sortBy(col: Int) {
//        println("sorted by : $col")

        val sortDir = arrayOf("Strong Buy", "Buy", "Neutral", "Sell", "Strong Sell")
        fun getSortResult(t1: SummaryListItem, t2: SummaryListItem, f: Int): Int {
            val c = f.absoluteValue - 2
            if (t1.sums.size <= c) return 0
            val n1 = t1.sums[c]
            val n2 = t2.sums[c]
            val pos1 = sortDir.binarySearch(n1)
            val pos2 = sortDir.binarySearch(n2)
            return Math.signum((pos1 - pos2).toFloat()).toInt() * f.sign
        }
//        class MyComparator : Comparator<SummaryListItem> {
//            override fun compare(p0: SummaryListItem?, p1: SummaryListItem?): Int {
//                val r = p0?.name!!.compareTo(p1?.name!!)
//                print(p0.name + " : " + p1.name + " = $r ; ")
//                return r.sign
//            }
//        }
//
//        val compByNameAsc = Comparator<SummaryListItem>{t1, t2 ->
//            return@Comparator t1.name!!.compareTo(t2.name!!)
//        }
//        val compByNameDesc = Comparator<SummaryListItem>{t1, t2 ->
//            val r = t2.name!!.compareTo(t1.name!!)
//            print(t1.name + " : " + t2.name + " = $r ; ")
//            return@Comparator r.sign
//        }

        fun compByCol(col: Int): Comparator<SummaryListItem> {
            val comp = Comparator<SummaryListItem>{t1, t2 ->
                return@Comparator getSortResult(t1,t2,col)
            }
            return comp
        }
//        println(mValues)
        mValues = when (col) {
            0,1 -> mValues.asSequence().sortedWith(compareBy { it.name }).toMutableList()
            -1 -> mValues.asSequence().sortedWith(compareByDescending { it.name }).toMutableList()
            else -> mValues.asSequence().sortedWith(compByCol(col)).toMutableList()
//            else -> mValues.sortedWith(compareBy { it.sums[col.absoluteValue - 2] })
        }
        notifyDataSetChanged()
//        println(mValues)

    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val tvPid: TextView = mView.tvPid
        val tvPDesc: TextView = mView.tvPairDescription
        val tvPrice: TextView = mView.tvPrice
        val tvSymbol: TextView = mView.tvSymbol
        val llSummary: LinearLayout = mView.llSummary
        val llMa: LinearLayout = mView.llMa
        val llInd: LinearLayout = mView.llInd
        private val tvSumTit : TextView = mView.tvSummaryTitle
        private val tvMaTit : TextView = mView.tvMaTitle
        private val tvIndTit : TextView = mView.tvIndTitle
        private val expandables = arrayOf(llInd,llMa,tvIndTit,tvMaTit,tvSumTit, tvPDesc)

        init {
            mView.tag = this
        }

        fun setExpand(isExp : Boolean) {
            if (isExp) {
                expandables.forEach {
                    it.visibility = View.VISIBLE
                }
            } else {
                expandables.forEach {
                    it.visibility = View.GONE
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + tvPid.text + " : " + tvPrice.text + "'"
        }
    }
}
