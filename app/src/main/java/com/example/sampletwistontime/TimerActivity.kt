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
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long):Long {
            val wakeUpTime = (nowSeconds+secondsRemaining)*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds, context)
            return  wakeUpTime
        }

        fun removeAlarm(context:Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context,0, intent, 0)
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }


        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis/1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L
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
        //start timer
        startTimerTutorial()
        timerState = TimerState.Running
        updateButtons()
    }
    fun pauseTimer(view: View) {
        timer.cancel()
        timerState = TimerState.Paused
        updateButtons()
    }

    override fun onResume() {
        super.onResume()
        initTimer()

        removeAlarm(this)
        NotificationUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()
        if(timerState == TimerState.Running) {
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds,secondsRemaining)
            NotificationUtil.showTimerRunning(this, wakeUpTime)
        } else if (timerState == TimerState.Paused) {
            NotificationUtil.showTimerPaused(this)
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        if (timerState == TimerState.Running || timerState == TimerState.Paused) {
            secondsRemaining = PrefUtil.getSecondsRemaining(this)
        } else {
            secondsRemaining = timerLengthSeconds
        }

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0) {
            secondsRemaining -= nowSeconds - alarmSetTime
        }
        if (secondsRemaining <= 0) {
            onTimerFinished()
        }

        else if (timerState == TimerState.Running) {
            startTimerTutorial()
        }

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        setNewTimerLength()
        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds
    }

    private fun startTimerTutorial() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining*1000, 1000) {
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()

            }
        }.start()
    }

    private fun setNewTimerLength() {
        var jsonObject = JSONObject(PrefUtil.getTimerLength(this))
        val lengthInMinutes = Integer.parseInt(jsonObject.get("Duration").toString())
        val timerName = jsonObject.get("TimerName").toString()
        timerNameLabel.text = timerName
        timerLengthSeconds = (lengthInMinutes*60L)
    }

    private fun setPreviousTimerLength(){
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
    }

    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining/60
        val secondsInMinutesUntilFinished = secondsRemaining - minutesUntilFinished *60
        val secondsStr = secondsInMinutesUntilFinished.toString()
        val count_text = findViewById<TextView>(R.id.count_text)
        count_text.text = "$minutesUntilFinished:${
            if(secondsStr.length == 2) secondsStr
            else "0"+ secondsStr}"
    }

    private  fun updateButtons() {
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