package com.abeja.gamecenterreplacer.observers

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings

class GameSwitchObserver(
    private val context: Context,
    private val onGameSwitchChanged: (Boolean) -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // 0 = on, 1 = off
        val isGameSwitchOn = Settings.Global.getInt(context.contentResolver, "gcs_need_kill_game_launcher", 0) == 0
        onGameSwitchChanged(isGameSwitchOn)
    }

    fun register() {
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor("gcs_need_kill_game_launcher"),
            false,
            this
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }
}