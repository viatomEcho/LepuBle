package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.ble.cmd.LewBleCmd
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toLong
import com.lepu.blepro.utils.toUInt

class RtData(val bytes: ByteArray) {

    var deviceStatus: DeviceStatus
    var sportData: SportData
    var moduleData : ModuleData

    init {
        var index = 0
        deviceStatus = DeviceStatus(bytes.copyOfRange(index, index+28))
        index += 28
//        val type = byte2UInt(bytes[index])
        sportData = SportData(bytes.copyOfRange(index, index+36))
        index += 36
        moduleData = ModuleData(bytes.copyOfRange(index, bytes.size))
    }

    override fun toString(): String {
        return """
            RtData : 
            bytes : ${bytesToHex(bytes)}
            deviceStatus : $deviceStatus
            sportData : $sportData
            moduleData : $moduleData
        """.trimIndent()
    }

    class DeviceStatus(val bytes: ByteArray) {
        var battery: Int     // 100
        var rssi: Int        // 4G信号强度
        var longitude: Long  // 经度，1000000倍
        var latitude: Long   // 纬度，1000000倍
        var wear: Boolean    // 0：未佩戴，1：佩戴
        var mode: Int        // 设备当前模式 LewBleCmd.DeviceMode
        // reserved 8
        init {
            var index = 0
            battery = byte2UInt(bytes[index])
            index++
            rssi = bytes[index].toInt()
            index++
            longitude = toLong(bytes.copyOfRange(index, index+8))
            index += 8
            latitude = toLong(bytes.copyOfRange(index, index+8))
            index += 8
            wear = byte2UInt(bytes[index]) == 1
            index++
            mode = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
            DeviceStatus : 
            bytes : ${bytesToHex(bytes)}
            battery : $battery
            rssi : $rssi
            longitude : $longitude
            latitude : $latitude
            wear : $wear
            mode : $mode
        """.trimIndent()
        }
    }

    class SportData(val bytes: ByteArray) {
        var type: Int        // LewBleCmd.SportType
        var data: ByteArray  // 根据运动类型解析数据
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            data = bytes.copyOfRange(index, bytes.size)
        }
        override fun toString(): String {
            return """
                SportData : 
                bytes : ${bytesToHex(bytes)}
                type : $type
                data : ${bytesToHex(data)}
            """.trimIndent()
        }
    }
    // 跑步实时数据
    class RunData(val bytes: ByteArray) {
        var startTime: String  // 时间8位bcd码  20220812084048
        var steps: Int         // 当前跑步步数
        var distance: Int      // 跑步距离，单位m
        var calories: Int      // 当前消耗卡路里，100倍
        var duration: Int      // 持续时间，单位s
        var stepFreq: Int      // 步频
        var stepStride: Int    // 步幅
        var pace0: Int         // 配速，单位s
        var pace1: Int
        var pace2: Int
        // reserved 7
        init {
            var index = 0
            startTime = trimStr(String(bytes.copyOfRange(index, index+8)))
            index += 8
            steps = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            distance = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            calories = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            duration = toUInt(bytes.copyOfRange(index, index+4))
            index += 4
            stepFreq = byte2UInt(bytes[index])
            index++
            stepStride = byte2UInt(bytes[index])
            index++
            pace0 = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pace1 = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            pace2 = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                RunData : 
                bytes : ${bytesToHex(bytes)}
                startTime : $startTime
                steps : $steps
                distance : $distance
                calories : $calories
                duration : $duration
                stepFreq : $stepFreq
                stepStride : $stepStride
                pace0 : $pace0
                pace1 : $pace1
                pace2 : $pace2
            """.trimIndent()
        }
    }

    class ModuleData(val bytes: ByteArray) {
        var cardiotConnect: Boolean  // cardiot连接状态，0：断开，1：已连接
        // reserved 4
        var moduleSize: Int
        var watchEcg: WatchEcg? = null
        var watchOxy: WatchOxy? = null
        var moduleEr1: ModuleEr1? = null
        init {
            var index = 0
            cardiotConnect = byte2UInt(bytes[index]) == 1
            index++
            // reserved 4
            index += 4
            moduleSize = byte2UInt(bytes[index])
            index++
            for (i in 0 until moduleSize) {
                when (byte2UInt(bytes[index])) {
                    LewBleCmd.ModuleType.WATCH_ECG -> {
                        watchEcg = WatchEcg(bytes.copyOfRange(index, index+10))
                        index += 10
                    }
                    LewBleCmd.ModuleType.WATCH_OXY -> {
                        watchOxy = WatchOxy(bytes.copyOfRange(index, index+10))
                        index += 10
                    }
                    LewBleCmd.ModuleType.MODULE_ER1 -> {
                        moduleEr1 = ModuleEr1(bytes.copyOfRange(index, index+10))
                        index += 10
                    }
                }
            }
        }
        override fun toString(): String {
            return """
                ModuleData : 
                bytes : ${bytesToHex(bytes)}
                cardiotConnect : $cardiotConnect
                moduleSize : $moduleSize
                watchEcg : $watchEcg
                watchOxy : $watchOxy
                moduleEr1 : $moduleEr1
            """.trimIndent()
        }
    }
    class WatchEcg(val bytes: ByteArray) {
        var type: Int  // 模块类型 LewBleCmd.ModuleType
        var hr: Int
        // reserved 7
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
        }
        override fun toString(): String {
            return """
                WatchEcg : 
                bytes : ${bytesToHex(bytes)}
                type : $type
                hr : $hr
            """.trimIndent()
        }
    }
    class WatchOxy(val bytes: ByteArray) {
        var type: Int  // 模块类型 LewBleCmd.ModuleType
        var spo2: Int
        var pr: Int
        // reserved 7
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            spo2 = byte2UInt(bytes[index])
            index++
            pr = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                WatchOxy : 
                bytes : ${bytesToHex(bytes)}
                type : $type
                spo2 : $spo2
                pr : $pr
            """.trimIndent()
        }
    }
    class ModuleEr1(val bytes: ByteArray) {
        var type: Int            // 模块类型 LewBleCmd.ModuleType
        var bleConnect: Boolean  // 蓝牙连接状态
        var leadOff: Boolean     // 导联状态
        var hr: Int
        var battery: Int         // 100
        // reserved 4
        init {
            var index = 0
            type = byte2UInt(bytes[index])
            index++
            bleConnect = byte2UInt(bytes[index]) == 1
            index++
            leadOff = byte2UInt(bytes[index]) == 1
            index++
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            battery = byte2UInt(bytes[index])
        }
        override fun toString(): String {
            return """
                ModuleEr1 : 
                bytes : ${bytesToHex(bytes)}
                type : $type
                bleConnect : $bleConnect
                leadOff : $leadOff
                hr : $hr
                battery : $battery
            """.trimIndent()
        }
    }
}