package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr

object BiolandBgmBleResponse {

    @ExperimentalUnsignedTypes
    class BleResponse (val bytes: ByteArray) {
        var head: Int
        var len: Int
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            var index = 0
            head = byte2UInt(bytes[index])
            index++
            len = byte2UInt(bytes[index])
            index++
            cmd = byte2UInt(bytes[index])
            index++
            content = bytes.copyOfRange(index, bytes.size - 1)
        }
    }

    @ExperimentalUnsignedTypes
    class DeviceInfo (val bytes: ByteArray) {
        var version: String    // 版本号
        var customerType: Int  // 客户代码（0：苹果，1：爱奥乐，2：海尔，3：无，4：小米，5：道通，6：KANWEI）
        var battery: Int       // 电量（0-100%）
        var deviceType: Int    // 设备类型（1：血压计，2：血糖仪）
        var deviceCode: Int    // 设备型号
        var sn: String         // 9位

        init {
            var index = 0
            version = byteToPointInt(bytes[index])
            index++
            customerType = byte2UInt(bytes[index])
            index++
            battery = byte2UInt(bytes[index])
            index++
            deviceType = byte2UInt(bytes[index])
            index++
            deviceCode = byte2UInt(bytes[index])
            index++
            sn = trimStr(String(bytes.copyOfRange(index, bytes.size)))
        }

        override fun toString(): String {
            return """
                DeviceInfo : 
                bytes : ${bytesToHex(bytes)}
                version : $version
                customerType : $customerType
                battery : $battery
                deviceType : $deviceType
                deviceCode : $deviceCode
                sn : $sn
            """.trimIndent()
        }
    }

    @ExperimentalUnsignedTypes
    class GluData(val bytes: ByteArray) {
        var year: Int
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int
        var resultMg: Int      // 单位：mg/dl (18-Li，707-Hi)
        var resultMmol: Float  // 单位：mmol/l (1.0-Li，39.3-Hi)

        init {
            var index = 0
            year = byte2UInt(bytes[index]) + 2000
            index++
            month = byte2UInt(bytes[index])
            index++
            day = byte2UInt(bytes[index])
            index++
            hour = byte2UInt(bytes[index])
            index++
            minute = byte2UInt(bytes[index])
            index++
            index++
            resultMg = toUInt(bytes.copyOfRange(index, index+2))
            resultMmol = toUInt(bytes.copyOfRange(index, index+2)).div(18f)
        }

        override fun toString(): String {
            return """
                GluData : 
                bytes : ${bytesToHex(bytes)}
                year : $year
                month : $month
                day : $day
                hour : $hour
                minute : $minute
                resultMg : $resultMg
                resultMmol : $resultMmol
            """.trimIndent()
        }
    }

}