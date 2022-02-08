package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt


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


class Bp2BleRtState {

    var status : Int
    var battery : KtBleBattery
    var avgCnt: Int
    var avgWaitTick: Int
    // reserve 2


    constructor(bytes : ByteArray) {
        status = bytes[0].toInt()
        battery = KtBleBattery(bytes.copyOfRange(1, 5))
        avgCnt = byte2UInt(bytes[5])
        avgWaitTick = byte2UInt(bytes[6])
    }

    override fun toString(): String {
        return """
            status : $status
            battery : $battery
            avgCnt : $avgCnt
            avgWaitTick : $avgWaitTick
        """.trimIndent()
    }
}

