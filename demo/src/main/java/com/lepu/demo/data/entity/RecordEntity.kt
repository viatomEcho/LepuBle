package com.lepu.demo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val startTime: Long,
        val endTime: Long,
        val name: String,
        val age: String,
        val gender: String,
        val patientId: Long,
)