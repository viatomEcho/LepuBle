package com.lepu.demo.local

import androidx.room.*
import com.lepu.demo.data.entity.DeviceEntity

/**
 * author: wujuan
 * created on: 2021/7/15 16:01
 * description:
 */
@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDevice(deviceEntity: DeviceEntity)

    @Query("SELECT * FROM devices")
    fun getCurrentDevices(): List<DeviceEntity>?

    @Delete
    fun deleteDevice(deviceEntity: DeviceEntity)

    @Query("DELETE FROM devices WHERE modelNo=:model")
    fun deleteDevice(model: Int)

}