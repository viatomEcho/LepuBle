package com.lepu.blepro.ble.data
import com.lepu.blepro.utils.toUInt


const val NORMAL = 0
const val CHARGING = 1
const val CHARGED = 2
const val LOW_BATTERY = 3

@ExperimentalUnsignedTypes
public class KtBleBattery constructor(bytes: ByteArray) {
    var state: Int
    var percent: Int
    var vol: Float // 3.92V

    init {
        state = bytes[0].toInt()
        percent = bytes[1].toInt()
        vol = toUInt(bytes.copyOfRange(2,3)).toFloat() / 1000
    }

    override fun toString(): String {

        var s = "Normal"

        when(state) {
            NORMAL -> s="Normal"
            CHARGING -> s="Charging"
            CHARGED -> s="Charged"
            LOW_BATTERY -> s="Low battery"
        }

        return """
            state: $s
            percent: $percent
            voltage: $vol
        """
    }
}