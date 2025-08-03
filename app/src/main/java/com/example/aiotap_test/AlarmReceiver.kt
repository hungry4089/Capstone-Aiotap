package com.example.aiotap_test

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val builder = NotificationCompat.Builder(context, "default_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ 예약 알람")
            .setContentText("설정하신 시간이 되었습니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(1001, builder.build())
    }
}
