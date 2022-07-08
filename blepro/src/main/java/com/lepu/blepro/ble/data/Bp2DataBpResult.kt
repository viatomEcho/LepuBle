package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataBpResult {
    var bytes: ByteArray
    var isDeflate : Boolean = false
    var pressure : Int = 0
    var sys : Int = 0
    var dia : Int = 0
    var mean : Int = 0
    var pr : Int = 0
    var code : Int = 0

    constructor(bytes: ByteArray) {
        this.bytes = bytes
        this.isDeflate = bytes[0].toInt() == 1
        this.pressure = toInt(bytes.copyOfRange(1,3))
        this.sys = toUInt(bytes.copyOfRange(3,5))
        this.dia = toUInt(bytes.copyOfRange(5,7))
        this.mean = toUInt(bytes.copyOfRange(7,9))
        this.pr = toUInt(bytes.copyOfRange(9,11))
        this.code = bytes[11].toInt()
    }

    override fun toString(): String {
        return """
            Bp2DataBpResult : 
            bytes : ${bytesToHex(bytes)}
            isDeflate: $isDeflate
            pressure: $pressure
            sys: $sys
            dia: $dia
            mean: $mean
            pr: $pr
            code: $code
        """.trimIndent()
    }

//    fun getResultStr() = when(code) {
//        1 -> R.string.be_device_bp_result_error1
//        2 -> R.string.be_device_bp_result_error2
//        3 -> R.string.be_device_bp_result_error3
//        else -> R.string.be_device_bp_result_error4
//    }
//
//    fun getResultSrc() = when(code) {
//        1 -> R.mipmap.bp_pic_error_01_n
//        2 -> R.mipmap.bp_pic_error_02_n
//        3 -> R.mipmap.bp_pic_error_03_n
//        else -> R.mipmap.bp_pic_error_04_n
//    }
}