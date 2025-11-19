package com.example.activitymonitorapp.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.activitymonitorapp.data.db.AppDatabase
import com.example.activitymonitorapp.data.entity.SensorData
import kotlinx.coroutines.*
import kotlin.math.sqrt

class SensorService(
    private val context: Context,
    private val intervalSeconds: Long = 2
) : SensorEventListener {

    private val dao = AppDatabase.getInstance(context).sensorDao()

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var job: Job? = null
    private var lastMagnitude = 0f

    var onValueChanged: ((Float) -> Unit)? = null

    fun startRecording() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                dao.insert(
                    SensorData(
                        timestamp = System.currentTimeMillis(),
                        magnitude = lastMagnitude
                    )
                )
                delay(intervalSeconds * 1000)
            }
        }
    }

    fun stopRecording() {
        sensorManager.unregisterListener(this)
        job?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        lastMagnitude = sqrt(x * x + y * y + z * z)

        onValueChanged?.invoke(lastMagnitude)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}