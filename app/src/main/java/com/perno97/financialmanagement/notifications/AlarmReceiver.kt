package com.perno97.financialmanagement.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.perno97.financialmanagement.MainActivity
import com.perno97.financialmanagement.R

class AlarmReceiver : BroadcastReceiver() {

    private val channelId: String = "incoming_movements_channel"
    private val logTag = "AlarmReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            var title = ""
            var category = ""
            var euroAmount = ""

            if (intent != null) {
                title = intent.getStringExtra("incomingMovTitle") ?: ""
                category = intent.getStringExtra("incomingMovCategory") ?: ""
                val amount = intent.getFloatExtra("incomingMovAmount", 0f)

                euroAmount = context.getString(R.string.euro_value, amount)
            }

            val i = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = "ACTION_INCOMING_MOVEMENTS"
            }

            val notificationTitle =
                if (intent != null && title.isNotEmpty() && category.isNotEmpty() && euroAmount.isNotEmpty())
                    context.getString(R.string.notification_title, euroAmount, title, category)
                else context.getString(R.string.notification_new_incoming)

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_baseline_attach_money_24)
                .setContentTitle(notificationTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(0, builder.build())
            }
        } else {
            Log.e(logTag, "Context is null")
        }
    }
}