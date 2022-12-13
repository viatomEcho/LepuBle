package com.lepu.demo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * author: wujuan
 * description:
 */
@Entity(tableName = "patient")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    var patientId: Long = 0,
    var name: String,
    var age: String,
    var gender: String,
    var height: String,
    var weight: String,
    var medicalHistory: String,
    var symptom: List<String>,
    var remark: String
)
