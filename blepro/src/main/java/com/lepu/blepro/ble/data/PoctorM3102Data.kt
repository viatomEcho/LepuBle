package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.isNumber

class PoctorM3102Data(val bytes: ByteArray) {

    var type: Int        // 0：血糖，1：尿酸，3：血酮
    var normal: Boolean  // 结果正常时设备保存数据，结果不正常时设备不保存数据
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0
    var result: Int       // normal=false(0是低值Lo,1是高值Hi),血糖、血酮的结果四位最后一位为小数点后一位数据
//    var unit: Int         // 尿酸：umol/L，血糖、血酮：mmol_L

    init {
        val data = String(bytes).split(",")
        var index = 0
        type = if (isNumber(data[index])) {
            data[index].toInt()
        } else {
            1
        }
        index++
        if (data.size == 9) {
            normal = true
            year = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                0
            }
            index++
            month = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                0
            }
            index++
            day = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                0
            }
            index++
            hour = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                0
            }
            index++
            minute = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                0
            }
            index++
            result = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                1
            }
            index++
//            unit = String(bytes.copyOfRange(index, index + 1)).toInt()
        } else {
            normal = false
            result = if (isNumber(data[index])) {
                data[index].toInt()
            } else {
                1
            }
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
        """.trimIndent()
    }

}