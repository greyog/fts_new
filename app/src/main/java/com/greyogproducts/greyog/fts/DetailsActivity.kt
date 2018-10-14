package com.greyogproducts.greyog.fts

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_tabs.*
import kotlinx.android.synthetic.main.fragment_tabs.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.sql.Timestamp

class DetailsActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private lateinit var mPairId: String
    private lateinit var mPairName: String
    private val tabTitles = listOf("5MIN", "15MIN", "1H", "5H", "DAY", "WEEK", "MONTH")
    val periods = listOf("300", "900",  "3600" ,"18000","86400", "week", "month")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mPairId = intent.getStringExtra("pair")
        mPairName = intent.getStringExtra("name")
        supportActionBar?.title = mPairName
        supportActionBar?.subtitle = Timestamp(System.currentTimeMillis()).toString()
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        tabTitles.forEach {
            val newTab = tabs.newTab()
            newTab.text = it
            tabs.addTab(newTab)
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_tabs, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), RetrofitHelper.OnResponsePairDataListener {
        override fun onResponseTechData(period: String, raw: String) {
            val retPos = periods.indexOf(period)
            fragments[retPos]?.setTechData(raw)
        }

        private val fragments = emptyMap<Int, PlaceholderFragment>().toMutableMap()
        init {
            RetrofitHelper.instance.onResponsePairDataListener = this
        }


        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            val fragment =PlaceholderFragment.newInstance(position, mPairId, mPairName)
            fragments[position] = fragment
            return fragment
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return tabTitles.size
        }

//        override fun getPageTitle(position: Int): CharSequence {
//            return tabTitles[position]
//        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        fun setTechData(txt: String?) {
            val doc = Jsoup.parse(txt)
            val summaryElement = doc.getElementById("techStudiesInnerWrap")
            val sumStr = summaryElement.selectFirst(".summary")
            val tl0 = newTableLayout()
            val tr0 = newTableRow(tl0)
            newTextView(sumStr, tr0, true)
            newTextView(sumStr.children().first(), tr0)
            val stls = summaryElement.getElementsByClass("summaryTableLine")
            val tl1 = newTableLayout()
            stls.forEach {stl ->
                val tr1 = newTableRow(tl1)
                stl.children().forEach {
                    val tv = newTextView(it, tr1)
                }
            }
            makeDataTable(doc, "pivot-points", ".crossRatesTbl" )
            makeDataTable(doc, "indicators", ".technicalIndicatorsTbl" )
            makeDataTable(doc, "moving-averages", ".movingAvgsTbl" )
//            TODO parse html to views
        }

        private fun makeDataTable(doc: Document, titleData: String, tableClass: String) {
            val pivotTitle = doc.selectFirst("[href*=$titleData]")
            val pivotTime = pivotTitle.nextElementSibling()
            val tl2 = newTableLayout()
            val tr2 = newTableRow(tl2)
            newTextView(pivotTitle, tr2,true)
            newTextView(pivotTime, tr2,true)
            val pivotTable = doc.selectFirst(tableClass)
            val pivotHead = pivotTable.select("th")
            val tlPivot = newTableLayout()
            val trPivotHead = newTableRow(tlPivot)
            pivotHead.forEach {
                newTextView(it, trPivotHead, true).typeface = Typeface.DEFAULT_BOLD
            }
            val pivotTrs = pivotTable.select("tr")
            pivotTrs.forEach { tr ->
                val trPivotData = newTableRow(tlPivot)
                tr.select("td").forEach { td ->
//                    println(td.html())
                    if (!td.classNames().contains("lastRow")) {
//                        println("first is ${td.text()}")
                        val tv = newTextView(td, trPivotData)
                    }else{
                        td.select(".inlineblock").forEach {ib->
                            val tvLast = newTextView(ib, null)
                            details_container.addView(tvLast)
                        }
                    }
                }
            }
        }

        private val defLLParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        private val defTLParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)
        private val defTRParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

        private fun newTextView(element:Element, row: TableRow?, ownText: Boolean = false) :TextView {
            val tv = TextView(this.context)
            val m = dpToPx(8)
            defTRParams.setMargins(m,m/2,0,m/2)
            tv.layoutParams = defTRParams
            if (ownText) tv.text = element.ownText()
            else tv.text = element.text()
            row?.addView(tv)

            fun setTextStyle(element: Element, textView: TextView) {
                println(element.classNames().toString())
                if (element.hasClass("bold"))
                    textView.typeface = Typeface.DEFAULT_BOLD
                if (element.hasClass("greenFont"))
                    textView.setTextColor(Color.GREEN)
                if (element.hasClass("redFont"))
                    textView.setTextColor(Color.RED)
            }
            return tv
        }
        private fun newTableLayout() :TableLayout {
            val tl = TableLayout(this.context)
            tl.layoutParams = defLLParams
            details_container.addView(tl)
            return tl
        }
        private fun newTableRow(table: TableLayout) :TableRow {
            val tr = TableRow(this.context)
            tr.layoutParams = defTLParams
            table.addView(tr)
            return tr
        }

        fun dpToPx(dps: Int): Int {
            val scale = resources.displayMetrics.density
            return (dps * scale + 0.5f).toInt()
        }

        private var pair: String? = null
        private var pairName: String? = null

        private var secNum: Int? = null
        private val periods = listOf("300", "900",  "3600" ,"18000","86400", "week", "month")

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.fragment_tabs, container, false)
//            rootView.section_label.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_NUMBER))
            pair = arguments?.getString(ARG_PAIR)
            pairName = arguments?.getString(ARG_PAIR_NAME)
            secNum = arguments?.getInt(ARG_SECTION_NUMBER)
            if (pair != null && secNum !=null) {
                RetrofitHelper.instance.doSinglePairRequest(pair!!,periods[secNum!!])
//                println("createView: section = $secNum, pair = $pair, period = ${periods[secNum!!]}")
            }
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"
            private const val ARG_PAIR = "pair"
            private const val ARG_PAIR_NAME = "name"
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int, pair: String, name:String): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                args.putString(ARG_PAIR, pair)
                args.putString(ARG_PAIR_NAME, name)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
