package com.example.activitymonitorapp.ui.chart

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.activitymonitorapp.data.db.AppDatabase
import com.example.activitymonitorapp.databinding.ActivityChartBinding
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding
    private val dao by lazy { AppDatabase.getInstance(this).sensorDao() }

    private var filterMinutes: Int = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChart()
        setupButtons()
        observeLiveData()
    }

    private fun setupButtons() {
        binding.btnBackChart.setOnClickListener { finish() }

        binding.btnHour.setOnClickListener {
            filterMinutes = 60
            binding.txtFilterLabel.text = "Дані за останню годину"
        }

        binding.btnDay.setOnClickListener {
            filterMinutes = 60 * 24
            binding.txtFilterLabel.text = "Дані за останній день"
        }
    }

    private fun observeLiveData() {
        lifecycleScope.launch {
            dao.getAllFlow().collectLatest { allData ->

                val now = System.currentTimeMillis()
                val fromTime = now - filterMinutes * 60_000L

                val filtered = allData.filter { it.timestamp >= fromTime }

                updateChart(filtered)
            }
        }
    }

    private fun setupChart() {
        val chart = binding.chart

        chart.setBackgroundColor(Color.WHITE)
        chart.setNoDataText("Дані відсутні")

        val desc = Description()
        desc.text = "Рухова активність"
        chart.description = desc

        chart.axisRight.isEnabled = false
        chart.axisLeft.textColor = Color.BLACK

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.textColor = Color.BLACK
        chart.xAxis.labelRotationAngle = -45f

        chart.setPinchZoom(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        chart.legend.isEnabled = true
        chart.legend.textColor = Color.BLACK
        chart.legend.textSize = 14f
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.setDrawInside(false)
        chart.extraBottomOffset = 16f
    }

    private fun updateChart(data: List<com.example.activitymonitorapp.data.entity.SensorData>) {
        if (data.isEmpty()) {
            binding.chart.clear()
            binding.chart.invalidate()
            return
        }

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        data.forEachIndexed { index, item ->
            entries.add(Entry(index.toFloat(), item.magnitude))
            labels.add(formatter.format(Date(item.timestamp)))
        }

        val dataSet = LineDataSet(entries, "Прискорення (magnitude)").apply {
            color = Color.BLUE
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chart.data = LineData(dataSet)
        binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        binding.chart.invalidate()
    }
}