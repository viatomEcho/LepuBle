package com.lepu.blepro.ble.cmd

import com.lepu.blepro.utils.ByteUtils.*
import com.lepu.blepro.utils.*
import java.util.*

object TmbBleResponse {

    class BleResponse(val bytes: ByteArray) {
        var head: Int
        var len: Int
        var cmd: Int
        var content: ByteArray
        init {
            var index = 0
            head = byte2UInt(bytes[index])
            index++
            len = byte2UInt(bytes[index])
            index++
            if (head == 0x21 || head == 0x00) {
                cmd = 0
            } else {
                cmd = bytes2UIntBig(bytes[index], bytes[index+1])
                index += 2
            }
            content = bytes.copyOfRange(index, bytes.size)
        }
        override fun toString(): String {
            return """
                BleResponse : 
                bytes : ${bytesToHex(bytes)}
                head : $head
                len : $len
                cmd : $cmd
                content : ${bytesToHex(content)}
            """.trimIndent()
        }
    }
//    • [Flag]: 4 bytes.
//    • bit0: unit flag; 0 - mmHg; 1 - kPa.
//    • bit1: pulserate flag: 0 – Not Support, 1 - Support.
//    • bit2: userid flag. • bit3: utc flag. • bit4: body movement flag.
//    • bit5: cuff fit flag. • bit6: irregular pulse flag. • bit7: measurement position flag.
//    • bit8: time zone flag. • bit9:time stamp flag.
//    • [Systolic]:uint16, unit is mmHg.
//    • [Diastolic]:uint16, unit is mmHg.
//    • [MeanPressure]:uint16, unit is mmHg.
//    • [BloodPressureStatus]:uint16, 2bytes.
//    • [PulseRate]:uint16.
//    • [UserNumber]:uint8.
//    • [UTC]: uint32. If the values is 0x59897751, the UTC is 1502181201
    class Record(val bytes: ByteArray) {
        var number: Int  // surplus data count
        var flag: Int
        var sys: Int
        var dia: Int
        var mean: Int
        var ps: Int
        var pr: Int
        var id: Int
        var time: Long
        init {
            var index = 0
            number = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            flag = bytes2UIntBig(bytes[index], bytes[index+1], bytes[index+2], bytes[index+3])
            index += 4
            sys = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            dia = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            mean = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            ps = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            pr = bytes2UIntBig(bytes[index], bytes[index+1])
            index += 2
            id = byte2UInt(bytes[index])
            index++
            // 设备基准时间：2010-01-01 00:00:00，对应时间戳是1262304000
            time = toLongBig(bytes.copyOfRange(index, index+4))+1262304000-DateUtil.getTimeZoneOffset().div(1000)
        }
        override fun toString(): String {
            return """
                Record : 
                number : $number
                flag : $flag
                sys : $sys
                dia : $dia
                mean : $mean
                ps : $ps
                pr : $pr
                id : $id
                time : $time
                time : ${DateUtil.stringFromDate(Date(time*1000), "yyyy-MM-dd HH:mm:ss")}
            """.trimIndent()
        }
    }
}