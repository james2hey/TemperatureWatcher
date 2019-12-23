package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log


class TempService: Service() {
    private lateinit var handler: Handler
    private val defaultInterval: Long = 10 * 1000 // todo make it every 60 seconds
    private var prevTemps: ArrayList<Int> = arrayListOf()
    private val numOfPreviousReadings = 30
    private var mainInput: MainInput? = null

    private val runnableService: Runnable = object : Runnable {
        override fun run() {

            val tempChecker = TempAsyncTask()
            val tempReading = tempChecker.execute().get()
            tempReading?.let {tr ->
                val currentTemp = tr.temp
                mainInput?.let {
                    if (currentTemp < it.low && prevTemps.all { t -> t >= it.low }) {
                        notify("Low Temperature","The temperature has gone below ${it.low} degrees.")
                    } else if (currentTemp > it.high && prevTemps.all { t -> t <= it.high}) {
                        notify("High Temperature","The temperature has gone above ${it.high} degrees.")
                    }
                    Log.d("asdf", currentTemp.toString())

                    prevTemps.add(currentTemp)
                    if (prevTemps.size > numOfPreviousReadings) prevTemps.remove(0)
                }
            }
            handler.postDelayed(this, defaultInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.extras?.let {
            val highTempThreshold = it.getDouble("highTempThreshold")
            val lowTempThreshold = it.getDouble("lowTempThreshold")
            val city = it.getString("city")
            val measurementType = it.get("measurementType") as TemperatureMeasurement
            mainInput = MainInput(lowTempThreshold, highTempThreshold, measurementType, city!!)
        }
        handler = Handler()
        handler.post(runnableService)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? = null


    override fun onDestroy() {
        handler.removeCallbacks(runnableService)
        stopSelf()
        super.onDestroy()
    }

    private fun notify(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        val notification = Notification.Builder(this, Notification.CATEGORY_EVENT).run {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(intent)
            setAutoCancel(true)
            build()
        }
        startForeground(1, notification)
    }

}
