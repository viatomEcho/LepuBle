package com.lepu.demo.local

import androidx.room.*
import com.lepu.demo.data.entity.PatientEntity

/**
 * author: wujuan
 * description:
 */
@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPatient(patientEntity: PatientEntity): Long


    @Query("SELECT * FROM patient ORDER BY patientId DESC LIMIT 1")
    fun getCurrentPatient(): PatientEntity?




}