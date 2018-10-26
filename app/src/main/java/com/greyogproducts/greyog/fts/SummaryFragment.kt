package com.greyogproducts.greyog.fts

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import com.greyogproducts.greyog.fts.MainActivity.PlaceholderFragment.Companion.ARG_SECTION_NUMBER
import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.data.SummaryListData
import kotlinx.android.synthetic.main.fragment_pair_list.*
import kotlinx.android.synthetic.main.fragment_pair_list.view.*
import kotlinx.android.synthetic.main.simple_text_view.view.*
import java.sql.Timestamp

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SummaryFragment.OnListFragmentInteractionListener] interface.
 */
class SummaryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val viewModel: SummaryListViewModel by lazy {
        println("viewModel create")
        ViewModelProviders.of(this).get(SummaryListViewModel::class.java)
    }

    private val itemsChangeObserver =
            Observer<SummaryListData> {
                if (it != null) {
//                    mItems = it.items
//                    mColumns = it.columns
                    onSummaryResponse(it.columns, it.items)
                }
            }

    override fun onRefresh() {
        srLayout.isRefreshing = true
        viewModel.refresh(tabNum)
    }

    private var mColumns = ArrayList<String>()

    private var mItems = ArrayList<SummaryItemData>()

    private lateinit var mPrefs: SharedPreferences

    private var tabNum = 0

    private fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryItemData>) {
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
            (activity as MainActivity).supportActionBar?.also {
                it.title = getString(R.string.last_update)
                it.subtitle = Timestamp(System.currentTimeMillis()).toString()
            }
        }

    }

//    private fun onBadSessId() {
//        Toast.makeText(context,"BAD CONNECTION!", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun onHappySessId() {
//        Toast.makeText(context,"Connection OK", Toast.LENGTH_SHORT).show()
//        srLayout.isRefreshing = true
//        RetrofitHelper.instance.doSummaryRequest(listener)
//    }

    private lateinit var llColumns : LinearLayout
    private lateinit var srLayout: SwipeRefreshLayout

    private var listener: OnListFragmentInteractionListener? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        viewModel.dataMap[tabNum]?.observe(this, itemsChangeObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pair_list, container, false)
        srLayout = view.swipeContainer
        srLayout.setOnRefreshListener(this)
        llColumns = view.llColumns
        tabNum = arguments.getInt(ARG_SECTION_NUMBER)

        onRefresh()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("sumfrag.onAttach")
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        println("sumfrag.onDetach")
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
        fun onListFragmentInteraction(item: SummaryItemData?)
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
                    //                    arguments = Bundle().apply {
//                        putInt(ARG_COLUMN_COUNT, columnCount)
//                    }
                }
    }

}
