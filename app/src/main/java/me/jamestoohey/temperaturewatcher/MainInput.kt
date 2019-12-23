package me.jamestoohey.temperaturewatcher

data class MainInput(val low: Double, val high: Double, val measurementType: TemperatureMeasurement, val city: String)