package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.hexToBytes
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toIntBig

class Lpm311Data(val bytes: ByteArray) {

    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int
    var second: Int
    var chol: Double
    var hdl: Double
    var trig: Double
    var ldl: Double
    var cholDivHdl: Double
    var unit: Int           // 单位：0为mmol/L 1为mg/dL
    var user: String

    init {
        var index = 0
        year = String(bytes.copyOfRange(index, index+4)).toInt()
        index += 4
        month = String(bytes.copyOfRange(index, index+2)).toInt()
        index += 2
        day = String(bytes.copyOfRange(index, index+2)).toInt()
        index += 2
        hour = String(bytes.copyOfRange(index, index+2)).toInt()
        index += 2
        minute = String(bytes.copyOfRange(index, index+2)).toInt()
        index += 2
        second = String(bytes.copyOfRange(index, index+2)).toInt()
        index += 2
        index++
        chol = toIntBig(hexToBytes(String(bytes.copyOfRange(index, index+8)))).toDouble()
        index += 8
        hdl = toIntBig(hexToBytes(String(bytes.copyOfRange(index, index+8)))).toDouble()
        index += 8
        trig = toIntBig(hexToBytes(String(bytes.copyOfRange(index, index+8)))).toDouble()
        index += 8
        index++
        unit = String(bytes.copyOfRange(index, index+1)).toInt()
        index++
        user = trimStr(String(bytes.copyOfRange(index, bytes.size)))
        if (unit == 0) {
            chol = chol.div(100)
            hdl = hdl.div(100)
            trig = trig.div(100)
            ldl = chol-hdl-trig.div(2.2)
        } else {
            ldl = chol-hdl-trig.div(5)
        }
        cholDivHdl = chol.div(hdl)
    }

    override fun toString(): String {
        return """
            Lpm311Data : 
            bytes : ${bytesToHex(bytes)}
            string : ${String(bytes)}
            year : $year
            month : $month
            day : $day
            hour : $hour
            minute : $minute
            second : $second
            chol : $chol
            hdl : $hdl
            trig : $trig
            ldl : $ldl
            cholDivHdl : $cholDivHdl
            unit : $unit
            user : $user
        """.trimIndent()
    }

}