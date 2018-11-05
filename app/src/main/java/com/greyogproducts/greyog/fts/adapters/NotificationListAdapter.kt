package com.greyogproducts.greyog.fts.adapters

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.greyogproducts.greyog.fts.NotificationListActivity
import com.greyogproducts.greyog.fts.R
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.vm.NotificationsListViewModel
import kotlinx.android.synthetic.main.notification_list_item.view.*

class NotificationListAdapter(private val viewModel: NotificationsListViewModel,
                              private val mListener: OnListInteractionListener)
    : RecyclerView.Adapter<NotificationListAdapter.ViewHolder>() {

    interface OnListInteractionListener {
        fun onActionEdit(item: NotificationData?)
    }

    private val mOnClickListener: View.OnClickListener

    private var mItems = emptyList<NotificationData>().toMutableList()

    private val itemsObserver = Observer<List<NotificationData>> {
        if (it == null) {
            return@Observer
        }
        mItems = it as MutableList<NotificationData>
        notifyDataSetChanged()
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val vh = v.tag as ViewHolder
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            editItem(vh.tvnPId.text.toString())
            println("${this::class.java.simpleName}: list click")
        }
        viewModel.notificationList.observe(mListener as NotificationListActivity, itemsObserver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        with(holder) {
            tvnPId.text = item.pairId
            tvnSymbol.text = item.symbol
            tvnDescription.text = item.description
            setViewTextAndColor(tvnFiveMin, item.fiveMin.toString())
            setViewTextAndColor(tvnFifteenMin, item.fifteenMin.toString())
            setViewTextAndColor(tvnHour, item.hour.toString())
            setViewTextAndColor(tvnFiveHour, item.fiveHour.toString())
            setViewTextAndColor(tvnDay, item.day.toString())
            setViewTextAndColor(tvnWeek, item.week.toString())
            setViewTextAndColor(tvnMonth, item.month.toString())
        }
//        holder.mView.setOnClickListener {
//            val h = it.tag as ViewHolder?
//            if (expandedPos >= 0) notifyItemChanged(expandedPos)
//            expandedPos = if (h != null) {
//                if (expandedPos == h.adapterPosition) -1 else h.adapterPosition
//            } else -1
//            notifyItemChanged(expandedPos)
//        }

        holder.mView.setOnClickListener(mOnClickListener)

        holder.mView.setOnLongClickListener { it ->
            val builder = AlertDialog.Builder(this.mListener as Context)
            val listener = DialogInterface.OnClickListener { dialogInterface, i ->
                val h = it.tag as ViewHolder?
                if (h != null) {
                    val itemId = h.tvnPId.text
                    println("long click listener $i , $itemId")
                    when (i) {
                        1 -> { //delete action
                            viewModel.deleteItem(itemId.toString())
                        }
                        0 -> { //edit action
                            editItem(itemId.toString())
                        }
                    }

                }
                dialogInterface.dismiss()
            }

            builder.setItems(R.array.notification_list_long_menu, listener)
            builder.create().show()
            return@setOnLongClickListener false
        }

    }

    private fun editItem(itemId: String) {
        mListener.onActionEdit(mItems.find { it.pairId == itemId })
    }

    private var mDefaultColorStateList: ColorStateList? = null

    private fun setViewTextAndColor(textView: TextView?, s: String) {
        textView?.text = s
        when {
            s.contains("Buy") -> textView?.setTextColor(Color.GREEN)
            s.contains("Sell") -> textView?.setTextColor(Color.RED)
            else -> textView?.setTextColor(mDefaultColorStateList)
        }
    }

    override fun getItemCount(): Int = mItems.size

    private fun sortBy() {
        mItems = mItems.asSequence().sortedWith(compareBy { it.symbol }).toMutableList()
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        val tvnPId: TextView = mView.tvnPid
        val tvnSymbol: TextView = mView.tvnSymbol
        val tvnFiveMin: TextView = mView.tvn5m
        val tvnFifteenMin: TextView = mView.tvn15m
        val tvnHour: TextView = mView.tvn1h
        val tvnFiveHour: TextView = mView.tvn5h
        val tvnDay: TextView = mView.tvnd
        val tvnWeek: TextView = mView.tvnw
        val tvnMonth: TextView = mView.tvnm
        val tvnDescription: TextView = mView.tvnDescription

        init {
            mView.tag = this
            if (mDefaultColorStateList == null) {
                mDefaultColorStateList = tvnDay.textColors
            }
        }

    }
}
