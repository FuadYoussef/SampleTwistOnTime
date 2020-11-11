package com.example.sampletwistontime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when(intent.action) {
            AppConstants.ACTION_STOP -> {
                TimerActivity.clearTimer(context)
                SettingsUtil.setTimerState(TimerActivity.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
            }
            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = SettingsUtil.getSecondsRemaining(context)
                val alarmSetTime = SettingsUtil.getTimerSetTime(context)
                val nowSeconds = TimerActivity.currTime

                secondsRemaining -= nowSeconds - alarmSetTime
                SettingsUtil.setSecondsRemaining(secondsRemaining, context)

                TimerActivity.clearTimer(context)
                SettingsUtil.setTimerState(TimerActivity.TimerState.Paused, context)
                NotificationUtil.timerPaused(context)
            }
            AppConstants.ACTION_RESUME -> {
                var secondsRemaining = SettingsUtil.getSecondsRemaining(context)
                val wakeUpTime = TimerActivity.setTimer(context, TimerActivity.currTime, secondsRemaining)
                SettingsUtil.setTimerState(TimerActivity.TimerState.Running, context)
                NotificationUtil.timerRunning(context, wakeUpTime)
            }
            AppConstants.ACTION_START -> {
                var jsonObject = JSONObject(SettingsUtil.getTimerStringFromFile(context))
                val minutesRemaining = Integer.parseInt(jsonObject.get("Duration").toString())
                //val minutesRemaining = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = TimerActivity.setTimer(context, TimerActivity.currTime, secondsRemaining)
                SettingsUtil.setTimerState(TimerActivity.TimerState.Running, context)
                SettingsUtil.setSecondsRemaining(secondsRemaining, context)
                NotificationUtil.timerRunning(context, wakeUpTime)
            }
        }

    }
}
