package com.example.sampletwistontime

import android.content.Context
import android.preference.PreferenceManager
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class SettingsUtil {
    companion object {
        fun getTimerStringFromFile(context: Context):String{
            val file = File(context.filesDir, "timersettings.txt")
            val fileReader = FileReader(file)
            val bufferedReader = BufferedReader(fileReader)
            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            var json = stringBuilder.toString()
            return json
        }

        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.example.sampletwistontime.previous_timer_length"
        fun getPreviousTimerLengthSeconds(context: Context) : Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, 0)
        }

        fun setPreviousTimerLengthSeconds(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID, seconds)
            editor.apply()
        }

        private  const val TIMER_STATE_ID = "com.example.sampletwistontime.timer_state"

        fun getTimerState(context: Context): TimerActivity.TimerState{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return TimerActivity.TimerState.values()[ordinal]
        }

        fun setTimerState(state: TimerActivity.TimerState, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID = "com.example.sampletwistontime.seconds_remaining"
        fun getSecondsRemaining(context: Context) : Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        private const val TIMER_SET_TIME_ID = "com.example.sampletwistontime.backgrounded_time"

        fun getTimerSetTime(context: Context):Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(TIMER_SET_TIME_ID, 0)
        }

        fun setTimerSetTime(time: Long, context: Context) {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(TIMER_SET_TIME_ID, time)
            editor.apply()

        }

    }
}