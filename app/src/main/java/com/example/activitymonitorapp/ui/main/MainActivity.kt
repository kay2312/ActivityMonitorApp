package com.example.activitymonitorapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.activitymonitorapp.databinding.ActivityMainBinding
import com.example.activitymonitorapp.sensor.SensorService
import com.example.activitymonitorapp.ui.chart.ChartActivity
import com.example.activitymonitorapp.ui.stats.StatsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorService: SensorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorService = SensorService(this)

        sensorService.onValueChanged = { value ->
            runOnUiThread {
                binding.txtCurrentValue.text = "Поточне значення: %.2f".format(value)
            }
        }

        binding.btnStart.setOnClickListener {
            sensorService.startRecording()
            binding.txtStatus.text = "Акселерометр: запис активний"
        }

        binding.btnStop.setOnClickListener {
            sensorService.stopRecording()
            binding.txtStatus.text = "Акселерометр: запис зупинено"
        }

        binding.btnChart.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        binding.btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorService.stopRecording()
    }
}