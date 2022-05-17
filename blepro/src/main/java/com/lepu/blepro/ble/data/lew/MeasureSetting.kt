package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

class MeasureSetting() {

    var sportTarget = SportTarget()
    var targetRemind = false
    var sittingRemind = SittingRemind()
    var hrDetect = HrDetect()

    constructor(bytes: ByteArray) : this() {
        var index = 0
        sportTarget = SportTarget(bytes.copyOfRange(index, index+16))
        index += 16
        targetRemind = byte2UInt(bytes[index]) == 1
        index++
        sittingRemind = SittingRemind(bytes.copyOfRange(index, index+7))
        index += 7
//        hrDetect = HrDetect(bytes.copyOfRange(index, bytes.size))
        hrDetect = HrDetect(bytes.copyOfRange(index, index+4))
    }

    fun getDataBytes(): ByteArray {
        val on = if (targetRemind) {
            1
        } else {
            0
        }
        return sportTarget.getDataBytes()
            .plus(on.toByte())
            .plus(sittingRemind.getDataBytes())
            .plus(hrDetect.getDataBytes())
    }

    override fun toString(): String {
        return """
            MeasureSetting : 
            bytes : ${bytesToHex(getDataBytes())}
            sportTarget : $sportTarget
            targetRemind : $targetRemind
            sittingRemind : $sittingRemind
            hrDetect : $hrDetect
        """.trimIndent()
    }

}