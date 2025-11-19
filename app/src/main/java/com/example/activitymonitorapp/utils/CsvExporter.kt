package com.example.activitymonitorapp.utils

import android.content.Context
import com.example.activitymonitorapp.data.entity.SensorData
import java.io.File

object CsvExporter {

    fun export(context: Context, data: List<SensorData>): File {
        val file = File(context.cacheDir, "activity_data.csv")
        file.printWriter().use { out ->
            out.println("timestamp,magnitude")
            data.forEach {
                out.println("${it.timestamp},${it.magnitude}")
            }
        }
        return file
    }
}