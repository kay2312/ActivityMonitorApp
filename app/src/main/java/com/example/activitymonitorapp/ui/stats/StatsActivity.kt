package com.example.activitymonitorapp.ui.stats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import com.example.activitymonitorapp.data.db.AppDatabase
import com.example.activitymonitorapp.databinding.ActivityStatsBinding
import com.example.activitymonitorapp.utils.CsvExporter
import kotlinx.coroutines.launch

class StatsActivity : ComponentActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(this).sensorDao()

        lifecycleScope.launch {
            dao.minFlow().collect { binding.txtMin.text = it?.toString() ?: "0" }
        }
        lifecycleScope.launch {
            dao.maxFlow().collect { binding.txtMax.text = it?.toString() ?: "0" }
        }
        lifecycleScope.launch {
            dao.avgFlow().collect { binding.txtAvg.text = it?.toString() ?: "0" }
        }
        lifecycleScope.launch {
            dao.countFlow().collect { binding.txtCount.text = it.toString() }
        }

        binding.btnBackStats.setOnClickListener { finish() }

        binding.btnShareCsv.setOnClickListener { shareCsv() }

        binding.btnClearOld.setOnClickListener {
            lifecycleScope.launch {
                val limit = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
                dao.deleteOlderThan(limit)
                Toast.makeText(this@StatsActivity,
                    "Дані старші за 24 години видалено",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareCsv() {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(this@StatsActivity).sensorDao()
            val data = dao.getFromTime(0)

            if (data.isEmpty()) {
                Toast.makeText(this@StatsActivity, "Немає даних для експорту", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val file = CsvExporter.export(this@StatsActivity, data)
                val uri = FileProvider.getUriForFile(
                    this@StatsActivity,
                    "${packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, "Поділитися CSV"))
            } catch (e: Exception) {
                Toast.makeText(this@StatsActivity, "Помилка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}