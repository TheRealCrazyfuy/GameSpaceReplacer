package com.abeja.gamecenterreplacer

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import java.util.Objects

class ViewModel : ViewModel() {

    fun getServiceData() : serviceData {
        return serviceData
    }

    fun setApptarget(packageName: String, context: Context) {
        serviceData.appTargetPackage = packageName
        if (checkifServiceisRunning(context, BackgroundService::class.java)) {
            stopService(context)
            startService(context)
        }
    }

    fun startService(context: Context) {
        val serviceIntent = Intent(context, BackgroundService::class.java)
        context.startForegroundService(serviceIntent)
        serviceData.serviceStatus = checkifServiceisRunning(context, BackgroundService::class.java)
    }

    fun stopService(context: Context) {
        val serviceIntent = Intent(context, BackgroundService::class.java)
        context.stopService(serviceIntent)
        serviceData.serviceStatus = checkifServiceisRunning(context, BackgroundService::class.java)
    }

    fun checkifServiceisRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun setServiceStatus(context: Context, serviceStatus: Boolean) {
        Log.d("ViewModel", "Service status: $serviceStatus")
        serviceData.serviceStatus = serviceStatus
        if (serviceStatus) {
            startService(context)
        } else {
            stopService(context)
        }
    }

    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    fun isOnTopPermissionGranted(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                android.os.Process.myUid(),
                context.packageName
            )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestOnTopPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        context.startActivity(intent)
    }
}