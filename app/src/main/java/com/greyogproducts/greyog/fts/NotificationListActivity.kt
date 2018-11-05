package com.greyogproducts.greyog.fts

import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.greyogproducts.greyog.fts.adapters.NotificationListAdapter
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.data.TrendCondition
import com.greyogproducts.greyog.fts.vm.NotificationsListViewModel
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.content_notifications.*

class NotificationListActivity : AppCompatActivity(), NotificationListAdapter.OnListInteractionListener {
    override fun onActionEdit(item: NotificationData?) {
        editNotification(item)
    }

    private val viewModel: NotificationsListViewModel by lazy {
        ViewModelProviders.of(this)[NotificationsListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setSupportActionBar(toolbar)

        fabNewNotification.setOnClickListener { view ->
            editNotification(null)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        list_notifications.adapter = NotificationListAdapter(viewModel, this)

    }

//    private fun showHasNotificationsAlert() {
//
//        val alertDialog = AlertDialog.Builder(this)
//        alertDialog.setMessage(getString(R.string.notification_service_alert))
//        alertDialog.setNegativeButton("No", null)
//        alertDialog.setPositiveButton("Yes, turn it on.") {dialog,i ->
//            switchOnNotifications(true)
//            dialog.dismiss()
//        }
//        alertDialog.setOnDismissListener { finish() }
//        alertDialog.create().show()
//    }

    private var mSwitchNotify: Switch? = null

//    private fun checkNotifyService() {
//        var hasNotifications = false
//        viewModel.notificationList.value?.let {
//            hasNotifications = it.isNotEmpty()
//        }
//        if (hasNotifications && !mSwitchNotify!!.isChecked) showHasNotificationsAlert()
//        if (!hasNotifications && mSwitchNotify!!.isChecked) {
//            switchOnNotifications(false)
//            finish()
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_notifications, menu)
        mSwitchNotify = menu.findItem(R.id.switchNotifications).actionView.findViewById(R.id.switcher)
        mSwitchNotify?.setOnClickListener {
            switchOnNotifications((it as Switch).isChecked)
            return@setOnClickListener
        }
        return true
    }

    private fun switchOnNotifications(checked: Boolean) {
        if (checked) {
//            println("mSwitchNotify on")
            viewModel.turnNotificationServiceOn(true)
        } else {
            viewModel.turnNotificationServiceOn(false)
//            println("mSwitchNotify off")
        }
    }

    private fun editNotification(nd: NotificationData?) {
        val builder = AlertDialog.Builder(this)
//        val v = layoutInflater.inflate(R.layout.about_layout, null)
        builder.setTitle(R.string.new_notification)
        val view = LayoutInflater.from(this).inflate(R.layout.notification_edit, null)
        if (nd != null) {
            println("${this.localClassName}: data to edit $nd")
            buildNotificationEditDialog(view, nd)
        }
//todo auto fill symbol text view
        builder.setView(view)
        val listener = DialogInterface.OnClickListener { dialogInterface, i ->
            if (view.findViewById<EditText>(R.id.etId).text.toString() == "") return@OnClickListener

            val data = NotificationData(view.findViewById<EditText>(R.id.etId).text.toString(),
                    view.findViewById<EditText>(R.id.etSymbol).text.toString(),
                    view.findViewById<EditText>(R.id.etDescription).text.toString(),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinner5min).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinner15min).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinner1HOUR).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinner5HOUR).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinnerDAY).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinnerWEEK).selectedItem.toString()),
                    TrendCondition.fromString(view.findViewById<Spinner>(R.id.spinnerMONTH).selectedItem.toString())
            )
            println("${this.localClassName}: data to save $data")
            viewModel.addItem(data)
        }
        builder.setPositiveButton(R.string.ok, listener)
        builder.setNegativeButton(R.string.cancel, null)
        builder.create().show()
    }

    private fun buildNotificationEditDialog(v: View, nd: NotificationData) {

        fun setSpinnerPosition(spinnerId: Int, tc: TrendCondition) {
            val spnr = v.findViewById<Spinner>(spinnerId)
            val conditions = (application.resources.getStringArray(R.array.notification_conditions))
            val pos = conditions.indexOf(tc.toString())
            spnr.setSelection(pos, true)
        }

        v.findViewById<EditText>(R.id.etId).setText(nd.pairId, TextView.BufferType.EDITABLE)
        v.findViewById<EditText>(R.id.etSymbol).setText(nd.symbol, TextView.BufferType.EDITABLE)
        v.findViewById<EditText>(R.id.etDescription).setText(nd.description, TextView.BufferType.EDITABLE)
        val spinnerIds = mapOf(
                R.id.spinner5min to nd.fiveMin,
                R.id.spinner15min to nd.fifteenMin,
                R.id.spinner1HOUR to nd.hour,
                R.id.spinner5HOUR to nd.fiveHour,
                R.id.spinnerDAY to nd.day,
                R.id.spinnerWEEK to nd.week,
                R.id.spinnerMONTH to nd.month
        )
        spinnerIds.map {
            setSpinnerPosition(it.key, it.value)
        }
    }
}
