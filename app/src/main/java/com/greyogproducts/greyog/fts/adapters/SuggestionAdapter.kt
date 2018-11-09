package com.greyogproducts.greyog.fts.adapters
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.greyogproducts.greyog.fts.data.All


class SuggestionAdapter(context: Context, private val items: List<All>)
    : ArrayAdapter<String>(context,  android.R.layout.simple_dropdown_item_1line) {
    private var filteredItems: List<String>? = null
    private var mFilter: ArrayFilter? = null
    private var strItems: List<String> = List(items.size) {"${items[it].symbol} - ${items[it].name}"}

    init {
        addAll(strItems)
    }

    fun getItemPairId(position: Int): String {
        return items[position].pairID.toString()
    }

    fun getItemSymbol(position: Int): String {
        return items[position].symbol.toString()
    }

    fun getItemDescription(position: Int): String {
        return items[position].transName.toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): String? {
        return strItems[position]
    }

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter as ArrayFilter
    }

    override fun getCount(): Int {
        return items.size
    }

    private inner class ArrayFilter : Filter() {
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results == null) {
                return
            }
            filteredItems = results.values as List<String>?
            if (results.count > 0) notifyDataSetChanged()
            else notifyDataSetInvalidated()
        }

        override fun performFiltering(prefix: CharSequence): FilterResults {
            val results = FilterResults()

            //custom-filtering of results
            results.values = items
            results.count = items.size

            return results
        }


    }
}
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
//        val view : View?
//        val vh: ListItemHolder
//        if (convertView == null) {
//            view = inflater.inflate(R.layout.search_item, parent)
//            vh = ListItemHolder(view)
//            view.tag = vh
//        }else {
//            view = convertView
//            vh = view.tag as ListItemHolder
//        }
//        vh.label?.text  = mResponse.all[position].symbol
//        vh.desc?.text = mResponse.all[position].name
//
//        return view
//    }
//
//    class ListItemHolder(row: View?) {
//        val label = row?.findViewById<TextView>(R.id.label)
//        val desc = row?.findViewById<TextView>(R.id.desc)
//    }


