package com.lepu.demo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * author: wujuan
 * created on: 2021/7/15 12:37
 * description:
 */
@Entity(
        tableName = "devices")
data class DeviceEntity(
        val deviceName: String,
        val deviceMacAddress: String,
        @PrimaryKey
        val modelNo: Int,
)