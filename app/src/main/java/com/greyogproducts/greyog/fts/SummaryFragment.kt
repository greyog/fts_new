package com.greyogproducts.greyog.fts

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast

import com.greyogproducts.greyog.fts.dummy.DummyContent
import com.greyogproducts.greyog.fts.dummy.DummyContent.DummyItem
import kotlinx.android.synthetic.main.fragment_pair_list.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SummaryFragment.OnListFragmentInteractionListener] interface.
 */
class SummaryFragment : Fragment(), RetrofitHelper.OnResponseListener {
    override fun onSummaryResponse(columns: ArrayList<String>, items: ArrayList<SummaryListItem>) {
        val newAdapter = MyPairRecyclerViewAdapter(items, listener)
        activity.runOnUiThread({
            list.adapter = newAdapter
//            list.adapter.notifyDataSetChanged()
        })

    }

    override fun onBadSessId() {
        Toast.makeText(context,"BAD CONNECTION!", Toast.LENGTH_SHORT).show()
    }

    override fun onHappySessId() {
        Toast.makeText(context,"session ID obtained", Toast.LENGTH_SHORT).show()
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

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pair_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MyPairRecyclerViewAdapter(null, listener)
            }
        }
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

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                SummaryFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
