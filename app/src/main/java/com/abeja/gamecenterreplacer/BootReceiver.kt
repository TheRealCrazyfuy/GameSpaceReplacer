package com.abeja.gamecenterreplacer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPref = context.getSharedPreferences("com.abeja.gamecenterreplacer", Context.MODE_PRIVATE)
            val startOnBoot = sharedPref.getString("startOnBoot", "false")?.toBoolean() ?: false

            if (startOnBoot) {
                val serviceIntent = Intent(context, BackgroundService::class.java)
                context.startForegroundService(serviceIntent)
                Log.d("BootReceiver", "Background service started on boot")
            }
        }
    }
}