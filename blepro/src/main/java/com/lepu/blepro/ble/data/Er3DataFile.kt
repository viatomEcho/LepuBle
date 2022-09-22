package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.toUInt

/**
 * Txxxxxxxxxxxxxx：心率、体温、血氧等数据的存储文件
 */
class Er3DataFile(val bytes: ByteArray) {

    var fileVersion: Int     // 文件版本 e.g.  0x01 :  V1，固定为0x01
    var fileType: Int        // 文件类型，固定为0x04
    var leadType: Int        // 导联类型，无效数据
    // reserved 7

    var datas = mutableListOf<EachData>()

    var recordingTime: Int   // 记录时长 e.g. 3600 :  3600s
    var dataCrc: Int         // 文件头部+原始波形和校验
    // reserved 10
    var magic: Int           // 文件标志 固定值为0xA55A0438

    init {
        var index = 0
        fileVersion = byte2UInt(bytes[index])
        index++
        fileType = byte2UInt(bytes[index])
        index++
        leadType = byte2UInt(bytes[index])
        index++
        index += 7
        val len = bytes.size-10-20
        for (i in 0 until len.div(10)) {
            datas.add(EachData(bytes.copyOfRange(index+i*10, index+(i+1)*10)))
        }
        index += len
        recordingTime = toUInt(bytes.copyOfRange(index, index+4))
        index += 4
        dataCrc = toUInt(bytes.copyOfRange(index, index+2))
        index += 2
        index += 10
        magic = toUInt(bytes.copyOfRange(index, index+4))
    }

    override fun toString(): String {
        return """
            Er3DataFile :
            fileVersion : $fileVersion
            fileType : $fileType
            leadType : $leadType
            recordingTime : $recordingTime
            dataCrc : $dataCrc
            magic : $magic
            datas : $datas
        """.trimIndent()
    }

    class EachData(val bytes: ByteArray) {
        var hr: Int        //心率
        var temp: Float	   //体温，示例：值为 2500时，体温即为25.0℃ 。无效值 0xFFFF
        var spo2: Int	   //血氧，无效值 0xFF
        var pr: Int	       //脉率
        var respRate: Int  //呼吸率
        // reserved
        init {
            var index = 0
            hr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            temp = toUInt(bytes.copyOfRange(index, index+2)).div(100f)
            index += 2
            spo2 = byte2UInt(bytes[index])
            index++
            pr = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
            respRate = toUInt(bytes.copyOfRange(index, index+2))
            index += 2
        }
        override fun toString(): String {
            return """
                EachData :
                hr : $hr
                temp : $temp
                spo2 : $spo2
                pr : $pr
                respRate : $respRate
            """.trimIndent()
        }
    }

}