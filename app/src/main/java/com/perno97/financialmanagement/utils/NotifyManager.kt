package com.perno97.financialmanagement.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.perno97.financialmanagement.notifications.AlarmReceiver
import java.time.LocalDate
import java.time.ZoneId

object NotifyManager {
    fun setAlarm(
        context: Context,
        incomingMovementId: Int,
        title: String,
        category: String,
        amount: Float,
        date: LocalDate
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_INCOMING_MOVEMENT_ALARM"
            putExtra("incomingMovTitle", title)
            putExtra("incomingMovCategory", category)
            putExtra("incomingMovAmount", amount)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            incomingMovementId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            date.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant()
                .toEpochMilli(),
            pendingIntent
        )
    }

    fun removeAlarm(
        context: Context,
        incomingMovementId: Int,
        title: String,
        category: String,
        amount: Float
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_INCOMING_MOVEMENT_ALARM"
            putExtra("incomingMovTitle", title)
            putExtra("incomingMovCategory", category)
            putExtra("incomingMovAmount", amount)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            incomingMovementId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}