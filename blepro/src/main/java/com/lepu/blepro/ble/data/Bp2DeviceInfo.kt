package com.lepu.blepro.ble.data


import com.lepu.blepro.utils.toString
import com.lepu.blepro.utils.toUInt
import java.text.SimpleDateFormat
import java.util.*

class Bp2DeviceInfo {
    var name: String

    var hwV: String //hardware version

    var fmV: String //firmware version

    var btlV: String // btl version

    var branchCode: String

    // reserve 3 byte
    var deviceType: Int

    var protocolV: String // protocol version

    var curTime: Long

    var protocolMaxLen: Int //protocol support max length

    // reserve 4 byte
    var snLen: Int

    var sn: String
    var bytes:ByteArray
    @ExperimentalUnsignedTypes
    constructor(bytes: ByteArray, deviceName: String) {
        this.bytes=bytes;
        this.name = deviceName
        this.hwV = bytes[0].toChar().toString()
        this.fmV = "${bytes[4]}.${bytes[3]}.${bytes[2]}.${bytes[1]}"
        this.btlV = "${bytes[8]}.${bytes[7]}.${bytes[6]}.${bytes[5]}"
        this.branchCode = toString(bytes.copyOfRange(9, 17))
        this.deviceType = toUInt(bytes.copyOfRange(20, 22))
        this.protocolV = "${bytes[23]}.${bytes[22]}"

        val c = Calendar.getInstance()
        c.set(toUInt(bytes.copyOfRange(24, 26)), bytes[26].toInt(), bytes[27].toInt(), bytes[28].toInt(), bytes[29].toInt(), bytes[30].toInt())
        this.curTime = c.timeInMillis
        this.protocolMaxLen = toUInt(bytes.copyOfRange(31, 33))
        this.snLen = bytes[37].toInt()
        this.sn = toString(bytes.copyOfRange(38, 38 + snLen))
    }

    override fun toString(): String {
        val format : SimpleDateFormat = SimpleDateFormat("HH:mm:ss MMM dd, yyyy", Locale.getDefault())
        val d : Date = Date(curTime)
        val dateStr = format.format(d)

        val string = """
            Bp2DeviceInfo :
            name : $name
            hwV : $hwV
            fmV : $fmV
            btlV : $btlV
            branchCode : $branchCode
            deviceType : $deviceType
            protocolV : $protocolV
            curTime : $curTime
            dateStr : $dateStr
            protocolMaxLen : $protocolMaxLen
            snLen : $snLen
            sn : $sn
        """.trimIndent()
        return string
    }

}