package com.greyogproducts.greyog.fts

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.greyogproducts.greyog.fts.RetrofitHelper.OnSearchResponseListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.auto_update_layout.view.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.util.*

class MainActivity : AppCompatActivity(), SummaryFragment.OnListFragmentInteractionListener, OnSearchResponseListener {
    private lateinit var mSearchAutoComplete: SearchView.SearchAutoComplete
    @SuppressLint("RestrictedApi")
    override fun onSearchResponse(response: MyResponseResult?) {
//        val respList = response?.all?.size?.let { it -> List(it) {"${response.all[it].symbol} - ${response.all[it].transName}"} }
//        println("onSearchResponse: ${response?.all}")
//        val adptr = respList?.let { SuggestionAdapter(this, it) }
        val adptr = response?.all?.let { SuggestionAdapter(this, it) }
        mSearchAutoComplete.setAdapter(adptr)
        mSearchAutoComplete.threshold = 1
        mSearchAutoComplete.showDropDown()


    }

    override fun onListFragmentInteraction(item: SummaryListItem?) {
        println("not implemented yet")
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onResume() {
        super.onResume()
        RetrofitHelper.instance.doSummaryRequest()
    }

    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        loadAds()
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

//        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
//        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        fab.visibility = View.VISIBLE
        fab.setOnClickListener { view ->
            launchTestDetails()
//            showInterstitial()
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        RetrofitHelper.instance.prefs = prefs

    }

    private fun launchTestDetails() {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("pair", "1")
        intent.putExtra("name", "NAME_HERE")
        startActivity(intent)
    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded && Math.random() > 0.7)
            mInterstitialAd.show()
        else
            println("No interstitial")
    }

    private lateinit var mInterstitialAd: InterstitialAd

    private fun loadAds() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-7481139450301121/4167406890"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }


    private var mShareActionProvider: ShareActionProvider? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val mSearch = menu.findItem(R.id.app_bar_search).actionView as SearchView
        RetrofitHelper.instance.onSearchResponseListener = this
        mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                RetrofitHelper.instance.doSearchRequest(newText ?: "")

                return false
            }

        })
        mSearchAutoComplete  = mSearch.findViewById(android.support.v7.appcompat.R.id.search_src_text)
        mSearchAutoComplete.setOnItemClickListener { adapterView, view, i, l ->
//            Toast.makeText(this, (mSearchAutoComplete.adapter as SuggestionAdapter).getItemPairId(position = i), Toast.LENGTH_SHORT).show()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val emptSet = emptySet<String>().toMutableSet()
            val idSet = prefs.getStringSet("pairs", emptSet)
            val itemId = (mSearchAutoComplete.adapter as SuggestionAdapter).getItemPairId(i)
            val itemName = (mSearchAutoComplete.adapter as SuggestionAdapter).getItem(i)
            if (idSet.contains(itemId)) {
                Toast.makeText(this, "Already exists.", Toast.LENGTH_SHORT).show()
                mSearchAutoComplete.showDropDown()
            } else if (idSet.size >= 20)
                Toast.makeText(this, "Too many elements! Delete something before add.", Toast.LENGTH_SHORT).show()
            else {
                idSet.plusAssign(itemId)
                val newSet = setOf<String>().toMutableSet()
                newSet.addAll(idSet)
                prefs.edit().remove("pairs").apply()
                prefs.edit().putStringSet("pairs",newSet).apply()
                println("saved $idSet")
                Toast.makeText(this, "Added element $itemName to list.", Toast.LENGTH_SHORT).show()
                mSearchAutoComplete.showDropDown()
                RetrofitHelper.instance.doSummaryRequest()
            }
        }
//        menu.findItem(R.id.menu_item_share).also {
//            mShareActionProvider = MenuItemCompat.getActionProvider(it) as? ShareActionProvider
//            it.setOnMenuItemClickListener {mi ->
//                val bitmap = takeScreenshot()
//                saveBitmap(bitmap)
//                shareIt()
//                return@setOnMenuItemClickListener true
//            }
//        }

        return true
    }

//    private fun shareIt() {
//        val uri = Uri.fromFile(File(imgPath))
//        val sharingIntent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, uri)
//            type = "image/jpeg"
//        }
//        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_title)))
//    }
//
//    private lateinit var imgPath: String
//
//    private fun saveBitmap(bitmap: Bitmap) {
//        imgPath = Environment.getExternalStorageDirectory().absolutePath + "/screenshot.png"
//        try {
//            val fos = FileOutputStream(imgPath)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
//            fos.flush()
//            fos.close()
//        } catch (e: FileNotFoundException) {
//            println(e.message)
//        } catch (e : IOException) {
//            println(e.message)
//        }
//    }
//
//    private fun takeScreenshot(): Bitmap {
//        val rootView = findViewById<View>(android.R.id.content)
//        rootView.isDrawingCacheEnabled = true
//        return rootView.drawingCache
//    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_refresh -> reloadFragments()
            R.id.action_sort -> showSortDialog()
            R.id.action_set_auto_update -> showAutoUpdateDialog()
            R.id.action_about -> showAboutDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showAutoUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val v = layoutInflater.inflate(R.layout.auto_update_layout, null)
        val sw = v.swAutoUpd
        sw.isChecked = prefs.getBoolean("auto", false)
        val ed = v.etAutoUpd
        ed.setText(prefs.getInt("auto_time", 5).toString())
        ed.isEnabled = sw.isChecked

        sw.setOnClickListener {
            ed.isEnabled = sw.isChecked
        }

        val listener = DialogInterface.OnClickListener { dialogInterface, i ->
            println("onClick: sw, edt = " + sw.isChecked + ed.text.toString())
            var time = Integer.parseInt(ed.text.toString())
            println("onClick: time = $time")
            if (time <= 0) time = 5
            prefs.edit().putInt("auto_time", time).putBoolean("auto", sw.isChecked).apply()
            setUpdTimer()
            showInterstitial()
        }
//        builder.setTitle(R.string.action_auto_update);
        builder.setView(v)
        builder.setPositiveButton(R.string.ok, listener)
        builder.create().show()
    }

    private var mUpdTimer: Timer? = null

    private fun setUpdTimer() {
        if (mUpdTimer != null) mUpdTimer?.cancel()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!prefs.getBoolean("auto", false)) return
        val minutes = prefs.getInt("auto_time", 5)
        mUpdTimer = Timer("autoUpdTimer")
        val task = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    reloadFragments()
                    println("run: autoUpdTimer, time = $minutes")
                }
            }
        }
        mUpdTimer?.schedule(task, (minutes * 60 * 1000).toLong(), (minutes * 60000).toLong())
        println("setUpdTimer: set timer, time = " + minutes * 60000)

    }


    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
//        val v = layoutInflater.inflate(R.layout.about_layout, null)
        builder.setTitle(R.string.action_about)
        builder.setView(R.layout.about_layout)
        val listener = DialogInterface.OnClickListener { dialogInterface, i ->
            showInterstitial()
        }
        builder.setPositiveButton(R.string.ok, listener)
        builder.create().show()
    }

    private fun showSortDialog() {
        (mSectionsPagerAdapter?.currentFragment as? SummaryFragment)?.showSortDialog()
        showInterstitial()

    }

    private fun reloadFragments() {
        (mSectionsPagerAdapter?.currentFragment as? SummaryFragment)?.onRefresh()
        showInterstitial()
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        var currentFragment: Fragment? = null

        override fun setPrimaryItem(container: ViewGroup?, position: Int, `object`: Any?) {
            if (currentFragment != `object`) currentFragment = `object` as Fragment?
            super.setPrimaryItem(container, position, `object`)
        }

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.getInstance(position)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 1
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val defView = inflater.inflate(R.layout.fragment_main, container, false)
            defView.section_label.text = getString(R.string.section_format, arguments?.getInt(ARG_SECTION_NUMBER))
            return defView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"
            private val fragments = ArrayList<Fragment>()


            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun getInstance(sectionNumber: Int): Fragment {
                if (fragments.size > sectionNumber) return fragments[sectionNumber]
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                val fragment = if (sectionNumber == 0) SummaryFragment.newInstance(3)
                else PlaceholderFragment()
                fragment.arguments = args
                fragments.add(fragment)
                return fragment
            }
        }
    }
}
