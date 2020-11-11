package com.example.sampletwistontime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        NotificationUtil.timerFinished(context)
        SettingsUtil.setTimerState(TimerActivity.TimerState.Stopped, context)
        SettingsUtil.setTimerSetTime(0, context)
    }
}
