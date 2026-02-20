/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "fps_sessions")
data class FpsSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    
    // FPS Stats
    val avgFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val variance: Float,      // [BARU] Tingkat naik-turun FPS (Jitter)
    val smoothness: Float,    // [BARU] Persentase FPS stabil
    
    // Other Stats
    val avgTemp: Float,
    val maxTemp: Float,
    val avgWatt: Float,
    val maxWatt: Float,
    val avgRam: Int,
    val maxRam: Int
)

@Entity(
    tableName = "fps_points",
    foreignKeys = [ForeignKey(
        entity = FpsSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["sessionId"])]
)
data class FpsDataPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long, // Offset waktu (detik ke-0, ke-1, ...)
    val fps: Float,
    val cpuLoad: Int,
    val temp: Float,
    val watt: Float,
    val ramUsageMb: Int
)

@Dao
interface FpsDao {
    @Insert
    suspend fun insertSession(session: FpsSession): Long

    @Insert
    suspend fun insertDataPoints(points: List<FpsDataPoint>)

    @Query("SELECT * FROM fps_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<FpsSession>>

    @Query("SELECT * FROM fps_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSession(sessionId: Long): List<FpsDataPoint>

    @Query("DELETE FROM fps_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
    
    @Query("DELETE FROM fps_sessions")
    suspend fun clearAll()
}

@Database(entities = [FpsSession::class, FpsDataPoint::class], version = 2, exportSchema = false)
abstract class FpsDatabase : RoomDatabase() {
    abstract fun fpsDao(): FpsDao

    companion object {
        @Volatile
        private var INSTANCE: FpsDatabase? = null

        fun getDatabase(context: Context): FpsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FpsDatabase::class.java,
                    "zkm_fps_db"
                )
                .fallbackToDestructiveMigration() // Aman untuk development jika ganti versi DB
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
