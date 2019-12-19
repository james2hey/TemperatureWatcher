package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import java.util.*


class TempService: Service() {
    private lateinit var handler: Handler
    private val defaultInterval: Long = 10 * 1000 // todo make it every 60 seconds
    private var prevTemps: ArrayList<Int> = arrayListOf()
    private var highTempThreshold: Int? = null
    private var lowTempThreshold: Int? = null

    private val runnableService: Runnable = object : Runnable {
        override fun run() {

            val tempChecker = TempAsyncTask()
            val tempReading = tempChecker.execute().get()
            tempReading?.let {
                val currentTemp = it.temp

                if (currentTemp < lowTempThreshold!! && prevTemps.all { t -> t >= lowTempThreshold!! }) {
                    notify("Temperature has gone below your low threshold.")

                } else if (currentTemp > highTempThreshold!! && prevTemps.all { t -> t <= highTempThreshold!!}) {
                    notify("Temperature has gone above your high threshold.")

                }
                Log.d("asdf", currentTemp.toString())

                prevTemps.add(currentTemp)
                if (prevTemps.size > 10) prevTemps.remove(0)
            }


            handler.postDelayed(this, defaultInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.extras?.let {
            highTempThreshold = it.getInt("highTempThreshold")
            lowTempThreshold = it.getInt("lowTempThreshold")
        }

        becomeForegroundService()
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

    private fun becomeForegroundService() {
        val intent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        val notification = Notification.Builder(this, Notification.CATEGORY_ALARM).run {
            setSmallIcon(R.drawable.ic_launcher_background)
            setContentTitle("Temperature Watcher")
            setContentText("Notifications for temperature.")
            setContentIntent(intent)
            setAutoCancel(true)
            build()
        }

        startForeground(1, notification)
    }

    private fun notify(message: String) {
        val notification = Notification.Builder(this, Notification.CATEGORY_ALARM).run {
            setSmallIcon(R.drawable.ic_launcher_background)
            setContentTitle("Temperature Watcher")
            setContentText(message)
            setAutoCancel(true)
        }

        with(NotificationManagerCompat.from(this)) {
            notify(1, notification.build())
        }
    }

}
