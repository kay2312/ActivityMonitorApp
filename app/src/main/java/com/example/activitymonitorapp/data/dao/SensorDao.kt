package com.example.activitymonitorapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.activitymonitorapp.data.entity.SensorData
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {

    @Insert
    suspend fun insert(item: SensorData)

    @Query("SELECT * FROM sensor_data ORDER BY timestamp ASC")
    fun getAllFlow(): Flow<List<SensorData>>

    @Query("SELECT MIN(magnitude) FROM sensor_data")
    fun minFlow(): Flow<Float?>

    @Query("SELECT MAX(magnitude) FROM sensor_data")
    fun maxFlow(): Flow<Float?>

    @Query("SELECT AVG(magnitude) FROM sensor_data")
    fun avgFlow(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM sensor_data")
    fun countFlow(): Flow<Int>

    @Query("SELECT * FROM sensor_data WHERE timestamp >= :time ORDER BY timestamp ASC")
    suspend fun getFromTime(time: Long): List<SensorData>

    @Query("DELETE FROM sensor_data WHERE timestamp < :limit")
    suspend fun deleteOlderThan(limit: Long)
}
