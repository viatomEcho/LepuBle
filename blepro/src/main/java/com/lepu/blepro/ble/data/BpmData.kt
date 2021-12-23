package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.bytesToHex

class BpmData(val data: ByteArray)  {

    var sys: Int
    var dia: Int
    var regularHrFlag: Boolean
    var pr: Int
    var deviceUserId: Int
    var storeId: Int
    var year: Int
    var month: Int
    var day: Int
    var hour: Int
    var minute: Int

    init {
        sys = ((data[0].toUInt() and 0xFFu).toInt() shl 8) + (data[1].toUInt() and 0xFFu).toInt()
        dia = ((data[2].toUInt() and 0xFFu).toInt() shl 8) + (data[3].toUInt() and 0xFFu).toInt()
        regularHrFlag = (data[4].toUInt() and 0xFFu).toInt() != 0
        pr = (data[5].toUInt() and 0xFFu).toInt()
        deviceUserId = (data[6].toUInt() and 0xFFu).toInt()
        storeId = ((data[7].toUInt() and 0xFFu).toInt() shl 8) + (data[8].toUInt() and 0xFFu).toInt()
        year = (data[9].toUInt() and 0xFFu).toInt() + 2000
        month = (data[10].toUInt() and 0xFFu).toInt()
        day = (data[11].toUInt() and 0xFFu).toInt()
        hour = (data[12].toUInt() and 0xFFu).toInt()
        minute = (data[13].toUInt() and 0xFFu).toInt()
    }

    override fun toString(): String {
        return """
            data : ${bytesToHex(data)}
            sys : $sys
            dia : $dia
            regularHrFlag : $regularHrFlag
            pr : $pr
            deviceUserId : $deviceUserId
            storeId : $storeId
            year : $year
            month : $month
            day : $day
            hour : $hour
            minute : $minute
        """.trimIndent()
    }
}