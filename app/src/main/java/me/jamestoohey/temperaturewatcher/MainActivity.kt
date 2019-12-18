package me.jamestoohey.temperaturewatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

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
    }



}

