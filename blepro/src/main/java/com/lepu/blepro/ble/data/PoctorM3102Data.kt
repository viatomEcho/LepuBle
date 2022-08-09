package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex

class PoctorM3102Data(val bytes: ByteArray) {

    var type: Int        // 0：血糖，1：尿酸，3：血酮
    var normal: Boolean  // 结果正常时设备保存数据，结果不正常时设备不保存数据
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0
    var result: Int       // normal=false(0是低值Lo,1是高值Hi)
    var unit: Int = 1     // 0：umol/L，1：mmol_L

    init {
        var index = 0
        type = String(bytes.copyOfRange(index, index+1)).toInt()
        index++
        index++
        if (bytes.size == 31) {
            normal = true
            year = String(bytes.copyOfRange(index, index + 4)).toInt()
            index += 4
            index++
            month = String(bytes.copyOfRange(index, index + 2)).toInt()
            index += 2
            index++
            day = String(bytes.copyOfRange(index, index + 2)).toInt()
            index += 2
            index++
            hour = String(bytes.copyOfRange(index, index + 2)).toInt()
            index += 2
            index++
            minute = String(bytes.copyOfRange(index, index + 2)).toInt()
            index += 2
            index++
            result = String(bytes.copyOfRange(index, index + 4)).toInt()
            index += 4
            index++
            unit = String(bytes.copyOfRange(index, index + 1)).toInt()
        } else {
            normal = false
            result = String(bytes.copyOfRange(index, index+1)).toInt()
        }
    }

    override fun toString(): String {
        return """
            PoctorM3102Data : 
            bytes : ${bytesToHex(bytes)}
            string : ${String(bytes)}
            type : $type
            year : $year
            month : $month
            day : $day
            hour : $hour
            minute : $minute
            result : $result
            unit : $unit
        """.trimIndent()
    }

}