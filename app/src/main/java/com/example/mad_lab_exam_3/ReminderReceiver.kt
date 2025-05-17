package com.example.mad_lab_exam_3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import sendDailyReminderNotification

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        sendDailyReminderNotification(context)
    }
}
