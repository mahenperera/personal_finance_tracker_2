import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun sendBudgetNotification(context: Context, message: String) {
    val channelId = "budget_channel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Budget Alerts"
        val description = "Notifies when budget is nearing/exceeded"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Budget Alert")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    NotificationManagerCompat.from(context).notify(1, builder.build())
}

private const val REMINDER_CHANNEL_ID = "daily_reminder_channel"

fun sendDailyReminderNotification(context: Context) {
    createChannelIfNeeded(
        context,
        REMINDER_CHANNEL_ID,
        "Daily Reminders",
        "Reminds user to log daily expenses"
    )

    val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Daily Expense Reminder")
        .setContentText("Don’t forget to log today’s expenses!")
        .setStyle(NotificationCompat.BigTextStyle().bigText("Don’t forget to log today’s expenses!"))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    NotificationManagerCompat.from(context).notify(1002, builder.build())
}

private fun createChannelIfNeeded(context: Context, channelId: String, name: String, description: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
