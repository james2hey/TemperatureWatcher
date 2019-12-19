package me.jamestoohey.temperaturewatcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var highTempThresholdText: EditText
    private lateinit var lowTempThresholdText: EditText
    private lateinit var cityText: EditText
    private lateinit var saveButton: Button
    private lateinit var notificationsSwitch: Switch
    private lateinit var tempMeasurementSpinner: Spinner

    private var highTempThreshold = 18
    private var lowTempThreshold = 18
    private var city = "Auckland"
    private var serviceIntent: Intent? = null
    private var measurementType = TemperatureMeasurement.CELSIUS


    // If the given temperature changes above/below a threshold, send a notification.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        highTempThresholdText = findViewById(R.id.high_temp_threshold)
        lowTempThresholdText = findViewById(R.id.low_temp_threshold)
        cityText = findViewById(R.id.city)
        saveButton = findViewById(R.id.save_button)
        tempMeasurementSpinner = findViewById(R.id.spinner)
        notificationsSwitch = findViewById(R.id.notificationsSwitch)


        saveButton.setOnClickListener {
//            highTempThreshold = highTempThresholdText.text.toString().toInt()
//            lowTempThreshold = lowTempThresholdText.text.toString().toInt()
//            city = lowTempThresholdText.text.toString()

            val tempChecker = TempAsyncTask()
            val tempReading = tempChecker.execute().get()
            tempReading?.let {
                Log.d("asdf", it.temp.toString())
            }
        }

        createNotificationChannel()

        serviceIntent = Intent(this, TempService::class.java).apply {
            putExtra("highTempThreshold", highTempThreshold)
            putExtra("lowTempThreshold", lowTempThreshold)
        }

        val temperatureMeasurementItems = arrayOf(TemperatureMeasurement.CELSIUS, TemperatureMeasurement.FAHRENHEIT, TemperatureMeasurement.KELVIN)
        val adapter = ArrayAdapter<TemperatureMeasurement>(this, R.layout.support_simple_spinner_dropdown_item, temperatureMeasurementItems)
        tempMeasurementSpinner.adapter = adapter
//
//        tempMeasurementSpinner.onItemSelectedListener

        notificationsSwitch.setOnCheckedChangeListener { _, isOn ->
            if (isOn) startForegroundService(serviceIntent) else stopService(serviceIntent)
        }
    }

    private fun createNotificationChannel() {
        val name = "Temperature Checker Notifications"
        val descriptionText = "Receive notifications that the current temperature has gone above/below your given threshold."
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(Notification.CATEGORY_EVENT, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            measurementType = it.getItemAtPosition(position) as TemperatureMeasurement
            serviceIntent?.putExtra("measurementType", measurementType)
        }
    }


}

