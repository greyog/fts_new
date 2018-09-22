package com.greyogproducts.greyog.fts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class SearchListViewAdapter(context: Context?, response: MyResponseResult?) : BaseAdapter(){
    private val inflater = LayoutInflater.from(context)
    private val mResponse = response ?: MyResponseResult()

    fun setNewData(newResponse: MyResponseResult?): Unit {
        mResponse.all = emptyList()
        if (newResponse != null) {
            mResponse.all.plus(newResponse.all)
        }
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view : View?
        val vh: ListItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.search_item, parent)
            vh = ListItemHolder(view)
            view.tag = vh
        }else {
            view = convertView
            vh = view.tag as ListItemHolder
        }
        vh.label?.text  = mResponse.all[position].symbol
        vh.desc?.text = mResponse.all[position].name

        return view
    }

    class ListItemHolder(row: View?) {
        val label = row?.findViewById<TextView>(R.id.label)
        val desc = row?.findViewById<TextView>(R.id.desc)
    }

    override fun getItem(p0: Int): Any {
        return mResponse.all[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return mResponse.all.size
    }

}
