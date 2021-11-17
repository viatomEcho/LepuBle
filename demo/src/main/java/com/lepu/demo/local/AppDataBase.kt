package com.lepu.demo.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.data.entity.PatientEntity
import com.lepu.demo.data.entity.RecordEntity

@Database(
    entities = [DeviceEntity::class, RecordEntity::class, PatientEntity::class],
    version = 1, exportSchema = false
)
@TypeConverters(value = [LocalTypeConverter::class])
abstract class AppDataBase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao
    abstract fun recordDao(): RecordDao
    abstract fun patientDao(): PatientDao

}
