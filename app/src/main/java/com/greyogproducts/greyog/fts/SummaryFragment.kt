package com.greyogproducts.greyog.fts

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_pair_list.*
import kotlinx.android.synthetic.main.fragment_pair_list.view.*
import kotlinx.android.synthetic.main.simple_text_view.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SummaryFragment.OnListFragmentInteractionListener] interface.
 */
class SummaryFragment : Fragment(), RetrofitHelper.OnResponseListener, SwipeRefreshLayout.OnRefreshListener {
//    override fun onSearchResponse(response: MyResponseResult?) {
//        println("onSearchResponse: response = $response")
//        searchAdapter.setNewData(response)
//    }

    override fun onRefresh() {
        srLayout.isRefreshing = true
        RetrofitHelper.instance.doSummaryRequest(null,null)
    }

    private var mColumns = ArrayList<String>()

    private var mItems = ArrayList<SummaryListItem>()

    private lateinit var mPrefs: SharedPreferences

    override fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryListItem>) {
        mItems = items
        val newAdapter = MyPairRecyclerViewAdapter(mPrefs, mItems, listener)
        activity.runOnUiThread {
            Toast.makeText(context,"Loaded ${items.size} items", Toast.LENGTH_SHORT).show()
            list.adapter = newAdapter
            llColumns.removeAllViews()
            this.mColumns = columns
            for (col in columns) {
                val view = LayoutInflater.from(context)
                        .inflate(R.layout.simple_text_view, llColumns, false)
                view.tvSimple.text = col
                llColumns.addView(view)
            }
            srLayout.isRefreshing = false
        }

    }

    override fun onBadSessId() {
        Toast.makeText(context,"BAD CONNECTION!", Toast.LENGTH_SHORT).show()
    }

    override fun onHappySessId() {
        Toast.makeText(context,"Connection OK", Toast.LENGTH_SHORT).show()
        srLayout.isRefreshing = true
        RetrofitHelper.instance.doSummaryRequest(null,null)
    }

    override fun onResponseTechData(raw: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onResponse(responseResult: MyResponseResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    // TODO: Customize parameters
    private var columnCount = 1
    private lateinit var llColumns : LinearLayout
    private lateinit var srLayout: SwipeRefreshLayout

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pair_list, container, false)
        srLayout = view.swipeContainer
        srLayout.setOnRefreshListener(this)
        llColumns = view.llColumns
//        searchAdapter = SearchListViewAdapter(this.context,null)
//        view.lvSearch.adapter = searchAdapter


        RetrofitHelper.instance.onResponseListener = this
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: SummaryListItem?)
    }

    fun showSortDialog() {
        val builder = AlertDialog.Builder(this.context)
        val sort = mPrefs.getInt("sort", 1)
        val listener = DialogInterface.OnClickListener { dialogInterface, i ->
            var el = i
            val prefSort = mPrefs.getInt("sort", 1)
            println( "onClick: before i , sort = $i , $prefSort")
            el += 1
            if (el == Math.abs(prefSort)) {
                el = -prefSort
            }
            mPrefs.edit().putInt("sort", el).apply()
            println("onClick: after el = $el")

            (list.adapter as MyPairRecyclerViewAdapter).sortBy(el)

            dialogInterface.dismiss()
        }
        builder.setTitle(R.string.sort_by)
        val arr = arrayOf(getString(R.string.sort_by_name)).plus(mColumns.toTypedArray())
        builder.setSingleChoiceItems(arr, Math.abs(sort) - 1, listener)
        builder.setPositiveButton(R.string.ok, null)
        builder.create().show()
    }


    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
                SummaryFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }

}
