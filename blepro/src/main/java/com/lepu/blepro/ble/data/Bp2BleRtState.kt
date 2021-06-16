package com.lepu.blepro.ble.data



const val STATUS_SLEEP = 0
const val STATUS_HISTORY = 1 // read history
const val STATUS_CHARGING = 2
const val STATUS_READY = 3
const val STATUS_BP_ING = 4
const val STATUS_BP_END = 5
const val STATUS_ECG_ING = 6
const val STATUS_ECG_END = 7


class Bp2BleRtState {


    var status : Int
    var battery : KtBleBattery
    // reserve 4


    constructor(bytes : ByteArray) {
        status = bytes[0].toInt()
        battery = KtBleBattery(bytes.copyOfRange(1, 5))
    }

    override fun toString(): String {
        var str : String = "status: "
        when(status) {
            STATUS_SLEEP -> {
                str += "STATUS_SLEEP"
            }
            STATUS_HISTORY -> {
                str += "STATUS_HISTORY"
            }
            STATUS_CHARGING -> {
                str += "STATUS_CHARGING"
            }
            STATUS_READY -> {
                str += "STATUS_READY"
            }
            STATUS_BP_ING -> {
                str += "STATUS_BP_ING"
            }
            STATUS_BP_END -> {
                str += "STATUS_BP_END"
            }
            STATUS_ECG_ING -> {
                str += "STATUS_ECG_ING"
            }
            STATUS_ECG_END -> {
                str += "STATUS_ECG_END"
            }
        }

        str += battery.toString()
        return str
    }
}

