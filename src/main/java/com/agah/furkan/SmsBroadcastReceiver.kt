package com.agah.furkan

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsBroadcastReceiver : BroadcastReceiver() {

    private var listener: SmsListener? = null

    fun setListener(listener: SmsListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == SmsRetriever.SMS_RETRIEVED_ACTION) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    /* automatic sms verification sample
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                    message?.let { listener?.onSmsReceived(it) }
                    */

                    //one time consent sample
                    val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)

                    try {
                        // Start activity to show consent dialog to user, activity must be started in
                        // 5 minutes, otherwise you'll receive another TIMEOUT intent
                        listener?.startActivityForResult(consentIntent)
                    } catch (e: ActivityNotFoundException) {
                        // Handle the exception ...
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    listener?.onSmsTimeOut()
                }
            }
        }
    }

    interface SmsListener {
        fun onSmsReceived(message: String)
        fun onSmsTimeOut()
        fun startActivityForResult(consentIntent: Intent?)
    }
}
