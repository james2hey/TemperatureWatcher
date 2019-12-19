package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {

    private lateinit var highTempThresholdText: EditText
    private lateinit var lowTempThresholdText: EditText
    private lateinit var cityText: EditText
    private lateinit var saveButton: Button

    private var highTempThreshold = 0
    private var lowTempThreshold = 0
    private var city = "Auckland"

    // If the given temperature changes above/below a threshold, send a notification.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        highTempThresholdText = findViewById(R.id.high_temp_threshold)
        lowTempThresholdText = findViewById(R.id.low_temp_threshold)
        cityText = findViewById(R.id.city)
        saveButton = findViewById(R.id.save_button)


        saveButton.setOnClickListener {
//            highTempThreshold = highTempThresholdText.text.toString().toInt()
//            lowTempThreshold = lowTempThresholdText.text.toString().toInt()
//            city = lowTempThresholdText.text.toString()

            val tempChecker = TempChecker()
            val tempReading = tempChecker.execute().get()
            tempReading?.let {
                Log.d("asdf", it.temp.toString())
            }


        }

        createNotificationChannel()

        val serviceIntent = Intent(this, TempService::class.java)
        serviceIntent.putExtra("highTempThreshold", highTempThreshold)
        serviceIntent.putExtra("lowTempThreshold", lowTempThreshold)
        startForegroundService(serviceIntent)
    }

    private fun createNotificationChannel() {
        val name = "Temperature Checker Notifications"
        val descriptionText = "Receive notifications that the current temperature has gone above/below your given threshold."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(Notification.CATEGORY_ALARM, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }



}

