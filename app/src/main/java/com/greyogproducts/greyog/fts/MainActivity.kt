package com.greyogproducts.greyog.fts

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.greyogproducts.greyog.fts.adapters.SuggestionAdapter
import com.greyogproducts.greyog.fts.data.SearchResponseResult
import com.greyogproducts.greyog.fts.data.SummaryItemData
import com.greyogproducts.greyog.fts.vm.SearchViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.auto_update_layout.view.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.leave_feedback_layout.view.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity(), SummaryFragment.OnListFragmentInteractionListener {
    override fun onListFragmentLongClick() {
        showInterstitial()
    }

    private lateinit var mSearchAutoComplete: SearchView.SearchAutoComplete

    override fun onListFragmentInteraction(item: SummaryItemData?) {
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

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

//        fab.visibility = View.VISIBLE
        fabNewNotification.setOnClickListener {

            showInterstitial()
        }

//        showNotificationListActivity()

        checkForFeedback()
    }

    private fun checkForFeedback() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val curTime = System.currentTimeMillis()
        val firstRunTime = prefs.getLong("first_run", 0)
        if (firstRunTime.compareTo(0) == 0) {
            prefs.edit().putLong("first_run", curTime).apply()
//            println("it's first run. Time is ${Date(firstRunTime)}")
        } else {
            val raznost = (curTime - firstRunTime).toFloat() / (1000 * 60 * 60 * 24)
            val lastFeedbackTime = prefs.getLong("feedback_time", 0)
            if (raznost > 7 && lastFeedbackTime.compareTo(0) == 0) showFeedbackDialog()
        }
    }

    private fun showNotificationListActivity() {
        val intent = Intent(this, NotificationListActivity::class.java)
        startActivity(intent)
    }

//    private fun launchTestDetails() {
//        val intent = Intent(this, DetailsActivity::class.java)
//        intent.putExtra("pair", "1")
//        intent.putExtra("name", "NAME_HERE")
//        startActivity(intent)
//    }

    private fun showInterstitial() {
        if (mInterstitialAd.isLoaded && Math.random() > 0.7)
            mInterstitialAd.show()
        else
            println("No interstitial")
    }


    private lateinit var mInterstitialAd: InterstitialAd

    private lateinit var mAdView: AdView

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private fun loadAds() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        logLoadAds()

        MobileAds.initialize(this, "ca-app-pub-7481139450301121~7017797434")
//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713") // test
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().addTestDevice("2BBD1FDAC2C6B57F6321A92C8C286579")
                .build()
        mAdView.loadAd(adRequest)
//        adid for native ca-app-pub-7481139450301121/4509652193

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-7481139450301121/4167406890"
        mInterstitialAd.loadAd(AdRequest.Builder()
                .addTestDevice("2BBD1FDAC2C6B57F6321A92C8C286579")
                .build())
    }

    private fun logLoadAds() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, this.localClassName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "load Ads")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }


//    private var mShareActionProvider: ShareActionProvider? = null

    private val viewModel: SearchViewModel by lazy {
        ViewModelProviders.of(this).get(SearchViewModel::class.java)
    }

    @SuppressLint("RestrictedApi")
    private val searchResultObserver =
            Observer<SearchResponseResult> { response ->
                val adptr = response?.all?.let { SuggestionAdapter(this, it) }
                mSearchAutoComplete.setAdapter(adptr)
                mSearchAutoComplete.threshold = 1
                mSearchAutoComplete.showDropDown()
            }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        prepareSearch(menu)
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

    private fun prepareSearch(menu: Menu) {
        viewModel.searchData.observe(this, searchResultObserver)
        val mSearch = menu.findItem(R.id.app_bar_search).actionView as SearchView
        mSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.searchFor(newText)
                }
                return false
            }

        })
        mSearchAutoComplete = mSearch.findViewById(android.support.v7.appcompat.R.id.search_src_text)
        mSearchAutoComplete.setOnItemClickListener { _, _, i, _ ->
            //            Toast.makeText(this, (mSearchAutoComplete.adapter as SuggestionAdapter).getItemPairId(position = i), Toast.LENGTH_SHORT).show()
            val itemId = (mSearchAutoComplete.adapter as SuggestionAdapter).getItemPairId(i)
            //            val itemName = (mSearchAutoComplete.adapter as SuggestionAdapter).getItem(i)
            val tabFragment = (mSectionsPagerAdapter?.currentFragment as SummaryFragment)
            tabFragment.viewModel.addItemToList(itemId)
            tabFragment.onRefresh()
        }
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
            R.id.action_manage_notifications -> showNotificationListActivity()
            R.id.action_leave_feedback -> showFeedbackDialog()
            R.id.action_share -> launchShareDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun launchShareDialog() {
        val preview = window.decorView
        takeScreenShotAndShare(this,
                preview,
                true,
                getString(R.string.share_text))
    }

    private fun takeScreenShotAndShare(context: Context, view: View, incText: Boolean, text: String) {

        try {

            val mPath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "fts_screenshot.png")
            //File imageDirectory = new File(mPath, "screenshot.png");

            view.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false

            val fOut = FileOutputStream(mPath)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut)
            fOut.flush()
            fOut.close()

            val shareIntent = Intent(Intent.ACTION_SEND)
            val pictureUri = Uri.fromFile(mPath)
            shareIntent.type = "image/*"
            if (incText) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, text)
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)))
        } catch (tr: Throwable) {
            println("Couldn't save screenshot, $tr")
        }

    }

    private fun logNegative() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, this.localClassName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "negative feedback")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private fun logPositive() {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, this.localClassName)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "positive feedback")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private fun openAppRating(context: Context) {
        // you can also use BuildConfig.APPLICATION_ID
        val appId = context.packageName
        val rateIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appId"))
        var marketFound = false

        // find all applications able to handle our rateIntent
        val otherApps = context.packageManager
                .queryIntentActivities(rateIntent, 0)
        for (otherApp in otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {

                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appId"))
            context.startActivity(webIntent)
        }
    }

    private fun showFeedbackDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putLong("last_feedback_ask", System.currentTimeMillis()).apply()
        val v = layoutInflater.inflate(R.layout.leave_feedback_layout, null)
        val btNegative = v.btNegativeFeedback
        val btNeutral = v.btNeutralFeedback
        val btPositive = v.btPositiveFeedback

        val negListener = View.OnClickListener {
            Toast.makeText(this, R.string.next_time, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            logNegative()
        }


        btNegative.setOnClickListener(negListener)
        btNeutral.setOnClickListener(negListener)

        btPositive.setOnClickListener {
            dialog.dismiss()
            prefs.edit().putLong("feedback_time", System.currentTimeMillis()).apply()
            logPositive()

            openAppRating(this)

        }



        dialog.setTitle(R.string.title_leave_feedback)
        dialog.setView(v)
        val dialogBtListener = DialogInterface.OnClickListener { _, _ ->
            prefs.edit().putLong("first_run", System.currentTimeMillis()).apply()
        }
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                getString(R.string.leave_me_alone),
                dialogBtListener)
        dialog.show()
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

        val listener = DialogInterface.OnClickListener { _, _ ->
            //            println("onClick: sw, edt = " + sw.isChecked + ed.text.toString())
            var time = Integer.parseInt(ed.text.toString())
//            println("onClick: time = $time")
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
//                    println("run: autoUpdTimer, time = $minutes")
                }
            }
        }
        mUpdTimer?.schedule(task, (minutes * 60 * 1000).toLong(), (minutes * 60000).toLong())
//        println("setUpdTimer: set timer, time = " + minutes * 60000)

    }


    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
//        val v = layoutInflater.inflate(R.layout.about_layout, null)
        builder.setTitle(R.string.action_about)
        builder.setView(R.layout.about_layout)
        val listener = DialogInterface.OnClickListener { _, _ ->
            showInterstitial()
        }
        builder.setPositiveButton(R.string.ok, listener)
        builder.create().show()
    }

    private fun showSortDialog() {
        (mSectionsPagerAdapter?.currentFragment as? SummaryFragment)?.showSortDialog()
//        showInterstitial()

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

        override fun setPrimaryItem(container: ViewGroup?, position: Int, obj: Any?) {
            if (currentFragment != obj) currentFragment = obj as Fragment?
            super.setPrimaryItem(container, position, obj)
        }

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.getInstance(position)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 4
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
            const val ARG_SECTION_NUMBER = "section_number"
            private val fragments = ArrayList<Fragment>()


            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun getInstance(sectionNumber: Int): Fragment {
                if (fragments.size > sectionNumber) return fragments[sectionNumber]
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                val fragment = SummaryFragment.newInstance(4)
//                else PlaceholderFragment()
                fragment.arguments = args
                fragments.add(fragment)
                return fragment
            }
        }
    }
}
