package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex

const val STATUS_SLEEP = 0
const val STATUS_HISTORY = 1 // read history
const val STATUS_CHARGING = 2
const val STATUS_READY = 3
const val STATUS_BP_ING = 4
const val STATUS_BP_END = 5
const val STATUS_ECG_ING = 6
const val STATUS_ECG_END = 7
const val STATUS_BP_AVG_ING = 15
const val STATUS_BP_AVG_WAIT = 16
const val STATUS_BP_AVG_END = 17

class Bp2BleRtState(val bytes: ByteArray) {

    var status : Int             // STATUS_SLEEP, STATUS_HISTORY ...
    var statusMsg: String
    var battery : KtBleBattery
    var avgCnt: Int              // x3当前测量下标 0,1,2
    var avgWaitTick: Int         // x3等待计时
    // bp2a sibel
    var key: Boolean             // 按键开关
    // reserved 1

    init {
        status = bytes[0].toInt()
        statusMsg = getStatusMsg(status)
        battery = KtBleBattery(bytes.copyOfRange(1, 5))
        avgCnt = byte2UInt(bytes[5])
        avgWaitTick = byte2UInt(bytes[6])
        key = byte2UInt(bytes[7]) == 1
    }

    private fun getStatusMsg(status: Int): String {
        return when(status) {
            STATUS_SLEEP -> "STATUS_SLEEP"
            STATUS_HISTORY -> "STATUS_HISTORY"
            STATUS_CHARGING -> "STATUS_CHARGING"
            STATUS_READY -> "STATUS_READY"
            STATUS_BP_ING -> "STATUS_BP_ING"
            STATUS_BP_END -> "STATUS_BP_END"
            STATUS_ECG_ING -> "STATUS_ECG_ING"
            STATUS_BP_AVG_ING -> "STATUS_BP_AVG_ING"
            STATUS_BP_AVG_WAIT -> "STATUS_BP_AVG_WAIT"
            STATUS_BP_AVG_END -> "STATUS_BP_AVG_END"
            else -> ""
        }
    }

    override fun toString(): String {
        return """
            Bp2BleRtState : 
            bytes : ${bytesToHex(bytes)}
            status : $status
            statusMsg : $statusMsg
            battery : $battery
            avgCnt : $avgCnt
            avgWaitTick : $avgWaitTick
            key : $key
        """.trimIndent()
    }
}

