package com.abeja.gamecenterreplacer

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModel : ViewModel() {
    private val _isUsageStatsPermissionGranted = MutableLiveData<Boolean>()
    val isUsageStatsPermissionGranted: LiveData<Boolean> get() = _isUsageStatsPermissionGranted

    private val _isOnTopPermissionGranted = MutableLiveData<Boolean>()
    val isOnTopPermissionGranted: LiveData<Boolean> get() = _isOnTopPermissionGranted

    private val _appHasBeenChoosed = MutableLiveData<Boolean>()
    val appHasBeenChoosed: LiveData<Boolean> get() = _appHasBeenChoosed

    private val _appTargetName = MutableLiveData<String>()
    val appTargetName: LiveData<String> get() = _appTargetName

    fun getServiceData() : serviceData {
        return serviceData
    }

    fun onLaunch(context: Context) {
        val appTargetPackage = getPreferences(context, "appTargetPackage") ?: ""
        val appTargetName = getPreferences(context, "appTargetName") ?: ""

        if (appTargetPackage.isNotEmpty() && appTargetName.isNotEmpty()) { // only load preferences if an app has been already saved
            setApptarget(appTargetPackage, appTargetName, context)
        } else {
            Log.d("ViewModel", "No app target set initially.")
        }
        Log.d("ViewModel", "Launched viewmodel")
        _isUsageStatsPermissionGranted.value = isUsageStatsPermissionGranted(context)
        _isOnTopPermissionGranted.value = isOnTopPermissionGranted(context)
    }

    fun setApptarget(packageName: String, appName: String, context: Context) {
        serviceData.appTargetPackage = packageName
        serviceData.appTargetName = appName
        _appHasBeenChoosed.value = getApptargetPackage() != null
        _appTargetName.value = getApptargetName() ?: ""
        // Save preferences
        savePreferences(context, "appTargetPackage", packageName)
        savePreferences(context, "appTargetName", appName)
        if (checkifServiceisRunning(context, BackgroundService::class.java)) { // we restart the service if it is running
            stopService(context)
            startService(context)
        }
    }

    fun getApptargetPackage(): String? {
        return serviceData.appTargetPackage
    }

    fun getApptargetName(): String? {
        return serviceData.appTargetName
    }

    private fun startService(context: Context) {
        val serviceIntent = Intent(context, BackgroundService::class.java)
        context.startForegroundService(serviceIntent)
        serviceData.serviceStatus = checkifServiceisRunning(context, BackgroundService::class.java)
    }

    private fun stopService(context: Context) {
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

    fun getLaunchableApps(context: Context): List<ResolveInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivities(intent, 0)
    }

    fun openGitHubRepository(context: Context, repositoryUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repositoryUrl))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun savePreferences(context: Context, key: String, value: String) {
        val sharedPref = context.getSharedPreferences("com.abeja.gamecenterreplacer", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(key, value)
            apply()
        }
    }

    fun getPreferences(context: Context, key: String): String? {
        val sharedPref = context.getSharedPreferences("com.abeja.gamecenterreplacer", Context.MODE_PRIVATE)
        return sharedPref.getString(key, null)
    }

}