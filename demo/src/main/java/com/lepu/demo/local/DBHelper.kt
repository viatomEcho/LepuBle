package com.lepu.demo.local

import android.content.Context
import androidx.room.Room
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.data.entity.PatientEntity
import com.lepu.demo.data.entity.RecordEntity
import com.lepu.demo.util.LpResult
import com.lepu.demo.util.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * author: wujuan
 * created on: 2021/7/15 10:28
 * description:
 */
class DBHelper private constructor(application: Context) {
    companion object : SingletonHolder<DBHelper, Context>(::DBHelper)

    val db = Room.databaseBuilder(
        application.applicationContext,
        AppDataBase::class.java, "OXY-AI-DB"
    ).build()

    suspend fun getCurrentDevices(): Flow<LpResult<List<DeviceEntity>?>> {
        return flow{
            try {
               db.deviceDao().getCurrentDevices().let {
                   emit(LpResult.Success(it))
               }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun insertOrUpdateDevice(deviceEntity: DeviceEntity) {
        db.deviceDao().insertDevice(deviceEntity)
    }

    fun deleteDevice(deviceEntity: DeviceEntity) {
        db.deviceDao().deleteDevice(deviceEntity)
    }

    fun deleteDevice(model: Int) {
        db.deviceDao().deleteDevice(model)
    }

    suspend fun insertRecord(recordEntity: RecordEntity):Flow<LpResult<Boolean>> {
        return flow{
            try {
                db.recordDao().insertRecord(recordEntity).let {
                    emit(LpResult.Success(true))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun insertPatient(patientEntity: PatientEntity):Flow<LpResult<Long>> {
        return flow{
            try {
                db.patientDao().insertPatient(patientEntity).let {
                    emit(LpResult.Success(it))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getCurrentPatient(): Flow<LpResult<PatientEntity>> {
        return flow<LpResult<PatientEntity>>{
            try {
                db.patientDao().getCurrentPatient()?.let {
                    emit(LpResult.Success(it))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getRecordEntityList(offset: Long, pageSize: Int): Flow<LpResult<List<RecordEntity>?>> {
        return flow<LpResult<List<RecordEntity>?>>{
            try {
                db.recordDao().getRecordEntityList(offset, pageSize)?.let {
                    LepuBleLog.d("record", it.toString())
                    emit(LpResult.Success(it))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getRecordCount(): Flow<LpResult<Long>> {
        return flow{
            try {
                db.recordDao().getCount().let {
                    emit(LpResult.Success(it))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)
    }

}