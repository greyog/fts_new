package com.greyogproducts.greyog.fts

import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Spinner
import com.greyogproducts.greyog.fts.adapters.NotificationListAdapter
import com.greyogproducts.greyog.fts.data.NotificationData
import com.greyogproducts.greyog.fts.data.TrendCondition
import com.greyogproducts.greyog.fts.vm.NotificationsListViewModel
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.content_notifications.*

class NotificationListActivity : AppCompatActivity(), NotificationListAdapter.OnListInteractionListener {
    override fun onListInteraction(item: NotificationData?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val viewModel: NotificationsListViewModel by lazy {
        ViewModelProviders.of(this)[NotificationsListViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setSupportActionBar(toolbar)

        fabNewNotification.setOnClickListener { view ->
            newNotification()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        list_notifications.adapter = NotificationListAdapter(viewModel, this)

    }

    private fun newNotification() {
        val builder = AlertDialog.Builder(this)
//        val v = layoutInflater.inflate(R.layout.about_layout, null)
        builder.setTitle(R.string.new_notification)
        val view = LayoutInflater.from(this).inflate(R.layout.notification_edit, null)
        builder.setView(view)
        val listener = DialogInterface.OnClickListener { dialogInterface, i ->
            fun textToCondition(text: String): TrendCondition {
                return when (text) {
                    "Buy" -> TrendCondition.BUY
                    "Sell" -> TrendCondition.SELL
                    "Any Buy" -> TrendCondition.ANY_BUY
                    "Any Sell" -> TrendCondition.ANY_SELL
                    "Neutral" -> TrendCondition.NEUTRAL
                    "Strong Buy" -> TrendCondition.STRONG_BUY
                    "Strong Sell" -> TrendCondition.STRONG_SELL
                    else -> TrendCondition.ANY
                }
            }

            val data = NotificationData(view.findViewById<EditText>(R.id.etId).text.toString(),
                    view.findViewById<EditText>(R.id.etSymbol).text.toString(),
                    view.findViewById<EditText>(R.id.etDescription).text.toString(),
                    textToCondition(view.findViewById<Spinner>(R.id.spinner5min).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinner15min).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinner1HOUR).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinner5HOUR).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinnerDAY).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinnerWEEK).selectedItem.toString()),
                    textToCondition(view.findViewById<Spinner>(R.id.spinnerMONTH).selectedItem.toString())
            )
            viewModel.addItem(data)
        }
        builder.setPositiveButton(R.string.ok, listener)
        builder.setNegativeButton(R.string.cancel, null)
        builder.create().show()
    }

}
