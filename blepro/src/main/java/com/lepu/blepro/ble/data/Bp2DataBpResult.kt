package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toInt
import com.lepu.blepro.utils.toUInt

class Bp2DataBpResult {
    var bytes: ByteArray
    var isDeflate : Boolean = false  // 是否放气
    var pressure : Int = 0           // 实时压
    var sys : Int = 0                // 收缩压
    var dia : Int = 0                // 舒张压
    var mean : Int = 0               // 平均圧
    var pr : Int = 0                 // 脉率
    var code : Int = 0               // 状态码 0：正常，1：无法分析（袖套绑的太松，充气慢，缓慢漏气，气容大），
                                     //       2：波形混乱（打气过程中检测到胳膊有动作或者有其他干扰），3：信号弱，检测不到脉搏波（有干扰袖套的衣物），
                                     //       >=4：设备错误（堵阀，血压测量超量程，袖套漏气严重，软件系统异常，硬件系统错误，以及其他异常）

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