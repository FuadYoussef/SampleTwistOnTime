package com.example.sampletwistontime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_timer.*
import org.json.JSONObject
import java.util.*

class TimerActivity : AppCompatActivity() {
    companion object {
        fun setTimer(context: Context, currTime: Long, secondsRemaining: Long):Long {
            val wakeUpTime = (currTime+secondsRemaining)*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            SettingsUtil.setTimerSetTime(currTime, context)
            return  wakeUpTime
        }

        fun clearTimer(context:Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)
            alarmManager.cancel(pendingIntent)
            SettingsUtil.setTimerSetTime(0,context)
        }


        val currTime: Long
            get() = Calendar.getInstance().timeInMillis/1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthInSeconds = 0L
    private var timerState = TimerState.Stopped
    private var remainingTimeInSeconds = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
    }

    fun stopTimer(view: View) {
        timer.cancel()
        timerState = TimerState.Stopped
        onTimerFinished()

    }
    fun startTimer(view: View) {
        runTimer()
        timerState = TimerState.Running
        refreshButtons()
    }
    fun pauseTimer(view: View) {
        timer.cancel()
        timerState = TimerState.Paused
        refreshButtons()
    }

    override fun onResume() {
        super.onResume()
        initializeTimer()

        clearTimer(this)
        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()
        if(timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setTimer(this, currTime,remainingTimeInSeconds)
            NotificationUtil.timerRunning(this, wakeUpTime)
        } else if (timerState == TimerState.Paused) {
            NotificationUtil.timerPaused(this)
        }

        SettingsUtil.setPreviousTimerLengthSeconds(timerLengthInSeconds, this)
        SettingsUtil.setSecondsRemaining(remainingTimeInSeconds,this)
        SettingsUtil.setTimerState(timerState, this)
    }

    private fun initializeTimer() {
        timerState = SettingsUtil.getTimerState(this)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        if (timerState == TimerState.Running || timerState == TimerState.Paused) {
            remainingTimeInSeconds = SettingsUtil.getSecondsRemaining(this)
        } else {
            remainingTimeInSeconds = timerLengthInSeconds
        }

        val alarmSetTime = SettingsUtil.getTimerSetTime(this)
        if (alarmSetTime > 0) {
            remainingTimeInSeconds -= currTime - alarmSetTime
        }
        if (remainingTimeInSeconds <= 0) {
            onTimerFinished()
        }

        else if (timerState == TimerState.Running) {
            runTimer()
        }

        refreshButtons()
        updateTimerText()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        setNewTimerLength()
        SettingsUtil.setSecondsRemaining(timerLengthInSeconds, this)
        refreshButtons()
        remainingTimeInSeconds = timerLengthInSeconds
    }

    private fun runTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(remainingTimeInSeconds*1000, 1000) {
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeInSeconds = millisUntilFinished / 1000
                updateTimerText()

            }
        }.start()
    }

    private fun setNewTimerLength() {
        var jsonObject = JSONObject(SettingsUtil.getTimerStringFromFile(this))
        val lengthInMinutes = Integer.parseInt(jsonObject.get("Duration").toString())
        val timerName = jsonObject.get("TimerName").toString()
        timerNameLabel.text = timerName
        timerLengthInSeconds = (lengthInMinutes*60L)
    }

    private fun setPreviousTimerLength(){
        timerLengthInSeconds = SettingsUtil.getPreviousTimerLengthSeconds(this)
    }

    private fun updateTimerText() {
        val minutesUntilFinished = remainingTimeInSeconds/60
        val secondsInMinutesUntilFinished = remainingTimeInSeconds - minutesUntilFinished *60
        val secondsStr = secondsInMinutesUntilFinished.toString()
        val countText = findViewById<TextView>(R.id.count_text)
        countText.text = "$minutesUntilFinished:${
            if(secondsStr.length == 2) secondsStr
            else "0"+ secondsStr}"
    }

    private  fun refreshButtons() {
        when (timerState) {
            TimerState.Running -> {
                val startButton = findViewById<Button>(R.id.start_button)
                startButton.isEnabled = false
                val pauseButton = findViewById<Button>(R.id.pause_button)
                pauseButton.isEnabled = true
                val stopButton = findViewById<Button>(R.id.stop_button)
                stopButton.isEnabled = true
            }
            TimerState.Stopped -> {
                val startButton = findViewById<Button>(R.id.start_button)
                startButton.isEnabled = true
                val pauseButton = findViewById<Button>(R.id.pause_button)
                pauseButton.isEnabled = false
                val stopButton = findViewById<Button>(R.id.stop_button)
                stopButton.isEnabled = false
            }
            TimerState.Paused -> {
                val startButton = findViewById<Button>(R.id.start_button)
                startButton.isEnabled = true
                val pauseButton = findViewById<Button>(R.id.pause_button)
                pauseButton.isEnabled = false
                val stopButton = findViewById<Button>(R.id.stop_button)
                stopButton.isEnabled = true
            }
        }
    }
}