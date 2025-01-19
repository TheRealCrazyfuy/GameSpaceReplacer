package com.abeja.gamecenterreplacer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BackgroundService : Service() {
    private val targetAppPackage = serviceData.appTargetPackage
    private val targetAppName = serviceData.appTargetName
    private val triggerAppPackage = "cn.nubia.gamelauncher"
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //requestUsageStatsPermission()

        val channelId = "GameCenterReplacer"
        val channelName = "Background Service"
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Red switch set to: $targetAppName")
            //.setContentText("Long press this notification to hide.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)

        serviceScope.launch {
            monitorApps().collect { event ->
                //Log.d("BackgroundService", "Event: ${event.packageName}")
                if ( event.packageName == triggerAppPackage) {
                    //Log.d("BackgroundService", "Trigger app detected")
                    //startOrResumeTargetApp()
                }
            }
        }

        return START_STICKY
    }

    private fun monitorApps(): Flow<UsageEvents.Event> = flow {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        while (true) {
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 5000
            val events = usageStatsManager.queryEvents(beginTime, endTime)
            val event = UsageEvents.Event()
            var currentForegroundApp: String? = null

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    currentForegroundApp = event.packageName
                }
                emit(event)
            }

            if (currentForegroundApp == triggerAppPackage) {
                Log.d("BackgroundService", "Trigger app is currently on screen")
                startOrResumeTargetApp()
            }

            delay(1000) // TODO: get rid of this delay while not tanking the battery life //
        }
    }

    private fun startOrResumeTargetApp() {
        val needKillGameLauncher = Settings.Global.getInt(contentResolver, "gcs_need_kill_game_launcher", 0)
        if (needKillGameLauncher == 0) { // check if the competitive key is on too
            Log.d("BackgroundService", "Attempting to start target app: $targetAppPackage")
            val launchIntent = targetAppPackage?.let { packageManager.getLaunchIntentForPackage(it) }
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Log.d("BackgroundService", "Launching target app: $targetAppPackage")
                startActivity(launchIntent)
            } else {
                Log.e("BackgroundService", "Failed to find launch intent for package: $targetAppPackage")
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}