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
    private var highTempThreshold: Int? = null
    private var lowTempThreshold: Int? = null
    private var measurementType: TemperatureMeasurement = TemperatureMeasurement.CELSIUS

    fun setMeasurementType(type: TemperatureMeasurement) {
        measurementType = type
    }

    private val runnableService: Runnable = object : Runnable {
        override fun run() {

            val tempChecker = TempAsyncTask()
            val tempReading = tempChecker.execute().get()
            tempReading?.let {
                val currentTemp = it.temp

                if (currentTemp < lowTempThreshold!! && prevTemps.all { t -> t >= lowTempThreshold!! }) {
                    notify("Low Temperature","The temperature has gone below $lowTempThreshold degrees.")

                } else if (currentTemp > highTempThreshold!! && prevTemps.all { t -> t <= highTempThreshold!!}) {
                    notify("High Temperature","The temperature has gone above $highTempThreshold degrees.")

                }
                Log.d("asdf", currentTemp.toString())

                prevTemps.add(currentTemp)
                if (prevTemps.size > numOfPreviousReadings) prevTemps.remove(0)
            }

            handler.postDelayed(this, defaultInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.extras?.let {
            highTempThreshold = it.getInt("highTempThreshold")
            lowTempThreshold = it.getInt("lowTempThreshold")

        }
        handler = Handler()
        handler.post(runnableService)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


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
